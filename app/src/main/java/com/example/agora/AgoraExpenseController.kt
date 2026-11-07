package com.example.agora

import android.content.Context
import com.example.agora.config.QuickstartConfig
import com.example.agora.data.ConversationAgoraApi
import com.example.agora.data.ConversationRepository
import com.example.agora.rtc.AgoraConversationSessionManager
import com.example.ai.AgentExecutionResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/** Session orchestration adapted from the reference ConversationViewModel. */
class AgoraExpenseController(context: Context, private val executeExpense: suspend (String) -> AgentExecutionResult) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val api = ConversationAgoraApi()
    private val repository = ConversationRepository(api)
    private val session = AgoraConversationSessionManager(context)
    val snapshot = session.snapshot
    private val _active = MutableStateFlow(false)
    val active = _active.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _status = MutableStateFlow("Agora uses cloud audio. Tap the mic to connect.")
    val status = _status.asStateFlow()
    private var connectionJob: Job? = null
    private var relayJob: Job? = null
    private var agentId: String? = null
    private var channel: String? = null
    private var closing = false

    fun start() {
        if (_active.value || _busy.value || closing) return
        if (!QuickstartConfig.isConfigured) {
            _status.value = "Configure QUICKSTART_SERVER_URL in local.properties and rebuild to use Agora cloud voice."
            return
        }
        _busy.value = true
        _status.value = "Connecting to Agora cloud voice…"
        connectionJob = scope.launch {
            try {
                repository.checkHealth()
                repository.requireOnDeviceBackend()
                val bootstrap = repository.requestSessionBootstrap()
                channel = bootstrap.channel
                session.connect(bootstrap) { name, uid, rtm -> repository.renewTokens(name, uid, rtm) }
                val relayToken = UUID.randomUUID().toString()
                // Retain the invite result across cancellation so cleanup can leave the agent.
                val invite = withContext(NonCancellable) {
                    repository.inviteAgent(bootstrap.channel, session.snapshot.value.localRtcUid.toString(), relayToken)
                }
                agentId = invite.agentId
                currentCoroutineContext().ensureActive()
                session.setActiveAgentId(invite.agentId)
                _active.value = true
                _status.value = "Connected to Agora cloud. Speak an expense or ask for a total."
                relayJob = scope.launch { serveExpenses(bootstrap.channel, relayToken) }
            } catch (e: CancellationException) {
                cleanup()
                throw e
            } catch (e: Exception) {
                cleanup()
                _status.value = "Agora connection failed: ${e.localizedMessage}"
            } finally { _busy.value = false }
        }
    }

    private suspend fun serveExpenses(name: String, token: String) {
        val responses = linkedMapOf<String, String>()
        while (currentCoroutineContext().isActive) {
            try {
                val request = api.pollDocument(name, token)
                val id = request.requestId ?: continue
                val question = request.question ?: continue
                if (!api.documentRequestActive(name, token, id)) continue
                val answer = responses[id] ?: withContext(NonCancellable) {
                    val result = executeExpense(question)
                    val message = when (result) {
                        is AgentExecutionResult.LoggedExpenses -> result.responseMessage
                        is AgentExecutionResult.QueryResponse -> result.responseMessage
                        is AgentExecutionResult.Error -> result.message
                    }
                    responses[id] = message
                    if (responses.size > 256) responses.remove(responses.keys.first())
                    message
                }
                api.submitDocumentResult(name, token, id, answer)
                _status.value = "Cloud voice • $answer"
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _status.value = "Cloud connection interrupted: ${e.localizedMessage}. Reconnecting…"
                delay(1500)
            }
        }
    }

    fun sendText(text: String) {
        val id = agentId
        val name = channel
        if (!_active.value || id == null || name == null) {
            _status.value = "Start the Agora microphone session before sending a message."
            return
        }
        scope.launch {
            try { repository.sendText(id, name, text, speak = false, append = false) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _status.value = "Could not send message: ${e.localizedMessage}" }
        }
    }

    fun stop() {
        if (closing) return
        closing = true
        _busy.value = true
        _status.value = "Ending cloud voice session…"
        connectionJob?.cancel()
        relayJob?.cancel()
        // Release the microphone immediately, even if the backend is slow.
        session.disconnect()
        scope.launch {
            connectionJob?.join()
            cleanup()
            _status.value = "Cloud voice ended."
            _busy.value = false
            closing = false
        }
    }

    private suspend fun cleanup() = withContext(NonCancellable) {
        relayJob?.cancel()
        val id = agentId
        val name = channel
        agentId = null
        channel = null
        if (id != null && name != null) {
            runCatching { withTimeout(15_000) { repository.stopConversation(id, name) } }
                .onFailure { _status.value = "Local audio stopped; server cleanup failed. Agent may remain until its idle timeout." }
        }
        session.disconnect()
        _active.value = false
    }

    fun close() {
        connectionJob?.cancel()
        relayJob?.cancel()
        session.disconnect()
        scope.launch { connectionJob?.join(); cleanup(); session.release(); scope.cancel() }
    }
}

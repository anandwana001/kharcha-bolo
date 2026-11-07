package com.example.ai

import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.example.data.ExpenseDao
import com.example.data.ExpenseEntity
import com.google.mlkit.genai.agent.Agent
import com.google.mlkit.genai.agent.AgentEvent
import com.google.mlkit.genai.agent.AgentRunner
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

sealed class AgentExecutionResult {
    data class LoggedExpenses(
        val items: List<ExpenseEntity>,
        val responseMessage: String,
        val latencyMs: Long,
        val engineUsed: String
    ) : AgentExecutionResult()

    data class QueryResponse(
        val totalAmount: Double,
        val categoryFilter: String?,
        val responseMessage: String,
        val latencyMs: Long,
        val engineUsed: String
    ) : AgentExecutionResult()

    data class Error(
        val message: String,
        val latencyMs: Long,
        val engineUsed: String
    ) : AgentExecutionResult()
}

class KharchaAgentManager(private val context: Context, private val expenseDao: ExpenseDao) {
    private val lock = Mutex()
    private var promptModel: GenerativeModel? = null
    private var agentModel: GenerativeModel? = null
    private var runner: AgentRunner? = null
    private val _status = MutableStateFlow("Select an engine, then submit an expense.")
    val status = _status.asStateFlow()
    private val _agentReady = MutableStateFlow(false)
    val agentReady = _agentReady.asStateFlow()

    private fun model(agent: Boolean): GenerativeModel {
        if (agent) {
            return agentModel ?: Generation.getClient(generationConfig {
                modelConfig = modelConfig {
                    releaseStage = ModelReleaseStage.PREVIEW
                    preference = ModelPreference.FULL
                }
            }).also { agentModel = it }
        }
        return promptModel ?: Generation.getClient().also { promptModel = it }
    }

    private suspend fun ensureReady(model: GenerativeModel, agent: Boolean) {
        _status.value = "Checking on-device model…"
        when (model.checkStatus()) {
            FeatureStatus.UNAVAILABLE -> error(if (agent)
                "AICore agent model is unavailable. Use supported hardware enrolled in the AICore Developer Preview."
                else "Gemini Nano Prompt API is unavailable on this device. Update AICore and check supported hardware.")
            FeatureStatus.DOWNLOADABLE -> {
                _status.value = "Downloading on-device model. Initial setup needs internet…"
                model.download().collect { event ->
                    when (event) {
                        is DownloadStatus.DownloadFailed -> throw event.e
                        is DownloadStatus.DownloadProgress -> _status.value = "Downloading model: ${event.totalBytesDownloaded / 1_048_576} MB"
                        else -> {}
                    }
                }
            }
            FeatureStatus.DOWNLOADING -> error("Model download is in progress. Wait for it to finish and try again.")
        }
        check(model.checkStatus() == FeatureStatus.AVAILABLE) { "Model is not ready yet. Try again after setup completes." }
        if (agent) _agentReady.value = true
        _status.value = "Running on-device inference…"
    }

    suspend fun execute(input: String, engine: AiEngineType): AgentExecutionResult = lock.withLock {
        withContext(Dispatchers.IO) {
            val start = SystemClock.elapsedRealtime()
            try {
                withTimeout(180_000) {
                    when (engine) {
                        AiEngineType.LOCAL_OFFLINE -> executeBasicVoice(input, start)
                        AiEngineType.GEMINI_NANO_4_AGENT -> executeAgent(input, start)
                        AiEngineType.GEMINI_NANO_PROMPT_API -> executePrompt(input, start)
                        AiEngineType.AGORA_CONVERSATIONAL_CLOUD -> error("Agora is not connected yet. Select an on-device engine.")
                    }
                }.also { _status.value = "Request completed locally." }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _status.value = "Request timed out. Check expenses before retrying; an agent tool may have completed."
                AgentExecutionResult.Error(_status.value, elapsed(start), engine.displayName)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (engine == AiEngineType.GEMINI_NANO_4_AGENT) _agentReady.value = false
                _status.value = e.localizedMessage ?: "On-device AI failed. Try again."
                AgentExecutionResult.Error(_status.value, elapsed(start), engine.displayName)
            }
        }
    }

    private suspend fun executeAgent(input: String, start: Long): AgentExecutionResult {
        check(Build.VERSION.SDK_INT >= 36) { "Expense agent tools require Android 16 or newer. Try Prompt API on supported hardware." }
        val model = model(true)
        ensureReady(model, true)
        val activeRunner = runner ?: AgentRunner(context, Agent.builder()
            .setName("KharchaBoloAgent")
            .setInstructions("""
                You track expenses in Indian rupees. Understand Hindi, English and Hinglish.
                For an explicit expense, call log_diwali_expense exactly once per item.
                For totals, call calculate_total_expenses. Never invent amounts or claim a save without a successful tool response.
                Do not log an expense when asked a question. Ask for clarification for unclear input.
                Summarize the actual tool results concisely.
            """.trimIndent()).enableAppFunctions(true).setMaxTurns(10).build(), model, temperature = 0.0f)
            .also { runner = it }
        val beforeIds = expenseDao.getAllExpenses().map { it.id }.toSet()
        var text = ""
        var toolCompleted = false
        try {
            activeRunner.runSingleTurnAsync(Content.builder().text(input).build()).collect { event ->
                when (event) {
                    is AgentEvent.TextMessage -> text = if (event.isPartial) text + event.text else event.text.ifEmpty { text }
                    is AgentEvent.FunctionCall -> _status.value = "Executing expense tool…"
                    is AgentEvent.FunctionResponse -> { toolCompleted = true }
                    else -> {}
                }
            }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            error("Agent failed: ${e.localizedMessage}. Check your expense list before retrying; completed tool writes are retained.")
        }
        val added = expenseDao.getAllExpenses().filter { it.id !in beforeIds }
        check(toolCompleted || text.isNotBlank()) { "Agent returned no response. Check AICore preview setup." }
        return if (added.isNotEmpty()) AgentExecutionResult.LoggedExpenses(added,
            "Saved ${added.size} expense(s), ₹${added.sumOf { it.amount }}.", elapsed(start), "Gemini Nano Agent")
        else AgentExecutionResult.QueryResponse(expenseDao.getTotalExpense(), null,
            text.ifBlank { "Expense tool completed. Total saved: ₹${expenseDao.getTotalExpense()}" }, elapsed(start), "Gemini Nano Agent")
    }

    private suspend fun executePrompt(input: String, start: Long): AgentExecutionResult {
        val model = model(false)
        ensureReady(model, false)
        check(model.isStructuredOutputFeatureAvailable()) { "This device's Nano model does not support structured expense output. Update AICore or use supported hardware." }
        val request = GenerateContentRequest.Builder(TextPart("""
            Interpret this expense request in Hindi, English or Hinglish.
            Use action log only for explicit purchases with positive amounts in rupees.
            Use query for asking saved totals, and unknown for unclear input.
            Do not invent purchases. For queries return no items; use an English category/item substring, or empty for all.
            User input: $input
        """.trimIndent())).build()
        val intent = model.generateContent(generateTypedContentRequest(request, ExpenseIntent::class))
            .candidates.firstOrNull()?.response ?: error("Nano returned no expense intent. Please rephrase.")
        return applyIntent(intent, input, start)
    }

    internal suspend fun applyIntent(intent: ExpenseIntent, input: String, start: Long, label: String = "Gemini Nano Prompt API"): AgentExecutionResult {
        when (intent.action) {
            "query" -> {
                require(intent.items.isEmpty()) { "Invalid model query response; no expenses were saved." }
                val filter = intent.categoryFilter.trim()
                val total = if (filter.isBlank()) expenseDao.getTotalExpense() else expenseDao.getTotalByCategory(filter)
                return AgentExecutionResult.QueryResponse(total, filter.ifBlank { null }, "Total${if (filter.isBlank()) "" else " for $filter"}: ₹$total", elapsed(start), label)
            }
            "log" -> {
                require(intent.items.isNotEmpty() && intent.items.size <= 20 && intent.items.all {
                    it.itemName.isNotBlank() && it.amount.isFinite() && it.amount > 0
                }) { "Invalid expense details from model; no expenses were saved. Please rephrase." }
                val entities = intent.items.map { ExpenseEntity(itemName = it.itemName.trim(), amount = it.amount,
                    category = HinglishExpenseParser.determineCategory(it.itemName), rawInputPrompt = input, processedByEngine = label) }
                expenseDao.insertExpenses(entities)
                return AgentExecutionResult.LoggedExpenses(entities, "Saved ${entities.size} expense(s), ₹${entities.sumOf { it.amount }}.", elapsed(start), label)
            }
            else -> error("Please include an item and amount, or ask for your expense total.")
        }
    }

    private suspend fun executeBasicVoice(input: String, start: Long): AgentExecutionResult {
        _status.value = "Processing with basic offline expense rules…"
        val normalized = SpokenExpenseNormalizer.normalize(input)
        val intent = when (val parsed = HinglishExpenseParser.parse(normalized)) {
            is ParseResult.ExpenseItems -> ExpenseIntent("log", parsed.items.map { ExpenseIntentItem(it.itemName, it.amount) }, "")
            is ParseResult.TotalQuery -> ExpenseIntent("query", emptyList(), parsed.categoryFilter.orEmpty())
            is ParseResult.Unknown -> error("Please say an item and amount in Hindi, for example 'पांच सौ की मिठाई और बारह सौ की लाइट्स', or ask 'कितना खर्च हुआ'.")
        }
        return applyIntent(intent, input, start, "Basic offline rules")
    }

    private fun elapsed(start: Long) = SystemClock.elapsedRealtime() - start
    fun close() { runner?.close(); promptModel?.close(); agentModel?.close() }
}

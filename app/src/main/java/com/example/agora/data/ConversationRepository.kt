package com.example.agora.data

import com.example.agora.model.AgentInviteResult
import com.example.agora.model.AgoraTokenBundle
import com.example.agora.model.BackendHealthResult
import com.example.agora.model.RenewalTokens

class ConversationRepository(
    private val api: ConversationAgoraApi = ConversationAgoraApi(),
) {
    suspend fun requireOnDeviceBackend() = api.requireOnDeviceBackend()

    suspend fun checkHealth(): BackendHealthResult {
        return api.checkHealth()
    }

    suspend fun sendText(agentId: String, channelName: String, text: String, speak: Boolean, append: Boolean) {
        api.sendText(agentId, channelName, text, speak, append)
    }

    suspend fun requestSessionBootstrap(): AgoraTokenBundle {
        return api.requestSessionBootstrap()
    }

    suspend fun inviteAgent(
        channelName: String,
        requesterRtcUid: String,
        documentToken: String? = null,
    ): AgentInviteResult {
        return api.inviteAgent(
            channelName = channelName,
            requesterRtcUid = requesterRtcUid,
            documentToken = documentToken,
        )
    }

    suspend fun stopConversation(
        agentId: String,
        channelName: String,
    ) {
        api.stopConversation(agentId, channelName)
    }

    suspend fun interruptConversation(
        agentId: String,
        channelName: String,
    ) {
        api.interruptAgent(agentId, channelName)
    }

    suspend fun renewTokens(
        channel: String,
        rtcUid: Int,
        rtmUserId: String,
    ): RenewalTokens {
        return api.renewTokens(
            channel = channel,
            rtcUid = rtcUid,
            rtmUserId = rtmUserId,
        )
    }
}

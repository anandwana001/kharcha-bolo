package com.example.ai

enum class AiEngineType(val displayName: String, val badge: String, val description: String) {
    LOCAL_OFFLINE(
        displayName = "Basic offline voice",
        badge = "Local rules • No Nano required",
        description = "Bundled Hindi speech recognition and local expense rules. Works on emulators."
    ),
    GEMINI_NANO_4_AGENT(
        displayName = "Gemini Nano (AICore Agentic)",
        badge = "On-device agent",
        description = "On-device agent using ML Kit AgentRunner + AppFunctions directly calling Room DB."
    ),
    GEMINI_NANO_PROMPT_API(
        displayName = "Gemini Nano (Prompt API)",
        badge = "Zero Network • Offline Prompt",
        description = "On-device Nano structured expense extraction on supported hardware."
    ),
    AGORA_CONVERSATIONAL_CLOUD(
        displayName = "Agora Conversational AI",
        badge = "Cloud voice • Internet required",
        description = "Agora cloud speech and spoken replies with local expense storage."
    )
}

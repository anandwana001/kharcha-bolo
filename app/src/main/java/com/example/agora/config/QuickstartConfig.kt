package com.example.agora.config

import com.example.BuildConfig

object QuickstartConfig {
    val backendBaseUrl: String = BuildConfig.QUICKSTART_SERVER_URL.trim().trimEnd('/')

    fun missingRequiredValues(): List<String> {
        val missing = mutableListOf<String>()
        if (backendBaseUrl.isBlank()) {
            missing += "QUICKSTART_SERVER_URL"
        }
        return missing
    }

    val isConfigured: Boolean
        get() = missingRequiredValues().isEmpty()

    fun startupHelpMessage(): String? {
        val missing = missingRequiredValues()
        if (missing.isEmpty()) {
            return null
        }
        return "Add ${missing.joinToString()} to local.properties after starting the Python server and HTTPS tunnel."
    }
}

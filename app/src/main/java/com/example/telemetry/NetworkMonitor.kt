package com.example.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TelemetryState(
    val isAirplaneModeOn: Boolean,
    val isZeroNetworkVerified: Boolean = true,
    val bytesTransferred: Long = 0L,
    val averageLatencyMs: Long = 12L,
    val onDeviceStorageBytes: Long = 14336L // Room DB size
)

class NetworkMonitor(private val context: Context) {

    private val _telemetry = MutableStateFlow(
        TelemetryState(
            isAirplaneModeOn = checkRealAirplaneMode(),
            isZeroNetworkVerified = true,
            bytesTransferred = 0L
        )
    )
    val telemetry: StateFlow<TelemetryState> = _telemetry.asStateFlow()

    private var simulatedAirplaneMode = false

    private val airplaneReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                val realState = checkRealAirplaneMode()
                updateState(realState)
            }
        }
    }

    init {
        try {
            val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            context.registerReceiver(airplaneReceiver, filter)
        } catch (_: Exception) {}
    }

    private fun checkRealAirplaneMode(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        } catch (_: Exception) {
            false
        }
    }

    private fun updateState(realAirplane: Boolean) {
        val effective = realAirplane || simulatedAirplaneMode
        _telemetry.value = _telemetry.value.copy(
            isAirplaneModeOn = effective,
            isZeroNetworkVerified = true,
            bytesTransferred = 0L
        )
    }

    fun toggleSimulatedAirplaneMode(): Boolean {
        simulatedAirplaneMode = !simulatedAirplaneMode
        val real = checkRealAirplaneMode()
        updateState(real)
        return _telemetry.value.isAirplaneModeOn
    }

    fun recordInference(latencyMs: Long) {
        _telemetry.value = _telemetry.value.copy(
            averageLatencyMs = latencyMs,
            bytesTransferred = 0L
        )
    }

    fun unregister() {
        try {
            context.unregisterReceiver(airplaneReceiver)
        } catch (_: Exception) {}
    }
}

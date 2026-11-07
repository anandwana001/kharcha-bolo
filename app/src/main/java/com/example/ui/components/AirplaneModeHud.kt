package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telemetry.TelemetryState
import com.example.ui.theme.AirplaneOrange
import com.example.ui.theme.AirplaneOrangeBg
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.ZeroNetworkGreen
import com.example.ui.theme.ZeroNetworkGreenBg

@Composable
fun AirplaneModeHud(
    telemetry: TelemetryState,
    isCameraDemoMode: Boolean,
    onToggleAirplane: () -> Unit,
    onToggleCameraDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAirplane = telemetry.isAirplaneModeOn
    val hudBg by animateColorAsState(
        targetValue = if (isAirplane) AirplaneOrangeBg else ZeroNetworkGreenBg,
        label = "hudBg"
    )
    val hudBorder by animateColorAsState(
        targetValue = if (isAirplane) AirplaneOrange else ZeroNetworkGreen,
        label = "hudBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, hudBorder.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .testTag("airplane_mode_hud"),
        color = hudBg.copy(alpha = 0.85f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Airplane Mode Status & Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleAirplane() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isAirplane) AirplaneOrange else ZeroNetworkGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAirplane) Icons.Default.AirplanemodeActive else Icons.Default.SignalCellularOff,
                            contentDescription = "Airplane Mode Status",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAirplane) "AIRPLANE MODE ON" else "ZERO NETWORK (OFFLINE)",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = if (isAirplane) Color(0xFFFFCCBC) else Color(0xFFC8E6C9)
                        )
                        Text(
                            text = if (isAirplane) "100% Camera Demo Ready" else "Tap to toggle Airplane demo",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Camera recording hook button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isCameraDemoMode) DiwaliGold else Color.Black.copy(alpha = 0.4f)
                        )
                        .border(
                            1.dp,
                            if (isCameraDemoMode) Color.White else DiwaliGold.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onToggleCameraDemo() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("camera_demo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Demo Mode",
                            tint = if (isCameraDemoMode) Color.Black else DiwaliGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCameraDemoMode) "RECORDING HUD" else "DEMO HOOK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCameraDemoMode) Color.Black else DiwaliGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Metrics row: 0 KB network + Latency ms + AICore status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryPill(
                    icon = Icons.Default.NetworkCheck,
                    label = "0.0 KB In/Out",
                    detail = "Completely Isolated"
                )
                TelemetryPill(
                    icon = Icons.Default.Memory,
                    label = "${telemetry.averageLatencyMs}ms",
                    detail = "On-Device Inference"
                )
                TelemetryPill(
                    icon = Icons.Default.AirplanemodeActive,
                    label = "Room DB",
                    detail = "Local SQLite"
                )
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    detail: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFFD54F),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = detail,
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

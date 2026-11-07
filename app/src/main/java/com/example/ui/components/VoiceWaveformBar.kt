package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiwaliAmber
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSaffron
import com.example.ui.theme.DiwaliSurfaceElevated
import com.example.ui.theme.DiwaliVermilion

@Composable
fun VoiceWaveformBar(
    isListening: Boolean,
    audioLevel: Float,
    transcription: String,
    isProcessing: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onOpenKeyboardInput: () -> Unit,
    statusMessage: String = "",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(
                1.5.dp,
                if (isListening) DiwaliGold else DiwaliAmber.copy(alpha = 0.5f),
                RoundedCornerShape(28.dp)
            )
            .testTag("voice_waveform_bar"),
        color = DiwaliSurfaceElevated,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Transcription or Prompt Helper
            AnimatedVisibility(
                visible = isListening || isProcessing || transcription.isNotEmpty() || statusMessage.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            statusMessage.isNotEmpty() -> statusMessage
                            isListening && transcription.isNotEmpty() -> "🎙️ \"$transcription\""
                            isListening -> "Sun rahe hain... Boliye '500 ka mithai, 1200 ki lights'..."
                            transcription.isNotEmpty() -> "\"$transcription\""
                            else -> "Boliye ya type karein"
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isListening) DiwaliGold else Color(0xFFFFD180),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Mic action and controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Keyboard manual input button
                IconButton(
                    onClick = onOpenKeyboardInput,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("keyboard_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Manual Text Input",
                        tint = DiwaliGold
                    )
                }

                // Center Pulsating Mic
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    // Outer Ripple when listening
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .scale(1f + (audioLevel * 0.4f).coerceAtLeast(pulseScale - 1f))
                                .clip(CircleShape)
                                .background(DiwaliGold.copy(alpha = 0.25f))
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(1f + audioLevel * 0.25f)
                                .clip(CircleShape)
                                .background(DiwaliSaffron.copy(alpha = 0.35f))
                        )
                    }

                    // Main Mic Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isListening) DiwaliVermilion else DiwaliGold,
                                        if (isListening) DiwaliSaffron else DiwaliAmber
                                    )
                                )
                            )
                            .clickable {
                                if (!isProcessing) { if (isListening) onStopListening() else onStartListening() }
                            }
                            .testTag("voice_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Hint / Status Badge
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        text = if (isListening) "Listening" else "Tap Mic",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) DiwaliGold else Color.White
                    )
                    Text(
                        text = if (isListening) "Zero Net" else "Kharcha Bolo",
                        fontSize = 10.sp,
                        color = Color(0xFFFFD180).copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

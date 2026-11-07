package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiEngineType
import com.example.ui.theme.DiwaliAmber
import com.example.ui.theme.DiwaliBorderGold
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSurfaceDark
import com.example.ui.theme.DiwaliSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineSelectorSheet(
    selectedEngine: AiEngineType,
    isNano4Available: Boolean,
    onSelectEngine: (AiEngineType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DiwaliSurfaceDark,
        tonalElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("engine_selector_sheet")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "🤖 AI Engine & Fallback Architecture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF8E7)
                    )
                    Text(
                        text = "Choose offline processing or Agora cloud voice",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD180)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EngineOptionCard(
                engine = AiEngineType.LOCAL_OFFLINE,
                isSelected = selectedEngine == AiEngineType.LOCAL_OFFLINE,
                icon = Icons.Default.RecordVoiceOver,
                title = "Basic offline voice",
                badge = "Works on emulators • No Nano required",
                description = "Bundled Hindi speech recognition with local expense rules. No AI model inference.",
                onClick = { onSelectEngine(AiEngineType.LOCAL_OFFLINE); onDismiss() }
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: Gemini Nano 4 Agentic Preview
            EngineOptionCard(
                engine = AiEngineType.GEMINI_NANO_4_AGENT,
                isSelected = selectedEngine == AiEngineType.GEMINI_NANO_4_AGENT,
                icon = Icons.Default.Psychology,
                title = "Gemini Nano (AICore Agentic)",
                badge = if (isNano4Available) "Model available" else "Readiness checked when you submit",
                description = "ML Kit AgentRunner + AppFunctions automatically invoking Room Database queries.",
                onClick = {
                    onSelectEngine(AiEngineType.GEMINI_NANO_4_AGENT)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Gemini Nano Prompt API
            EngineOptionCard(
                engine = AiEngineType.GEMINI_NANO_PROMPT_API,
                isSelected = selectedEngine == AiEngineType.GEMINI_NANO_PROMPT_API,
                icon = Icons.Default.Smartphone,
                title = "Gemini Nano (Prompt API Offline)",
                badge = "Requires a supported Nano device",
                description = "Real offline inference using Nano structured output; requires a supported device.",
                onClick = {
                    onSelectEngine(AiEngineType.GEMINI_NANO_PROMPT_API)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3: Agora Conversational AI
            EngineOptionCard(
                engine = AiEngineType.AGORA_CONVERSATIONAL_CLOUD,
                isSelected = selectedEngine == AiEngineType.AGORA_CONVERSATIONAL_CLOUD,
                icon = Icons.Default.RecordVoiceOver,
                title = "Agora Conversational AI",
                badge = "Cloud audio • Internet required",
                description = "Live Agora voice and spoken replies. Audio leaves the device; expense rules and storage run locally.",
                onClick = {
                    onSelectEngine(AiEngineType.AGORA_CONVERSATIONAL_CLOUD)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mandatory Agora Disclosure Box as requested:
            // "Agora tie-in (optional, only if it fits): a cloud fallback voice mode via Agora Conversational AI for phones without Nano 4. If you add it, disclose that you work at Agora."
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F1A2A))
                    .border(1.dp, Color(0xFF1E88E5).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclosure",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DISCLOSURE & ATTRIBUTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF90CAF9),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Agora cloud mode sends audio and transcripts through Agora and its speech providers. It requires internet. The developer works at Agora.",
                            fontSize = 11.sp,
                            color = Color(0xFFE3F2FD),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EngineOptionCard(
    engine: AiEngineType,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String,
    description: String,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) DiwaliGold else DiwaliBorderGold.copy(alpha = 0.4f)
    val bgColor = if (isSelected) DiwaliSurfaceElevated else DiwaliSurfaceDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) DiwaliGold else Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else DiwaliGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF8E7)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF5D2C04) else Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiwaliGold
                    )
                }

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 15.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = DiwaliGold,
                    unselectedColor = Color.White.copy(alpha = 0.4f)
                )
            )
        }
    }
}

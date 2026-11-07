package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
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
import com.example.ui.theme.DiwaliAmber
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSaffron
import com.example.ui.theme.DiwaliSurfaceDark

data class PresetHinglishPhrase(
    val title: String,
    val subtitle: String,
    val isQuery: Boolean = false,
    val emoji: String = "🎙️"
)

@Composable
fun QuickHinglishChips(
    onSelectPhrase: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        PresetHinglishPhrase(
            title = "500 ka mithai, 1200 ki lights",
            subtitle = "Multi-item logging",
            isQuery = false,
            emoji = "🍬"
        ),
        PresetHinglishPhrase(
            title = "Kitna kharcha hua?",
            subtitle = "Query total so far",
            isQuery = true,
            emoji = "❓"
        ),
        PresetHinglishPhrase(
            title = "250 ke diye aur 800 ka kurta",
            subtitle = "Diyas & Clothes logging",
            isQuery = false,
            emoji = "🪔"
        ),
        PresetHinglishPhrase(
            title = "Mithai pe kitna kharcha hua?",
            subtitle = "Category specific total",
            isQuery = true,
            emoji = "🍬"
        ),
        PresetHinglishPhrase(
            title = "1500 ki phuljhadiyan aur 350 ke patakhe",
            subtitle = "Fireworks multi-item",
            isQuery = false,
            emoji = "🎆"
        ),
        PresetHinglishPhrase(
            title = "Bhai ko 2000 ka shagun",
            subtitle = "Gifts & Shagun logging",
            isQuery = false,
            emoji = "🎁"
        ),
        PresetHinglishPhrase(
            title = "Pooja ka samaan 650",
            subtitle = "Pooja samagri",
            isQuery = false,
            emoji = "✨"
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ TAP TO SAY (DEMO PHRASES)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DiwaliGold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Zero Network",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presets.forEachIndexed { index, preset ->
                HinglishChip(
                    preset = preset,
                    onClick = { onSelectPhrase(preset.title) },
                    modifier = Modifier.testTag("preset_chip_$index")
                )
            }
        }
    }
}

@Composable
private fun HinglishChip(
    preset: PresetHinglishPhrase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (preset.isQuery) Color(0xFF64B5F6) else DiwaliGold
    val bgColor = if (preset.isQuery) Color(0xFF0D2840) else DiwaliSurfaceDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = preset.emoji,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "\"${preset.title}\"",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF8E7)
                )
                Text(
                    text = preset.subtitle,
                    fontSize = 10.sp,
                    color = if (preset.isQuery) Color(0xFF90CAF9) else Color(0xFFFFD180)
                )
            }
        }
    }
}

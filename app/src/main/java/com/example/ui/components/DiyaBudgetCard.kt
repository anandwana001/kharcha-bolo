package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiwaliAmber
import com.example.ui.theme.DiwaliBorderGold
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSaffron
import com.example.ui.theme.DiwaliSurfaceDark
import com.example.ui.theme.DiwaliSurfaceElevated
import com.example.ui.theme.DiwaliVermilion

@Composable
fun DiyaBudgetCard(
    totalSpent: Double,
    budget: Double,
    engineLabel: String,
    onEditBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (budget > 0) (totalSpent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = (budget - totalSpent).coerceAtLeast(0.0)

    val infiniteTransition = rememberInfiniteTransition(label = "flameGlow")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    val flameAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameAlpha"
    )

    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            DiwaliSurfaceElevated,
            DiwaliSurfaceDark,
            Color(0xFF190D05)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(DiwaliGold, DiwaliSaffron, DiwaliBorderGold)),
                RoundedCornerShape(24.dp)
            )
            .testTag("diya_budget_card"),
        color = Color.Transparent,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(cardBrush)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪔",
                            fontSize = 20.sp,
                            modifier = Modifier.scale(flameScale)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "शुभ दीपावली • DIWALI EXPENSES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DiwaliGold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Engine Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, DiwaliGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DiwaliGold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zero Network",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiwaliGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Amount and Diya Flame
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Kul Kharcha (Total Spent)",
                            fontSize = 12.sp,
                            color = Color(0xFFFFD180),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹${formatCurrency(totalSpent)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFF8E7),
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Flame Aura Graphic
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD54F).copy(alpha = flameAlpha),
                                        DiwaliSaffron.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🪔",
                            fontSize = 32.sp,
                            modifier = Modifier.scale(flameScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (progress > 0.85f) DiwaliVermilion else DiwaliGold,
                    trackColor = Color.Black.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Budget & Remaining Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEditBudget() }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "Budget: ₹${formatCurrency(budget)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = DiwaliGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Text(
                        text = "Remaining: ₹${formatCurrency(remaining)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining > 0) Color(0xFF81C784) else DiwaliVermilion
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val intPart = amount.toLong()
    return String.format("%,d", intPart)
}

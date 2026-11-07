package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategorySummary
import com.example.ui.theme.DiwaliBorderGold
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSurfaceDark

@Composable
fun CategoryBreakdownRow(
    summaries: List<CategorySummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🪔 CATEGORY BREAKDOWN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DiwaliGold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${summaries.size} Categories",
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
            summaries.forEach { item ->
                CategoryCard(item = item)
            }
        }
    }
}

@Composable
private fun CategoryCard(item: CategorySummary) {
    val emoji = getCategoryEmoji(item.category)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DiwaliSurfaceDark)
            .border(1.dp, DiwaliBorderGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF8E7)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${formatCurrency(item.totalAmount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DiwaliGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${item.itemCount} item${if (item.itemCount > 1) "s" else ""}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,d", amount.toLong())
}

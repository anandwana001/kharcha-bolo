package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.ExpenseEntity
import com.example.ui.theme.DiwaliBorderGold
import com.example.ui.theme.DiwaliGold
import com.example.ui.theme.DiwaliSurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = getCategoryEmoji(expense.category)
    val timeFormatted = formatTimestamp(expense.timestamp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, DiwaliBorderGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .testTag("expense_item_${expense.id}"),
        color = DiwaliSurfaceDark,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon & Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, DiwaliGold.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = categoryIcon,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.itemName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF8E7)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expense.category,
                            fontSize = 11.sp,
                            color = Color(0xFFFFD180),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = " • $timeFormatted",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    // Engine tag
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C1C10))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = expense.processedByEngine,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }

            // Amount and Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${formatCurrency(expense.amount)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = DiwaliGold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_expense_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Expense",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when {
        category.contains("Mithai") -> "🍬"
        category.contains("Light") -> "💡"
        category.contains("Diya") || category.contains("Pooja") -> "🪔"
        category.contains("Gift") || category.contains("Shagun") -> "🎁"
        category.contains("Firework") || category.contains("Patakhe") -> "🎆"
        category.contains("Cloth") || category.contains("Attire") -> "👗"
        category.contains("Feast") || category.contains("Snack") -> "🍲"
        else -> "✨"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 60000) return "Just now"
    if (diff < 3600000) return "${diff / 60000}m ago"
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,d", amount.toLong())
}

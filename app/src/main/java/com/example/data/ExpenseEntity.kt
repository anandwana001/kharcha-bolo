package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val amount: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawInputPrompt: String = "",
    val processedByEngine: String = "Gemini Nano 4"
)

data class CategorySummary(
    val category: String,
    val totalAmount: Double,
    val itemCount: Int
)

package com.example.ai

import com.google.mlkit.genai.schema.annotations.Generable
import com.google.mlkit.genai.schema.annotations.Guide

@Generable
data class ExpenseIntent(
    @Guide(description = "One of log, query, unknown", enumValues = ["log", "query", "unknown"])
    val action: String,
    @Guide(description = "Expenses explicitly mentioned by user; empty for queries or unknown input", maxItems = 20)
    val items: List<ExpenseIntentItem>,
    @Guide(description = "Category or item to filter a query; empty means all expenses")
    val categoryFilter: String
)

@Generable
data class ExpenseIntentItem(
    @Guide(description = "Name of purchased item") val itemName: String,
    @Guide(description = "Positive amount in Indian rupees") val amount: Double
)

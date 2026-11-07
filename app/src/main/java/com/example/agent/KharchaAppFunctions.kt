package com.example.agent

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionAccessLevel
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.ExperimentalAppFunctionsApi
import androidx.appfunctions.metadata.AppFunctionMetadata
import com.example.ai.HinglishExpenseParser
import com.example.data.AppDatabase
import com.example.data.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppFunctionSerializable
data class LogExpenseResult(val success: Boolean, val itemName: String, val amount: Double, val category: String, val totalExpensesNow: Double)

@AppFunctionSerializable
data class ExpenseSummaryResult(val totalAmount: Double, val categoryFilter: String, val itemCount: Int)

@RequiresApi(36)
@OptIn(ExperimentalAppFunctionsApi::class)
@AppFunctionServiceEntryPoint(serviceName = "KharchaExpenseService", appFunctionXmlFileName = "kharcha_expenses")
abstract class KharchaAppFunctions : AppFunctionService() {
    private val dao by lazy { AppDatabase.getInstance(applicationContext).expenseDao() }

    /** Logs one explicitly requested expense in rupees. Call once per purchased item, never for a total query.
     * @param itemName Purchased item, preserving the user's meaning.
     * @param amount Positive finite cost in Indian rupees.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(level = AppFunctionMetadata.ACCESS_LEVEL_SELF, isCompatEnforcementEnabled = true)
    suspend fun log_diwali_expense(itemName: String, amount: Double): LogExpenseResult = withContext(Dispatchers.IO) {
        require(itemName.isNotBlank() && amount.isFinite() && amount > 0) { "Expense needs an item and positive amount" }
        val category = HinglishExpenseParser.determineCategory(itemName)
        dao.insertExpense(ExpenseEntity(itemName = itemName.trim(), amount = amount, category = category,
            processedByEngine = "Gemini Nano Agent", rawInputPrompt = "$amount $itemName"))
        LogExpenseResult(true, itemName, amount, category, dao.getTotalExpense())
    }

    /** Reads the saved expense total. Does not add or modify expenses.
     * @param categoryFilter Category or item substring; empty string means all saved expenses.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(level = AppFunctionMetadata.ACCESS_LEVEL_SELF, isCompatEnforcementEnabled = true)
    suspend fun calculate_total_expenses(categoryFilter: String? = null): ExpenseSummaryResult = withContext(Dispatchers.IO) {
        val filter = categoryFilter?.trim().takeUnless { it.equals("all", true) }.orEmpty()
        val items = dao.getAllExpenses().filter { filter.isEmpty() || it.category.contains(filter, true) || it.itemName.contains(filter, true) }
        ExpenseSummaryResult(items.sumOf { it.amount }, filter, items.size)
    }
}

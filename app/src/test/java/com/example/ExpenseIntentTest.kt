package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.*
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExpenseIntentTest {
    private lateinit var db: AppDatabase
    private lateinit var manager: KharchaAgentManager
    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        manager = KharchaAgentManager(context, db.expenseDao())
    }
    @After fun close() { manager.close(); db.close() }

    @Test fun validModelOutputPersistsAndQueryDoesNotWrite() = runBlocking {
        val result = manager.applyIntent(ExpenseIntent("log", listOf(ExpenseIntentItem("Mithai", 500.0), ExpenseIntentItem("Lights", 1200.0)), ""), "input", 0)
        assertTrue(result is AgentExecutionResult.LoggedExpenses)
        assertEquals(1700.0, db.expenseDao().getTotalExpense(), 0.0)
        assertTrue(db.expenseDao().getAllExpenses().all { it.processedByEngine == "Gemini Nano Prompt API" })
        val query = manager.applyIntent(ExpenseIntent("query", emptyList(), "mithai"), "query", 0) as AgentExecutionResult.QueryResponse
        assertEquals(500.0, query.totalAmount, 0.0)
        assertEquals(2, db.expenseDao().getAllExpenses().size)
    }

    @Test fun invalidBatchNeverPartiallyWrites() = runBlocking {
        for (badAmount in listOf(-1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            try {
                manager.applyIntent(ExpenseIntent("log", listOf(ExpenseIntentItem("Valid", 10.0), ExpenseIntentItem("Invalid", badAmount)), ""), "input", 0)
                fail("Invalid amount must be rejected")
            } catch (_: IllegalArgumentException) {}
            assertTrue(db.expenseDao().getAllExpenses().isEmpty())
        }
    }

    @Test fun queryWithPurchasesIsRejected() = runBlocking {
        try {
            manager.applyIntent(ExpenseIntent("query", listOf(ExpenseIntentItem("Mithai", 10.0)), ""), "input", 0)
            fail("Ambiguous model output must be rejected")
        } catch (_: IllegalArgumentException) {}
        assertTrue(db.expenseDao().getAllExpenses().isEmpty())
    }

    @Test fun agoraNeverPretendsToRunOrWrites() = runBlocking {
        assertTrue(manager.execute("500 ka mithai", AiEngineType.AGORA_CONVERSATIONAL_CLOUD) is AgentExecutionResult.Error)
        assertTrue(db.expenseDao().getAllExpenses().isEmpty())
    }
    @Test fun offlineHindiVoiceLogsWithoutNano() = runBlocking {
        val result = manager.execute("पांच सौ की मिठाई और बारह सौ की लाइट्स", AiEngineType.LOCAL_OFFLINE)
        assertTrue(result is AgentExecutionResult.LoggedExpenses)
        assertEquals(1700.0, db.expenseDao().getTotalExpense(), 0.0)
        assertEquals(2, db.expenseDao().getAllExpenses().size)
        assertTrue(db.expenseDao().getAllExpenses().all { it.processedByEngine == "Basic offline rules" })
        val query = manager.execute("मिठाई पे कितना खर्च हुआ", AiEngineType.LOCAL_OFFLINE) as AgentExecutionResult.QueryResponse
        assertEquals(500.0, query.totalAmount, 0.0)
        assertEquals(2, db.expenseDao().getAllExpenses().size)
    }

    @Test fun spokenHindiAmountsNormalize() {
        assertEquals("1250 ka kurta", SpokenExpenseNormalizer.normalize("एक हजार दो सौ पचास का कुर्ता"))
        assertEquals("250 ke diye", SpokenExpenseNormalizer.normalize("२५० के दीये"))
        assertEquals("500 ki mithai , 1200 ki lights", SpokenExpenseNormalizer.normalize("पांच सौ की मिठाई और बारह सौ की लाइट्स"))
    }
}

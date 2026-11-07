package com.example

import com.example.ai.HinglishExpenseParser
import com.example.ai.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMultiItemHinglishParsing() {
        val result = HinglishExpenseParser.parse("500 ka mithai, 1200 ki lights")
        assertTrue("Result must be ExpenseItems", result is ParseResult.ExpenseItems)
        val items = (result as ParseResult.ExpenseItems).items
        assertEquals(2, items.size)

        assertEquals(500.0, items[0].amount, 0.01)
        assertTrue(items[0].itemName.contains("Mithai", ignoreCase = true))
        assertEquals("Mithai & Sweets", items[0].category)

        assertEquals(1200.0, items[1].amount, 0.01)
        assertTrue(items[1].itemName.contains("Lights", ignoreCase = true))
        assertEquals("Lights & Decor", items[1].category)
    }

    @Test
    fun testDiyeAndKurtaParsing() {
        val result = HinglishExpenseParser.parse("250 ke diye aur 800 ka kurta")
        assertTrue(result is ParseResult.ExpenseItems)
        val items = (result as ParseResult.ExpenseItems).items
        assertEquals(2, items.size)
        assertEquals(250.0, items[0].amount, 0.01)
        assertEquals(800.0, items[1].amount, 0.01)
    }

    @Test
    fun testTotalQueryDetection() {
        val result = HinglishExpenseParser.parse("kitna kharcha hua?")
        assertTrue(result is ParseResult.TotalQuery)
        val query = result as ParseResult.TotalQuery
        assertEquals(null, query.categoryFilter)
    }

    @Test
    fun testCategorySpecificQuery() {
        val result = HinglishExpenseParser.parse("mithai pe kitna kharcha hua?")
        assertTrue(result is ParseResult.TotalQuery)
        val query = result as ParseResult.TotalQuery
        assertEquals("mithai", query.categoryFilter)
    }
}

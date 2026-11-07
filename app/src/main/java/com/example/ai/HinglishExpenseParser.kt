package com.example.ai

data class ParsedExpenseItem(
    val itemName: String,
    val amount: Double,
    val category: String,
    val rawText: String
)

sealed class ParseResult {
    data class ExpenseItems(val items: List<ParsedExpenseItem>) : ParseResult()
    data class TotalQuery(val categoryFilter: String?) : ParseResult()
    data class Unknown(val rawText: String) : ParseResult()
}

object HinglishExpenseParser {

    private val QUERY_KEYWORDS = listOf(
        "kitna kharcha", "kitna kharch", "total kitna", "kul kharcha",
        "kitna hua", "hisab batao", "kharcha batao", "total batao",
        "how much spent", "total expenses"
    )

    fun parse(input: String): ParseResult {
        val trimmed = input.trim().lowercase()

        // 1. Check if user is asking for total expenses / query
        if (isQuery(trimmed)) {
            val filter = extractQueryCategoryFilter(trimmed)
            return ParseResult.TotalQuery(filter)
        }

        // 2. Parse expense items (supports multiple items separated by comma, "aur", "and", "+")
        val items = extractExpenseItems(input)
        if (items.isNotEmpty()) {
            return ParseResult.ExpenseItems(items)
        }

        return ParseResult.Unknown(input)
    }

    private fun isQuery(text: String): Boolean {
        if (text.endsWith("?")) return true
        return QUERY_KEYWORDS.any { text.contains(it) }
    }

    private fun extractQueryCategoryFilter(text: String): String? {
        val clean = text.replace("?", "").trim()
        val categories = listOf("mithai", "sweets", "lights", "diye", "diya", "pooja", "gift", "patakhe", "kurta", "kapde")
        for (cat in categories) {
            if (clean.contains(cat)) {
                return cat
            }
        }
        return null
    }

    private fun extractExpenseItems(input: String): List<ParsedExpenseItem> {
        val results = mutableListOf<ParsedExpenseItem>()

        // Split by clauses: commas, "aur", "and", ";", "&"
        val clauses = input.split(Regex("[,;&]|\\baur\\b|\\band\\b|\\bplus\\b", RegexOption.IGNORE_CASE))

        for (clause in clauses) {
            val trimmedClause = clause.trim()
            if (trimmedClause.isEmpty()) continue

            val item = parseSingleClause(trimmedClause)
            if (item != null) {
                results.add(item)
            }
        }

        // Fallback: If split by conjunctions didn't catch multiple amounts in one clause (e.g. "500 mithai 1200 lights")
        if (results.isEmpty()) {
            val multiMatches = parseMultiNumbersInClause(input)
            results.addAll(multiMatches)
        }

        return results
    }

    private fun parseSingleClause(clause: String): ParsedExpenseItem? {
        // Pattern A: Amount followed by "ka/ki/ke" and itemName
        // e.g. "500 ka mithai", "1200 ki lights", "250 ke diye", "₹500 for sweets", "rs 800 kurta"
        val regexAmountFirst = Regex(
            """(?:rs\.?|inr|₹)?\s*(\d+(?:\.\d+)?)\s*(?:ka|ki|ke|rupaye|rupee|rupees|wala|wali)?\s*(.+?)$""",
            RegexOption.IGNORE_CASE
        )
        val matchA = regexAmountFirst.find(clause)
        if (matchA != null) {
            val amountStr = matchA.groupValues[1]
            val itemStr = matchA.groupValues[2].trim()
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0 && itemStr.isNotEmpty() && !isPureNoise(itemStr)) {
                val cleanItem = cleanItemName(itemStr)
                val category = determineCategory(cleanItem)
                return ParsedExpenseItem(cleanItem, amount, category, clause)
            }
        }

        // Pattern B: Item name followed by amount
        // e.g. "lights 1200", "mithai 500 ka", "kurta 800 rupees"
        val regexItemFirst = Regex(
            """^(.+?)\s*(?:ka|ki|ke|pe|par)?\s*(?:rs\.?|inr|₹)?\s*(\d+(?:\.\d+)?)\s*(?:ka|ki|ke|rupaye|rupee|rupees)?$""",
            RegexOption.IGNORE_CASE
        )
        val matchB = regexItemFirst.find(clause)
        if (matchB != null) {
            val itemStr = matchB.groupValues[1].trim()
            val amountStr = matchB.groupValues[2]
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0 && itemStr.isNotEmpty() && !isPureNoise(itemStr)) {
                val cleanItem = cleanItemName(itemStr)
                val category = determineCategory(cleanItem)
                return ParsedExpenseItem(cleanItem, amount, category, clause)
            }
        }

        // Pattern C: "Bhai ko 2000 ka gift/shagun" or "mummy ko 1000 shagun"
        val regexGift = Regex(
            """(.+?)\s*ko\s*(\d+(?:\.\d+)?)\s*(?:ka|ki|ke)?\s*(.*)""",
            RegexOption.IGNORE_CASE
        )
        val matchC = regexGift.find(clause)
        if (matchC != null) {
            val recipient = matchC.groupValues[1].trim()
            val amount = matchC.groupValues[2].toDoubleOrNull()
            val suffix = matchC.groupValues[3].trim()
            if (amount != null && amount > 0) {
                val fullItem = if (suffix.isNotEmpty()) "$recipient ka $suffix" else "$recipient ka shagun"
                val cleanItem = cleanItemName(fullItem)
                return ParsedExpenseItem(cleanItem, amount, "Gifts & Shagun", clause)
            }
        }

        return null
    }

    private fun parseMultiNumbersInClause(clause: String): List<ParsedExpenseItem> {
        val list = mutableListOf<ParsedExpenseItem>()
        // Find all numbers with surrounding text
        val numberMatches = Regex("""(\d+(?:\.\d+)?)""").findAll(clause).toList()
        if (numberMatches.size >= 2) {
            var lastIndex = 0
            for (i in numberMatches.indices) {
                val numMatch = numberMatches[i]
                val amount = numMatch.value.toDoubleOrNull() ?: continue
                val start = numMatch.range.first
                val end = if (i < numberMatches.size - 1) numberMatches[i + 1].range.first else clause.length
                val snippet = clause.substring(start, end).trim()
                val parsed = parseSingleClause(snippet)
                if (parsed != null) {
                    list.add(parsed)
                }
            }
        }
        return list
    }

    private fun isPureNoise(text: String): Boolean {
        val noiseWords = setOf("ka", "ki", "ke", "ko", "se", "pe", "par", "hai", "tha", "mein", "aur", "and", "the")
        return noiseWords.contains(text.lowercase().trim())
    }

    private fun cleanItemName(raw: String): String {
        return raw.replace(Regex("""\b(ka|ki|ke|rupaye|rupee|rupees|inr|rs|₹|purchased|bought|liya|liye|kharida)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .replaceFirstChar { it.uppercase() }
            .ifEmpty { "Diwali Expense" }
    }

    fun determineCategory(itemName: String): String {
        val lower = itemName.lowercase()
        return when {
            // Sweets & Treats
            lower.contains("mithai") || lower.contains("sweet") || lower.contains("laddu") ||
            lower.contains("ladoo") || lower.contains("kaju") || lower.contains("katli") ||
            lower.contains("barfi") || lower.contains("gulab jamun") || lower.contains("rasgulla") ||
            lower.contains("halwa") || lower.contains("peda") || lower.contains("soan papdi") ||
            lower.contains("chocolate") || lower.contains("pastry") -> "Mithai & Sweets"

            // Diyas & Puja
            lower.contains("diya") || lower.contains("diye") || lower.contains("deepak") ||
            lower.contains("pooja") || lower.contains("puja") || lower.contains("agarbatti") ||
            lower.contains("kapoor") || lower.contains("camphor") || lower.contains("dhoop") ||
            lower.contains("prasad") || lower.contains("roli") || lower.contains("chawal") ||
            lower.contains("murti") || lower.contains("idol") || lower.contains("lakshmi") ||
            lower.contains("ganesh") || lower.contains("havan") -> "Diyas & Pooja"

            // Lights & Decor
            lower.contains("light") || lower.contains("lights") || lower.contains("ladi") ||
            lower.contains("jhalar") || lower.contains("fairy") || lower.contains("lantern") ||
            lower.contains("kandil") || lower.contains("rangoli") || lower.contains("toran") ||
            lower.contains("flower") || lower.contains("phool") || lower.contains("genda") ||
            lower.contains("marigold") || lower.contains("decor") || lower.contains("candle") -> "Lights & Decor"

            // Gifts & Shagun
            lower.contains("gift") || lower.contains("shagun") || lower.contains("lifafa") ||
            lower.contains("bonus") || lower.contains("bhai") || lower.contains("behen") ||
            lower.contains("didi") || lower.contains("mummy") || lower.contains("papa") ||
            lower.contains("dry fruit") || lower.contains("badam") || lower.contains("kaju box") -> "Gifts & Shagun"

            // Fireworks & Crackers
            lower.contains("patakhe") || lower.contains("patakha") || lower.contains("cracker") ||
            lower.contains("phuljhadi") || lower.contains("anar") || lower.contains("chakri") ||
            lower.contains("rocket") || lower.contains("sparkler") || lower.contains("firework") -> "Fireworks & Crackers"

            // Clothes & Attire
            lower.contains("kurta") || lower.contains("pyjama") || lower.contains("pajama") ||
            lower.contains("saree") || lower.contains("sari") || lower.contains("suit") ||
            lower.contains("lehenga") || lower.contains("kapde") || lower.contains("dress") ||
            lower.contains("shoes") || lower.contains("jutti") || lower.contains("clothes") -> "Clothes & Attire"

            // Feast & Food
            lower.contains("samosa") || lower.contains("kachori") || lower.contains("namkeen") ||
            lower.contains("snack") || lower.contains("dinner") || lower.contains("party") ||
            lower.contains("lunch") || lower.contains("chai") || lower.contains("mathri") -> "Feast & Snacks"

            else -> "Diwali Essentials"
        }
    }
}

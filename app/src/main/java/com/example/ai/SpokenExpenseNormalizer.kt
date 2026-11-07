package com.example.ai

/** Converts Hindi speech transcripts to the limited grammar used by basic offline mode. */
object SpokenExpenseNormalizer {
    private val hindiNumbers = ("शून्य एक दो तीन चार पांच छह सात आठ नौ दस ग्यारह बारह तेरह चौदह पंद्रह सोलह सत्रह अठारह उन्नीस " +
        "बीस इक्कीस बाईस तेईस चौबीस पच्चीस छब्बीस सत्ताईस अट्ठाईस उनतीस तीस इकतीस बत्तीस तैंतीस चौंतीस पैंतीस छत्तीस सैंतीस अड़तीस उनतालीस " +
        "चालीस इकतालीस बयालीस तैंतालीस चवालीस पैंतालीस छियालीस सैंतालीस अड़तालीस उनचास पचास इक्यावन बावन तिरपन चौवन पचपन छप्पन सत्तावन अट्ठावन उनसठ " +
        "साठ इकसठ बासठ तिरसठ चौंसठ पैंसठ छियासठ सड़सठ अड़सठ उनहत्तर सत्तर इकहत्तर बहत्तर तिहत्तर चौहत्तर पचहत्तर छिहत्तर सतहत्तर अठहत्तर उनासी " +
        "अस्सी इक्यासी बयासी तिरासी चौरासी पचासी छियासी सत्तासी अट्ठासी नवासी नब्बे इक्यानवे बानवे तिरानवे चौरानवे पचानवे छियानवे सत्तानवे अट्ठानवे निन्यानवे")
        .split(" ").withIndex().associate { it.value to it.index.toLong() } + mapOf("पाँच" to 5L, "छः" to 6L, "पन्द्रह" to 15L, "अठ्ठाईस" to 28L)
    private val scales = mapOf("सौ" to 100L, "हजार" to 1000L, "हज़ार" to 1000L, "लाख" to 100000L)
    private val words = mapOf(
        "मिठाई" to "mithai", "मिठाइयां" to "mithai", "मिठाइयाँ" to "mithai", "लड्डू" to "laddu",
        "लाइट" to "lights", "लाइट्स" to "lights", "लाइटें" to "lights", "दीये" to "diye", "दिए" to "diye", "दीया" to "diya",
        "पूजा" to "pooja", "गिफ्ट" to "gift", "शगुन" to "shagun", "पटाखे" to "patakhe", "कुर्ता" to "kurta", "कपड़े" to "kapde",
        "का" to "ka", "की" to "ki", "के" to "ke", "और" to ",", "प्लस" to ",", "रुपये" to "", "रुपए" to "", "रुपया" to "",
        "कितना" to "kitna", "कितने" to "kitna", "खर्च" to "kharcha", "खर्चा" to "kharcha", "कुल" to "total", "टोटल" to "total",
        "बताओ" to "batao", "हिसाब" to "hisab", "हुआ" to "hua", "है" to "", "पे" to "pe", "पर" to "par"
    )

    fun normalize(input: String): String {
        val tokens = input.map { c -> if (c in '०'..'९') ('0'.code + (c - '०')).toChar() else c }.joinToString("")
            .replace(Regex("[।,;]"), " , ").trim().split(Regex("\\s+"))
        val output = mutableListOf<String>()
        var group = 0L
        var total = 0L
        var hasNumber = false
        fun flush() {
            if (hasNumber) output.add((total + group).toString())
            group = 0; total = 0; hasNumber = false
        }
        for (token in tokens) {
            val number = hindiNumbers[token]
            val scale = scales[token]
            when {
                number != null -> { group += number; hasNumber = true }
                scale != null -> {
                    group = group.coerceAtLeast(1) * scale; hasNumber = true
                    if (scale >= 1000) { total += group; group = 0 }
                }
                else -> { flush(); output.add(words[token] ?: token) }
            }
        }
        flush()
        return output.joinToString(" ").replace(Regex("\\s+"), " ").trim()
    }
}

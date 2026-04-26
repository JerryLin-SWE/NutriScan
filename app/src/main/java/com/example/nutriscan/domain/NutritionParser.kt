package com.example.nutriscan.domain

object NutritionParser {

    // Each entry: pair of (field name for logging, list of regex patterns to try)
    private val caloriePatterns = listOf(
        Regex("""calories[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
        Regex("""energy[^\d]*(\d+)\s*kcal""", RegexOption.IGNORE_CASE),
        Regex("""cal\.[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
    )
    private val proteinPatterns = listOf(
        Regex("""protein[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
    )
    private val carbPatterns = listOf(
        Regex("""total\s+carb\w*[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
        Regex("""carbohydrate[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
        Regex("""carbs[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
    )
    private val fatPatterns = listOf(
        Regex("""total\s+fat[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
        Regex("""(?<!\w)fat[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
    )
    private val sugarPatterns = listOf(
        Regex("""total\s+sugar\w*[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
        Regex("""sugars?[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
    )
    private val waterPatterns = listOf(
        Regex("""water[^\d]*(\d+)\s*ml""", RegexOption.IGNORE_CASE),
        Regex("""water[^\d]*(\d+)""", RegexOption.IGNORE_CASE),
    )
    private val servingPatterns = listOf(
        Regex("""serving\s+size[^\n]*""", RegexOption.IGNORE_CASE),
        Regex("""serv\.\s+size[^\n]*""", RegexOption.IGNORE_CASE),
    )

    fun parse(rawText: String): NutritionLabel? {
        if (rawText.isBlank()) return null

        val flat = rawText.replace('\n', ' ')

        val calories = firstMatch(caloriePatterns, flat) ?: return null
        val protein  = firstMatch(proteinPatterns, flat) ?: 0
        val carbs    = firstMatch(carbPatterns,    flat) ?: 0
        val fat      = firstMatch(fatPatterns,     flat) ?: 0
        val sugar    = firstMatch(sugarPatterns,   flat) ?: 0
        val water    = firstMatch(waterPatterns,   flat) ?: 0
        val serving  = servingPatterns
            .firstNotNullOfOrNull { it.find(flat)?.value?.trim() } ?: ""

        return NutritionLabel(
            calories    = calories,
            proteinG    = protein,
            carbsG      = carbs,
            fatG        = fat,
            sugarG      = sugar,
            waterMl     = water,
            servingSize = serving,
            rawText     = rawText,
        )
    }

    private fun firstMatch(patterns: List<Regex>, text: String): Int? =
        patterns.firstNotNullOfOrNull { regex ->
            regex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
}

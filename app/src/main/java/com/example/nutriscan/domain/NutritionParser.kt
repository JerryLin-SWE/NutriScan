package com.example.nutriscan.domain

/**
 * Parses raw OCR text from a nutrition facts label into a [NutritionLabel].
 *
 * Strategy: scan each line for a keyword, then grab the first integer that
 * follows it on the same line (or on the very next non-empty line).
 * This handles both single-column ("Calories 250") and two-column
 * ("Calories    250  125") label layouts.
 */
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
        val serving  = servingPatterns
            .firstNotNullOfOrNull { it.find(flat)?.value?.trim() } ?: ""

        return NutritionLabel(
            calories    = calories,
            proteinG    = protein,
            carbsG      = carbs,
            fatG        = fat,
            servingSize = serving,
            rawText     = rawText,
        )
    }

    private fun firstMatch(patterns: List<Regex>, text: String): Int? =
        patterns.firstNotNullOfOrNull { regex ->
            regex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
}

package com.example.nutriscan.domain

object DietAnalyzer {

    fun analyze(label: NutritionLabel, goals: UserGoals): FitResult {
        val remCalories = goals.dailyCalories - label.calories
        val remProtein  = goals.dailyProteinG - label.proteinG
        val remCarbs    = goals.dailyCarbsG   - label.carbsG
        val remFat      = goals.dailyFatG     - label.fatG

        val fits = remCalories >= 0 && remProtein >= 0 && remCarbs >= 0 && remFat >= 0

        val message = if (fits) {
            "This item fits your daily goals. $remCalories kcal remaining."
        } else {
            buildString {
                append("This item may exceed your goals. ")
                if (remCalories < 0) append("Calories over by ${-remCalories}. ")
                if (remProtein  < 0) append("Protein over by ${-remProtein}g. ")
                if (remCarbs    < 0) append("Carbs over by ${-remCarbs}g. ")
                if (remFat      < 0) append("Fat over by ${-remFat}g.")
            }.trim()
        }

        return FitResult(
            fitsInDiet        = fits,
            remainingCalories = remCalories,
            remainingProteinG = remProtein,
            remainingCarbsG   = remCarbs,
            remainingFatG     = remFat,
            message           = message,
        )
    }
}

package com.example.nutriscan.domain

data class NutritionLabel(
    val calories:    Int,
    val proteinG:    Int,
    val carbsG:      Int,
    val fatG:        Int,
    val servingSize: String = "",
    val rawText:     String = "",
)

data class UserGoals(
    val dailyCalories: Int,
    val dailyProteinG: Int,
    val dailyCarbsG:   Int,
    val dailyFatG:     Int,
)

data class FitResult(
    val fitsInDiet:        Boolean,
    val remainingCalories: Int,
    val remainingProteinG: Int,
    val remainingCarbsG:   Int,
    val remainingFatG:     Int,
    val message:           String,
)

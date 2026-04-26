package com.example.nutriscan

data class NutritionLog(
    val logId: String = "",
    val userId: String = "",
    val calories: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

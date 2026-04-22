package com.example.nutriscan

data class Dietary(
    val dietaryId: String = "",
    val age: Int,
    val weight: Int,
    val metricUnit: String,
    val activityLevel: Int,
    val goals: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val diets: List<String> = emptyList(),
    val userId: String = ""
)
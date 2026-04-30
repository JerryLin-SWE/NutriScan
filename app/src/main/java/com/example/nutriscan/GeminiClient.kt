package com.example.nutriscan

import com.example.nutriscan.domain.NutritionLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiClient(private val apiKey: String) {

    suspend fun getNutritionOverview(
        label: NutritionLabel,
        goals: List<String>,
        allergens: List<String>,
        diets: List<String>
    ): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 15000

        val body = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("max_tokens", 150)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", buildPrompt(label, goals, allergens, diets))
                })
            })
        }.toString()

        OutputStreamWriter(connection.outputStream).use { it.write(body) }

        val code = connection.responseCode
        val response = if (code == 200)
            connection.inputStream.bufferedReader().readText()
        else
            connection.errorStream?.bufferedReader()?.readText() ?: ""

        if (code == 200) {
            JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } else {
            println("OpenAI error($code): $response")
            ""
        }
    }

    private fun buildPrompt(
        label: NutritionLabel,
        goals: List<String>,
        allergens: List<String>,
        diets: List<String>
    ): String {
        return """
            You are a nutrition assistant. Based on the food's nutrition info and the user's profile, give a 2-3 sentence overview of whether this food is a good choice. Be direct and specific. No markdown, no bullet points, plain sentences only.

            Nutrition facts:
            - Calories: ${label.calories} kcal
            - Protein: ${label.proteinG}g
            - Carbs: ${label.carbsG}g
            - Fat: ${label.fatG}g
            - Sugar: ${label.sugarG}g

            User profile:
            - Goals: ${goals.ifEmpty { listOf("none specified") }.joinToString(", ")}
            - Allergens to avoid: ${allergens.ifEmpty { listOf("none") }.joinToString(", ")}
            - Diet type: ${diets.ifEmpty { listOf("no specific diet") }.joinToString(", ")}
        """.trimIndent()
    }
}

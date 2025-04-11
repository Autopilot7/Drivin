package com.example.drivin

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdviceGenerator {
    // Store the advice from API
    val adviceList: MutableState<List<String>> = mutableStateOf(emptyList())

    // Loading state
    val isLoading: MutableState<Boolean> = mutableStateOf(false)

    // API key - replace with your actual API key
    private const val API_KEY = "A API KEY :>"
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = API_KEY
        )
    }

    fun generateAdvice(suddenBrakesCount: Int, suddenAccelerationCount: Int, suddenDirectionChangesCount: Int) {
        isLoading.value = true

        val prompt = buildPrompt(suddenBrakesCount, suddenAccelerationCount, suddenDirectionChangesCount)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)
                val advice = parseResponse(response)

                // Update the advice on main thread
                adviceList.value = advice
            } catch (e: Exception) {
                // Fallback to default advice if API fails
                adviceList.value = getDefaultAdvice()
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun buildPrompt(suddenBrakesCount: Int, suddenAccelerationCount: Int, suddenDirectionChangesCount: Int): String {
        return "Đưa ra lời khuyên lái xe cho một người có $suddenBrakesCount lần phanh gấp, " +
                "$suddenDirectionChangesCount lần đổi hướng đột ngột, và $suddenAccelerationCount lần tăng tốc đột ngột. " +
                "Hãy liệt kê 4 lời khuyên ngắn gọn bằng tiếng anh, mỗi lời khuyên không quá 15 từ."
    }

    private fun parseResponse(response: GenerateContentResponse): List<String> {
        val text = response.text?.trim() ?: return getDefaultAdvice()

        // Split by line breaks or numbered lists (1., 2., etc.)
        val adviceItems = text.split("\n")
            .filter { it.isNotBlank() }
            .map { it.replace(Regex("^\\d+\\.\\s*"), "").trim() }
            .take(4)

        // If we got fewer than 4 items, use default advice
        return if (adviceItems.size < 4) getDefaultAdvice() else adviceItems
    }

    private fun getDefaultAdvice(): List<String> = listOf(
        "Anticipate stops to avoid sudden braking",
        "Accelerate gradually to improve fuel efficiency",
        "Signal early before changing direction",
        "Maintain safe distance from other vehicles"
    )
}
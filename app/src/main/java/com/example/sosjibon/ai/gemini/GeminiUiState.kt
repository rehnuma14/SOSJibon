package com.example.sosjibon.ai.gemini

data class GeminiUiState(
    val isLoading: Boolean = false,
    val response: String = "",
    val error: String? = null
)
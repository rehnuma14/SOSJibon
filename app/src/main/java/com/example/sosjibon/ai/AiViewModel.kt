package com.example.sosjibon.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiUiState(
    val isLoading: Boolean = false,
    val message: String = "",
    val intent: AiIntent = AiIntent.UNKNOWN,
    val route: String? = null
)

class AiViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val classifier =
        IntentClassifier(application.applicationContext)

    private val controller =
        AiAssistantController(
            intentClassifier = classifier
        )

    private val _uiState =
        MutableStateFlow(AiUiState())

    val uiState: StateFlow<AiUiState> =
        _uiState.asStateFlow()

    fun ask(question: String) {

        if (question.isBlank()) {
            return
        }

        _uiState.value =
            AiUiState(
                isLoading = true
            )

        viewModelScope.launch(Dispatchers.Default) {

            try {

                val result =
                    controller.handleQuery(
                        question
                    )

                _uiState.value =
                    AiUiState(
                        isLoading = false,
                        message = result.message,
                        intent = result.intent,
                        route = result.intent.route
                    )

            } catch (e: Exception) {

                _uiState.value =
                    AiUiState(
                        isLoading = false,
                        message =
                            "Sorry, I couldn't process that request.",
                        intent = AiIntent.UNKNOWN,
                        route = null
                    )
            }
        }
    }

    fun clearRoute() {

        _uiState.value =
            _uiState.value.copy(
                route = null
            )
    }

    fun clearMessage() {

        _uiState.value =
            _uiState.value.copy(
                message = ""
            )
    }

    override fun onCleared() {

        classifier.close()

        super.onCleared()
    }
}
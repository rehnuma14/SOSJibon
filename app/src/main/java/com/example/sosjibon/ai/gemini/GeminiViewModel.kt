package com.example.sosjibon.ai.gemini

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = GeminiRepository(
        geminiService = GeminiService(),
        context = application.applicationContext
    )

    private val _uiState =
        MutableStateFlow(
            GeminiUiState()
        )

    val uiState: StateFlow<GeminiUiState> =
        _uiState.asStateFlow()

    /**
     * Currently selected PDF.
     *
     * The Uri points to the PDF selected by the user.
     */
    private var selectedPdfUri: Uri? = null

    /**
     * Current Gemini request.
     *
     * Prevents multiple Gemini requests from
     * running at the same time.
     */
    private var requestJob: Job? = null

    /**
     * Prevents immediately sending another request
     * after the previous request has finished.
     */
    private var requestCooldownUntil = 0L

    companion object {

        /**
         * Normal cooldown after a request.
         *
         * Prevents accidental double taps.
         */
        private const val NORMAL_COOLDOWN_MS = 3_000L

        /**
         * Local cooldown after Gemini quota error.
         */
        private const val QUOTA_COOLDOWN_MS = 60_000L

        /**
         * Local cooldown when Gemini server
         * reports high demand.
         */
        private const val SERVER_BUSY_COOLDOWN_MS = 15_000L
    }

    /**
     * Called when the user selects a PDF.
     */
    fun selectPdf(uri: Uri) {

        selectedPdfUri = uri

        /**
         * Clear previous Gemini response/error.
         */
        _uiState.value =
            GeminiUiState()
    }

    /**
     * Sends either:
     *
     * 1. Text question
     *
     * OR
     *
     * 2. PDF + question
     *
     * OR
     *
     * 3. PDF without question
     */
    fun askGemini(
        question: String
    ) {

        val cleanQuestion =
            question.trim()

        val pdfUri =
            selectedPdfUri

        /**
         * Nothing to send.
         *
         * Don't send an empty request.
         */
        if (
            cleanQuestion.isBlank() &&
            pdfUri == null
        ) {
            _uiState.value =
                GeminiUiState(
                    isLoading = false,
                    error = "Please enter a question or upload a PDF."
                )

            return
        }

        /**
         * IMPORTANT:
         *
         * If Gemini is already processing a request,
         * ignore another button press.
         */
        if (requestJob?.isActive == true) {
            return
        }

        /**
         * Check local cooldown.
         */
        val currentTime =
            System.currentTimeMillis()

        if (
            currentTime <
            requestCooldownUntil
        ) {

            val remainingSeconds =
                (
                        (requestCooldownUntil - currentTime) / 1000
                        ).coerceAtLeast(1)

            _uiState.value =
                GeminiUiState(
                    isLoading = false,
                    error =
                        "Please wait $remainingSeconds seconds before trying again."
                )

            return
        }

        /**
         * Show loading state immediately.
         */
        _uiState.value =
            GeminiUiState(
                isLoading = true
            )

        /**
         * Start Gemini request.
         *
         * The repository.askGemini() call MUST be
         * inside this coroutine because it is suspend.
         */
        requestJob =
            viewModelScope.launch(
                Dispatchers.IO
            ) {

                val result =
                    repository.askGemini(
                        question = cleanQuestion,
                        pdfUri = pdfUri
                    )

                result
                    .onSuccess { response ->

                        /**
                         * Successful response.
                         */
                        requestCooldownUntil =
                            System.currentTimeMillis() +
                                    NORMAL_COOLDOWN_MS

                        _uiState.value =
                            GeminiUiState(
                                isLoading = false,
                                response = response
                            )
                    }
                    .onFailure { error ->

                        when (error) {

                            /**
                             * Gemini quota exceeded.
                             */
                            is GeminiQuotaException -> {

                                requestCooldownUntil =
                                    System.currentTimeMillis() +
                                            QUOTA_COOLDOWN_MS

                                _uiState.value =
                                    GeminiUiState(
                                        isLoading = false,
                                        error =
                                            "Gemini request limit reached. " +
                                                    "Please wait about 1 minute and try again."
                                    )
                            }

                            /**
                             * Gemini server is currently busy.
                             */
                            is GeminiServerBusyException -> {

                                requestCooldownUntil =
                                    System.currentTimeMillis() +
                                            SERVER_BUSY_COOLDOWN_MS

                                _uiState.value =
                                    GeminiUiState(
                                        isLoading = false,
                                        error =
                                            "Gemini is currently experiencing high demand. " +
                                                    "Please try again in a few moments."
                                    )
                            }

                            /**
                             * Internet/network problem.
                             */
                            is GeminiNetworkException -> {

                                requestCooldownUntil =
                                    System.currentTimeMillis() +
                                            NORMAL_COOLDOWN_MS

                                _uiState.value =
                                    GeminiUiState(
                                        isLoading = false,
                                        error =
                                            error.message
                                                ?: "Please check your internet connection."
                                    )
                            }

                            /**
                             * Other Gemini errors.
                             */
                            else -> {

                                requestCooldownUntil =
                                    System.currentTimeMillis() +
                                            NORMAL_COOLDOWN_MS

                                _uiState.value =
                                    GeminiUiState(
                                        isLoading = false,
                                        error =
                                            error.message
                                                ?: "Something went wrong while contacting Gemini."
                                    )
                            }
                        }
                    }
            }
    }

    /**
     * Clears the current Gemini response/error.
     */
    fun clearResponse() {

        /**
         * Don't clear the state while a request
         * is actively running.
         */
        if (requestJob?.isActive == true) {
            return
        }

        _uiState.value =
            GeminiUiState()
    }

    /**
     * Removes the selected PDF.
     *
     * Useful if your UI has a remove/close button
     * on the PDF attachment.
     */
    fun clearPdf() {

        if (requestJob?.isActive == true) {
            return
        }

        selectedPdfUri = null

        _uiState.value =
            GeminiUiState()
    }

    /**
     * Returns whether a PDF is currently selected.
     */
    fun hasSelectedPdf(): Boolean {
        return selectedPdfUri != null
    }

    /**
     * Cancel Gemini request when ViewModel is destroyed.
     */
    override fun onCleared() {

        requestJob?.cancel()

        super.onCleared()
    }
}
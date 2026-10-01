/*
 * GeminiService.kt
 * What this file does: Sends prompts and PDF files to Google Gemini API via direct REST or Firebase AI SDK.
 *
 * Pseudo-code:
 * 1. Receive text prompt or PDF byte array.
 * 2. Try direct REST request to Gemini Flash endpoints (gemini-2.5-flash, gemini-2.0-flash, gemini-flash-latest).
 * 3. If direct REST fails, fallback to Firebase AI SDK model loop.
 * 4. Parse candidates text response and return cleaned output string.
 */

package com.example.sosjibon.ai.gemini

import android.util.Base64
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    companion object {
        private const val TAG = "GeminiService"
        private const val DEFAULT_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
        
        private var customTokenId: String? = null

        fun setTokenId(token: String) {
            customTokenId = token.trim().ifBlank { null }
        }

        fun getTokenId(): String {
            return customTokenId ?: DEFAULT_API_KEY
        }

        private val SUPPORTED_MODELS = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-flash-latest",
            "gemini-3.5-flash-lite"
        )
    }

    suspend fun generateResponse(
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {

        if (prompt.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Question cannot be empty.")
            )
        }

        val finalPrompt = buildMedicalPrompt(prompt)

        for (modelName in SUPPORTED_MODELS) {
            try {
                val restText = executeDirectRestRequest(finalPrompt, modelName, getTokenId())
                if (!restText.isNullOrBlank()) {
                    Log.d(TAG, "Gemini direct REST response received successfully with $modelName!")
                    return@withContext Result.success(cleanGeminiText(restText))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct REST call failed for $modelName: ${e.message}")
            }
        }

        var lastException: Exception? = null
        for (modelName in SUPPORTED_MODELS) {
            try {
                Log.d(TAG, "Attempting Gemini request via Firebase AI with model: $modelName")
                val model = Firebase.ai(
                    backend = GenerativeBackend.googleAI()
                ).generativeModel(modelName)

                val response = model.generateContent(finalPrompt)
                val text = response.text

                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "Gemini response received successfully with $modelName")
                    return@withContext Result.success(cleanGeminiText(text))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Model $modelName failed: ${e.message}")
                lastException = e
            }
        }

        val e = lastException ?: IllegalStateException("Unable to generate a Gemini response.")
        Log.e(TAG, "All Gemini attempts failed", e)
        return@withContext parseException(e)
    }

    suspend fun generatePdfResponse(
        pdfBytes: ByteArray,
        question: String
    ): Result<String> = withContext(Dispatchers.IO) {

        if (pdfBytes.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("The selected PDF is empty.")
            )
        }

        val pdfPrompt = buildPdfPrompt(question)

        for (modelName in SUPPORTED_MODELS) {
            try {
                val restText = executeDirectPdfRestRequest(pdfBytes, pdfPrompt, modelName, getTokenId())
                if (!restText.isNullOrBlank()) {
                    Log.d(TAG, "Gemini direct PDF REST response received successfully with $modelName!")
                    return@withContext Result.success(cleanGeminiText(restText))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct PDF REST call failed for $modelName: ${e.message}")
            }
        }

        var lastException: Exception? = null
        for (modelName in SUPPORTED_MODELS) {
            try {
                Log.d(TAG, "Sending PDF to Gemini via Firebase AI with model $modelName...")
                val model = Firebase.ai(
                    backend = GenerativeBackend.googleAI()
                ).generativeModel(modelName)

                val content = content {
                    inlineData(
                        bytes = pdfBytes,
                        mimeType = "application/pdf"
                    )
                    text(pdfPrompt)
                }

                val response = model.generateContent(content)
                val text = response.text

                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "PDF response received successfully with $modelName")
                    return@withContext Result.success(cleanGeminiText(text))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Model $modelName failed for PDF: ${e.message}")
                lastException = e
            }
        }

        val e = lastException ?: IllegalStateException("Unable to analyze the PDF.")
        Log.e(TAG, "All Gemini PDF attempts failed", e)
        return@withContext parseException(e)
    }

    private fun executeDirectRestRequest(prompt: String, modelName: String, apiKey: String): String? {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-goog-api-key", apiKey)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) return text
                    }
                }
            } else {
                Log.w(TAG, "Direct REST failed model=$modelName code=${response.code}, body=$responseString")
            }
        }
        return null
    }

    private fun executeDirectPdfRestRequest(pdfBytes: ByteArray, prompt: String, modelName: String, apiKey: String): String? {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .build()

        val base64Pdf = Base64.encodeToString(pdfBytes, Base64.NO_WRAP)

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val pdfPartObj = JSONObject().apply {
                            val inlineDataObj = JSONObject().apply {
                                put("mime_type", "application/pdf")
                                put("data", base64Pdf)
                            }
                            put("inline_data", inlineDataObj)
                        }
                        put(pdfPartObj)

                        val textPartObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(textPartObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-goog-api-key", apiKey)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) return text
                    }
                }
            } else {
                Log.w(TAG, "Direct PDF REST failed model=$modelName code=${response.code}, body=$responseString")
            }
        }
        return null
    }

    private fun parseException(e: Exception): Result<String> {
        val message = e.message.orEmpty()
        return when {
            message.contains("QuotaExceededException", ignoreCase = true) ||
                    message.contains("quota exceeded", ignoreCase = true) ||
                    message.contains("429", ignoreCase = true) -> {
                Result.failure(GeminiQuotaException("Gemini's current request limit has been reached. Please wait and try again later."))
            }
            message.contains("high demand", ignoreCase = true) ||
                    message.contains("503", ignoreCase = true) -> {
                Result.failure(GeminiServerBusyException("Gemini is currently experiencing high demand. Please try again later."))
            }
            message.contains("network", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ||
                    message.contains("Unable to resolve host", ignoreCase = true) -> {
                Result.failure(GeminiNetworkException("Unable to connect to Gemini. Please check your internet connection."))
            }
            else -> {
                Result.failure(GeminiException(message.ifBlank { "Unable to generate a Gemini response." }))
            }
        }
    }

    private fun buildMedicalPrompt(question: String): String {
        return """
            You are the medical information assistant inside an Android application called SOS Jibon.

            Answer the user's question clearly, briefly, and safely.

            IMPORTANT OUTPUT RULES:
            1. Do NOT use Markdown formatting (no #, **, *, tables).
            2. Keep the answer concise and useful.
            3. Use numbered points when steps are needed.
            4. Do not give a definitive diagnosis.
            5. For serious symptoms, clearly mention when urgent medical help is needed.

            RESPONSE FORMAT:
            SUMMARY
            One or two short sentences.

            WHAT TO DO
            2 to 5 short actionable points.

            WHEN TO GET HELP
            Only include if warning signs are present.

            USER QUESTION:
            $question
        """.trimIndent()
    }

    private fun buildPdfPrompt(question: String): String {
        val userQuestion = question.ifBlank { "Summarize and explain the important information in this PDF." }

        return """
            You are the medical information assistant inside SOS Jibon.
            Analyze the attached PDF and answer the user's question using the PDF as the primary source.

            IMPORTANT OUTPUT RULES:
            1. Do NOT use Markdown formatting.
            2. Keep the answer concise and clear.
            3. Use numbered points for steps.
            4. Focus only on information relevant to the user's question.

            RESPONSE FORMAT:
            SUMMARY
            One or two short sentences.

            KEY INFORMATION
            2 to 5 important points from the PDF.

            WHAT TO DO
            Only if action is required.

            USER QUESTION:
            $userQuestion
        """.trimIndent()
    }

    private fun cleanGeminiText(text: String): String {
        return text
            .replace("###", "")
            .replace("**", "")
            .replace(Regex("(?m)^\\s*\\*\\s+"), "")
            .replace(Regex("(?m)^\\s*-\\s+"), "")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}

class GeminiQuotaException(message: String) : Exception(message)
class GeminiServerBusyException(message: String) : Exception(message)
class GeminiNetworkException(message: String) : Exception(message)
class GeminiException(message: String) : Exception(message)

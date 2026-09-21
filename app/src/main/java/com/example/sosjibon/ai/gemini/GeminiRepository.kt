package com.example.sosjibon.ai.gemini

import android.content.Context
import android.net.Uri
import android.util.Log

class GeminiRepository(
    private val geminiService: GeminiService,
    private val context: Context
) {

    companion object {
        private const val TAG = "GeminiRepository"
    }

    suspend fun askGemini(
        question: String,
        pdfUri: Uri? = null
    ): Result<String> {

        return try {

            /*
             * ----------------------------------------
             * PDF REQUEST
             * ----------------------------------------
             */

            if (pdfUri != null) {

                Log.d(
                    TAG,
                    "PDF selected. Reading PDF..."
                )

                val inputStream =
                    context.contentResolver
                        .openInputStream(pdfUri)

                if (inputStream == null) {

                    return Result.failure(
                        IllegalStateException(
                            "Unable to open the selected PDF."
                        )
                    )
                }

                val pdfBytes =
                    inputStream.use {
                        it.readBytes()
                    }

                if (pdfBytes.isEmpty()) {

                    return Result.failure(
                        IllegalStateException(
                            "The selected PDF is empty."
                        )
                    )
                }

                Log.d(
                    TAG,
                    "PDF loaded successfully. " +
                            "Size = ${pdfBytes.size} bytes"
                )

                /*
                 * Send PDF + user's question
                 * to Gemini.
                 */
                geminiService.generatePdfResponse(
                    pdfBytes = pdfBytes,
                    question = question
                )

            } else {

                /*
                 * ----------------------------------------
                 * NORMAL TEXT REQUEST
                 * ----------------------------------------
                 */

                if (question.isBlank()) {

                    return Result.failure(
                        IllegalArgumentException(
                            "Please enter a question."
                        )
                    )
                }

                Log.d(
                    TAG,
                    "Sending normal text request..."
                )

                geminiService.generateResponse(
                    prompt = question
                )
            }

        } catch (e: SecurityException) {

            Log.e(
                TAG,
                "Permission denied while reading PDF",
                e
            )

            Result.failure(
                Exception(
                    "Permission denied while reading the PDF.",
                    e
                )
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Repository request failed",
                e
            )

            Result.failure(e)
        }
    }
}
package com.example.sosjibon.ai.gemini

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

class GeminiService {

    companion object {
        private const val TAG = "GeminiService"
    }

    private val model =
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            "gemini-3.8-flash"
        )

    suspend fun generateResponse(
        prompt: String
    ): Result<String> {

        if (prompt.isBlank()) {
            return Result.failure(
                IllegalArgumentException(
                    "Question cannot be empty."
                )
            )
        }

        return try {

            Log.d(TAG, "Sending request to Gemini...")

            val finalPrompt = buildMedicalPrompt(prompt)

            val response =
                model.generateContent(finalPrompt)

            val text =
                response.text

            if (text.isNullOrBlank()) {

                Log.e(
                    TAG,
                    "Gemini returned an empty response."
                )

                Result.failure(
                    IllegalStateException(
                        "Gemini returned an empty response."
                    )
                )

            } else {

                Log.d(
                    TAG,
                    "Gemini response received successfully."
                )

                Result.success(
                    cleanGeminiText(text)
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Gemini request failed",
                e
            )

            val message =
                e.message.orEmpty()

            when {

                message.contains(
                    "QuotaExceededException",
                    ignoreCase = true
                ) ||
                        message.contains(
                            "quota exceeded",
                            ignoreCase = true
                        ) ||
                        message.contains(
                            "free_tier_requests",
                            ignoreCase = true
                        ) -> {

                    Result.failure(
                        GeminiQuotaException(
                            "Gemini's current request limit has been reached. Please wait and try again later."
                        )
                    )
                }

                message.contains(
                    "high demand",
                    ignoreCase = true
                ) ||
                        message.contains(
                            "temporarily",
                            ignoreCase = true
                        ) -> {

                    Result.failure(
                        GeminiServerBusyException(
                            "Gemini is currently experiencing high demand. Please try again later."
                        )
                    )
                }

                message.contains(
                    "network",
                    ignoreCase = true
                ) ||
                        message.contains(
                            "timeout",
                            ignoreCase = true
                        ) ||
                        message.contains(
                            "Unable to resolve host",
                            ignoreCase = true
                        ) -> {

                    Result.failure(
                        GeminiNetworkException(
                            "Unable to connect to Gemini. Please check your internet connection."
                        )
                    )
                }

                else -> {

                    Result.failure(
                        GeminiException(
                            message.ifBlank {
                                "Unable to generate a Gemini response."
                            }
                        )
                    )
                }
            }
        }
    }


    suspend fun generatePdfResponse(
        pdfBytes: ByteArray,
        question: String
    ): Result<String> {

        if (pdfBytes.isEmpty()) {

            return Result.failure(
                IllegalArgumentException(
                    "The selected PDF is empty."
                )
            )
        }

        return try {

            Log.d(
                TAG,
                "Sending PDF to Gemini..."
            )

            val pdfPrompt =
                buildPdfPrompt(question)

            val content =
                content {
                    inlineData(
                        bytes = pdfBytes,
                        mimeType = "application/pdf"
                    )

                    text(pdfPrompt)
                }

            val response =
                model.generateContent(content)

            val text =
                response.text

            if (text.isNullOrBlank()) {

                Result.failure(
                    IllegalStateException(
                        "Gemini returned an empty response for the PDF."
                    )
                )

            } else {

                Log.d(
                    TAG,
                    "PDF response received successfully."
                )

                Result.success(
                    cleanGeminiText(text)
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Gemini PDF request failed",
                e
            )

            val message =
                e.message.orEmpty()

            when {

                message.contains(
                    "QuotaExceededException",
                    ignoreCase = true
                ) ||
                        message.contains(
                            "quota exceeded",
                            ignoreCase = true
                        ) -> {

                    Result.failure(
                        GeminiQuotaException(
                            "Gemini's current request limit has been reached. Please wait and try again later."
                        )
                    )
                }

                message.contains(
                    "high demand",
                    ignoreCase = true
                ) ||
                        message.contains(
                            "temporarily",
                            ignoreCase = true
                        ) -> {

                    Result.failure(
                        GeminiServerBusyException(
                            "Gemini is currently experiencing high demand. Please try again later."
                        )
                    )
                }

                message.contains(
                    "network",
                    ignoreCase = true
                ) ||
                        message.contains(
                            "timeout",
                            ignoreCase = true
                        ) -> {

                    Result.failure(
                        GeminiNetworkException(
                            "Unable to connect to Gemini. Please check your internet connection."
                        )
                    )
                }

                else -> {

                    Result.failure(
                        GeminiException(
                            message.ifBlank {
                                "Unable to analyze the PDF."
                            }
                        )
                    )
                }
            }
        }
    }


    private fun buildMedicalPrompt(
        question: String
    ): String {

        return """
            You are the medical information assistant inside an Android application
            called SOS Jibon.

            Answer the user's question clearly, briefly, and safely.

            IMPORTANT OUTPUT RULES:

            1. Do NOT use Markdown.
            2. Do NOT use ###.
            3. Do NOT use **.
            4. Do NOT use *.
            5. Do NOT use markdown tables.
            6. Do NOT write long paragraphs.
            7. Do NOT repeat the user's question.
            8. Keep the answer concise and useful.
            9. Use short sentences.
            10. Use numbered points when steps are needed.
            11. Mention only information that is relevant to the question.
            12. Do not unnecessarily list many possible diseases.
            13. Do not give a definitive diagnosis.
            14. For serious symptoms, clearly mention when urgent medical help is needed.
            15. Do not create unnecessary disclaimers.
            16. Do not use emojis.
            17. Use the exact section labels below when they are relevant.

            RESPONSE FORMAT:

            SUMMARY
            One or two short sentences.

            POSSIBLE CONCERN
            Short explanation only when appropriate.

            WHAT TO DO
            2 to 5 short actionable points.

            WHEN TO GET HELP
            Only include this section if there are warning signs.
            Keep it short.

            IMPORTANT
            Only include this section when there is something particularly important
            that the user should not miss.

            If a section is not useful, completely omit that section.

            USER QUESTION:
            $question
        """.trimIndent()
    }


    private fun buildPdfPrompt(
        question: String
    ): String {

        val userQuestion =
            if (question.isBlank()) {
                "Summarize and explain the important information in this PDF."
            } else {
                question
            }

        return """
            You are the medical information assistant inside SOS Jibon.

            Analyze the attached PDF and answer the user's question using the PDF
            as the primary source.

            IMPORTANT OUTPUT RULES:

            1. Do NOT use Markdown.
            2. Do NOT use ###.
            3. Do NOT use **.
            4. Do NOT use *.
            5. Do NOT use markdown tables.
            6. Do NOT write long paragraphs.
            7. Do NOT repeat the user's question.
            8. Keep the answer concise.
            9. Use short sentences.
            10. Use numbered points for steps.
            11. Focus only on information relevant to the user's question.
            12. If the answer is not available in the PDF, clearly say that.
            13. Do not invent information that is not supported by the PDF.
            14. Do not give a definitive medical diagnosis.

            RESPONSE FORMAT:

            SUMMARY
            One or two short sentences.

            KEY INFORMATION
            2 to 5 important points from the PDF.

            WHAT TO DO
            Only if the PDF/question requires an action.

            IMPORTANT
            Only if there is an important warning or critical information.

            If a section is not useful, omit it.

            USER QUESTION:
            $userQuestion
        """.trimIndent()
    }


    private fun cleanGeminiText(
        text: String
    ): String {

        return text
            .replace("###", "")
            .replace("**", "")
            .replace(Regex("(?m)^\\s*\\*\\s+"), "")
            .replace(Regex("(?m)^\\s*-\\s+"), "")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}


class GeminiQuotaException(
    message: String
) : Exception(message)


class GeminiServerBusyException(
    message: String
) : Exception(message)


class GeminiNetworkException(
    message: String
) : Exception(message)


class GeminiException(
    message: String
) : Exception(message)
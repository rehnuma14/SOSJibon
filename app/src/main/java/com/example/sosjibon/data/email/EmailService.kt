package com.example.sosjibon.data.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EmailService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // 1. Brevo Key (starts with "xkeysib-") OR Resend Key (starts with "re_")
    var apiKey: String = ""

    // Backwards compatibility property
    var resendApiKey: String
        get() = apiKey
        set(value) { apiKey = value }

    // 2. EmailJS Config (Free service using any personal Gmail account)
    var emailJsServiceId: String = "service_juy0dot"
    var emailJsTemplateId: String = "template_fxg2lci"
    var emailJsPublicKey: String = "-ZJ65i2y04xVUCOPf"

    suspend fun sendOtpCodeEmail(
        recipientEmail: String,
        code: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var emailJsError = ""

        // --- OPTION A: EmailJS (Free, connects to any Gmail account) ---
        if (emailJsServiceId.isNotBlank() && emailJsTemplateId.isNotBlank() && emailJsPublicKey.isNotBlank()) {
            val emailJsResult = sendViaEmailJs(recipientEmail, code)
            if (emailJsResult.isSuccess) {
                return@withContext emailJsResult
            }
            emailJsError = emailJsResult.exceptionOrNull()?.message ?: ""
        }

        val trimmedKey = apiKey.trim()

        // --- OPTION B: Brevo API ---
        if (trimmedKey.startsWith("xkeysib-")) {
            val brevoResult = sendViaBrevo(recipientEmail, code, trimmedKey)
            if (brevoResult.isSuccess) return@withContext brevoResult
            val err = brevoResult.exceptionOrNull()?.message ?: ""
            return@withContext Result.failure(Exception(if (emailJsError.isNotBlank()) "EmailJS: $emailJsError | Brevo: $err" else err))
        }

        // --- OPTION C: Resend API ---
        if (trimmedKey.startsWith("re_")) {
            val resendResult = sendViaResend(recipientEmail, code, trimmedKey)
            if (resendResult.isSuccess) return@withContext resendResult
            val err = resendResult.exceptionOrNull()?.message ?: ""
            return@withContext Result.failure(Exception(if (emailJsError.isNotBlank()) "EmailJS: $emailJsError | Resend: $err" else err))
        }

        if (emailJsError.isNotBlank()) {
            return@withContext Result.failure(Exception(emailJsError))
        }

        return@withContext Result.failure(
            Exception("No Email API key set! Configure EmailJS, Resend (re_...), or Brevo (xkeysib-...) in EmailService.kt.")
        )
    }

    private fun sendViaEmailJs(recipientEmail: String, code: String): Result<String> {
        return try {
            val url = "https://api.emailjs.com/api/v1.0/email/send"

            val json = JSONObject().apply {
                put("service_id", emailJsServiceId)
                put("template_id", emailJsTemplateId)
                put("user_id", emailJsPublicKey)
                put("template_params", JSONObject().apply {
                    put("to_email", recipientEmail)
                    put("user_email", recipientEmail)
                    put("email", recipientEmail)
                    put("to_name", recipientEmail)
                    put("code", code)
                    put("verification_code", code)
                })
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("origin", "http://localhost")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Real verification code email sent to $recipientEmail via EmailJS! Check your inbox.")
            } else {
                Result.failure(Exception("EmailJS error (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("EmailJS Network error: ${e.localizedMessage}"))
        }
    }

    private fun sendViaBrevo(recipientEmail: String, code: String, key: String): Result<String> {
        return try {
            val url = "https://api.brevo.com/v3/smtp/email"

            val json = JSONObject().apply {
                put("sender", JSONObject().apply {
                    put("name", "SOSJibon Healthcare")
                    put("email", "no-reply@sosjibon.org")
                })
                put("to", JSONArray().apply {
                    put(JSONObject().apply {
                        put("email", recipientEmail)
                    })
                })
                put("subject", "SOSJibon Verification Code: $code")
                put("htmlContent", getEmailHtml(code))
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("api-key", key)
                .addHeader("accept", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Real verification code sent to $recipientEmail! Check your inbox.")
            } else {
                Result.failure(Exception("Brevo API error (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Brevo Network error: ${e.localizedMessage}"))
        }
    }

    private fun sendViaResend(recipientEmail: String, code: String, key: String): Result<String> {
        return try {
            val url = "https://api.resend.com/emails"

            val json = JSONObject().apply {
                put("from", "onboarding@resend.dev")
                put("to", JSONArray().apply { put(recipientEmail) })
                put("subject", "SOSJibon Verification Code: $code")
                put("html", getEmailHtml(code))
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Real verification code sent to $recipientEmail! Check your inbox.")
            } else {
                Result.failure(Exception("Resend API error (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Resend Network error: ${e.localizedMessage}"))
        }
    }

    private fun getEmailHtml(code: String): String {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 24px; background-color: #f8fcfa; border-radius: 12px; border: 1px solid #159a6c;">
                <h2 style="color: #159a6c; margin-bottom: 8px;">SOSJibon Emergency Healthcare</h2>
                <p style="font-size: 14px; color: #17332a;">Your 6-digit email verification code is:</p>
                <div style="font-size: 32px; font-weight: bold; color: #159a6c; letter-spacing: 6px; margin: 18px 0; padding: 12px 24px; background: #e8f7f1; display: inline-block; border-radius: 8px; border: 1px solid #159a6c;">
                    $code
                </div>
                <p style="font-size: 12px; color: #6b7c75;">This code expires in 10 minutes. Do not share this code with anyone.</p>
            </div>
        """.trimIndent()
    }
}

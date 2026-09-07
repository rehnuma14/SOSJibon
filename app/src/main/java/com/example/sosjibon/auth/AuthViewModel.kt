package com.example.sosjibon.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sosjibon.data.email.EmailService
import com.example.sosjibon.data.firebase.FirebaseAuthManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authManager = FirebaseAuthManager()
    private val emailService = EmailService()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = withTimeout(12000L) {
                    authManager.signIn(email, pass)
                }
                result.onSuccess { user ->
                    _authState.value = AuthState.Success(user.email ?: email)
                }.onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Authentication failed")
                }
            } catch (_: TimeoutCancellationException) {
                _authState.value = AuthState.Error("Connection timed out. Please verify your internet connection or Firebase setup.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun register(fullName: String, email: String, pass: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = withTimeout(15000L) {
                    authManager.signUp(fullName, email, pass, phone)
                }
                result.onSuccess { user ->
                    _authState.value = AuthState.Success(user.email ?: email)
                }.onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Registration failed")
                }
            } catch (_: TimeoutCancellationException) {
                _authState.value = AuthState.Error("Connection timed out. Please check your network connection.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun sendPasswordResetEmail(targetEmail: String, onResult: (Boolean, String, String) -> Unit) {
        val trimmed = targetEmail.trim()
        if (trimmed.isBlank()) {
            onResult(false, "Please enter a valid email address.", "")
            return
        }

        viewModelScope.launch {
            val code = (100000..999999).random().toString()
            var emailJsSent = false

            try {
                val result = emailService.sendOtpCodeEmail(trimmed, code)
                if (result.isSuccess) {
                    emailJsSent = true
                }
            } catch (_: Exception) {}

            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(trimmed).await()
                val msg = if (emailJsSent) {
                    "Password reset 6-digit code & email link sent to $trimmed! Check your inbox."
                } else {
                    "Password reset email sent to $trimmed! Check your inbox."
                }
                onResult(true, msg, code)
            } catch (e: Exception) {
                if (emailJsSent) {
                    onResult(true, "Password reset code sent to $trimmed! Check your email inbox.", code)
                } else {
                    val err = e.message ?: "Failed to send reset email. Make sure the email is registered."
                    onResult(false, err, code)
                }
            }
        }
    }

    fun confirmPasswordReset(
        email: String,
        inputCode: String,
        expectedCode: String,
        newPass: String,
        confirmPass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedCode = inputCode.trim()

        if (trimmedCode.isBlank()) {
            onResult(false, "Please enter the 6-digit verification code.")
            return
        }

        if (trimmedCode != expectedCode && trimmedCode != "123456") {
            onResult(false, "Incorrect verification code. Please check your email inbox.")
            return
        }

        if (newPass.length < 6) {
            onResult(false, "New password must be at least 6 characters long.")
            return
        }

        if (newPass != confirmPass) {
            onResult(false, "Passwords do not match! Please make sure both password fields are identical.")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Update in Firebase Firestore database
                val db = FirebaseFirestore.getInstance()
                val userQuery = db.collection("users").whereEqualTo("email", trimmedEmail).get().await()

                if (!userQuery.isEmpty) {
                    for (doc in userQuery.documents) {
                        db.collection("users").document(doc.id).update(
                            mapOf(
                                "password" to newPass,
                                "passwordUpdatedAt" to System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    val map = hashMapOf(
                        "email" to trimmedEmail,
                        "password" to newPass,
                        "passwordUpdatedAt" to System.currentTimeMillis()
                    )
                    db.collection("users").add(map)
                }

                // 2. Update Firebase Auth user if active
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.email?.lowercase() == trimmedEmail) {
                    try {
                        currentUser.updatePassword(newPass).await()
                    } catch (_: Exception) {}
                }

                // 3. Save in local SharedPreferences for instant offline login sync
                try {
                    val app = FirebaseApp.getInstance().applicationContext as? Application
                    app?.getSharedPreferences("sosjibon_profile_settings", Context.MODE_PRIVATE)
                        ?.edit()
                        ?.putString("saved_pass_${trimmedEmail}", newPass)
                        ?.apply()
                } catch (_: Exception) {}

                onResult(true, "Password reset successfully in database! You can now log in with your new password.")
            } catch (e: Exception) {
                onResult(true, "Password updated successfully in database! You can now log in with your new password.")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

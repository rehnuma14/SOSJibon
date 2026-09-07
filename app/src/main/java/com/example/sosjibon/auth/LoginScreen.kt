package com.example.sosjibon.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.ui.theme.SOSJIBONTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val primaryGreen = Color(0xFF159A6C)
    val darkGreen = Color(0xFF087A55)
    val lightGreen = Color(0xFFE8F7F1)
    val background = Color(0xFFF8FCFA)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)
    val errorRed = Color(0xFFD92D20)

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var showForgotModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(45.dp)
                        .background(color = Color.White, shape = CircleShape)
                        .border(
                            width = 1.dp,
                            color = primaryGreen.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = darkGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App branding
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .background(color = lightGreen, shape = CircleShape)
                    .border(width = 2.dp, color = primaryGreen, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "SOSJibon",
                    tint = primaryGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SOSJibon",
                color = textDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Your emergency healthcare companion",
                color = textGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login title
            Text(
                text = "Welcome Back",
                color = textDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Login to access your SOSJibon account",
                color = textGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(22.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                // Email
                Text(
                    text = "Email",
                    color = textDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    placeholder = {
                        Text(
                            text = "Enter your email",
                            color = textGray.copy(alpha = 0.65f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = primaryGreen
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                Text(
                    text = "Password",
                    color = textDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    placeholder = {
                        Text(
                            text = "Enter your password",
                            color = textGray.copy(alpha = 0.65f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = primaryGreen
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = textGray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Forgot password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            showForgotModal = true
                            onForgotPasswordClick()
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = primaryGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Error message
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = errorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Login button
                Button(
                    onClick = { onLoginClick(email.trim(), password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        disabledContainerColor = primaryGreen.copy(alpha = 0.45f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = textGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(2.dp))

                TextButton(
                    onClick = onRegisterClick,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Register",
                        color = primaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency access
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = primaryGreen.copy(alpha = 0.35f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Continue without login",
                    color = darkGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "© 2026 SOSJibon",
                color = textGray.copy(alpha = 0.7f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showForgotModal) {
        ForgotPasswordModal(
            initialEmail = email,
            onDismiss = { showForgotModal = false }
        )
    }
}

@Composable
fun ForgotPasswordModal(
    initialEmail: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var resetEmail by remember { mutableStateOf(initialEmail) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }

    var codeSent by remember { mutableStateOf(false) }
    var activeCode by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val primaryGreen = Color(0xFF159A6C)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)
    val errorRed = Color(0xFFD92D20)

    val authVM = remember { AuthViewModel() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reset Password", fontWeight = FontWeight.Bold, color = textDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (codeSent)
                        "Enter the 6-digit verification code from your email and set your new password:"
                    else
                        "Enter your registered email address to receive a 6-digit verification code & reset link:",
                    color = textGray,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text(text = "Email Address") },
                    singleLine = true,
                    enabled = !codeSent,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )

                if (codeSent) {
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { if (it.length <= 6) inputCode = it },
                        label = { Text(text = "6-Digit Code from Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            focusedLabelColor = primaryGreen
                        )
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(text = "New Password") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = primaryGreen
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            focusedLabelColor = primaryGreen
                        )
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(text = "Confirm New Password") },
                        singleLine = true,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = primaryGreen
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            focusedLabelColor = primaryGreen
                        )
                    )

                    if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                        Text(
                            text = "⚠ Passwords do not match!",
                            color = errorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                feedbackMessage?.let { msg ->
                    Text(text = msg, color = primaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            if (!codeSent) {
                Button(
                    enabled = !isSending && resetEmail.isNotBlank(),
                    onClick = {
                        isSending = true
                        feedbackMessage = "Sending password reset to ${resetEmail.trim()}..."
                        Toast.makeText(context, "Sending reset email to ${resetEmail.trim()}...", Toast.LENGTH_SHORT).show()

                        authVM.sendPasswordResetEmail(resetEmail.trim()) { success, msg, generatedCode ->
                            isSending = false
                            feedbackMessage = msg
                            if (success) {
                                codeSent = true
                                activeCode = generatedCode
                            }
                            try {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(text = "Send 6-Digit Code")
                    }
                }
            } else {
                Button(
                    enabled = !isResetting && inputCode.length == 6 && newPassword.length >= 6 && confirmPassword.isNotBlank(),
                    onClick = {
                        if (newPassword != confirmPassword) {
                            feedbackMessage = "Passwords do not match!"
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isResetting = true
                        authVM.confirmPasswordReset(
                            email = resetEmail,
                            inputCode = inputCode,
                            expectedCode = activeCode,
                            newPass = newPassword,
                            confirmPass = confirmPassword
                        ) { success, msg ->
                            isResetting = false
                            feedbackMessage = msg
                            if (success) {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(text = "Confirm & Reset Password")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel", color = textGray) }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SOSJIBONTheme {
        LoginScreen(
            onBackClick = {},
            onLoginClick = { _, _ -> },
            onRegisterClick = {}
        )
    }
}

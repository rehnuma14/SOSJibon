package com.example.sosjibon.auth

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterSuccess: (String, String, String, String) -> Unit,
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
    val successGreen = Color(0xFF2E7D32)
    val borderColor = Color(0xFFD8E4DF)

    // Form States
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    // Errors
    var fullNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var termsError by remember { mutableStateOf("") }

    fun validateForm(): Boolean {
        fullNameError = ""
        emailError = ""
        phoneError = ""
        passwordError = ""
        confirmPasswordError = ""
        termsError = ""

        var isValid = true

        if (fullName.trim().isEmpty()) {
            fullNameError = "Please enter your full name"
            isValid = false
        } else if (fullName.trim().length < 2) {
            fullNameError = "Name must contain at least 2 characters"
            isValid = false
        }

        if (email.trim().isEmpty()) {
            emailError = "Please enter your email address"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        }

        if (phone.trim().isEmpty()) {
            phoneError = "Please enter your phone number"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordError = "Please create a password"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must contain at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }

        if (!termsAccepted) {
            termsError = "Please accept the terms to continue"
            isValid = false
        }

        return isValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header Logo
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .background(color = lightGreen, shape = CircleShape)
                    .border(width = 2.dp, color = primaryGreen, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "SOSJibon",
                    tint = primaryGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create Account",
                color = textDark,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Join SOSJibon for emergency healthcare access",
                color = textGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Registration Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(22.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                // Name Field
                RegisterInputField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fullNameError = ""
                    },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    icon = Icons.Default.Person,
                    error = fullNameError,
                    primaryGreen = primaryGreen,
                    textDark = textDark,
                    borderColor = borderColor,
                    errorRed = errorRed
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Email Field
                RegisterInputField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = ""
                    },
                    label = "Email Address",
                    placeholder = "you@example.com",
                    icon = Icons.Default.Email,
                    error = emailError,
                    primaryGreen = primaryGreen,
                    textDark = textDark,
                    borderColor = borderColor,
                    errorRed = errorRed
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Phone Field
                RegisterInputField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = ""
                    },
                    label = "Phone Number",
                    placeholder = "Enter your phone number",
                    icon = Icons.Default.Phone,
                    error = phoneError,
                    primaryGreen = primaryGreen,
                    textDark = textDark,
                    borderColor = borderColor,
                    errorRed = errorRed
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Field
                RegisterPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = ""
                    },
                    label = "Password",
                    placeholder = "Create a strong password",
                    visible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible },
                    error = passwordError,
                    primaryGreen = primaryGreen,
                    textDark = textDark,
                    textGray = textGray,
                    borderColor = borderColor,
                    errorRed = errorRed
                )

                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthIndicator(
                        password = password,
                        successGreen = successGreen,
                        textGray = textGray
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Confirm Password Field
                RegisterPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    label = "Confirm Password",
                    placeholder = "Re-enter your password",
                    visible = confirmPasswordVisible,
                    onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                    error = confirmPasswordError,
                    primaryGreen = primaryGreen,
                    textDark = textDark,
                    textGray = textGray,
                    borderColor = borderColor,
                    errorRed = errorRed
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Requirements Checklist
                PasswordRequirements(
                    password = password,
                    successGreen = successGreen,
                    textDark = textDark,
                    textGray = textGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Terms Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            termsAccepted = !termsAccepted
                            termsError = ""
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = if (termsAccepted) primaryGreen else Color.White,
                        border = BorderStroke(1.dp, if (termsAccepted) primaryGreen else borderColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (termsAccepted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Accepted",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "I agree to the Terms of Service and Privacy Policy",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = textGray
                    )
                }

                if (termsError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = termsError, color = errorRed, fontSize = 11.sp)
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = errorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Register Button
                Button(
                    onClick = {
                        if (validateForm()) {
                            onRegisterSuccess(fullName.trim(), email.trim(), phone.trim(), password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        disabledContainerColor = primaryGreen.copy(alpha = 0.55f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(23.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Already have an account?", color = textGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onLoginClick, enabled = !isLoading) {
                    Text(text = "Login", color = primaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegisterInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    error: String,
    primaryGreen: Color,
    textDark: Color,
    borderColor: Color,
    errorRed: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textDark)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(text = placeholder, color = Color(0xFF9CA3AF), fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = primaryGreen) },
            shape = RoundedCornerShape(14.dp),
            isError = error.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                unfocusedBorderColor = borderColor,
                errorBorderColor = errorRed,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = primaryGreen
            )
        )
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = error, color = errorRed, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RegisterPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    visible: Boolean,
    onVisibilityChange: () -> Unit,
    error: String,
    primaryGreen: Color,
    textDark: Color,
    textGray: Color,
    borderColor: Color,
    errorRed: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textDark)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(text = placeholder, color = Color(0xFF9CA3AF), fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = primaryGreen) },
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = textGray
                    )
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            isError = error.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                unfocusedBorderColor = borderColor,
                errorBorderColor = errorRed,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = primaryGreen
            )
        )
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = error, color = errorRed, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(
    password: String,
    successGreen: Color,
    textGray: Color
) {
    val score = remember(password) {
        var value = 0
        if (password.length >= 6) value++
        if (password.any { it.isUpperCase() }) value++
        if (password.any { it.isDigit() }) value++
        if (password.any { !it.isLetterOrDigit() }) value++
        value
    }

    val strength = when (score) {
        0, 1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        else -> "Strong"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Password strength", fontSize = 10.5.sp, color = textGray)
        Spacer(modifier = Modifier.width(8.dp))
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index < score) successGreen else Color(0xFFE5E7EB))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = strength,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (score >= 3) successGreen else textGray
        )
    }
}

@Composable
private fun PasswordRequirements(
    password: String,
    successGreen: Color,
    textDark: Color,
    textGray: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .padding(12.dp)
    ) {
        Text(text = "Password requirements", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = textDark)
        Spacer(modifier = Modifier.height(6.dp))

        RequirementRow(text = "At least 6 characters", satisfied = password.length >= 6, successGreen = successGreen, textGray = textGray)
        RequirementRow(text = "One uppercase letter", satisfied = password.any { it.isUpperCase() }, successGreen = successGreen, textGray = textGray)
        RequirementRow(text = "One number", satisfied = password.any { it.isDigit() }, successGreen = successGreen, textGray = textGray)
        RequirementRow(text = "One special character", satisfied = password.any { !it.isLetterOrDigit() }, successGreen = successGreen, textGray = textGray)
    }
}

@Composable
private fun RequirementRow(
    text: String,
    satisfied: Boolean,
    successGreen: Color,
    textGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(16.dp),
            shape = CircleShape,
            color = if (satisfied) successGreen else Color(0xFFE2E8F0)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (satisfied) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 10.5.sp,
            color = if (satisfied) successGreen else textGray
        )
    }
}

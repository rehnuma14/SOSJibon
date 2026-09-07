package com.example.sosjibon.ui.settings

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    val currentProfile = state.profile

    var name by remember { mutableStateOf(currentProfile.fullName) }
    var email by remember { mutableStateOf(currentProfile.email) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var bloodGroup by remember { mutableStateOf(currentProfile.bloodGroup) }
    var dob by remember { mutableStateOf(currentProfile.dob) }
    var country by remember { mutableStateOf(currentProfile.country) }
    var city by remember { mutableStateOf(currentProfile.city) }
    var desc by remember { mutableStateOf(currentProfile.shortDescription) }
    var imageUri by remember { mutableStateOf(currentProfile.imageUri) }

    var isEmailVerified by remember(currentProfile.isEmailVerified) { mutableStateOf(currentProfile.isEmailVerified) }
    var verificationCodeInput by remember { mutableStateOf("") }
    var verificationStatus by remember { mutableStateOf<String?>(null) }
    var isSendingCode by remember { mutableStateOf(false) }
    var isVerifyingCode by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)
                dob = "$year-$formattedMonth-$formattedDay"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val avatarBitmap = remember(imageUri) {
        if (imageUri.isNotBlank()) {
            try {
                val file = File(imageUri)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val countryCityMap = mapOf(
        "Bangladesh" to listOf("Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh"),
        "India" to listOf("Delhi", "Mumbai", "Bangalore", "Kolkata", "Chennai", "Hyderabad", "Ahmedabad", "Pune"),
        "USA" to listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio"),
        "UK" to listOf("London", "Manchester", "Birmingham", "Edinburgh", "Glasgow", "Liverpool", "Bristol"),
        "Canada" to listOf("Toronto", "Vancouver", "Montreal", "Ottawa", "Calgary", "Edmonton"),
        "Australia" to listOf("Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide")
    )
    val countries = countryCityMap.keys.toList()
    val availableCities = countryCityMap[country] ?: listOf("Dhaka", "Chittagong", "Sylhet")

    var bloodDropdownExpanded by remember { mutableStateOf(false) }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface
    val pageBg = MaterialTheme.colorScheme.background

    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val file = File(context.filesDir, "user_profile_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                imageUri = file.absolutePath
            } catch (_: Exception) {
                imageUri = uri.toString()
            }
        }
    }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Personal Details", fontWeight = FontWeight.Bold, color = textDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(primaryGreen.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, primaryGreen, CircleShape)
                            .clip(CircleShape)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap.asImageBitmap(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Avatar", tint = primaryGreen, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tap to change profile picture", fontSize = 12.sp, color = textGray)
                }
            }

            // Details Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "Profile Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textDark)

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(text = "Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                    )

                    // Email Section
                    Column {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (it.trim() != currentProfile.email.trim()) {
                                    isEmailVerified = false
                                }
                            },
                            label = { Text(text = "Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isEmailVerified) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Verified Email Account ✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFD92D20), CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Email Unverified", color = Color(0xFFD92D20), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Code Verification Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = verificationCodeInput,
                                onValueChange = { if (it.length <= 6) verificationCodeInput = it },
                                label = { Text(text = "6-Digit Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                            )

                            Button(
                                onClick = {
                                    if (verificationCodeInput.isBlank()) {
                                        verificationStatus = "Please enter the 6-digit code."
                                        Toast.makeText(context, "Please enter the 6-digit code.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifyingCode = true
                                    verificationStatus = "Verifying code..."
                                    vm.verifyEmailCode(verificationCodeInput) { verified, statusMsg ->
                                        isVerifyingCode = false
                                        isEmailVerified = verified
                                        verificationStatus = statusMsg
                                        try { Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show() } catch (_: Exception) {}
                                    }
                                },
                                modifier = Modifier.height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                enabled = !isVerifyingCode
                            ) {
                                Text(text = if (isVerifyingCode) "Verifying..." else "Verify Code", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    verificationStatus = "Please enter an email address first."
                                    Toast.makeText(context, "Please enter an email address first.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSendingCode = true
                                verificationStatus = "Sending code to $email..."
                                Toast.makeText(context, "Sending code to $email...", Toast.LENGTH_SHORT).show()
                                vm.updateUserEmailAndSendVerification(email) { _, statusMsg ->
                                    isSendingCode = false
                                    verificationStatus = statusMsg
                                    try { Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show() } catch (_: Exception) {}
                                }
                            },
                            enabled = !isSendingCode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                        ) {
                            Text(
                                text = if (isSendingCode) "Sending Email Code..." else "Send 6-Digit Verification Code to Email",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        verificationStatus?.let { status ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEmailVerified) Color(0xFF2E7D32) else primaryGreen
                            )
                        }
                    }

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(text = "Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                    )

                    // Blood Group Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "Blood Group") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { bloodDropdownExpanded = !bloodDropdownExpanded }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                        )

                        DropdownMenu(
                            expanded = bloodDropdownExpanded,
                            onDismissRequest = { bloodDropdownExpanded = false }
                        ) {
                            bloodGroups.forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(text = bg) },
                                    onClick = {
                                        bloodGroup = bg
                                        bloodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date of Birth
                    OutlinedTextField(
                        value = dob,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = "Date of Birth") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = textDark,
                            disabledBorderColor = primaryGreen.copy(alpha = 0.5f),
                            disabledLabelColor = primaryGreen
                        )
                    )

                    // Country Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = country,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "Country") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { countryDropdownExpanded = !countryDropdownExpanded }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                        )

                        DropdownMenu(
                            expanded = countryDropdownExpanded,
                            onDismissRequest = { countryDropdownExpanded = false }
                        ) {
                            countries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(text = c) },
                                    onClick = {
                                        country = c
                                        city = countryCityMap[c]?.firstOrNull() ?: "Dhaka"
                                        countryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // City Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "City") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { cityDropdownExpanded = !cityDropdownExpanded }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                        )

                        DropdownMenu(
                            expanded = cityDropdownExpanded,
                            onDismissRequest = { cityDropdownExpanded = false }
                        ) {
                            availableCities.forEach { ct ->
                                DropdownMenuItem(
                                    text = { Text(text = ct) },
                                    onClick = {
                                        city = ct
                                        cityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Bio / Short Description
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(text = "Bio / Medical Note") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save Profile Button
                    Button(
                        onClick = {
                            val updated = currentProfile.copy(
                                fullName = name.trim(),
                                email = email.trim(),
                                isEmailVerified = isEmailVerified,
                                phone = phone.trim(),
                                bloodGroup = bloodGroup,
                                dob = dob,
                                country = country,
                                city = city,
                                imageUri = imageUri,
                                shortDescription = desc.trim()
                            )
                            vm.updateProfile(updated)
                            Toast.makeText(context, "Profile details saved successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Text(text = "Save Profile Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

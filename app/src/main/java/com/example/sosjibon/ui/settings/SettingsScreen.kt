package com.example.sosjibon.ui.settings

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@Composable
fun SettingsScreen(
    vm: SettingsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToPrivacyTerms: () -> Unit = {},
    onNavigateToDevelopers: () -> Unit = {},
    onNavigateToEmergencyContacts: () -> Unit = {}
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadSettings()
    }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer
    val background = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val emergencyRed = Color(0xFFD92D20)
    val successGreen = Color(0xFF2E7D32)

    val profileBitmap = remember(state.profile.imageUri) {
        if (state.profile.imageUri.isNotBlank()) {
            try {
                val file = File(state.profile.imageUri)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    // Dialog States
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // TOP BANNER / MESSAGE
            state.message?.let { msg ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = lightGreen)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = primaryGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = msg, color = textDark, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { vm.clearMessage() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = textGray)
                            }
                        }
                    }
                }
            }

            // 1. ACCOUNT & PERSONAL DETAILS SEGMENT (Opens Dedicated Full Page)
            item {
                SettingsSectionTitle(title = "Account & Personal Details", color = textDark)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEditProfile() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(color = lightGreen, shape = CircleShape)
                                .border(1.5.dp, primaryGreen, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileBitmap != null) {
                                Image(
                                    bitmap = profileBitmap.asImageBitmap(),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.profile.fullName,
                                    color = textDark,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (state.profile.isEmailVerified) {
                                    Box(
                                        modifier = Modifier
                                            .background(primaryGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Verified ✓", color = primaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.profile.email.isNotBlank()) state.profile.email else "Guest Member",
                                color = textGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Blood Group: ${state.profile.bloodGroup} • ${state.profile.city}, ${state.profile.country}",
                                color = primaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit profile",
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. EMERGENCY CONTACTS SECTION (Jump to Dedicated Page)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSectionTitle(title = "Emergency Contacts (${state.emergencyContacts.size})", color = textDark)

                    TextButton(onClick = onNavigateToEmergencyContacts) {
                        Text("Manage All →", color = primaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEmergencyContacts() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(emergencyRed.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = emergencyRed, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Emergency Broadcast List", fontWeight = FontWeight.Bold, color = textDark, fontSize = 15.sp)
                            Text("${state.emergencyContacts.size} emergency contacts registered for SOS alerts", color = textGray, fontSize = 12.sp)
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Manage",
                            tint = textGray
                        )
                    }
                }
            }

            // 3. PREFERENCES SEGMENT
            item {
                SettingsSectionTitle(title = "Preferences", color = textDark)

                SettingsCard(cardBg = cardBg) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Receive important SOSJibon updates",
                        checked = state.notificationsEnabled,
                        onCheckedChange = { vm.toggleNotifications(it) },
                        switchColor = primaryGreen,
                        textDark = textDark,
                        textGray = textGray
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

                    SettingsSwitchRow(
                        icon = Icons.Default.Warning,
                        title = "Emergency Alerts",
                        subtitle = "Receive critical emergency broadcast alerts",
                        checked = state.emergencyAlertsEnabled,
                        onCheckedChange = { vm.toggleEmergencyAlerts(it) },
                        switchColor = emergencyRed,
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // 4. SECURITY & PRIVACY SEGMENT (Jumps to Dedicated Pages)
            item {
                SettingsSectionTitle(title = "Security & Privacy", color = textDark)

                SettingsCard(cardBg = cardBg) {
                    SettingsNavigationRow(
                        icon = Icons.Default.Security,
                        title = "Security & Password",
                        subtitle = "Change password manually or send reset link",
                        tint = primaryGreen,
                        onClick = onNavigateToSecurity,
                        textDark = textDark,
                        textGray = textGray
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

                    SettingsNavigationRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy & Terms Policy",
                        subtitle = "Medical disclaimers, data encryption & user agreement",
                        tint = primaryGreen,
                        onClick = onNavigateToPrivacyTerms,
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // 5. GENERAL SEGMENT
            item {
                SettingsSectionTitle(title = "General", color = textDark)

                SettingsCard(cardBg = cardBg) {
                    SettingsNavigationRow(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "Current: ${state.language}",
                        tint = primaryGreen,
                        onClick = { showLanguageDialog = true },
                        textDark = textDark,
                        textGray = textGray
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

                    SettingsNavigationRow(
                        icon = Icons.Default.Brightness4,
                        title = "Appearance",
                        subtitle = when (state.themeMode) {
                            "dark" -> "Dark Theme"
                            "light" -> "Light Theme"
                            else -> "Device Reference (System Default)"
                        },
                        tint = primaryGreen,
                        onClick = { showThemeDialog = true },
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // 6. DEVELOPER TEAM (Jump to Dedicated Page)
            item {
                SettingsSectionTitle(title = "Developer Team", color = textDark)

                SettingsCard(cardBg = cardBg) {
                    SettingsNavigationRow(
                        icon = Icons.Default.Code,
                        title = "Developer Team",
                        subtitle = "Meet the creators & engineering team behind SOSJibon",
                        tint = primaryGreen,
                        onClick = onNavigateToDevelopers,
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // 7. SUPPORT & ABOUT SEGMENT
            item {
                SettingsSectionTitle(title = "Support & Information", color = textDark)

                SettingsCard(cardBg = cardBg) {
                    SettingsNavigationRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Frequently asked questions & emergency contacts",
                        tint = primaryGreen,
                        onClick = { showHelpDialog = true },
                        textDark = textDark,
                        textGray = textGray
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

                    SettingsNavigationRow(
                        icon = Icons.Default.Info,
                        title = "About SOSJibon",
                        subtitle = "Version, system status & emergency services",
                        tint = primaryGreen,
                        onClick = { showAboutDialog = true },
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // 8. DANGER ZONE SEGMENT
            item {
                SettingsSectionTitle(title = "Account Actions", color = textDark)

                SettingsCard(cardBg = cardBg) {
                    SettingsNavigationRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Log Out",
                        subtitle = "Sign out of your active account session",
                        tint = emergencyRed,
                        onClick = { showLogoutDialog = true },
                        textDark = textDark,
                        textGray = textGray
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

                    SettingsNavigationRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Delete Account",
                        subtitle = "Permanently remove your profile & cloud vault data",
                        tint = emergencyRed,
                        onClick = { showDeleteDialog = true },
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // FOOTER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "SOSJibon Emergency Healthcare System", color = textGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "Version 1.0.0 • Connected to Firebase", color = textGray.copy(alpha = 0.8f), fontSize = 10.5.sp)
                    Spacer(modifier = Modifier.height(25.dp))
                }
            }
        }
    }

    // DIALOGS

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "Select Language", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            vm.setLanguage("English")
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "English", color = primaryGreen, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            vm.setLanguage("Bangla")
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "বাংলা (Bangla)", color = primaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Appearance & Theme", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose how SOSJibon looks on your device:",
                        color = textGray,
                        fontSize = 12.5.sp
                    )

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("system")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "system") primaryGreen else primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📱 Follow Device (System Default)", color = if (state.themeMode == "system") primaryGreen else textDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (state.themeMode == "system") {
                                Text(text = "✓", color = primaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("dark")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (state.themeMode == "dark") primaryGreen else primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌙 Dark Theme", color = if (state.themeMode == "dark") primaryGreen else textDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (state.themeMode == "dark") {
                                Text(text = "✓", color = primaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("light")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (state.themeMode == "light") primaryGreen else primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "☀️ Light Theme", color = if (state.themeMode == "light") primaryGreen else textDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (state.themeMode == "light") {
                                Text(text = "✓", color = primaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = "Help & Support", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Emergency Helpline: 999", fontWeight = FontWeight.Bold, color = emergencyRed)
                    Text(text = "National Health Call Center: 16263", fontWeight = FontWeight.Bold, color = primaryGreen)
                    Text(text = "Email: support@sosjibon.org", color = textGray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)) {
                    Text(text = "OK")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "About SOSJibon", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Emergency Healthcare & Safety Companion", fontWeight = FontWeight.ExtraBold, color = primaryGreen, fontSize = 15.sp)
                    Text(
                        text = "SOSJibon is an all-in-one emergency healthcare and medical record companion designed to save lives when every second counts.",
                        color = textDark,
                        fontSize = 13.sp
                    )

                    HorizontalDivider(color = primaryGreen.copy(alpha = 0.2f))

                    Text(text = "How SOSJibon Works:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 13.sp)

                    Text(text = "1. 🚨 Emergency SOS Broadcast: One-tap emergency alert that immediately broadcasts your precise GPS location, blood group, and emergency contact alerts.", color = textGray, fontSize = 12.sp)
                    Text(text = "2. 📁 Health Data Vault: Store long-term medical records, lab PDFs, and disease pictures offline in encrypted Room storage.", color = textGray, fontSize = 12.sp)
                    Text(text = "3. 📊 Vitals & Trend Graphs: Log Fasting/Post-meal Blood Sugar, BP, Heart Rate, and Weight with automatic time graphs.", color = textGray, fontSize = 12.sp)
                    Text(text = "4. 💊 Medication Tracker: Daily dosage alerts, required medicine flags, and timing notes.", color = textGray, fontSize = 12.sp)
                    Text(text = "5. 📖 First Aid E-Library: Comprehensive offline emergency guidance and first aid instructions.", color = textGray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)) {
                    Text(text = "Close")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = emergencyRed) },
            title = { Text(text = "Log out?", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to log out of your SOSJibon account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        vm.signOut()
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emergencyRed)
                ) {
                    Text(text = "Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(text = "Cancel", color = textGray) }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = emergencyRed) },
            title = { Text(text = "Delete Account?", fontWeight = FontWeight.Bold) },
            text = { Text(text = "This will permanently delete your account from Firebase Auth & Cloud Firestore. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        vm.deleteAccount(onSuccess = onLogoutClick)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emergencyRed)
                ) {
                    Text(text = "Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(text = "Cancel", color = textGray) }
            }
        )
    }
}

// SECTION TITLE
@Composable
private fun SettingsSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 3.dp, bottom = 1.dp)
    )
}

// SETTINGS CARD
@Composable
private fun SettingsCard(cardBg: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

// NAVIGATION ROW
@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
    textDark: Color,
    textGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(color = tint.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = textGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = textGray.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// SWITCH ROW
@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchColor: Color,
    textDark: Color,
    textGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(color = switchColor.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = switchColor, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = textGray, fontSize = 11.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = switchColor
            )
        )
    }
}

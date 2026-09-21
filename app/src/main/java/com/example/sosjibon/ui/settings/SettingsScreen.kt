package com.example.sosjibon.ui.settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.ui.components.GuestAccessWarningCard
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

@Composable
fun SettingsScreen(
    vm: SettingsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToPrivacyTerms: () -> Unit = {},
    onNavigateToDevelopers: () -> Unit = {},
    onNavigateToEmergencyContacts: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    val isGuest = !state.isLoggedIn || state.profile.fullName == "Guest Member" || state.profile.email.isBlank()

    if (isGuest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GuestAccessWarningCard(
                pageName = "Settings & Profile",
                onLoginRegisterClick = onNavigateToAuth
            )
        }
        return
    }

    LaunchedEffect(Unit) {
        if (state.profile.email.isBlank()) {
            vm.loadSettings()
        }
    }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer
    val background = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val emergencyRed = Color(0xFFD92D20)

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
    var showBugReportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isWide) Modifier.widthIn(max = 840.dp).align(Alignment.TopCenter) else Modifier)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // TOP BANNER / MESSAGE
            state.message?.let { msg ->
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

            // UNIFIED USER SETTINGS CONTENT FOR ALL USERS
            RegularUserSettingsContent(
                state = state,
                vm = vm,
                profileBitmap = profileBitmap,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToEmergencyContacts = onNavigateToEmergencyContacts,
                onNavigateToSecurity = onNavigateToSecurity,
                onNavigateToPrivacyTerms = onNavigateToPrivacyTerms,
                onNavigateToDevelopers = onNavigateToDevelopers,
                onShowLanguageDialog = { showLanguageDialog = true },
                onShowThemeDialog = { showThemeDialog = true },
                onShowHelpDialog = { showHelpDialog = true },
                onShowAboutDialog = { showAboutDialog = true },
                onShowBugReportDialog = { showBugReportDialog = true },
                onShowLogoutDialog = { showLogoutDialog = true },
                onShowDeleteDialog = { showDeleteDialog = true },
                primaryGreen = primaryGreen,
                lightGreen = lightGreen,
                cardBg = cardBg,
                textDark = textDark,
                textGray = textGray,
                emergencyRed = emergencyRed
            )
        }
    }

    // DIALOGS
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

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Appearance Theme", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Choose how SOSJibon looks on your device.", fontSize = 12.sp, color = textGray)
                    
                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("light")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "light") primaryGreen else primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Text(text = "☀️ Light Theme", color = primaryGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("dark")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "dark") primaryGreen else primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Text(text = "🌙 Dark Theme", color = primaryGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("system")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "system") primaryGreen else primaryGreen.copy(alpha = 0.3f))
                    ) {
                        Text(text = "📱 System Default", color = primaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = "Help & Medical Disclaimer", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Emergency Helpline: 999", fontWeight = FontWeight.Bold, color = emergencyRed)
                    Text(
                        text = "SOSJibon provides first-aid guidance and health tracking tools. It does NOT replace professional emergency medical services. In critical emergencies, always call 999 immediately.",
                        fontSize = 12.5.sp,
                        color = textDark
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text(text = "Understand") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "About SOSJibon", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "SOSJibon Healthcare Engine", fontWeight = FontWeight.Bold, color = primaryGreen, fontSize = 14.sp)
                    Text(text = "Version: 2.1.0 (Production Build)", fontSize = 12.sp, color = textGray)
                    Text(text = "Offline-First First Aid, Realtime GPS SOS & Encrypted Medical Vault.", fontSize = 12.sp, color = textDark)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text(text = "Close") }
            }
        )
    }

    if (showBugReportDialog) {
        var bugTitle by remember { mutableStateOf("") }
        var bugDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBugReportDialog = false },
            title = { Text(text = "Report Bug / System Issue", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Describe any crash or bug for Admin investigation.", fontSize = 12.sp, color = textGray)

                    OutlinedTextField(
                        value = bugTitle,
                        onValueChange = { bugTitle = it },
                        label = { Text("Issue Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bugDesc,
                        onValueChange = { bugDesc = it },
                        label = { Text("Detailed Description") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bugTitle.isNotBlank()) {
                            showBugReportDialog = false
                            try {
                                val bugMap = hashMapOf(
                                    "title" to bugTitle,
                                    "description" to bugDesc,
                                    "severity" to "MEDIUM",
                                    "reporterName" to state.profile.fullName.ifBlank { "Registered User" },
                                    "reporterEmail" to state.profile.email,
                                    "status" to "OPEN",
                                    "timestamp" to System.currentTimeMillis()
                                )
                                FirebaseFirestore.getInstance().collection("bug_reports").add(bugMap)
                                Toast.makeText(context, "Bug report submitted to Admin! Thank you ✓", Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Text("Submit to Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBugReportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Sign Out Account?", fontWeight = FontWeight.Bold, color = textDark) },
            text = { Text(text = "Are you sure you want to sign out? Your offline data will remain encrypted on your device.", color = textDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        vm.signOut()
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emergencyRed)
                ) {
                    Text(text = "Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        if (state.isAdminRole) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Admin Account Protected 🛡️", fontWeight = FontWeight.Bold, color = textDark) },
                text = { Text(text = "System Protection: Admin accounts cannot be deleted to maintain platform security and moderation services.", color = textDark) },
                confirmButton = {
                    Button(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Text(text = "Understand")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Delete Account?", fontWeight = FontWeight.Bold, color = emergencyRed) },
                text = { Text(text = "Warning: This action will permanently remove your user profile and cloud database backup.", color = textDark) },
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
                    TextButton(onClick = { showDeleteDialog = false }) { Text(text = "Cancel") }
                }
            )
        }
    }
}

// ------------------------------------------------------------
// REGULAR USER SETTINGS CONTENT
// ------------------------------------------------------------

// ------------------------------------------------------------
// REGULAR USER SETTINGS CONTENT
// ------------------------------------------------------------
@Composable
private fun RegularUserSettingsContent(
    state: SettingsState,
    vm: SettingsViewModel,
    profileBitmap: Bitmap?,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToPrivacyTerms: () -> Unit,
    onNavigateToDevelopers: () -> Unit,
    onShowLanguageDialog: () -> Unit,
    onShowThemeDialog: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onShowAboutDialog: () -> Unit,
    onShowBugReportDialog: () -> Unit,
    onShowLogoutDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    primaryGreen: Color,
    lightGreen: Color,
    cardBg: Color,
    textDark: Color,
    textGray: Color,
    emergencyRed: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. ACCOUNT & PERSONAL DETAILS SEGMENT
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
                            text = state.profile.fullName.ifBlank { "Tap to complete profile" },
                            color = textDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (state.isAdminRole) {
                            Box(
                                modifier = Modifier
                                    .background(primaryGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ADMIN 👑", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
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
                        text = "Blood Group: ${state.profile.bloodGroup.ifBlank { "Not Set" }} • Location: ${if (state.profile.city.isNotBlank() && state.profile.country.isNotBlank()) "${state.profile.city}, ${state.profile.country}" else if (state.profile.country.isNotBlank()) state.profile.country else "Not Set"}",
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

        // 2. EMERGENCY CONTACTS SECTION
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(color = lightGreen, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Emergency contacts",
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emergency Contacts Directory",
                        color = textDark,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.emergencyContacts.size} emergency contacts registered",
                        color = textGray,
                        fontSize = 11.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3. SECURITY & PRIVACY SEGMENT
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

        // 4. GENERAL SEGMENT
        SettingsSectionTitle(title = "General", color = textDark)

        SettingsCard(cardBg = cardBg) {
            SettingsNavigationRow(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "Current: ${state.language}",
                tint = primaryGreen,
                onClick = onShowLanguageDialog,
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
                    else -> "System Default"
                },
                tint = primaryGreen,
                onClick = onShowThemeDialog,
                textDark = textDark,
                textGray = textGray
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

            SettingsNavigationRow(
                icon = Icons.Default.Code,
                title = "Developer Team",
                subtitle = "Meet the creators & engineering leads behind SOSJibon",
                tint = primaryGreen,
                onClick = onNavigateToDevelopers,
                textDark = textDark,
                textGray = textGray
            )
        }

        // 5. SUPPORT & INFORMATION
        SettingsSectionTitle(title = "Support & Information", color = textDark)

        SettingsCard(cardBg = cardBg) {
            SettingsNavigationRow(
                icon = Icons.Default.HelpOutline,
                title = "Help & Medical Disclaimer",
                subtitle = "Emergency guidance & usage terms",
                tint = primaryGreen,
                onClick = onShowHelpDialog,
                textDark = textDark,
                textGray = textGray
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

            SettingsNavigationRow(
                icon = Icons.Default.Info,
                title = "About SOSJibon",
                subtitle = "Version 2.1.0 • Offline First Aid & GPS SOS",
                tint = primaryGreen,
                onClick = onShowAboutDialog,
                textDark = textDark,
                textGray = textGray
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

            SettingsNavigationRow(
                icon = Icons.Default.BugReport,
                title = "Report Bug / Issue",
                subtitle = "Submit issue report directly to Admin Diagnostics",
                tint = primaryGreen,
                onClick = onShowBugReportDialog,
                textDark = textDark,
                textGray = textGray
            )
        }

        // 6. ACCOUNT ACTIONS
        SettingsSectionTitle(title = "Account Actions", color = textDark)

        SettingsCard(cardBg = cardBg) {
            SettingsNavigationRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Sign Out",
                subtitle = "Sign out from your SOSJibon account",
                tint = emergencyRed,
                onClick = onShowLogoutDialog,
                textDark = textDark,
                textGray = textGray
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = primaryGreen.copy(alpha = 0.15f))

            if (!state.isAdminRole) {
                SettingsNavigationRow(
                    icon = Icons.Default.DeleteOutline,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account and user profile",
                    tint = emergencyRed,
                    onClick = onShowDeleteDialog,
                    textDark = textDark,
                    textGray = textGray
                )
            } else {
                SettingsNavigationRow(
                    icon = Icons.Default.Shield,
                    title = "Delete Account (Protected)",
                    subtitle = "🛡️ Admin accounts cannot be deleted for system safety",
                    tint = textGray,
                    onClick = onShowDeleteDialog,
                    textDark = textGray,
                    textGray = textGray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ------------------------------------------------------------
// HELPER COMPONENTS
// ------------------------------------------------------------
@Composable
private fun SettingsSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun SettingsCard(
    cardBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textDark,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = textGray,
                fontSize = 11.5.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = textGray,
            modifier = Modifier.size(18.dp)
        )
    }
}

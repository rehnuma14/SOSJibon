package com.example.sosjibon.ui.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.ui.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth

private val PrimaryGreen = Color(0xFF159A6C)
private val EmergencyRed = Color(0xFFD92D20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    onLogoutClick: () -> Unit = {},
    onNavigateToDevelopers: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToPrivacyTerms: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteProtectionDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Admin Settings", fontWeight = FontWeight.Black, fontSize = 20.sp, color = textDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("ADMIN 👑", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text("Dedicated System Control & Security Options", fontSize = 11.sp, color = textGray)
                        }
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. ADMIN PROFILE HEADER CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(34.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.profile.fullName.ifBlank { "System Administrator" },
                                color = textDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(PrimaryGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ADMIN 👑", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (state.profile.email.isNotBlank()) state.profile.email else "admin@gmail.com",
                            color = textGray,
                            fontSize = 12.5.sp
                        )

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Role: System Admin • Status: Active & Secured ✓",
                            color = PrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. GENERAL & APPEARANCE THEME CUSTOMIZATION
            AdminSectionTitle(title = "General & Appearance", color = textDark)

            AdminCardContainer(cardBg = cardBg) {
                AdminSettingRow(
                    icon = Icons.Default.Brightness4,
                    title = "Appearance Theme",
                    subtitle = when (state.themeMode) {
                        "dark" -> "Current Mode: 🌙 Dark Theme"
                        "light" -> "Current Mode: ☀️ Light Theme"
                        else -> "Current Mode: 📱 System Default"
                    },
                    onClick = { showThemeDialog = true },
                    textDark = textDark,
                    textGray = textGray
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PrimaryGreen.copy(alpha = 0.15f))

                AdminSettingRow(
                    icon = Icons.Default.Language,
                    title = "System Language",
                    subtitle = "Current Language: ${state.language}",
                    onClick = { showLanguageDialog = true },
                    textDark = textDark,
                    textGray = textGray
                )
            }

            // 3. ADMIN PLATFORM SECURITY & CONTROL
            AdminSectionTitle(title = "Platform Security & Governance", color = textDark)

            AdminCardContainer(cardBg = cardBg) {
                AdminSettingRow(
                    icon = Icons.Default.Security,
                    title = "Admin Security & Passwords",
                    subtitle = "Manage master password, 2FA & active sessions",
                    onClick = onNavigateToSecurity,
                    textDark = textDark,
                    textGray = textGray
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PrimaryGreen.copy(alpha = 0.15f))

                AdminSettingRow(
                    icon = Icons.Default.Code,
                    title = "Developer Permits Center",
                    subtitle = "Review developer keys & manage admin permit requests",
                    onClick = onNavigateToDevelopers,
                    textDark = textDark,
                    textGray = textGray
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PrimaryGreen.copy(alpha = 0.15f))

                AdminSettingRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy & Governance Policy",
                    subtitle = "System privacy rules, data encryption & admin terms",
                    onClick = onNavigateToPrivacyTerms,
                    textDark = textDark,
                    textGray = textGray
                )
            }

            // 4. SYSTEM DIAGNOSTICS & INFORMATION
            AdminSectionTitle(title = "System Information & Diagnostics", color = textDark)

            AdminCardContainer(cardBg = cardBg) {
                AdminSettingRow(
                    icon = Icons.Default.Info,
                    title = "About SOSJibon Admin Engine",
                    subtitle = "Version 2.1.0 • System Health & Maintenance",
                    onClick = { showAboutDialog = true },
                    textDark = textDark,
                    textGray = textGray
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PrimaryGreen.copy(alpha = 0.15f))

                AdminSettingRow(
                    icon = Icons.Default.Lock,
                    title = "Data Encryption Standards",
                    subtitle = "AES-256 Room Storage & Firestore SSL Security",
                    onClick = {
                        Toast.makeText(context, "System Storage Encrypted with AES-256 & Firestore Security Rules ✓", Toast.LENGTH_LONG).show()
                    },
                    textDark = textDark,
                    textGray = textGray
                )
            }

            // 5. ADMIN ACTIONS & ACCOUNT PROTECTION
            AdminSectionTitle(title = "Admin Account Actions", color = textDark)

            AdminCardContainer(cardBg = cardBg) {
                AdminSettingRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Sign Out Admin Session",
                    subtitle = "Safely sign out from Admin Control Panel",
                    tint = EmergencyRed,
                    onClick = { showLogoutDialog = true },
                    textDark = textDark,
                    textGray = textGray
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PrimaryGreen.copy(alpha = 0.15f))

                AdminSettingRow(
                    icon = Icons.Default.Shield,
                    title = "Delete Account (Protected 🛡️)",
                    subtitle = "Admin accounts cannot be deleted to ensure platform security",
                    tint = textGray,
                    onClick = { showDeleteProtectionDialog = true },
                    textDark = textGray,
                    textGray = textGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // THEME DIALOG
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Appearance Theme", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Choose appearance theme for SOSJibon Admin Panel.", fontSize = 12.sp, color = textGray)

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("light")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "light") PrimaryGreen else PrimaryGreen.copy(alpha = 0.3f))
                    ) {
                        Text(text = "☀️ Light Theme", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("dark")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "dark") PrimaryGreen else PrimaryGreen.copy(alpha = 0.3f))
                    ) {
                        Text(text = "🌙 Dark Theme", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            vm.setThemeMode("system")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.themeMode == "system") PrimaryGreen else PrimaryGreen.copy(alpha = 0.3f))
                    ) {
                        Text(text = "📱 System Default", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    // LANGUAGE DIALOG
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "Select System Language", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            vm.setLanguage("English")
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "English", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            vm.setLanguage("Bangla")
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "বাংলা (Bangla)", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    // LOGOUT DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out Admin?", fontWeight = FontWeight.Bold, color = textDark) },
            text = { Text("Are you sure you want to sign out from the Admin Control Panel?", color = textDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        try {
                            FirebaseAuth.getInstance().signOut()
                        } catch (_: Exception) {}
                        vm.signOut()
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Sign Out Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // DELETE PROTECTION DIALOG
    if (showDeleteProtectionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteProtectionDialog = false },
            title = { Text("Admin Account Protected 🛡️", fontWeight = FontWeight.Bold, color = textDark) },
            text = { Text("System Protection: Admin accounts cannot be deleted to maintain platform moderation, emergency SOS handling, and system continuity.", color = textDark) },
            confirmButton = {
                Button(
                    onClick = { showDeleteProtectionDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Understand")
                }
            }
        )
    }

    // ABOUT DIALOG
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("SOSJibon Admin Engine", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Platform Engine v2.1.0", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 14.sp)
                    Text("Dedicated System Control, User Records Audit, GPS SOS Monitoring & Community Permit Engine.", fontSize = 12.5.sp, color = textDark)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun AdminSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun AdminCardContainer(
    cardBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
private fun AdminSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = PrimaryGreen,
    onClick: () -> Unit,
    textDark: Color,
    textGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(tint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = textDark, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = textGray, fontSize = 11.5.sp)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}

package com.example.sosjibon.ui.settings

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primaryGreen = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    val user = FirebaseAuth.getInstance().currentUser
    val isCurrentAdmin = user?.email?.trim()?.lowercase() in listOf("admin@gmail.com", "admin@sosjibon.org", "shafiul@sosjibon.org")

    var selectedDeveloper by remember { mutableStateOf<DeveloperItem?>(null) }
    var showPermitDialog by remember { mutableStateOf(false) }

    var permitStatus by remember { mutableStateOf("none") } // "none", "pending", "approved"
    var grantedAdminEmail by remember { mutableStateOf("admin@gmail.com") }
    var grantedAdminPass by remember { mutableStateOf("adminx") }

    // Real-Time Listeners for Firestore Admin Permit Request & User Role
    DisposableEffect(user?.uid) {
        val uid = user?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val l1 = db.collection("admin_permit_requests").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val st = snapshot.getString("status") ?: "none"
                        if (st == "rejected" || st == "revoked") {
                            permitStatus = "none"
                        } else {
                            permitStatus = st
                            grantedAdminEmail = snapshot.getString("adminEmail") ?: "admin@gmail.com"
                            grantedAdminPass = snapshot.getString("adminPassword") ?: "adminx"
                        }
                    } else {
                        permitStatus = "none"
                    }
                }

            val l2 = db.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val role = snapshot.getString("role") ?: "user"
                        if (role == "admin") {
                            permitStatus = "approved"
                            grantedAdminEmail = "admin@gmail.com"
                            grantedAdminPass = "adminx"
                        } else if (permitStatus == "approved") {
                            permitStatus = "none"
                        }
                    }
                }

            onDispose {
                l1.remove()
                l2.remove()
            }
        } else {
            onDispose { }
        }
    }

    val developers = listOf(
        DeveloperItem(
            name = "Shafiul Islam",
            role = "Lead Developer",
            designation = "Lead Systems & Android Engineer",
            contribution = "Core System Architecture, App Navigation Host, Emergency SOS Engine, and Jetpack Compose UI Framework.",
            email = "shafiul@sosjibon.org",
            linkedin = "linkedin.com/in/shafiul-islam",
            initial = "SI"
        ),
        DeveloperItem(
            name = "Mahmudul Hasan",
            role = "Backend Lead",
            designation = "Cloud & Security Infrastructure Lead",
            contribution = "Firebase Authentication, Cloud Firestore Realtime Sync Engine, and Encrypted Room Database Storage.",
            email = "mahmudul@sosjibon.org",
            linkedin = "linkedin.com/in/mahmudul-hasan",
            initial = "MH"
        ),
        DeveloperItem(
            name = "Nusrat Jahan",
            role = "UI/UX Specialist",
            designation = "Mobile Product & Experience Designer",
            contribution = "Healthcare Vault UI, Vitals Trend Canvas Charts Visualization, Theme Systems & Accessibility Design.",
            email = "nusrat@sosjibon.org",
            linkedin = "linkedin.com/in/nusrat-jahan",
            initial = "NJ"
        ),
        DeveloperItem(
            name = "Tanvir Ahmed",
            role = "QA & Data Lead",
            designation = "Quality Assurance & Health Systems Lead",
            contribution = "Emergency GPS Location Services, First Aid E-Library Content Management, and Integration Testing.",
            email = "tanvir@sosjibon.org",
            linkedin = "linkedin.com/in/tanvir-ahmed",
            initial = "TA"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Developer Team & Permits", fontWeight = FontWeight.Bold, color = textDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background)
            )
        },
        containerColor = background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP BANNER: ADMIN MODE ACTIVE VS REGULAR USER PERMIT REQUEST
            if (isCurrentAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("👑 OFFICIAL ADMIN MODE ACTIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Text("You are logged in as System Administrator with full moderation, user directory, bug diagnostics, and emergency control privileges.", color = Color.White.copy(alpha = 0.95f), fontSize = 12.5.sp)
                    }
                }
            } else {
                when (permitStatus) {
                    "approved" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("🎉 ADMIN PERMIT GRANTED!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }

                                Text("Your request was approved by Lead Developers! To access the Admin Dashboard, please log out of your personal account and log in using the official Admin credentials below:", color = Color.White.copy(alpha = 0.95f), fontSize = 12.sp)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text("✉️ Admin Email: $grantedAdminEmail", color = Color(0xFF17332A), fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text("🔑 Admin Password: $grantedAdminPass", color = primaryGreen, fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        try {
                                            FirebaseAuth.getInstance().signOut()
                                        } catch (_: Exception) {}
                                        onNavigateToLogin()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🔒 Log Out & Log In As Admin", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    "pending" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD97706)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⏳ Admin Permit Request Pending Review", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                                Text("Your credentials have been submitted to Lead Developers. Once approved, your official Admin login credentials will appear right in this bar!", color = Color.White.copy(alpha = 0.95f), fontSize = 12.sp)
                            }
                        }
                    }

                    else -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = primaryGreen),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(24.dp))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Request Admin Permit & Access", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        Text(text = "Submit credentials to Lead Developers for Admin Panel permit", color = Color.White.copy(alpha = 0.9f), fontSize = 11.5.sp)
                                    }
                                }

                                Button(
                                    onClick = { showPermitDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Request Admin Access Permit", color = primaryGreen, fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            Text(text = "Engineering Lead Developers (2×2)", fontWeight = FontWeight.Bold, color = textDark, fontSize = 16.sp)

            // 2x2 Grid Layout
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GridDeveloperCard(
                        developer = developers[0],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[0] }
                    )
                    GridDeveloperCard(
                        developer = developers[1],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[1] }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GridDeveloperCard(
                        developer = developers[2],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[2] }
                    )
                    GridDeveloperCard(
                        developer = developers[3],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[3] }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = primaryGreen.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "SOSJibon Engineering", color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "Built for saving lives", color = textGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showPermitDialog) {
        RequestAdminPermitModal(
            defaultName = user?.displayName ?: "",
            defaultEmail = user?.email ?: "",
            onDismiss = { showPermitDialog = false },
            onSubmit = { name, email, phone, orgReason ->
                showPermitDialog = false
                permitStatus = "pending"
                scope.launch(Dispatchers.IO) {
                    try {
                        val uid = user?.uid ?: "guest_${System.currentTimeMillis()}"
                        val db = FirebaseFirestore.getInstance()
                        val permitMap = hashMapOf(
                            "uid" to uid,
                            "fullName" to name,
                            "email" to email,
                            "phone" to phone,
                            "organizationReason" to orgReason,
                            "status" to "pending",
                            "adminEmail" to "admin@gmail.com",
                            "adminPassword" to "adminx",
                            "requestedAt" to System.currentTimeMillis()
                        )
                        db.collection("admin_permit_requests").document(uid).set(permitMap).await()
                    } catch (_: Exception) {}
                }
                Toast.makeText(context, "Admin Permit Request Submitted! Lead Developers will review and grant access.", Toast.LENGTH_LONG).show()
            },
            primaryGreen = primaryGreen,
            textDark = textDark
        )
    }

    selectedDeveloper?.let { dev ->
        DeveloperDetailModal(developer = dev, onDismiss = { selectedDeveloper = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestAdminPermitModal(
    defaultName: String,
    defaultEmail: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    primaryGreen: Color,
    textDark: Color
) {
    var name by remember { mutableStateOf(defaultName) }
    var email by remember { mutableStateOf(defaultEmail) }
    var phone by remember { mutableStateOf("") }
    var orgReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Request Admin Access Permit", fontWeight = FontWeight.Bold, color = textDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Provide your information and organization details to request Admin Moderator privileges.", fontSize = 11.5.sp, color = Color.Gray)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = "Email Address") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(text = "Phone Number") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = orgReason,
                    onValueChange = { orgReason = it },
                    label = { Text(text = "Organization / Reason for Request") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && email.isNotBlank() && orgReason.isNotBlank(),
                onClick = { onSubmit(name.trim(), email.trim(), phone.trim(), orgReason.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text(text = "Submit Admin Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun GridDeveloperCard(
    developer: DeveloperItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(lightGreen, CircleShape)
                    .border(2.dp, primaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = developer.initial, fontWeight = FontWeight.Black, color = primaryGreen, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = developer.name,
                fontWeight = FontWeight.ExtraBold,
                color = textDark,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = developer.role,
                color = primaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = lightGreen
            ) {
                Text(
                    text = "Tap for details",
                    color = primaryGreen,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun DeveloperDetailModal(
    developer: DeveloperItem,
    onDismiss: () -> Unit
) {
    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(lightGreen, CircleShape)
                        .border(1.5.dp, primaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = developer.initial, fontWeight = FontWeight.Black, color = primaryGreen, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = developer.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = textDark)
                    Text(text = developer.role, fontSize = 12.sp, color = primaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = primaryGreen.copy(alpha = 0.2f))

                Text(text = "Designation:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 12.5.sp)
                Text(text = developer.designation, color = textGray, fontSize = 12.sp)

                Text(text = "Key Contribution:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 12.5.sp)
                Text(text = developer.contribution, color = textGray, fontSize = 12.sp, lineHeight = 17.sp)

                HorizontalDivider(color = primaryGreen.copy(alpha = 0.2f))

                Text(text = "Contact & Profiles:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 12.5.sp)
                Text(text = "✉️ Email: ${developer.email}", color = primaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "🔗 LinkedIn: ${developer.linkedin}", color = primaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)) {
                Text(text = "Close")
            }
        }
    )
}

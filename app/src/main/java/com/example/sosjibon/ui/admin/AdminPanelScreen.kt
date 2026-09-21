package com.example.sosjibon.ui.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private val PrimaryGreen = Color(0xFF159A6C)

data class AdminUserInfo(
    val uid: String = "",
    val fullName: String = "User",
    val email: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val gender: String = "",
    val lastDonationDate: String = "",
    val dob: String = "",
    val city: String = "",
    val country: String = "Bangladesh",
    val shortDescription: String = "",
    val isEmailVerified: Boolean = false,
    val role: String = "user",
    val createdAt: Long = 0L
)

data class AdminPermitRequest(
    val uid: String = "",
    val fullName: String = "User",
    val email: String = "",
    val phone: String = "",
    val organizationReason: String = "",
    val status: String = "pending",
    val requestedAt: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val userList = remember { mutableStateListOf<AdminUserInfo>() }
    val permitRequestsList = remember { mutableStateListOf<AdminPermitRequest>() }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    // Real-Time Listeners for Firestore Users Directory & Permit Requests
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        val listener = db.collection("users")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    userList.clear()
                    val users = snapshot.documents.mapNotNull { doc ->
                        val uid = doc.id
                        val name = doc.getString("fullName") ?: "User"
                        val email = doc.getString("email") ?: ""
                        val phone = doc.getString("phone") ?: ""
                        val bg = doc.getString("bloodGroup") ?: ""
                        val gender = doc.getString("gender") ?: ""
                        val lastDonation = doc.getString("lastDonationDate") ?: ""
                        val dob = doc.getString("dob") ?: ""
                        val city = doc.getString("city") ?: ""
                        val country = doc.getString("country") ?: "Bangladesh"
                        val desc = doc.getString("shortDescription") ?: ""
                        val verified = doc.getBoolean("isEmailVerified") == true
                        val role = doc.getString("role") ?: "user"
                        val created = doc.getLong("createdAt") ?: 0L

                        AdminUserInfo(uid, name, email, phone, bg, gender, lastDonation, dob, city, country, desc, verified, role, created)
                    }
                    userList.addAll(users)
                }
            }

        val permitListener = db.collection("admin_permit_requests")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    permitRequestsList.clear()
                    val reqs = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AdminPermitRequest::class.java)?.copy(uid = doc.id)
                    }
                    permitRequestsList.addAll(reqs.sortedByDescending { it.requestedAt })
                }
            }

        onDispose {
            listener.remove()
            permitListener.remove()
        }
    }

    val pendingPermitRequests = permitRequestsList.filter { it.status.equals("pending", ignoreCase = true) }

    val filteredUsers = if (searchQuery.isBlank()) {
        userList
    } else {
        val q = searchQuery.trim().lowercase()
        userList.filter {
            it.fullName.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.phone.contains(q) ||
                    it.bloodGroup.lowercase().contains(q) ||
                    it.city.lowercase().contains(q)
        }
    }

    val verifiedCount = userList.count { it.isEmailVerified }
    val adminCount = userList.count { it.role == "admin" }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Users Directory & Datas", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ADMIN 👑", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text("Full User Records, Blood Groups & Verification Permits", fontSize = 11.sp, color = textGray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // PENDING ADMIN PERMIT REQUESTS CARD LIST
            if (pendingPermitRequests.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🛡️ Pending Admin Permit Requests (${pendingPermitRequests.size})",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        pendingPermitRequests.forEach { req ->
                            val sdf = SimpleDateFormat("hh:mm a • dd MMM yyyy", Locale.getDefault())
                            val reqTimeStr = sdf.format(Date(req.requestedAt))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(req.fullName, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = textDark)
                                        Text(reqTimeStr, fontSize = 10.5.sp, color = textGray)
                                    }

                                    Text("✉️ Email: ${req.email}", fontSize = 12.sp, color = textDark)
                                    if (req.phone.isNotBlank()) {
                                        Text("📞 Phone: ${req.phone}", fontSize = 12.sp, color = textDark)
                                    }
                                    if (req.organizationReason.isNotBlank()) {
                                        Text("📝 Reason: ${req.organizationReason}", fontSize = 12.sp, color = textGray)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    try {
                                                        val db = FirebaseFirestore.getInstance()
                                                        db.collection("admin_permit_requests").document(req.uid).set(
                                                            mapOf(
                                                                "status" to "approved",
                                                                "adminEmail" to "admin@gmail.com",
                                                                "adminPassword" to "adminx"
                                                            ),
                                                            SetOptions.merge()
                                                        ).await()

                                                        db.collection("users").document(req.uid).update("role", "admin").await()

                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Admin Permit Approved for ${req.fullName}! ✓", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve & Make Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    try {
                                                        val db = FirebaseFirestore.getInstance()
                                                        db.collection("admin_permit_requests").document(req.uid).set(
                                                            mapOf("status" to "rejected"),
                                                            SetOptions.merge()
                                                        ).await()

                                                        db.collection("users").document(req.uid).update("role", "user").await()

                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Admin Permit Request Rejected.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFD92D20))
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFD92D20), modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", fontSize = 11.sp, color = Color(0xFFD92D20), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // STATS SUMMARY ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserStatSummaryCard(
                    title = "Total Users",
                    value = "${userList.size}",
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f),
                    cardBg = cardBg,
                    textDark = textDark,
                    textGray = textGray
                )
                UserStatSummaryCard(
                    title = "Verified Accounts",
                    value = "$verifiedCount",
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f),
                    cardBg = cardBg,
                    textDark = textDark,
                    textGray = textGray
                )
                UserStatSummaryCard(
                    title = "Admins",
                    value = "$adminCount",
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f),
                    cardBg = cardBg,
                    textDark = textDark,
                    textGray = textGray
                )
            }

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, email, phone, blood group...", fontSize = 12.5.sp, color = textGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textGray)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = textGray.copy(alpha = 0.3f),
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                singleLine = true
            )

            // USER CARDS LIST
            if (filteredUsers.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                        Text("No user records found matching search.", color = textGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(42.dp).background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(user.fullName.ifBlank { "User" }, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = textDark)
                                            Text(user.email.ifBlank { "No Email Registered" }, fontSize = 12.sp, color = textGray)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (user.isEmailVerified) {
                                            Box(
                                                modifier = Modifier.background(PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Verified ✓", color = PrimaryGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }

                                        if (user.role == "admin") {
                                            Box(
                                                modifier = Modifier.background(PrimaryGreen, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ADMIN", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = textGray.copy(alpha = 0.15f))

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("📞 Phone: ${user.phone.ifBlank { "Not Registered" }}", fontSize = 12.sp, color = textDark, fontWeight = FontWeight.Medium)
                                    Text("🩸 Blood Group: ${user.bloodGroup.ifBlank { "Not Set" }} • Gender: ${user.gender.ifBlank { "Not Set" }}", fontSize = 12.sp, color = textDark, fontWeight = FontWeight.Medium)
                                    Text("🎂 Date of Birth: ${user.dob.ifBlank { "Not Set" }}", fontSize = 12.sp, color = textDark, fontWeight = FontWeight.Medium)
                                    Text("📅 Last Blood Donated: ${user.lastDonationDate.ifBlank { "Not Set / No Record" }}", fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                    Text("📍 Location: ${if (user.city.isNotBlank() && user.country.isNotBlank()) "${user.city}, ${user.country}" else if (user.country.isNotBlank()) user.country else "Not Set"}", fontSize = 12.sp, color = textDark, fontWeight = FontWeight.Medium)
                                    if (user.shortDescription.isNotBlank()) {
                                        Text("💼 Bio / Profession: ${user.shortDescription}", fontSize = 12.sp, color = textGray)
                                    }
                                    Text("🔑 User UID: ${user.uid}", fontSize = 10.5.sp, color = textGray)
                                }

                                HorizontalDivider(color = textGray.copy(alpha = 0.15f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!user.isEmailVerified) {
                                        Button(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    try {
                                                        val db = FirebaseFirestore.getInstance()
                                                        db.collection("users").document(user.uid).update("isEmailVerified", true).await()
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Verification permit granted to ${user.fullName}! ✓", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Grant Verify Permit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {},
                                            enabled = false,
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Verified ✓", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    val newRole = if (user.role == "admin") "user" else "admin"
                                                    db.collection("users").document(user.uid).update("role", newRole).await()

                                                    val permitMap = hashMapOf(
                                                        "uid" to user.uid,
                                                        "fullName" to user.fullName,
                                                        "email" to user.email,
                                                        "status" to if (newRole == "admin") "approved" else "none",
                                                        "adminEmail" to "admin@gmail.com",
                                                        "adminPassword" to "adminx",
                                                        "grantedAt" to System.currentTimeMillis()
                                                    )
                                                    db.collection("admin_permit_requests").document(user.uid).set(permitMap).await()

                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Role updated to $newRole for ${user.fullName}!", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, PrimaryGreen)
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (user.role == "admin") "Revoke Admin" else "Make Admin", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserStatSummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    cardBg: Color,
    textDark: Color,
    textGray: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textGray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

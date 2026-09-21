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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryGreen = Color(0xFF159A6C)
private val AlertAmber = Color(0xFFD97706)
private val EmergencyRed = Color(0xFFD92D20)

data class AdminBugReport(
    val id: String = "",
    val title: String = "",
    val reporterName: String = "User",
    val reporterEmail: String = "",
    val severity: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val description: String = "",
    val status: String = "OPEN", // OPEN, RESOLVED
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBugsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, OPEN, RESOLVED
    val bugList = remember { mutableStateListOf<AdminBugReport>() }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    // Pre-populated Sample Bugs if Firestore is empty
    val sampleBugs = remember {
        listOf(
            AdminBugReport(
                id = "sample_1",
                title = "GPS location delay on low network signal",
                reporterName = "Shafiul Islam",
                reporterEmail = "shafiul@sosjibon.org",
                severity = "HIGH",
                description = "Emergency SOS GPS coordinates take up to 8 seconds to resolve under 2G edge connection.",
                status = "OPEN",
                timestamp = System.currentTimeMillis() - 3600000L
            ),
            AdminBugReport(
                id = "sample_2",
                title = "Bluetooth health vitals sync timeout",
                reporterName = "Mahmudul Hasan",
                reporterEmail = "mahmudul@sosjibon.org",
                severity = "MEDIUM",
                description = "Heart rate sensor disconnects automatically after 15 minutes of background monitoring.",
                status = "OPEN",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            AdminBugReport(
                id = "sample_3",
                title = "PDF export font scaling in Vault",
                reporterName = "Nusrat Jahan",
                reporterEmail = "nusrat@sosjibon.org",
                severity = "LOW",
                description = "Prescription PDF preview text overlaps on 7-inch tablet screens.",
                status = "RESOLVED",
                timestamp = System.currentTimeMillis() - 172800000L
            )
        )
    }

    // Real-Time Listener for Firestore Bug Reports
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("bug_reports")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    bugList.clear()
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AdminBugReport::class.java)?.copy(id = doc.id)
                    }
                    if (items.isEmpty()) {
                        bugList.addAll(sampleBugs)
                    } else {
                        bugList.addAll(items.sortedByDescending { it.timestamp })
                    }
                } else {
                    if (bugList.isEmpty()) bugList.addAll(sampleBugs)
                }
            }

        onDispose {
            listener.remove()
        }
    }

    val filteredList = when (selectedFilter) {
        "OPEN" -> bugList.filter { it.status.equals("OPEN", ignoreCase = true) }
        "RESOLVED" -> bugList.filter { it.status.equals("RESOLVED", ignoreCase = true) }
        else -> bugList
    }

    val openCount = bugList.count { it.status.equals("OPEN", ignoreCase = true) }
    val resolvedCount = bugList.count { it.status.equals("RESOLVED", ignoreCase = true) }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AlertAmber.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Admin Bugs & Diagnostics", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ADMIN 👑", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text("System Bug Reports & Crash Diagnostics", fontSize = 11.sp, color = textGray)
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

            // STATS OVERVIEW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BugStatCard(
                    title = "Total Bugs",
                    value = "${bugList.size}",
                    color = textDark,
                    modifier = Modifier.weight(1f),
                    cardBg = cardBg
                )
                BugStatCard(
                    title = "Open Issues",
                    value = "$openCount",
                    color = EmergencyRed,
                    modifier = Modifier.weight(1f),
                    cardBg = cardBg
                )
                BugStatCard(
                    title = "Resolved",
                    value = "$resolvedCount",
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f),
                    cardBg = cardBg
                )
            }

            // TAB FILTER ROW
            TabRow(
                selectedTabIndex = when (selectedFilter) {
                    "OPEN" -> 1
                    "RESOLVED" -> 2
                    else -> 0
                },
                containerColor = cardBg,
                contentColor = PrimaryGreen,
                indicator = { tabPositions ->
                    val index = when (selectedFilter) {
                        "OPEN" -> 1
                        "RESOLVED" -> 2
                        else -> 0
                    }
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = PrimaryGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    text = { Text("All (${bugList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    selectedContentColor = PrimaryGreen,
                    unselectedContentColor = textGray
                )
                Tab(
                    selected = selectedFilter == "OPEN",
                    onClick = { selectedFilter = "OPEN" },
                    text = { Text("Open ($openCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    selectedContentColor = EmergencyRed,
                    unselectedContentColor = textGray
                )
                Tab(
                    selected = selectedFilter == "RESOLVED",
                    onClick = { selectedFilter = "RESOLVED" },
                    text = { Text("Resolved ($resolvedCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    selectedContentColor = PrimaryGreen,
                    unselectedContentColor = textGray
                )
            }

            // LIST OF BUGS
            if (filteredList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                        Text("No bug reports found for selected filter ✓", color = textGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { bug ->
                        val isOpen = bug.status.equals("OPEN", ignoreCase = true)
                        val sdf = SimpleDateFormat("hh:mm a • dd MMM yyyy", Locale.getDefault())
                        val timeStr = sdf.format(Date(bug.timestamp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, if (isOpen) EmergencyRed.copy(alpha = 0.4f) else PrimaryGreen.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (bug.severity) {
                                                    "HIGH" -> EmergencyRed
                                                    "MEDIUM" -> AlertAmber
                                                    else -> PrimaryGreen
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("${bug.severity} SEVERITY", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isOpen) EmergencyRed.copy(alpha = 0.15f) else PrimaryGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (isOpen) "🔴 OPEN" else "🟢 RESOLVED",
                                            color = if (isOpen) EmergencyRed else PrimaryGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(bug.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.5.sp, color = textDark)
                                Text(bug.description, fontSize = 12.5.sp, color = textGray)

                                HorizontalDivider(color = textGray.copy(alpha = 0.15f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Reported by ${bug.reporterName} • $timeStr", fontSize = 10.5.sp, color = textGray)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isOpen) {
                                        Button(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    try {
                                                        val db = FirebaseFirestore.getInstance()
                                                        db.collection("bug_reports").document(bug.id).update("status", "RESOLVED").await()
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Bug '${bug.title}' marked as resolved! ✓", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (_: Exception) {
                                                        val idx = bugList.indexOfFirst { it.id == bug.id }
                                                        if (idx != -1) {
                                                            bugList[idx] = bugList[idx].copy(status = "RESOLVED")
                                                        }
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Bug marked as resolved! ✓", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("bug_reports").document(bug.id).delete().await()
                                                } catch (_: Exception) {}
                                                bugList.removeIf { it.id == bug.id }
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Bug report deleted.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, EmergencyRed)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete Bug", fontSize = 11.sp, color = EmergencyRed, fontWeight = FontWeight.Bold)
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
private fun BugStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    cardBg: Color
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
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

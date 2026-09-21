package com.example.sosjibon.ui.admin

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.elibrary.location.ActiveSosAlert
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryGreen = Color(0xFF159A6C)
private val EmergencyRed = Color(0xFFD92D20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSosHistoryScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "LIVE", "SOLVED"
    val sosHistoryList = remember { mutableStateListOf<ActiveSosAlert>() }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    // Real-Time Atomic Merger for Both sos_history_records & active_sos_alerts
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val historyMap = HashMap<String, ActiveSosAlert>()
        val activeMap = HashMap<String, ActiveSosAlert>()
        val activeSet = HashSet<String>()

        fun rebuildList() {
            val combined = HashMap<String, ActiveSosAlert>()

            // 1. Process history items, using activeSet presence as source of truth for live vs solved
            historyMap.forEach { (id, alert) ->
                val isCurrentlyActive = activeSet.contains(id)
                val finalStatus = if (isCurrentlyActive) "LIVE" else "SOLVED"
                val finalIsActive = isCurrentlyActive
                combined[id] = alert.copy(
                    status = finalStatus,
                    isActive = finalIsActive
                )
            }

            // 2. Add active items that haven't landed in historyMap yet
            activeMap.forEach { (id, alert) ->
                if (!combined.containsKey(id)) {
                    combined[id] = alert.copy(status = "LIVE", isActive = true)
                } else {
                    combined[id] = combined[id]!!.copy(status = "LIVE", isActive = true)
                }
            }

            val sorted = combined.values.sortedByDescending { it.timestamp }
            sosHistoryList.clear()
            sosHistoryList.addAll(sorted)
        }

        val l1 = db.collection("sos_history_records")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    historyMap.clear()
                    snapshot.documents.forEach { doc ->
                        doc.toObject(ActiveSosAlert::class.java)?.copy(id = doc.id)?.let { alert ->
                            historyMap[doc.id] = alert
                        }
                    }
                    rebuildList()
                }
            }

        val l2 = db.collection("active_sos_alerts")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    activeMap.clear()
                    activeSet.clear()
                    snapshot.documents.forEach { doc ->
                        activeSet.add(doc.id)
                        doc.toObject(ActiveSosAlert::class.java)?.copy(id = doc.id, isActive = true, status = "LIVE")?.let { alert ->
                            activeMap[doc.id] = alert
                        }
                    }
                    rebuildList()
                }
            }

        onDispose {
            l1.remove()
            l2.remove()
        }
    }

    val filteredList = sosHistoryList.filter { item ->
        val isLive = item.status.equals("LIVE", ignoreCase = true) || item.isActive
        val matchesFilter = when (selectedFilter) {
            "LIVE" -> isLive
            "SOLVED" -> !isLive
            else -> true
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            val q = searchQuery.trim().lowercase()
            item.userName.lowercase().contains(q) ||
                    item.userPhone.lowercase().contains(q) ||
                    item.locationName.lowercase().contains(q) ||
                    item.status.lowercase().contains(q)
        }

        matchesFilter && matchesSearch
    }

    val liveCount = sosHistoryList.count { it.status.equals("LIVE", ignoreCase = true) || it.isActive }
    val solvedCount = sosHistoryList.count { !it.status.equals("LIVE", ignoreCase = true) && !it.isActive }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(EmergencyRed.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Admin SOS History Audit", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ADMIN 👑", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text("Live & Solved Emergency Records Audit", fontSize = 11.sp, color = textGray)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by victim name, phone, location...", fontSize = 12.5.sp, color = textGray) },
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

            // FILTER CHIPS & COUNTERS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Filter SOS Status Logs", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textDark)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${sosHistoryList.size})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White)
                        )

                        FilterChip(
                            selected = selectedFilter == "LIVE",
                            onClick = { selectedFilter = "LIVE" },
                            label = { Text("🔴 Live ($liveCount)") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmergencyRed, selectedLabelColor = Color.White)
                        )

                        FilterChip(
                            selected = selectedFilter == "SOLVED",
                            onClick = { selectedFilter = "SOLVED" },
                            label = { Text("🟢 Solved ($solvedCount)") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            // EMERGENCY LOGS LIST
            if (filteredList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                        Text("No emergency SOS records found.", color = textGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { item ->
                        val isLive = item.status.equals("LIVE", ignoreCase = true) || item.isActive
                        val sdf = SimpleDateFormat("hh:mm:ss a • dd MMM yyyy", Locale.getDefault())
                        val timeStr = sdf.format(Date(item.timestamp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.5.dp, if (isLive) EmergencyRed else Color(0xFF2E7D32)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.userName.ifBlank { "Emergency Victim" },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (isLive) EmergencyRed else Color(0xFF2E7D32)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isLive) EmergencyRed else Color(0xFF2E7D32))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isLive) "🔴 LIVE EMERGENCY" else "🟢 SOLVED & SAFE",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                HorizontalDivider(color = textGray.copy(alpha = 0.15f))

                                // DETAILED INFOS FOR EVERY SOS
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Location: ${item.locationName.ifBlank { "Location Detecting" }}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textDark
                                        )
                                    }

                                    Text(
                                        text = "GPS Coordinates: ${item.latitude}, ${item.longitude} (${item.accuracy})",
                                        fontSize = 12.sp,
                                        color = textGray,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (item.userPhone.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Phone: ${item.userPhone}", fontSize = 12.sp, color = textDark)
                                        }
                                    }

                                    Text(
                                        text = "Broadcast Time: $timeStr",
                                        fontSize = 11.sp,
                                        color = textGray
                                    )
                                }

                                HorizontalDivider(color = textGray.copy(alpha = 0.15f))

                                // ACTION BUTTONS
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (item.userPhone.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.userPhone}"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, PrimaryGreen)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call Victim", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val uri = Uri.parse("geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}(${Uri.encode(item.locationName)})")
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, PrimaryGreen)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open Map", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                    }

                                    if (isLive) {
                                        Button(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    try {
                                                        val db = FirebaseFirestore.getInstance()
                                                        db.collection("active_sos_alerts").document(item.id).delete().await()
                                                        db.collection("sos_history_records").document(item.id).set(
                                                            mapOf(
                                                                "id" to item.id,
                                                                "userName" to item.userName,
                                                                "userPhone" to item.userPhone,
                                                                "locationName" to item.locationName,
                                                                "latitude" to item.latitude,
                                                                "longitude" to item.longitude,
                                                                "accuracy" to item.accuracy,
                                                                "timestamp" to item.timestamp,
                                                                "isActive" to false,
                                                                "status" to "SOLVED",
                                                                "resolvedAt" to System.currentTimeMillis()
                                                            ),
                                                            SetOptions.merge()
                                                        ).await()
                                                    } catch (_: Exception) {}

                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Emergency for ${item.userName} marked Solved & Safe! ✓", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Safe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("active_sos_alerts").document(item.id).delete().await()
                                                    db.collection("sos_history_records").document(item.id).delete().await()
                                                } catch (_: Exception) {}

                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "SOS Log Deleted.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed, modifier = Modifier.size(18.dp))
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

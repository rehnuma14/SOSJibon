package com.example.sosjibon.ui.admin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sosjibon.data.content.CommunityStory
import com.example.sosjibon.elibrary.location.ActiveSosAlert
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryGreen = Color(0xFF159A6C)
private val AlertAmber = Color(0xFFD97706)
private val EmergencyRed = Color(0xFFD92D20)

private const val GEOAPIFY_API_KEY = "f29d6b57297b4e16aaafc6668a7e7ae8"
private const val GEOAPIFY_STYLE_URL = "https://maps.geoapify.com/v1/styles/osm-bright/style.json?apiKey=$GEOAPIFY_API_KEY"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit = {},
    onNavigateToBugs: () -> Unit = {},
    onNavigateToSosHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isMapExpanded by remember { mutableStateOf(false) }

    val pendingStories = remember { mutableStateListOf<CommunityStory>() }
    val activeSosList = remember { mutableStateListOf<ActiveSosAlert>() }
    val userCountState = remember { mutableIntStateOf(0) }
    val storyPermitCountState = remember { mutableIntStateOf(0) }
    val activeSosCountState = remember { mutableIntStateOf(0) }
    val bugCountState = remember { mutableIntStateOf(0) }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    // Real-Time Listeners for Firestore Data
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        // 1. Registered Users Count
        val l1 = db.collection("users")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    userCountState.intValue = snapshot.size()
                }
            }

        // 2. Pending Story Permits List & Count
        val l2 = db.collection("pending_community_stories")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    pendingStories.clear()
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(CommunityStory::class.java)?.copy(id = doc.id)
                    }
                    pendingStories.addAll(items)
                    storyPermitCountState.intValue = items.size
                }
            }

        // 3. Active SOS Alerts Count & Real-time List
        val l3 = db.collection("active_sos_alerts")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    activeSosList.clear()
                    val alerts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ActiveSosAlert::class.java)?.copy(id = doc.id)
                    }
                    val activeItems = alerts.filter { it.isActive }
                    activeSosList.addAll(activeItems)
                    activeSosCountState.intValue = activeItems.size
                }
            }

        // 4. Bug Reports Count
        val l4 = db.collection("bug_reports")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    bugCountState.intValue = snapshot.size()
                }
            }

        onDispose {
            l1.remove()
            l2.remove()
            l3.remove()
            l4.remove()
        }
    }

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
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Admin Dashboard",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = textDark
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ADMIN 👑",
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Text(
                                text = "Realtime System Analytics, Live SOS Map & Story Permits",
                                fontSize = 11.sp,
                                color = textGray
                            )
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

            // 1. PRIMARY SYSTEM METRICS (2x2 GRID)
            Text(
                text = "Primary Platform Metrics",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Registered Users",
                        value = "${userCountState.intValue}",
                        subtitle = "Active User Accounts",
                        icon = Icons.Default.People,
                        color = PrimaryGreen,
                        modifier = Modifier.weight(1f).clickable { onNavigateToUsers() },
                        cardBg = cardBg,
                        textDark = textDark,
                        textGray = textGray
                    )

                    MetricCard(
                        title = "Pending Permits",
                        value = "${storyPermitCountState.intValue}",
                        subtitle = "Drafts Awaiting Review",
                        icon = Icons.Default.HourglassTop,
                        color = AlertAmber,
                        modifier = Modifier.weight(1f),
                        cardBg = cardBg,
                        textDark = textDark,
                        textGray = textGray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Active SOS Alerts",
                        value = "${activeSosCountState.intValue}",
                        subtitle = "Live GPS Emergency Signals",
                        icon = Icons.Default.Warning,
                        color = EmergencyRed,
                        modifier = Modifier.weight(1f).clickable { onNavigateToSosHistory() },
                        cardBg = cardBg,
                        textDark = textDark,
                        textGray = textGray
                    )

                    MetricCard(
                        title = "System Bug Reports",
                        value = "${bugCountState.intValue}",
                        subtitle = "Open Diagnostics & Issues",
                        icon = Icons.Default.BugReport,
                        color = AlertAmber,
                        modifier = Modifier.weight(1f).clickable { onNavigateToBugs() },
                        cardBg = cardBg,
                        textDark = textDark,
                        textGray = textGray
                    )
                }
            }

            // 2. EXPANDABLE LIVE GPS SOS MAP MONITOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Active SOS Map (${activeSosList.size})",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                }

                Row(
                    modifier = Modifier
                        .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .clickable { isMapExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.OpenInFull, contentDescription = "Expand Map", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expand Map ⤢",
                        fontSize = 12.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { isMapExpanded = true },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, EmergencyRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            try {
                                MapLibre.getInstance(ctx)
                            } catch (_: Exception) {}

                            MapView(ctx).apply {
                                onCreate(null)
                                onStart()
                                onResume()

                                getMapAsync { map ->
                                    try {
                                        map.setStyle(GEOAPIFY_STYLE_URL) { style ->
                                            map.clear()
                                            var defaultPoint = LatLng(23.8103, 90.4125)

                                            activeSosList.forEach { sos ->
                                                val lat = if (sos.latitude != 0.0) sos.latitude else 23.8103
                                                val lng = if (sos.longitude != 0.0) sos.longitude else 90.4125
                                                val pos = LatLng(lat, lng)
                                                defaultPoint = pos

                                                map.addMarker(
                                                    MarkerOptions()
                                                        .position(pos)
                                                        .title("🚨 SOS: ${sos.userName.ifBlank { "Victim" }}")
                                                        .snippet("${sos.locationName.ifBlank { "GPS Alert" }} • ${sos.userPhone}")
                                                )
                                            }

                                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultPoint, 10.0))
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // OVERLAY BANNER WITH EXPAND PROMPT
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activeSosList.isNotEmpty()) "🔴 ${activeSosList.size} Active GPS SOS Alerts Pinned" else "🟢 No Active SOS Alerts",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Tap to Expand ⤢",
                                color = PrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // 3. COMMUNITY STORIES PERMIT REVIEW SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Community Stories Permits (${pendingStories.size})",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                    Text(
                        text = "Review user-submitted story drafts and grant publication permits",
                        fontSize = 11.5.sp,
                        color = textGray
                    )
                }
            }

            if (pendingStories.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No pending stories waiting for review ✓ All permits processed", color = textGray, fontSize = 12.5.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingStories.forEach { story ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, AlertAmber.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(story.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertAmber)
                                    Text("By ${story.authorName.ifBlank { "Community Member" }}", fontSize = 11.5.sp, color = textGray)
                                }

                                Text(story.title, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                Text(story.excerpt, fontSize = 12.5.sp, color = textGray, maxLines = 3)

                                HorizontalDivider(color = textGray.copy(alpha = 0.15f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    val approved = story.copy(status = "published")
                                                    db.collection("community_stories").document(approved.id).set(approved).await()
                                                    db.collection("pending_community_stories").document(approved.id).delete().await()
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Story '${story.title}' permit granted & published! ✓", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Grant Permit & Publish", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("pending_community_stories").document(story.id).delete().await()
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Story rejected & permit denied.", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, EmergencyRed)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 11.5.sp, color = EmergencyRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. ADMIN SHORTCUTS CONTAINER
            Text(
                text = "Admin Control Navigation",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToUsers() },
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
                            .size(44.dp)
                            .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Users Directory & Records", fontWeight = FontWeight.Bold, color = textDark, fontSize = 14.5.sp)
                        Text("Inspect registered users, blood groups & grant verification permits", color = textGray, fontSize = 11.5.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = PrimaryGreen)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBugs() },
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
                            .size(44.dp)
                            .background(AlertAmber.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = AlertAmber)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Bug Diagnostics", fontWeight = FontWeight.Bold, color = textDark, fontSize = 14.5.sp)
                        Text("Review app bug reports, crash logs & resolve issues", color = textGray, fontSize = 11.5.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = PrimaryGreen)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSosHistory() },
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
                            .size(44.dp)
                            .background(EmergencyRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SOS Emergency History & Logs", fontWeight = FontWeight.Bold, color = textDark, fontSize = 14.5.sp)
                        Text("Monitor active emergency alerts, GPS locations & historical logs", color = textGray, fontSize = 11.5.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = PrimaryGreen)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSettings() },
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
                            .size(44.dp)
                            .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Admin Settings & Security", fontWeight = FontWeight.Bold, color = textDark, fontSize = 14.5.sp)
                        Text("Master security options, developer team & protected account", color = textGray, fontSize = 11.5.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = PrimaryGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // FULL-SCREEN EXPANDABLE SOS MAP MODAL
    if (isMapExpanded) {
        ExpandableSosMapModal(
            activeSosList = activeSosList,
            onDismiss = { isMapExpanded = false },
            scope = scope,
            context = context
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandableSosMapModal(
    activeSosList: List<ActiveSosAlert>,
    onDismiss: () -> Unit,
    scope: CoroutineScope,
    context: Context
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val pageBg = MaterialTheme.colorScheme.background
        val textDark = MaterialTheme.colorScheme.onBackground
        val textGray = MaterialTheme.colorScheme.onSurfaceVariant
        val cardBg = MaterialTheme.colorScheme.surface

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
                                Icon(Icons.Default.Map, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🚨 Live GPS SOS Map (Full Screen)", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = textDark)
                                Text("${activeSosList.size} Active GPS Emergency Signals Pinpoint", fontSize = 11.sp, color = EmergencyRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close Map", tint = EmergencyRed)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // FULL SCREEN MAP
                AndroidView(
                    factory = { ctx ->
                        try {
                            MapLibre.getInstance(ctx)
                        } catch (_: Exception) {}

                        MapView(ctx).apply {
                            onCreate(null)
                            onStart()
                            onResume()

                            getMapAsync { map ->
                                try {
                                    map.setStyle(GEOAPIFY_STYLE_URL) { style ->
                                        map.clear()
                                        var defaultPoint = LatLng(23.8103, 90.4125)

                                        activeSosList.forEach { sos ->
                                            val lat = if (sos.latitude != 0.0) sos.latitude else 23.8103
                                            val lng = if (sos.longitude != 0.0) sos.longitude else 90.4125
                                            val pos = LatLng(lat, lng)
                                            defaultPoint = pos

                                            map.addMarker(
                                                MarkerOptions()
                                                    .position(pos)
                                                    .title("🚨 SOS: ${sos.userName.ifBlank { "Victim" }}")
                                                    .snippet("${sos.locationName.ifBlank { "GPS Alert" }} • ${sos.userPhone}")
                                            )
                                        }

                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultPoint, 11.0))
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // FLOATING BOTTOM DRAWER WITH LIVE ALERTS
                if (activeSosList.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.95f)),
                        border = BorderStroke(1.5.dp, EmergencyRed),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔴 Live Active SOS Calls (${activeSosList.size})",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = EmergencyRed
                            )

                            activeSosList.take(2).forEach { sos ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sos.userName.ifBlank { "Victim" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textDark)
                                        Text("${sos.locationName.ifBlank { "GPS Active" }} • ${sos.userPhone}", fontSize = 11.sp, color = textGray)
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("active_sos_alerts").document(sos.id).update("isActive", false, "status", "SOLVED").await()
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "SOS Alert resolved ✓", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Resolve", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
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
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    cardBg: Color,
    textDark: Color,
    textGray: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textGray)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }

            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
            Text(subtitle, fontSize = 9.5.sp, color = textGray, fontWeight = FontWeight.Medium)
        }
    }
}

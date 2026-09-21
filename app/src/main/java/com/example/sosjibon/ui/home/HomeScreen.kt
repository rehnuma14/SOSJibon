package com.example.sosjibon.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import java.util.Date
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.sosjibon.ui.admin.AdminHomeScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import com.example.sosjibon.elibrary.location.ActiveSosAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.data.vault.DonationRecord
import com.example.sosjibon.ui.admin.AdminDashboardScreen
import com.example.sosjibon.ui.components.GuestAccessWarningCard
import com.example.sosjibon.ui.settings.SettingsViewModel
import com.example.sosjibon.ui.settings.UserProfileDetails
import com.google.firebase.auth.FirebaseAuth
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val EmergencyRed = Color(0xFFD92D20)
private val PUCFallback = LatLng(23.8103, 90.4125)
private const val GEOAPIFY_API_KEY = "f29d6b57297b4e16aaafc6668a7e7ae8"
private const val GEOAPIFY_STYLE_URL = "https://maps.geoapify.com/v1/styles/osm-bright/style.json?apiKey=$GEOAPIFY_API_KEY"

@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit = {},
    onNavigateToStories: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToBugs: () -> Unit = {},
    onNavigateToSosHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsState by settingsViewModel.state.collectAsState()
    val donationRecords by settingsViewModel.donationRecords.collectAsState()
    val profile = settingsState.profile

    val authUser = FirebaseAuth.getInstance().currentUser
    val authEmail = authUser?.email?.trim()?.lowercase() ?: ""
    val isAdmin = authUser != null && authEmail == "admin@gmail.com"

    if (isAdmin) {
        AdminDashboardScreen(
            onNavigateToUsers = onNavigateToUsers,
            onNavigateToBugs = onNavigateToBugs,
            onNavigateToSosHistory = onNavigateToSosHistory,
            onNavigateToSettings = onNavigateToSettings
        )
        return
    }

    val isGuest = !settingsState.isLoggedIn || profile.fullName == "Guest Member" || profile.email.isBlank()

    if (isGuest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GuestAccessWarningCard(
                pageName = "Home Dashboard",
                onLoginRegisterClick = onNavigateToAuth
            )
        }
        return
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val activeSosAlerts = remember { mutableStateListOf<ActiveSosAlert>() }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("active_sos_alerts")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    activeSosAlerts.clear()
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ActiveSosAlert::class.java)?.copy(id = doc.id)
                    }
                    activeSosAlerts.addAll(items)
                }
            }

        onDispose {
            listener.remove()
        }
    }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreenBg = MaterialTheme.colorScheme.primaryContainer
    val darkForestText = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface
    val pageBg = MaterialTheme.colorScheme.background

    val profileBitmap = remember(profile.imageUri) {
        if (profile.imageUri.isNotBlank()) {
            try {
                val file = File(profile.imageUri)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationManager = context.getSystemService(LocationManager::class.java)
            try {
                val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val location = gpsLocation ?: networkLocation
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                }
            } catch (_: SecurityException) { }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isWide) Modifier.widthIn(max = 840.dp).align(Alignment.TopCenter) else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // SOSJibon Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(lightGreenBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "SOSJibon",
                            tint = primaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SOSJibon Healthcare",
                            color = darkForestText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Emergency Response Engine Active",
                            color = textGray,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(lightGreenBg, RoundedCornerShape(20.dp))
                        .border(1.dp, primaryGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ready ✓", color = primaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // USER PROFILE & BLOOD DONATION ELIGIBILITY BANNER
            HomeProfileBloodDonationBar(
                profile = profile,
                profileBitmap = profileBitmap,
                donationRecords = donationRecords,
                onLogDonationRecord = { date, hospital, notes ->
                    settingsViewModel.addBloodDonationRecord(date, hospital, notes)
                },
                onUpdateDonationRecord = { record ->
                    settingsViewModel.updateBloodDonationRecord(record)
                },
                onDeleteDonationRecord = { record ->
                    settingsViewModel.deleteBloodDonationRecord(record)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LIVE COMMUNITY SOS ALERTS
            if (activeSosAlerts.isNotEmpty()) {
                ActiveSosCommunityAlertsSection(
                    alerts = activeSosAlerts,
                    onNavigateToMap = onNavigateToMap,
                    cardBg = cardBg,
                    textDark = darkForestText,
                    textGray = textGray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // MERGED EMERGENCY ASSISTANCE & LIVE GPS NAVIGATION HUB
            MergedEmergencyGpsMapHub(
                location = currentLocation ?: PUCFallback,
                activeSosAlerts = activeSosAlerts,
                onNavigateToMap = onNavigateToMap,
                cardBg = cardBg,
                primaryGreen = primaryGreen,
                textDark = darkForestText,
                textGray = textGray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Community Stories Card
            Text(
                text = "Community & Support Hub",
                color = darkForestText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            HomeCard(
                title = "Community Stories & Experiences",
                subtitle = "Read verified healthcare experiences and community responses.",
                icon = Icons.Default.Forum,
                backgroundColor = cardBg,
                iconBgColor = lightGreenBg,
                iconColor = primaryGreen,
                darkForestText = darkForestText,
                textGray = textGray,
                primaryGreen = primaryGreen,
                onClick = onNavigateToStories
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------
// USER PROFILE & BLOOD DONATION ELIGIBILITY BANNER
// ------------------------------------------------------------
@Composable
private fun HomeProfileBloodDonationBar(
    profile: UserProfileDetails,
    profileBitmap: Bitmap?,
    donationRecords: List<DonationRecord>,
    onLogDonationRecord: (String, String, String) -> Unit,
    onUpdateDonationRecord: (DonationRecord) -> Unit,
    onDeleteDonationRecord: (DonationRecord) -> Unit
) {
    val context = LocalContext.current
    var showHistoryDialog by remember { mutableStateOf(false) }

    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface
    val primaryGreen = MaterialTheme.colorScheme.primary

    // CALCULATE DAYS PASSED & DONATION ELIGIBILITY ACCORDING TO GENDER
    val lastDateStr = profile.lastDonationDate.trim()
    val isGenderFemale = profile.gender.equals("Female", ignoreCase = true)
    val requiredIntervalDays = if (isGenderFemale) 120 else 90 // Female = 120 days (4 months), Male = 90 days (3 months)

    var daysPassed by remember(lastDateStr) { mutableStateOf<Long?>(null) }
    var isEligible by remember(lastDateStr, isGenderFemale) { mutableStateOf(false) }

    LaunchedEffect(lastDateStr) {
        if (lastDateStr.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val donationTime = sdf.parse(lastDateStr)?.time ?: 0L
                val currentTime = System.currentTimeMillis()
                if (donationTime > 0) {
                    val diffMs = currentTime - donationTime
                    val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    daysPassed = days
                    isEligible = days >= requiredIntervalDays
                }
            } catch (_: Exception) {
                daysPassed = null
            }
        } else {
            daysPassed = null
            isEligible = false
        }
    }

    // Colors according to eligibility: Green if eligible, Yellow if wait period, Neutral if no date
    val boxBorderColor = when {
        lastDateStr.isBlank() -> primaryGreen.copy(alpha = 0.3f)
        isEligible -> Color(0xFF2E7D32)
        else -> Color(0xFFD97706) // Amber/Yellow
    }

    val boxBgColor = when {
        lastDateStr.isBlank() -> cardBg
        isEligible -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFFBEB)
    }

    val statusTextColor = when {
        lastDateStr.isBlank() -> textGray
        isEligible -> Color(0xFF2E7D32)
        else -> Color(0xFFB45309)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showHistoryDialog = true },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT SIDE: USER PROFILE AVATAR & USER NAME BELOW IT
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(82.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(primaryGreen.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, primaryGreen, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap.asImageBitmap(),
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = profile.fullName.ifBlank { "Guest Member" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // REMAINING PORTION: BLOOD GROUP & LAST DONATED DATE BOX
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Blood Group: ${profile.bloodGroup.ifBlank { "Not Set" }}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryGreen
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "History (${donationRecords.size}) >",
                            color = primaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // LAST DONATION DATE & ELIGIBILITY STATUS BOX
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(boxBgColor)
                        .border(1.dp, boxBorderColor, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        if (lastDateStr.isBlank()) {
                            Text(
                                text = "📅 Last Donated: Not Set",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap card to log or edit blood donation",
                                fontSize = 10.5.sp,
                                color = textGray
                            )
                        } else {
                            Text(
                                text = "📅 Last Donated: $lastDateStr (${donationRecords.size} logged)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            if (isEligible) {
                                Text(
                                    text = "🟢 Eligible to Donate Blood! ($daysPassed days passed • ${if (isGenderFemale) "4m" else "3m"} gap met)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusTextColor
                                )
                            } else {
                                val remaining = daysPassed?.let { (requiredIntervalDays - it).coerceAtLeast(1) } ?: 0
                                Text(
                                    text = "🟡 Wait Period: $remaining days left ($daysPassed days passed • ${if (isGenderFemale) "4m" else "3m"} required)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        DonationHistoryModal(
            donationRecords = donationRecords,
            onDismiss = { showHistoryDialog = false },
            onLogNew = { date, hospital, notes ->
                onLogDonationRecord(date, hospital, notes)
                showHistoryDialog = false
            },
            onUpdateRecord = { updated ->
                onUpdateDonationRecord(updated)
            },
            onDeleteRecord = { deleted ->
                onDeleteDonationRecord(deleted)
            },
            primaryGreen = primaryGreen,
            textDark = textDark,
            textGray = textGray
        )
    }
}

// ------------------------------------------------------------
// BLOOD DONATION HISTORY LOG MODAL
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationHistoryModal(
    donationRecords: List<DonationRecord>,
    onDismiss: () -> Unit,
    onLogNew: (String, String, String) -> Unit,
    onUpdateRecord: (DonationRecord) -> Unit,
    onDeleteRecord: (DonationRecord) -> Unit,
    primaryGreen: Color,
    textDark: Color,
    textGray: Color
) {
    val context = LocalContext.current
    var showAddForm by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<DonationRecord?>(null) }

    var inputDate by remember { mutableStateOf("") }
    var inputHospital by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val formattedMonth = String.format(Locale.US, "%02d", month + 1)
                val formattedDay = String.format(Locale.US, "%02d", day)
                inputDate = "$year-$formattedMonth-$formattedDay"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = primaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Blood Donation History", fontWeight = FontWeight.Bold, color = textDark)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Total Donations Logged: ${donationRecords.size}", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = primaryGreen)

                if (showAddForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, primaryGreen)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Log New Blood Donation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textDark)

                            OutlinedTextField(
                                value = inputDate.ifBlank { "Tap to pick date" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Donation Date") },
                                trailingIcon = {
                                    IconButton(onClick = { datePicker.show() }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = primaryGreen)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePicker.show() }
                            )

                            OutlinedTextField(
                                value = inputHospital,
                                onValueChange = { inputHospital = it },
                                label = { Text("Hospital / Blood Bank") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = inputNotes,
                                onValueChange = { inputNotes = it },
                                label = { Text("Notes / Recipient") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                enabled = inputDate.isNotBlank(),
                                onClick = {
                                    onLogNew(inputDate, inputHospital, inputNotes)
                                    showAddForm = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                            ) {
                                Text("Save Donation Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log New Blood Donation Record", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }

                HorizontalDivider(color = textGray.copy(alpha = 0.2f))

                if (donationRecords.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No past donation records logged yet.", color = textGray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(donationRecords) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(record.locationOrHospital, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textDark)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(record.donationDate, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = { editingRecord = record },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Record", tint = primaryGreen, modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = { onDeleteRecord(record) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Record", tint = Color(0xFFD92D20), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                    if (record.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(record.notes, fontSize = 11.sp, color = textGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = primaryGreen, fontWeight = FontWeight.Bold)
            }
        }
    )

    // EDIT RECORD MODAL DIALOG
    editingRecord?.let { recordToEdit ->
        var editDate by remember { mutableStateOf(recordToEdit.donationDate) }
        var editHospital by remember { mutableStateOf(recordToEdit.locationOrHospital) }
        var editNotes by remember { mutableStateOf(recordToEdit.notes) }

        val editDatePicker = remember {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val formattedMonth = String.format(Locale.US, "%02d", month + 1)
                    val formattedDay = String.format(Locale.US, "%02d", day)
                    editDate = "$year-$formattedMonth-$formattedDay"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        AlertDialog(
            onDismissRequest = { editingRecord = null },
            title = { Text("Edit Donation Record", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Donation Date") },
                        trailingIcon = {
                            IconButton(onClick = { editDatePicker.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = primaryGreen)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editDatePicker.show() }
                    )

                    OutlinedTextField(
                        value = editHospital,
                        onValueChange = { editHospital = it },
                        label = { Text("Hospital / Blood Bank") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes / Recipient") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateRecord(
                            recordToEdit.copy(
                                donationDate = editDate,
                                locationOrHospital = editHospital,
                                notes = editNotes
                            )
                        )
                        editingRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecord = null }) {
                    Text("Cancel", color = textGray)
                }
            }
        )
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun MergedEmergencyGpsMapHub(
    location: LatLng,
    activeSosAlerts: List<ActiveSosAlert> = emptyList(),
    onNavigateToMap: () -> Unit,
    cardBg: Color,
    primaryGreen: Color,
    textDark: Color,
    textGray: Color
) {
    val context = LocalContext.current
    remember {
        MapLibre.getInstance(context)
        true
    }

    var mapLibreMap by remember {
        mutableStateOf<MapLibreMap?>(null)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.5.dp, EmergencyRed.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // TOP INTEGRATED EMERGENCY ASSISTANCE BANNER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(EmergencyRed.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "SOS Emergency",
                            tint = EmergencyRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Emergency Assistance & Map",
                                color = textDark,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (activeSosAlerts.isNotEmpty()) "🚨 ${activeSosAlerts.size} Live SOS Alert(s) Active on Map!" else "Locate nearby hospitals & broadcast GPS SOS",
                            color = if (activeSosAlerts.isNotEmpty()) EmergencyRed else textGray,
                            fontSize = 11.sp,
                            fontWeight = if (activeSosAlerts.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToMap,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, primaryGreen),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expand Map", fontSize = 11.5.sp, color = primaryGreen, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = primaryGreen.copy(alpha = 0.15f))

            // MAP CANVAS BOX WITH OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                mapLibreMap = map
                                map.setStyle(GEOAPIFY_STYLE_URL)
                                map.cameraPosition = CameraPosition.Builder().target(location).zoom(14.0).build()
                            }
                        }
                    },
                    update = {
                        val map = mapLibreMap
                        if (map != null) {
                            map.cameraPosition = CameraPosition.Builder().target(location).zoom(14.0).build()
                            map.clear()
                            activeSosAlerts.forEach { alert ->
                                if (alert.latitude != 0.0 && alert.longitude != 0.0) {
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(alert.latitude, alert.longitude))
                                            .title("🚨 LIVE SOS: ${alert.userName}")
                                            .snippet(alert.locationName)
                                    )
                                }
                            }
                        }
                    }
                )

                // Transparent Click Layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onNavigateToMap() }
                )

                // Bottom Map Status Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xE617332A))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (activeSosAlerts.isNotEmpty()) EmergencyRed else primaryGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (activeSosAlerts.isNotEmpty()) "🚨 ${activeSosAlerts.size} LIVE SOS ALERT(S) ACTIVE • Tap to view routes" else "Live GPS Active • Tap to expand map & routes",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSosCommunityAlertsSection(
    alerts: List<ActiveSosAlert>,
    onNavigateToMap: () -> Unit,
    cardBg: Color,
    textDark: Color,
    textGray: Color
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFD92D20), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Community SOS Alerts (${alerts.size})",
                    color = Color(0xFFD92D20),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        alerts.forEach { alert ->
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeStr = sdf.format(Date(alert.timestamp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                border = BorderStroke(1.5.dp, Color(0xFFD92D20)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alert.userName.ifBlank { "Community Member" },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFFD92D20)
                        )

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD92D20), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔴 LIVE SOS • $timeStr",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFD92D20).copy(alpha = 0.2f))

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "📍 Location: ${alert.locationName.ifBlank { "Nearby Location" }}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                        Text(
                            text = "GPS: ${alert.latitude}, ${alert.longitude} (${alert.accuracy})",
                            fontSize = 11.5.sp,
                            color = textGray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (alert.userPhone.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${alert.userPhone}"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92D20))
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Victim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToMap,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF159A6C))
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF159A6C), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Map", fontSize = 11.sp, color = Color(0xFF159A6C), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconBgColor: Color,
    iconColor: Color,
    darkForestText: Color,
    textGray: Color,
    primaryGreen: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.3f)),
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
                    .size(50.dp)
                    .background(color = iconBgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = darkForestText,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = textGray,
                    fontSize = 11.5.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = primaryGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

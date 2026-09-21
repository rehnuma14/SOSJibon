package com.example.sosjibon.ui.emergency

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sosjibon.R
import com.example.sosjibon.elibrary.location.EmergencyLocationHelper
import com.example.sosjibon.ui.theme.SOSJIBONTheme
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val EmergencyRed = Color(0xFFD92D20)
private val PrimaryGreen = Color(0xFF159A6C)
private val AlertAmber = Color(0xFFD97706)
private val SoftYellow = Color(0xFFFFFBEB)

private const val GEOAPIFY_API_KEY = "f29d6b57297b4e16aaafc6668a7e7ae8"
private const val GEOAPIFY_STYLE_URL = "https://maps.geoapify.com/v1/styles/osm-bright/style.json?apiKey=$GEOAPIFY_API_KEY"
private val PUCFallback = LatLng(23.8103, 90.4125)

// ------------------------------------------------------------
// SOS LANGUAGE MODEL
// ------------------------------------------------------------
enum class SosLanguage(val displayName: String) {
    ENGLISH("English"),
    BANGLA("বাংলা (Bangla)")
}

// ------------------------------------------------------------
// Crisis State Machine
// ------------------------------------------------------------
private sealed class CrisisState {
    object Selection : CrisisState()
    object SymptomTriage : CrisisState()
    data class ActiveProtocol(val condition: EmergencyProtocol) : CrisisState()
}

data class StepDetail(
    val stepNumber: Int,
    val titleEn: String,
    val titleBn: String,
    val instructionEn: String,
    val instructionBn: String,
    val imageRes: Int
)

data class EmergencyProtocol(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val themeColor: Color,
    val isCritical: Boolean,
    val stepDetails: List<StepDetail>,
    val requiresCprBeat: Boolean = false
)

private val sampleProtocols = listOf(
    EmergencyProtocol(
        id = "cpr",
        titleEn = "Not Breathing / Unconscious",
        titleBn = "শ্বাসহীন / অচেতন",
        subtitleEn = "Start CPR & Chest Compressions",
        subtitleBn = "সিপিআর ও বুকে চাপ শুরু করুন",
        icon = Icons.Default.Favorite,
        themeColor = EmergencyRed,
        isCritical = true,
        stepDetails = listOf(
            StepDetail(
                stepNumber = 1,
                titleEn = "Call Emergency & Check Response",
                titleBn = "৯৯৯ কল করুন ও সাড়া পরীক্ষা করুন",
                instructionEn = "Call 999 or ask a bystander to call immediately. Tap shoulder firmly and shout 'Are you okay?'.",
                instructionBn = "৯৯৯ কল করুন অথবা কাউকে অবিলম্বে কল করতে বলুন। ব্যক্তিটির কাঁধে শক্ত করে ধাক্কা দিয়ে জিজ্ঞেস করুন 'আপনি কি ঠিক আছেন?'।",
                imageRes = R.drawable.ic_cpr
            ),
            StepDetail(
                stepNumber = 2,
                titleEn = "Hand Placement on Center of Chest",
                titleBn = "বুকের ঠিক মাঝে হাত রাখুন",
                instructionEn = "Place heel of one hand in center of chest. Interlock fingers of other hand over it.",
                instructionBn = "বুকের ঠিক মাঝে এক হাতের তালু রাখুন। অন্য হাতের আঙুল দিয়ে তা পেঁচিয়ে ধরুন।",
                imageRes = R.drawable.ic_cpr
            ),
            StepDetail(
                stepNumber = 3,
                titleEn = "Push Hard & Fast (100–120 bpm)",
                titleBn = "শক্ত ও দ্রুত বুকে চাপ দিন (১০০-১২০ বার)",
                instructionEn = "Keep arms straight and push down 2 inches into chest. Push continuously at 100-120 bpm.",
                instructionBn = "কনুই সোজা রেখে দ্রুত ও শক্তভাবে ২ ইঞ্চি ডেবে বুকে চাপ দিন। মিনিটে ১০০ থেকে ১২০ বার চাপ প্রয়োগ করুন।",
                imageRes = R.drawable.ic_cpr
            ),
            StepDetail(
                stepNumber = 4,
                titleEn = "Allow Full Chest Recoil",
                titleBn = "বুক সম্পূর্ণ উঠতে দিন",
                instructionEn = "Allow chest to recoil fully between push cycles. Continue until emergency services arrive.",
                instructionBn = "প্রতিটি চাপের পর বুক সম্পূর্ণ উঠতে দিন। অ্যাম্বুলেন্স না আসা পর্যন্ত চালিয়ে যান।",
                imageRes = R.drawable.ic_cpr
            )
        ),
        requiresCprBeat = true
    ),
    EmergencyProtocol(
        id = "bleeding",
        titleEn = "Severe Bleeding",
        titleBn = "প্রচুর রক্তপাত",
        subtitleEn = "Apply Direct Pressure & Tourniquet",
        subtitleBn = "সরাসরি চাপ ও ব্যান্ডেজ প্রয়োগ",
        icon = Icons.Default.Warning,
        themeColor = EmergencyRed,
        isCritical = true,
        stepDetails = listOf(
            StepDetail(
                stepNumber = 1,
                titleEn = "Apply Firm Direct Pressure",
                titleBn = "সরাসরি শক্তভাবে চাপ দিন",
                instructionEn = "Place a clean cloth or bandage directly on wound and push down with firm, steady pressure.",
                instructionBn = "ক্ষতস্থানের ওপর পরিষ্কার কাপড় রেখে দুই হাতে শক্ত করে চাপ দিয়ে ধরে রাখুন।",
                imageRes = R.drawable.ic_bleeding
            ),
            StepDetail(
                stepNumber = 2,
                titleEn = "Do Not Remove Cloth Layers",
                titleBn = "কাপড় তুলে ফেলবেন না",
                instructionEn = "If blood soaks through, do NOT remove first cloth. Place additional layers directly on top.",
                instructionBn = "কাপড় রক্তে ভিজে গেলেও তা তুলবেন না। উপরে আরও কাপড় রেখে চেপে ধরুন।",
                imageRes = R.drawable.ic_bleeding
            ),
            StepDetail(
                stepNumber = 3,
                titleEn = "Elevate & Apply Tourniquet",
                titleBn = "টার্নিকেট বা শক্ত ব্যান্ডেজ বাঁধুন",
                instructionEn = "If bleeding on a limb does not stop, apply a tourniquet 2 inches above the wound.",
                instructionBn = "হাত বা পায়ে রক্তপাত না থামলে ক্ষত থেকে ২ ইঞ্চি উপরে শক্ত করে টার্নিকেট বাঁধুন।",
                imageRes = R.drawable.ic_bleeding
            )
        )
    ),
    EmergencyProtocol(
        id = "choking",
        titleEn = "Choking / Airway",
        titleBn = "শ্বাসরোধ / গলায় আটকানো",
        subtitleEn = "Heimlich & Abdominal Thrusts",
        subtitleBn = "পিঠে চাপ ও হাইমলিচ ম্যানুভার",
        icon = Icons.Default.MedicalServices,
        themeColor = AlertAmber,
        isCritical = true,
        stepDetails = listOf(
            StepDetail(
                stepNumber = 1,
                titleEn = "5 Sharp Back Blows",
                titleBn = "পিঠে ৫ বার জোরে চাপ দিন",
                instructionEn = "Stand behind person, lean them forward, and give 5 sharp back blows between shoulder blades.",
                instructionBn = "ব্যক্তিকে সামনের দিকে ঝুঁকিয়ে পিঠে দুই কাঁধের মাঝে ৫ বার জোরে চাপ দিন।",
                imageRes = R.drawable.ic_choking
            ),
            StepDetail(
                stepNumber = 2,
                titleEn = "5 Abdominal Thrusts (Heimlich)",
                titleBn = "পেটে ৫ বার হাইমলিচ চাপ দিন",
                instructionEn = "Make a fist above navel. Grasp fist with other hand and pull sharply inwards and upwards 5 times.",
                instructionBn = "পেটের ওপর নাভির ঠিক উপরে মুষ্টিবদ্ধ হাত রেখে ৫ বার উপরের দিকে সজোরে টানুন।",
                imageRes = R.drawable.ic_choking
            ),
            StepDetail(
                stepNumber = 3,
                titleEn = "Repeat Cycles Until Cleared",
                titleBn = "আটকানো বস্তু বের হওয়া পর্যন্ত চালিয়ে যান",
                instructionEn = "Alternate 5 back blows and 5 abdominal thrusts until object clears or person collapses.",
                instructionBn = "আটকানো বস্তুটি বের না হওয়া পর্যন্ত পর্যায়ক্রমে পিঠে ও পেটে চাপ দিন।",
                imageRes = R.drawable.ic_choking
            )
        )
    ),
    EmergencyProtocol(
        id = "burns",
        titleEn = "Burn / Major Injury",
        titleBn = "পোড়া / মারাত্মক আঘাত",
        subtitleEn = "Cooling Water & Sterile Cover",
        subtitleBn = "ঠান্ডা পানি ও পরিষ্কার কাপড়",
        icon = Icons.Default.ElectricBolt,
        themeColor = AlertAmber,
        isCritical = false,
        stepDetails = listOf(
            StepDetail(
                stepNumber = 1,
                titleEn = "Cool under Running Water",
                titleBn = "ঠান্ডা প্রবাহমান পানি ঢালুন",
                instructionEn = "Cool burn immediately under cool running tap water for at least 20 minutes.",
                instructionBn = "পোড়া স্থানে অন্তত ২০ মিনিট ঠান্ডা পানি ঢালুন। বরফ বা টুথপেস্ট দেবেন না।",
                imageRes = R.drawable.ic_burns
            ),
            StepDetail(
                stepNumber = 2,
                titleEn = "Cover Burn Loosely",
                titleBn = "পরিষ্কার কাপড় দিয়ে ঢেকে দিন",
                instructionEn = "Cover burn loosely with clean plastic wrap or sterile bandage. Do NOT apply ice or oil.",
                instructionBn = "পরিষ্কার পলিথিন বা ব্যান্ডেজ দিয়ে আলতো করে ঢেকে দিন।",
                imageRes = R.drawable.ic_burns
            )
        )
    ),
    EmergencyProtocol(
        id = "seizure",
        titleEn = "Seizure / Stroke",
        titleBn = "খিঁচুনি / স্ট্রোক",
        subtitleEn = "Clear Area & Recovery Position",
        subtitleBn = "আশেপাশ পরিষ্কার ও রিকভারি পজিশন",
        icon = Icons.Default.Psychology,
        themeColor = PrimaryGreen,
        isCritical = false,
        stepDetails = listOf(
            StepDetail(
                stepNumber = 1,
                titleEn = "Clear Surrounding Hazards",
                titleBn = "আশেপাশের ধারালো বস্তু সরিয়ে দিন",
                instructionEn = "Clear hard or sharp objects away. Cushion head with clothes. Do NOT restrain or put items in mouth.",
                instructionBn = "আশেপাশের শক্ত বা ধারালো জিনিস সরিয়ে দিন। মাথায় নরম কাপড় দিন। মুখে কিছু দেবেন না।",
                imageRes = R.drawable.ic_seizure
            ),
            StepDetail(
                stepNumber = 2,
                titleEn = "Roll into Recovery Position",
                titleBn = "কাত করে শুইয়ে দিন",
                instructionEn = "Once shaking stops, gently roll person onto their side into Recovery Position to keep airway open.",
                instructionBn = "খিঁচুনি থামলে শ্বাসপথ খোলা রাখতে ব্যক্তিকে আলতো করে কাত করে শুইয়ে দিন।",
                imageRes = R.drawable.ic_seizure
            )
        )
    ),
    EmergencyProtocol(
        id = "cardiac",
        titleEn = "Chest Pain / Cardiac",
        titleBn = "বুকে ব্যথা / হার্ট অ্যাটাক",
        subtitleEn = "Rest, Aspirin & Immediate Call",
        subtitleBn = "বিশ্রাম ও অবিলম্বে ৯৯৯ কল",
        icon = Icons.Default.LocalHospital,
        themeColor = EmergencyRed,
        isCritical = true,
        stepDetails = listOf(
            StepDetail(
                stepNumber = 1,
                titleEn = "Sit Down & Rest Comfortably",
                titleBn = "আরামে বসিয়ে দিন",
                instructionEn = "Have person sit down on floor leaning against wall. Loosen tight clothing around neck and waist.",
                instructionBn = "ব্যক্তিকে দেওয়ালে হেলান দিয়ে আরামে বসিয়ে দিন। গলার পোশাক আলগা করুন।",
                imageRes = R.drawable.ic_cardiac
            ),
            StepDetail(
                stepNumber = 2,
                titleEn = "Assist with Aspirin",
                titleBn = "অ্যাসপিরিন চিবিয়ে খেতে দিন",
                instructionEn = "If person is conscious and not allergic, assist with 300mg Aspirin tablet to chew slowly.",
                instructionBn = "সচেতন থাকলে এবং অ্যালার্জি না থাকলে একটি অ্যাসপিরিন ট্যাবলেট চিবিয়ে খেতে দিন।",
                imageRes = R.drawable.ic_cardiac
            )
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun EmergencySosScreen(
    onBack: () -> Unit,
    onExpandMap: () -> Unit = {}
) {
    SOSJIBONTheme(darkTheme = false) {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val scope = rememberCoroutineScope()

        var selectedLanguage by remember { mutableStateOf(SosLanguage.ENGLISH) }
        var crisisState by remember { mutableStateOf<CrisisState>(CrisisState.Selection) }
        var showExitDialog by remember { mutableStateOf(false) }
        var showDeclareSolvedDialog by remember { mutableStateOf(false) }
        var isSosBroadcastPermitted by remember { mutableStateOf(false) }
        var showSosActivationPermitDialog by remember { mutableStateOf(true) }

        // User Identity for SOS Broadcast & 2-Way Admin Coordination
        val currentUser = FirebaseAuth.getInstance().currentUser
        val prefs = remember { context.getSharedPreferences("sosjibon_profile_settings", Context.MODE_PRIVATE) }
        val userUid = currentUser?.uid ?: "sos_user_${System.currentTimeMillis()}"
        val userName = currentUser?.displayName?.ifBlank { null } ?: prefs.getString("user_name", "")?.ifBlank { null } ?: "SOS Member"
        val userPhone = prefs.getString("user_phone", "") ?: ""

        // Location State
        var locationName by remember { mutableStateOf("Detecting Location...") }
        var latLngText by remember { mutableStateOf("Fetching GPS...") }
        var accuracyText by remember { mutableStateOf("±-- m") }
        var lastFixTime by remember { mutableStateOf("Fix: --") }
        var countryCode by remember { mutableStateOf<String?>(null) }
        var emergencyNumber by remember { mutableStateOf("999") }
        var isGpsAvailable by remember { mutableStateOf(false) }
        var currentLatLng by remember { mutableStateOf(PUCFallback) }

        val pageBg = Color(0xFFF8FCFA)
        val cardBg = Color.White
        val textDark = Color(0xFF17332A)
        val textGray = Color(0xFF6B7C75)

        // REAL-TIME LISTENER FOR ACTIVE SOS ALERT STATUS FROM ADMIN / DATABASE
        DisposableEffect(userUid) {
            val db = FirebaseFirestore.getInstance()
            val listener = db.collection("active_sos_alerts").document(userUid)
                .addSnapshotListener { snapshot, _ ->
                    if (isSosBroadcastPermitted) {
                        if (snapshot == null || !snapshot.exists()) {
                            isSosBroadcastPermitted = false
                            try {
                                Toast.makeText(context, "Emergency status updated to Solved & Safe! ✓", Toast.LENGTH_LONG).show()
                            } catch (_: Exception) {}
                        }
                    }
                }

            onDispose {
                listener.remove()
            }
        }

        // KEEP SCREEN ON UNTIL USER CLOSES IT
        DisposableEffect(Unit) {
            val activity = context as? Activity
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        // Permission Launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            isGpsAvailable = granted
        }

        // Location Resolver Effect
        LaunchedEffect(Unit) {
            if (!EmergencyLocationHelper.hasLocationPermission(context)) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                isGpsAvailable = true
            }
        }

        // High-Precision Real-time Fused Location Tracking Effect
        DisposableEffect(isGpsAvailable, isSosBroadcastPermitted) {
            if (isGpsAvailable) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setMinUpdateIntervalMillis(500L)
                    .setGranularity(Granularity.GRANULARITY_FINE)
                    .build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val loc = result.lastLocation ?: return
                        currentLatLng = LatLng(loc.latitude, loc.longitude)
                        latLngText = String.format(Locale.US, "%.5f, %.5f", loc.latitude, loc.longitude)
                        accuracyText = "±${loc.accuracy.toInt()}m"
                        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                        lastFixTime = "Live Fix: ${sdf.format(Date(loc.time))}"

                        scope.launch(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                val address = addresses?.firstOrNull()
                                if (address != null) {
                                    val feature = address.featureName ?: address.thoroughfare
                                    val area = address.subLocality ?: address.locality ?: address.subAdminArea ?: "Bangladesh"
                                    val city = address.adminArea ?: address.countryName ?: "Bangladesh"
                                    val fullLocName = if (feature != null && !feature.contains(area)) "$feature, $area, $city" else "$area, $city"
                                    countryCode = address.countryCode
                                    emergencyNumber = EmergencyLocationHelper.getEmergencyNumber(countryCode)
                                    if (isSosBroadcastPermitted) {
                                        EmergencyLocationHelper.updateSosBroadcast(
                                            id = userUid,
                                            userName = userName,
                                            userPhone = userPhone,
                                            locationName = fullLocName,
                                            latitude = loc.latitude,
                                            longitude = loc.longitude,
                                            accuracy = accuracyText
                                        )
                                    }
                                    withContext(Dispatchers.Main) {
                                        locationName = fullLocName
                                    }
                                }
                            } catch (_: Exception) {
                                withContext(Dispatchers.Main) {
                                    if (locationName == "Detecting Location...") {
                                        locationName = "GPS Signal Active"
                                    }
                                }
                            }
                        }
                    }
                }

                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                } catch (_: SecurityException) { }

                onDispose {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            } else {
                onDispose { }
            }
        }

        // Lock back handler during active crisis mode
        BackHandler {
            if (crisisState is CrisisState.ActiveProtocol || crisisState is CrisisState.SymptomTriage) {
                showExitDialog = true
            } else {
                onBack()
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
                                    .size(36.dp)
                                    .background(EmergencyRed.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (selectedLanguage == SosLanguage.BANGLA) "জরুরি এসওএস" else "Emergency SOS",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = textDark
                                )
                                Text(
                                    text = if (selectedLanguage == SosLanguage.BANGLA) "লাইভ জিপিএস সম্প্রচার • ফার্স্ট এইড" else "Live GPS Broadcast • First Aid",
                                    fontSize = 11.sp,
                                    color = textGray
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (crisisState is CrisisState.ActiveProtocol || crisisState is CrisisState.SymptomTriage) {
                                    showExitDialog = true
                                } else {
                                    onBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = EmergencyRed
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(EmergencyRed)
                                .clickable { EmergencyLocationHelper.launchDialer(context, emergencyNumber) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (selectedLanguage == SosLanguage.BANGLA) "কল $emergencyNumber" else "CALL $emergencyNumber",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = pageBg,
                        titleContentColor = textDark
                    )
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
                // 1. LIVE GPS MAP WITH DANGER BEACON MARKER
                EmergencyLiveGpsMapCard(
                    locationLatLng = currentLatLng,
                    locationName = locationName,
                    latLngText = latLngText,
                    accuracyText = accuracyText,
                    emergencyNumber = emergencyNumber,
                    selectedLanguage = selectedLanguage,
                    onCopyLocation = {
                        val readOut = if (selectedLanguage == SosLanguage.BANGLA) {
                            "আমি $locationName এর কাছে আছি। জিপিএস: $latLngText ($accuracyText)।"
                        } else {
                            "I am near $locationName. Coordinates: $latLngText ($accuracyText)."
                        }
                        clipboardManager.setText(AnnotatedString(readOut))
                        Toast.makeText(context, if (selectedLanguage == SosLanguage.BANGLA) "অবস্থান কপি করা হয়েছে!" else "Location copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    onExpandMap = onExpandMap,
                    textDark = textDark,
                    textGray = textGray
                )

                // 2. LIVE SOS STATUS OR EXPLORE MODE BANNER
                if (isSosBroadcastPermitted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeclareSolvedDialog = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.5.dp, Color(0xFF2E7D32)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (selectedLanguage == SosLanguage.BANGLA) "🚨 লাইভ এসওএস সম্প্রচার চালু রয়েছে" else "🚨 LIVE SOS BROADCAST ACTIVE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = if (selectedLanguage == SosLanguage.BANGLA) "নিরাপদ চিহ্নিত করতে ও এসওএস বন্ধ করতে ট্যাপ করুন" else "Tap to declare safe & stop GPS broadcast",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (selectedLanguage == SosLanguage.BANGLA) "🔍 এক্সপ্লোর মোড (কোনো এসওএস সংকেত পাঠানো হয়নি)" else "🔍 EXPLORE MODE ACTIVE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = if (selectedLanguage == SosLanguage.BANGLA) "জরুরি নম্বর ও নির্দেশিকা দেখুন • জিপিএস নিষ্ক্রিয়" else "No live SOS alert broadcasted • First Aid ready",
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Button(
                                onClick = { showSosActivationPermitDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                            ) {
                                Text(
                                    text = if (selectedLanguage == SosLanguage.BANGLA) "এসওএস চালু করুন" else "Activate SOS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. SOS LANGUAGE SELECTOR DROPDOWN
                SosLanguageDropdownSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it },
                    cardBg = cardBg,
                    textDark = textDark
                )

                // 3. STATE MACHINE BODY SWITCH
                when (val currentState = crisisState) {
                    is CrisisState.Selection -> {
                        SleekEmergencyGrid(
                            selectedLanguage = selectedLanguage,
                            onSelectProtocol = { protocol -> crisisState = CrisisState.ActiveProtocol(protocol) },
                            onStartTriage = { crisisState = CrisisState.SymptomTriage },
                            cardBg = cardBg,
                            textDark = textDark,
                            textGray = textGray
                        )
                    }

                    is CrisisState.SymptomTriage -> {
                        InteractiveTriageWizard(
                            selectedLanguage = selectedLanguage,
                            onSelectProtocol = { protocol -> crisisState = CrisisState.ActiveProtocol(protocol) },
                            onCancel = { crisisState = CrisisState.Selection },
                            cardBg = cardBg,
                            textDark = textDark,
                            textGray = textGray
                        )
                    }

                    is CrisisState.ActiveProtocol -> {
                        HeroActiveProtocolVisualized(
                            protocol = currentState.condition,
                            selectedLanguage = selectedLanguage,
                            onChangeCondition = { crisisState = CrisisState.Selection },
                            onStopEmergency = { showExitDialog = true },
                            textDark = textDark,
                            textGray = textGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // DECLARE SOLVED CONFIRMATION DIALOG
        if (showDeclareSolvedDialog) {
            AlertDialog(
                onDismissRequest = { showDeclareSolvedDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32)) },
                title = { Text(if (selectedLanguage == SosLanguage.BANGLA) "জরুরি অবস্থা সমাধান হয়েছে ঘোষণা করবেন?" else "Declare Emergency Solved & Safe?", fontWeight = FontWeight.ExtraBold, color = textDark) },
                text = {
                    Text(
                        if (selectedLanguage == SosLanguage.BANGLA)
                            "আপনি কি এখন নিরাপদে আছেন? জরুরি অবস্থা সমাধান হয়েছে ঘোষণা করলে জিপিএস লাইভ সম্প্রচার বন্ধ হবে এবং আপনি নিরাপদ হিসেবে চিহ্নিত হবেন।"
                        else
                            "Are you safe now? Declaring this emergency solved will stop active GPS location broadcasting and notify responders that you are safe.",
                        color = textDark,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeclareSolvedDialog = false
                            EmergencyLocationHelper.resolveSosBroadcast(userUid)
                            Toast.makeText(
                                context,
                                if (selectedLanguage == SosLanguage.BANGLA) "জরুরি অবস্থা সমাধান হয়েছে! আপনি নিরাপদ হিসেবে চিহ্নিত।" else "Emergency declared solved! You are marked as Safe. ✓",
                                Toast.LENGTH_LONG
                            ).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "✅ আমি নিরাপদ & এসওএস বন্ধ করুন" else "✅ Mark Me Safe & Close SOS", fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeclareSolvedDialog = false }) {
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "বাতিল" else "Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // PERMIT GATE DIALOG FOR LIVE SOS ACTIVATION
        if (showSosActivationPermitDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSosActivationPermitDialog = false
                    isSosBroadcastPermitted = false
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed) },
                title = {
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "লাইভ জিপিএস এসওএস সম্প্রচার চালু করবেন?" else "Activate Live SOS Broadcast?",
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                },
                text = {
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA)
                            "লাইভ এসওএস চালু করলে আপনার জিপিএস অবস্থান জরুরি ডাটাবেজে সম্প্রচারিত হবে। আপনি চাইলে কোনো সংকেত সম্প্রচার না করে ফার্স্ট এইড নির্দেশিকা ও জরুরি সার্ভিস এক্সপ্লোর করতে পারেন।"
                        else
                            "Activating Live SOS will broadcast your real-time GPS location to Cloud Responders & Admins. Alternatively, you can explore emergency numbers & first-aid protocols without broadcasting an SOS alert.",
                        color = textDark,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isSosBroadcastPermitted = true
                            showSosActivationPermitDialog = false
                            Toast.makeText(
                                context,
                                if (selectedLanguage == SosLanguage.BANGLA) "লাইভ এসওএস সম্প্রচার চালু হয়েছে! 🚨" else "Live SOS Broadcast Activated! 🚨",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                    ) {
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "🚨 লাইভ এসওএস চালু করুন" else "🚨 Activate Live SOS",
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            isSosBroadcastPermitted = false
                            showSosActivationPermitDialog = false
                            Toast.makeText(
                                context,
                                if (selectedLanguage == SosLanguage.BANGLA) "এক্সপ্লোর মোড চালু (কোনো এসওএস সংকেত পাঠানো হয়নি)" else "Explore Mode Active (No SOS Alert Broadcasted)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "🔍 এক্সপ্লোর মোড (এসওএস ছাড়া)" else "🔍 Explore First-Aid & Numbers",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

        // EXIT CONFIRMATION DIALOG
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed) },
                title = { Text(if (selectedLanguage == SosLanguage.BANGLA) "জরুরি সার্ভিস বন্ধ করবেন?" else "Exit Emergency Crisis Mode?", fontWeight = FontWeight.Bold, color = textDark) },
                text = { Text(if (selectedLanguage == SosLanguage.BANGLA) "আপনি কি জরুরি নির্দেশিকা বন্ধ করে মূল অ্যাপে ফিরে যেতে চান?" else "Are you sure you want to stop active emergency guidance and return to the main app?", color = textDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                    ) {
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "জরুরি মোড বন্ধ করুন" else "Exit Crisis Mode")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "নির্দেশিকা চালিয়ে যান" else "Continue Guidance", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// ------------------------------------------------------------
// LANGUAGE DROPDOWN SELECTOR
// ------------------------------------------------------------
@Composable
private fun SosLanguageDropdownSelector(
    selectedLanguage: SosLanguage,
    onLanguageSelected: (SosLanguage) -> Unit,
    cardBg: Color,
    textDark: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = "Language", tint = PrimaryGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "ভাষা নির্বাচন করুন" else "Emergency Language",
                        fontSize = 10.5.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedLanguage.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "পরিবর্তন ▾" else "Change ▾",
                        color = PrimaryGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    SosLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lang.displayName,
                                    fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == selectedLanguage) PrimaryGreen else textDark
                                )
                            },
                            onClick = {
                                onLanguageSelected(lang)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
// LIVE MAP WITH PULSING DANGER BEACON MARKER
// ------------------------------------------------------------
@Composable
private fun EmergencyLiveGpsMapCard(
    locationLatLng: LatLng,
    locationName: String,
    latLngText: String,
    accuracyText: String,
    emergencyNumber: String,
    selectedLanguage: SosLanguage,
    onCopyLocation: () -> Unit,
    onExpandMap: () -> Unit,
    textDark: Color,
    textGray: Color
) {
    val context = LocalContext.current
    remember {
        MapLibre.getInstance(context)
        true
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, EmergencyRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // MAP VIEW WITH DANGER SIGN MARKER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                mapLibreMap = map
                                map.setStyle(GEOAPIFY_STYLE_URL)
                                map.cameraPosition = CameraPosition.Builder().target(locationLatLng).zoom(15.0).build()
                                map.addMarker(
                                    MarkerOptions()
                                        .position(locationLatLng)
                                        .title("🚨 EMERGENCY DANGER SOS")
                                        .snippet("Pulsing Emergency Broadcast Location")
                                )
                            }
                        }
                    },
                    update = {
                        val map = mapLibreMap
                        if (map != null) {
                            map.cameraPosition = CameraPosition.Builder().target(locationLatLng).zoom(15.0).build()
                            map.clear()
                            map.addMarker(
                                MarkerOptions()
                                    .position(locationLatLng)
                                    .title("🚨 EMERGENCY DANGER SOS")
                                    .snippet("Pulsing Emergency Broadcast Location")
                            )
                        }
                    }
                )

                // DANGER BEACON TOP OVERLAY
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(EmergencyRed)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "🚨 জিপিএস মানচিত্রে জরুরি বিপৎসংকেত সক্রিয়" else "🚨 DANGER SIGN ACTIVE ON MAP",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // EXPAND MAP BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, PrimaryGreen, RoundedCornerShape(20.dp))
                        .clickable { onExpandMap() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.OpenInFull, contentDescription = "Expand Map", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "মানচিত্র ⤢" else "Expand Map ⤢",
                            color = PrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // DETAILS BELOW MAP
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "আপনার বর্তমান জিপিএস অবস্থান" else "BROADCASTING LIVE COORDINATES",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            color = EmergencyRed
                        )
                    }

                    IconButton(onClick = onCopyLocation) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Location", tint = EmergencyRed)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = locationName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                Text(text = "GPS: $latLngText • Accuracy $accuracyText", fontSize = 12.5.sp, color = textGray, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(10.dp))

                // READ TO DISPATCHER BANNER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftYellow)
                        .border(1.dp, AlertAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedLanguage == SosLanguage.BANGLA) "🗣️ $emergencyNumber অপারেটরকে বলুন:" else "🗣️ READ THIS TO $emergencyNumber DISPATCHER:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = AlertAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) {
                                "\"আমি $locationName এর কাছে আছি, জিপিএস: $latLngText ($accuracyText)\""
                            } else {
                                "\"I am near $locationName, Coordinates $latLngText ($accuracyText).\""
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
// SLEEK EMERGENCY GRID ("WHAT'S HAPPENING?")
// ------------------------------------------------------------
@Composable
private fun SleekEmergencyGrid(
    selectedLanguage: SosLanguage,
    onSelectProtocol: (EmergencyProtocol) -> Unit,
    onStartTriage: () -> Unit,
    cardBg: Color,
    textDark: Color,
    textGray: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedLanguage == SosLanguage.BANGLA) "জরুরি অবস্থা নির্বাচন করুন" else "Select Emergency Condition",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark
            )
            Text(
                text = if (selectedLanguage == SosLanguage.BANGLA) "সর্বোচ্চ ১-ট্যাপ কাজ" else "Max 1-Tap Action",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = EmergencyRed
            )
        }

        // 2-COLUMN TACTILE EMERGENCY GRID
        val pairs = sampleProtocols.chunked(2)
        pairs.forEach { rowProtocols ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowProtocols.forEach { protocol ->
                    EmergencyTileCard(
                        protocol = protocol,
                        selectedLanguage = selectedLanguage,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectProtocol(protocol) },
                        textDark = textDark,
                        textGray = textGray
                    )
                }
                if (rowProtocols.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // HERO "I DON'T KNOW" DECISION ASSISTANT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartTriage() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, PrimaryGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(26.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "❓ আমি জানি না / নিশ্চিত নই" else "❓ I Don't Know / I'm Not Sure",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "২টি প্রশ্নের উত্তর দিয়ে সঠিক নির্দেশিকা পান" else "Answer 2 observable signs to launch the right protocol",
                        fontSize = 12.sp,
                        color = textGray
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyTileCard(
    protocol: EmergencyProtocol,
    selectedLanguage: SosLanguage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    textDark: Color,
    textGray: Color
) {
    val title = if (selectedLanguage == SosLanguage.BANGLA) protocol.titleBn else protocol.titleEn
    val subtitle = if (selectedLanguage == SosLanguage.BANGLA) protocol.subtitleBn else protocol.subtitleEn

    Card(
        modifier = modifier
            .heightIn(min = 110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, protocol.themeColor.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(protocol.themeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(protocol.icon, contentDescription = null, tint = protocol.themeColor, modifier = Modifier.size(20.dp))
                }

                if (protocol.isCritical) {
                    Box(
                        modifier = Modifier
                            .background(EmergencyRed, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "জরুরি" else "CRITICAL",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = textGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ------------------------------------------------------------
// INTERACTIVE TRIAGE WIZARD ("I DON'T KNOW")
// ------------------------------------------------------------
@Composable
private fun InteractiveTriageWizard(
    selectedLanguage: SosLanguage,
    onSelectProtocol: (EmergencyProtocol) -> Unit,
    onCancel: () -> Unit,
    cardBg: Color,
    textDark: Color,
    textGray: Color
) {
    var isResponsive by remember { mutableStateOf<Boolean?>(null) }

    val cprProtocol = sampleProtocols.first { it.id == "cpr" }
    val chokingProtocol = sampleProtocols.first { it.id == "choking" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, PrimaryGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedLanguage == SosLanguage.BANGLA) "লক্ষণ ভিত্তিক প্রাথমিক চিকিৎসা সহকারী" else "Observable Triage Assistant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen
                )
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = textGray)
                }
            }

            if (isResponsive == null) {
                Text(
                    text = if (selectedLanguage == SosLanguage.BANGLA) "১. ব্যক্তিটি কি আপনার কথায় বা স্পর্শে সাড়া দিচ্ছে?" else "1. Is the person responding to you (voice or touch)?",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { isResponsive = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "হ্যাঁ, সাড়া দিচ্ছে" else "YES, Responsive", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isResponsive = false },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                    ) {
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "না, সাড়া নেই" else "NO Response", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = if (selectedLanguage == SosLanguage.BANGLA) "২. তিনি কি স্বাভাবিকভাবে শ্বাস নিচ্ছেন?" else "2. Are they breathing normally?",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onSelectProtocol(cprProtocol) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                    ) {
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "না / শুধু হাঁপাচ্ছে → সিপিআর শুরু করুন" else "NO / Only Gasping → Start CPR Now",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp
                        )
                    }

                    Button(
                        onClick = { onSelectProtocol(chokingProtocol) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "হ্যাঁ, কিন্তু কষ্ট হচ্ছে → ফার্স্ট এইড" else "YES, But Struggling → Airway First Aid",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
// HERO ACTIVE LIFE-SAVING PROTOCOL ENGINE
// ------------------------------------------------------------
@Composable
private fun HeroActiveProtocolVisualized(
    protocol: EmergencyProtocol,
    selectedLanguage: SosLanguage,
    onChangeCondition: () -> Unit,
    onStopEmergency: () -> Unit,
    textDark: Color,
    textGray: Color
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = protocol.stepDetails[currentStepIndex]

    val title = if (selectedLanguage == SosLanguage.BANGLA) protocol.titleBn else protocol.titleEn
    val stepTitle = if (selectedLanguage == SosLanguage.BANGLA) currentStep.titleBn else currentStep.titleEn
    val stepInstruction = if (selectedLanguage == SosLanguage.BANGLA) currentStep.instructionBn else currentStep.instructionEn

    // CPR Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "cpr_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cpr_pulse_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, protocol.themeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = protocol.themeColor)
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "ধাপ ${currentStepIndex + 1} / ${protocol.stepDetails.size}" else "Step ${currentStepIndex + 1} of ${protocol.stepDetails.size}",
                        fontSize = 12.sp,
                        color = textGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(protocol.themeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "জরুরি নির্দেশিকা" else "BEGINNER GUIDANCE",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // STEP PROGRESS BAR
            LinearProgressIndicator(
                progress = { (currentStepIndex + 1).toFloat() / protocol.stepDetails.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = protocol.themeColor,
                trackColor = protocol.themeColor.copy(alpha = 0.15f)
            )

            // CPR METRONOME VISUAL BEAT
            if (protocol.requiresCprBeat) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(EmergencyRed),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "বুকের ঠিক মাঝে দ্রুত ও শক্ত করে চাপ দিন" else "PUSH HARD & FAST IN CENTER OF CHEST",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedLanguage == SosLanguage.BANGLA) "⚡ সিপিআর বিট ছন্দ: ১১০ BPM ⚡" else "⚡ CPR CADENCE METRONOME: 110 BPM ⚡",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // STEP-BY-STEP VISUALIZED IMAGE & INSTRUCTION CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // IMAGE VISUALIZATION FOR BEGINNERS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = currentStep.imageRes),
                            contentDescription = stepTitle,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(protocol.themeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${currentStep.stepNumber}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(text = stepTitle, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stepInstruction,
                        fontSize = 14.5.sp,
                        color = textDark,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // STEPPER NAVIGATION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentStepIndex > 0) currentStepIndex-- },
                    enabled = currentStepIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, if (currentStepIndex > 0) PrimaryGreen else Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = if (currentStepIndex > 0) PrimaryGreen else Color(0xFF9CA3AF),
                        disabledContainerColor = Color(0xFFF3F4F6),
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Step",
                        tint = if (currentStepIndex > 0) PrimaryGreen else Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (selectedLanguage == SosLanguage.BANGLA) "পূর্ববর্তী" else "Previous",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStepIndex > 0) PrimaryGreen else Color(0xFF9CA3AF)
                    )
                }

                Button(
                    onClick = {
                        if (currentStepIndex < protocol.stepDetails.size - 1) {
                            currentStepIndex++
                        } else {
                            onChangeCondition()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStepIndex < protocol.stepDetails.size - 1) protocol.themeColor else PrimaryGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (currentStepIndex < protocol.stepDetails.size - 1) {
                            if (selectedLanguage == SosLanguage.BANGLA) "পরবর্তী ধাপ" else "Next Step"
                        } else {
                            if (selectedLanguage == SosLanguage.BANGLA) "সম্পন্ন ✓" else "Complete ✓"
                        },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Step",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onChangeCondition,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, PrimaryGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "পরিবর্তন" else "Change", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onStopEmergency,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, EmergencyRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedLanguage == SosLanguage.BANGLA) "বন্ধ করুন" else "Stop", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

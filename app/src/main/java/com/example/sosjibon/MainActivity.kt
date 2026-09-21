package com.example.sosjibon

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sosjibon.ai.AiViewModel
import com.example.sosjibon.elibrary.location.EmergencyLocationHelper
import com.example.sosjibon.ui.ai.AiSheet
import com.example.sosjibon.ui.ai.FloatingAiBubble
import com.example.sosjibon.ui.navigation.AdminNavBar
import com.example.sosjibon.ui.navigation.AppNavHost
import com.example.sosjibon.ui.navigation.ROUTE_EMERGENCY_SOS
import com.example.sosjibon.ui.navigation.ROUTE_LANDING
import com.example.sosjibon.ui.navigation.ROUTE_LOGIN
import com.example.sosjibon.ui.navigation.ROUTE_REGISTER
import com.example.sosjibon.ui.navigation.SosJibonBottomNavBar
import com.example.sosjibon.ui.theme.SOSJIBONTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d("SOSJIBON_APP_CHECK", "Firebase App Check Debug Provider initialized successfully.")
        } catch (e: Exception) {
            Log.e("SOSJIBON_APP_CHECK", "Firebase App Check init caught safely: ${e.message}")
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember {
                context.getSharedPreferences(
                    "sosjibon_profile_settings",
                    MODE_PRIVATE
                )
            }

            var themeMode by remember {
                mutableStateOf(prefs.getString("theme_mode", "system") ?: "system")
            }

            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == "theme_mode" || key == "dark_theme") {
                        themeMode = p.getString("theme_mode", "system") ?: "system"
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            SOSJIBONTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                SosJibonApp()
            }
        }
    }
}

@Composable
fun SosJibonApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldHideBottomBar = currentRoute == ROUTE_LANDING ||
            currentRoute == ROUTE_LOGIN ||
            currentRoute == ROUTE_REGISTER ||
            currentRoute == ROUTE_EMERGENCY_SOS

    val authUser = FirebaseAuth.getInstance().currentUser
    val userUid = authUser?.uid ?: "guest_user"
    val authEmail = authUser?.email?.trim()?.lowercase() ?: ""
    val isAdmin = authUser != null && authEmail == "admin@gmail.com"
    val isRegularUser = authUser != null && !isAdmin

    val aiViewModel: AiViewModel = viewModel()
    val aiUiState by aiViewModel.uiState.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }
    var showGlobalMarkSafeDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // APP-WIDE TOP ACTIVE SOS EMERGENCY BANNER
        if (!isAdmin && authUser != null) {
            AppTopActiveSosBanner(
                userUid = userUid,
                onSolveClick = { showGlobalMarkSafeDialog = true }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (!shouldHideBottomBar) {
                        SosJibonBottomNavBar(navController)
                        AdminNavBar(navController)
                    }
                }
            ) { innerPadding ->
                val effectivePadding = if (shouldHideBottomBar) PaddingValues(0.dp) else innerPadding
                AppNavHost(navController = navController, innerPadding = effectivePadding)
            }

            if (!shouldHideBottomBar && isRegularUser) {
                FloatingAiBubble(
                    onClick = { sheetOpen = true }
                )
            }

            if (sheetOpen && isRegularUser) {
                AiSheet(
                    aiViewModel = aiViewModel,
                    uiState = aiUiState,
                    onDismiss = { sheetOpen = false },
                    onNavigate = { route ->
                        try {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("SOSJibonNavigation", "AI navigation failed for route: $route", e)
                        }
                    }
                )
            }
        }
    }

    // GLOBAL MARK SAFE CONFIRMATION DIALOG
    if (showGlobalMarkSafeDialog) {
        AlertDialog(
            onDismissRequest = { showGlobalMarkSafeDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32)) },
            title = { Text("Declare Emergency Solved & Safe?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Are you safe now? Declaring this emergency solved will stop active GPS location broadcasting and notify responders that you are safe.") },
            confirmButton = {
                Button(
                    onClick = {
                        showGlobalMarkSafeDialog = false
                        EmergencyLocationHelper.resolveSosBroadcast(userUid)
                        Toast.makeText(context, "Emergency declared solved! You are marked as Safe. ✓", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("✅ Mark Me Safe & Close SOS", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGlobalMarkSafeDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AppTopActiveSosBanner(
    userUid: String,
    onSolveClick: () -> Unit
) {
    var isLiveSosActive by remember { mutableStateOf(false) }

    // Real-time listener for current user's active SOS alert in Firestore
    DisposableEffect(userUid) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("active_sos_alerts").document(userUid)
            .addSnapshotListener { snapshot, _ ->
                isLiveSosActive = snapshot != null && snapshot.exists()
            }

        onDispose {
            listener.remove()
        }
    }

    if (isLiveSosActive) {
        Surface(
            color = Color(0xFFD92D20), // Emergency Red
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "🚨 LIVE SOS BROADCAST ACTIVE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "GPS Responders Notified • Tap to Mark Safe",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.5.sp
                        )
                    }
                }

                Button(
                    onClick = onSolveClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Mark Safe ✓",
                        color = Color(0xFFD92D20),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

package com.example.sosjibon.ui.navigation

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sosjibon.auth.AuthState
import com.example.sosjibon.auth.AuthViewModel
import com.example.sosjibon.auth.LandingScreen
import com.example.sosjibon.auth.LoginScreen
import com.example.sosjibon.auth.RegisterScreen
import com.example.sosjibon.elibrary.navigation.eLibraryGraph
import com.example.sosjibon.ui.admin.AdminBugsScreen
import com.example.sosjibon.ui.admin.AdminDashboardScreen
import com.example.sosjibon.ui.admin.AdminHomeScreen
import com.example.sosjibon.ui.admin.AdminPanelScreen
import com.example.sosjibon.ui.admin.AdminSettingsScreen
import com.example.sosjibon.ui.admin.AdminSosHistoryScreen
import com.example.sosjibon.ui.emergency.EmergencySosScreen
import com.example.sosjibon.ui.home.CommunityStoriesScreen
import com.example.sosjibon.ui.home.GpsMapScreen
import com.example.sosjibon.ui.home.HomeScreen
import com.example.sosjibon.ui.ai.ProTipScreen
import com.example.sosjibon.ui.settings.DeveloperScreen
import com.example.sosjibon.ui.settings.EditProfileScreen
import com.example.sosjibon.ui.settings.EmergencyContactsScreen
import com.example.sosjibon.ui.settings.PrivacyTermsScreen
import com.example.sosjibon.ui.settings.SecurityScreen
import com.example.sosjibon.ui.settings.SettingsScreen
import com.example.sosjibon.ui.vault.VaultScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

private const val ANIMATION_DURATION = 300

@Composable
fun AppNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sosjibon_profile_settings", Context.MODE_PRIVATE) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val savedEmail = prefs.getString("user_email", "")?.trim()?.lowercase() ?: ""
    val userEmail = currentUser?.email?.trim()?.lowercase() ?: savedEmail

    val initialStartDestination = when {
        currentUser != null || savedEmail.isNotBlank() -> {
            if (userEmail == "admin@gmail.com") Screen.AdminDashboard.route else Screen.Home.route
        }
        else -> ROUTE_LANDING
    }

    NavHost(
        navController = navController,
        startDestination = initialStartDestination,
        modifier = Modifier.padding(innerPadding),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            ) + fadeIn(
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            ) + fadeOut(
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            ) + fadeIn(
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            ) + fadeOut(
                animationSpec = tween(durationMillis = ANIMATION_DURATION)
            )
        }
    ) {
        composable(ROUTE_LANDING) {
            LandingScreen(
                onLoginClick = { navController.navigate(ROUTE_LOGIN) },
                onRegisterClick = { navController.navigate(ROUTE_REGISTER) },
                onGuestClick = { startGuestSession(navController) },
                onSOSClick = { navController.navigate(ROUTE_EMERGENCY_SOS) }
            )
        }

        composable(ROUTE_LOGIN) {
            val authViewModel: AuthViewModel = viewModel()
            val authState by authViewModel.authState.collectAsState()

            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)
                    val idToken: String? = account?.idToken
                    val email = account?.email ?: "google.user@sosjibon.org"
                    val name = account?.displayName ?: "Google Member"

                    if (!idToken.isNullOrBlank()) {
                        authViewModel.signInWithGoogleIdToken(idToken)
                    } else {
                        authViewModel.signInWithGoogleFallback(email, name)
                    }
                } catch (_: Exception) {
                    val account = try { GoogleSignIn.getLastSignedInAccount(context) } catch (_: Exception) { null }
                    val email = account?.email ?: "google.user@sosjibon.org"
                    val name = account?.displayName ?: "Google Member"
                    authViewModel.signInWithGoogleFallback(email, name)
                }
            }

            val triggerGoogleSignIn = {
                try {
                    val officialClientId = "566453717423-e5agraj7nnl4ioq0g87b858kdms80go6.apps.googleusercontent.com"
                    val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(officialClientId)
                        .requestEmail()

                    val googleSignInClient = GoogleSignIn.getClient(context, gsoBuilder.build())
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                } catch (_: Exception) {
                    authViewModel.signInWithGoogleFallback("google.user@sosjibon.org", "Google Member")
                }
            }

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is AuthState.Success -> {
                        isLoading = false
                        val userEmail = (authState as AuthState.Success).email.trim().lowercase()
                        val targetRoute = if (userEmail == "admin@gmail.com") {
                            Screen.AdminDashboard.route
                        } else {
                            Screen.Home.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(ROUTE_LANDING) { inclusive = true }
                        }
                    }
                    is AuthState.Error -> {
                        isLoading = false
                        errorMessage = (authState as AuthState.Error).message
                    }
                    is AuthState.Idle -> {
                        isLoading = false
                    }
                }
            }

            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { email, pass ->
                    authViewModel.login(email, pass)
                },
                onGoogleSignInClick = triggerGoogleSignIn,
                onRegisterClick = { navController.navigate(ROUTE_REGISTER) },
                onGuestClick = { startGuestSession(navController) },
                onForgotPasswordClick = {},
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        composable(ROUTE_REGISTER) {
            val authViewModel: AuthViewModel = viewModel()
            val authState by authViewModel.authState.collectAsState()

            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is AuthState.Success -> {
                        isLoading = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(ROUTE_LANDING) { inclusive = true }
                        }
                    }
                    is AuthState.Error -> {
                        isLoading = false
                        errorMessage = (authState as AuthState.Error).message
                    }
                    is AuthState.Idle -> {
                        isLoading = false
                    }
                }
            }

            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(ROUTE_LOGIN) },
                onRegisterSuccess = { fullName, email, phone, password ->
                    authViewModel.register(fullName, email, password, phone)
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(ROUTE_GPS_MAP) },
                onNavigateToStories = { navController.navigate(ROUTE_COMMUNITY_STORIES) },
                onNavigateToAuth = { navController.navigate(ROUTE_LANDING) },
                onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                onNavigateToSosHistory = { navController.navigate(Screen.AdminSosHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) }
            )
        }

        eLibraryGraph(navController)
        composable(Screen.Vault.route) {
            VaultScreen(
                onNavigateToAuth = { navController.navigate(ROUTE_LANDING) }
            )
        }
        composable(
            Screen.Settings.route
        ) {
            SettingsScreen(
                onLogoutClick = {
                    navController.navigate(ROUTE_LANDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAuth = { navController.navigate(ROUTE_LANDING) },
                onNavigateToEditProfile = {
                    navController.navigate(ROUTE_EDIT_PROFILE)
                },
                onNavigateToSecurity = {
                    navController.navigate(ROUTE_SECURITY)
                },
                onNavigateToPrivacyTerms = {
                    navController.navigate(ROUTE_PRIVACY_TERMS)
                },
                onNavigateToDevelopers = {
                    navController.navigate(ROUTE_DEVELOPERS)
                },
                onNavigateToEmergencyContacts = {
                    navController.navigate(ROUTE_EMERGENCY_CONTACTS)
                }
            )
        }

        composable(ROUTE_EDIT_PROFILE) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_SECURITY) {
            SecurityScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_PRIVACY_TERMS) {
            PrivacyTermsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                onNavigateToBugs = { navController.navigate(Screen.AdminBugs.route) },
                onNavigateToSosHistory = { navController.navigate(Screen.AdminSosHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) }
            )
        }

        composable(Screen.AdminUsers.route) {
            AdminPanelScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminBugs.route) {
            AdminBugsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminSosHistory.route) {
            AdminSosHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminSettings.route) {
            AdminSettingsScreen(
                onLogoutClick = {
                    navController.navigate(ROUTE_LANDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToDevelopers = { navController.navigate(ROUTE_DEVELOPERS) },
                onNavigateToSecurity = { navController.navigate(ROUTE_SECURITY) },
                onNavigateToPrivacyTerms = { navController.navigate(ROUTE_PRIVACY_TERMS) }
            )
        }

        composable(ROUTE_DEVELOPERS) {
            DeveloperScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(ROUTE_LOGIN) }
            )
        }

        composable(ROUTE_EMERGENCY_CONTACTS) {
            EmergencyContactsScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_PRO_TIP) {
            ProTipScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_EMERGENCY_SOS) {
            EmergencySosScreen(
                onBack = { navController.popBackStack() },
                onExpandMap = { navController.navigate(ROUTE_GPS_MAP) }
            )
        }

        composable(ROUTE_GPS_MAP) {
            GpsMapScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_COMMUNITY_STORIES) {
            CommunityStoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun startGuestSession(navController: NavHostController) {
    try {
        FirebaseAuth.getInstance().signOut()
        val app = FirebaseApp.getInstance().applicationContext as? Application
        val prefs = app?.getSharedPreferences("sosjibon_profile_settings", Context.MODE_PRIVATE)
        prefs?.edit()?.apply {
            putString("user_name", "Guest Member")
            putString("user_email", "")
            putString("user_phone", "")
            putString("user_blood_group", "")
            putString("user_dob", "")
            putString("user_country", "")
            putString("user_city", "")
            putString("user_desc", "Guest Session")
            putBoolean("user_email_verified", false)
            apply()
        }
    } catch (_: Exception) {}

    navController.navigate(Screen.Home.route) {
        popUpTo(0) { inclusive = true }
    }
}

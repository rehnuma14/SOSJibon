package com.example.sosjibon.ui.navigation

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
import com.example.sosjibon.ui.emergency.EmergencySosScreen
import com.example.sosjibon.ui.home.CommunityStoriesScreen
import com.example.sosjibon.ui.home.GpsMapScreen
import com.example.sosjibon.ui.home.HomeScreen
import com.example.sosjibon.ui.settings.DeveloperScreen
import com.example.sosjibon.ui.settings.EditProfileScreen
import com.example.sosjibon.ui.settings.EmergencyContactsScreen
import com.example.sosjibon.ui.settings.PrivacyTermsScreen
import com.example.sosjibon.ui.settings.SecurityScreen
import com.example.sosjibon.ui.settings.SettingsScreen
import com.example.sosjibon.ui.vault.VaultScreen

private const val ANIMATION_DURATION = 300

@Composable
fun AppNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_LANDING,
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
                onSOSClick = { navController.navigate(ROUTE_EMERGENCY_SOS) }
            )
        }

        composable(ROUTE_LOGIN) {
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

            LoginScreen(
                onBackClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(ROUTE_LANDING) { inclusive = true }
                    }
                },
                onLoginClick = { email, pass ->
                    authViewModel.login(email, pass)
                },
                onRegisterClick = { navController.navigate(ROUTE_REGISTER) },
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
                onNavigateToStories = { navController.navigate(ROUTE_COMMUNITY_STORIES) }
            )
        }

        eLibraryGraph(navController)
        composable(Screen.Vault.route) { VaultScreen() }
        composable(
            Screen.Settings.route
        ) {
            SettingsScreen(
                onLogoutClick = {
                    navController.navigate(ROUTE_LANDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
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

        composable(ROUTE_DEVELOPERS) {
            DeveloperScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_EMERGENCY_CONTACTS) {
            EmergencyContactsScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_EMERGENCY_SOS) {
            EmergencySosScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_GPS_MAP) {
            GpsMapScreen(onBack = { navController.popBackStack() })
        }

        composable(ROUTE_COMMUNITY_STORIES) {
            CommunityStoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}

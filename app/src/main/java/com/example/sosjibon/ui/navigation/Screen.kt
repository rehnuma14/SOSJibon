package com.example.sosjibon.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Resources : Screen("resources", "Resources", Icons.Filled.Info)
    object Vault : Screen("vault", "Vault", Icons.Filled.Lock)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Resources, Screen.Vault, Screen.Settings)

// Reachable routes
const val ROUTE_LANDING = "landing"
const val ROUTE_LOGIN = "login"
const val ROUTE_REGISTER = "register"
const val ROUTE_EMERGENCY_SOS = "emergency_sos"
const val ROUTE_GPS_MAP = "gps_map"
const val ROUTE_COMMUNITY_STORIES = "community_stories"
const val ROUTE_DEVELOPERS = "developers"
const val ROUTE_EMERGENCY_CONTACTS = "emergency_contacts"
const val ROUTE_EDIT_PROFILE = "edit_profile"
const val ROUTE_SECURITY = "security"
const val ROUTE_PRIVACY_TERMS = "privacy_terms"

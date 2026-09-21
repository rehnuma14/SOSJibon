package com.example.sosjibon.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Resources : Screen("resources", "Resources", Icons.Filled.Info)
    object Vault : Screen("vault", "Vault", Icons.Filled.Lock)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    // Admin Dedicated Screens
    object AdminDashboard : Screen("admin_dashboard", "dashboard", Icons.Filled.Dashboard)
    object AdminUsers : Screen("admin_users", "users", Icons.Filled.People)
    object AdminBugs : Screen("admin_bugs", "bugs", Icons.Filled.BugReport)
    object AdminSosHistory : Screen("admin_soshistory", "history", Icons.Filled.Warning)
    object AdminSettings : Screen("admin_settings", "settings", Icons.Filled.Settings)
}

val userBottomNavItems = listOf(Screen.Home, Screen.Resources, Screen.Vault, Screen.Settings)
val adminNavItems = listOf(
    Screen.AdminDashboard,
    Screen.AdminUsers,
    Screen.AdminBugs,
    Screen.AdminSosHistory,
    Screen.AdminSettings
)

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
const val ROUTE_ADMIN_PANEL = "admin_panel"
const val ROUTE_ADMIN_DASHBOARD = "admin_dashboard"
const val ROUTE_ADMIN_USERS = "admin_users"
const val ROUTE_ADMIN_BUGS = "admin_bugs"
const val ROUTE_ADMIN_SOSHISTORY = "admin_soshistory"
const val ROUTE_ADMIN_SETTINGS = "admin_settings"
const val ROUTE_PRO_TIP = "pro_tip"

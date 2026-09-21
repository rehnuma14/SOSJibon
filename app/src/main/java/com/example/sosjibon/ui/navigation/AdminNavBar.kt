package com.example.sosjibon.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sosjibon.ui.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth

private val PrimaryGreen = Color(0xFF159A6C)
private val UnselectedColor = Color.Gray

@Composable
fun AdminNavBar(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val authUser = FirebaseAuth.getInstance().currentUser
    val authEmail = authUser?.email?.trim()?.lowercase() ?: ""
    val isAdmin = authUser != null && authEmail == "admin@gmail.com"

    if (!isAdmin) {
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val route = currentDestination?.route
    if (route == ROUTE_LANDING || route == ROUTE_LOGIN || route == ROUTE_REGISTER || route == ROUTE_EMERGENCY_SOS) {
        return
    }

    val adminItems = listOf(
        Screen.AdminDashboard,
        Screen.AdminUsers,
        Screen.AdminBugs,
        Screen.AdminSosHistory,
        Screen.AdminSettings
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFF111827),
        contentColor = PrimaryGreen
    ) {
        adminItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val iconColor by animateColorAsState(
                targetValue = if (selected) PrimaryGreen else UnselectedColor,
                animationSpec = tween(durationMillis = 250),
                label = "AdminNavIconColor"
            )

            val scale = if (selected) 1.1f else 1.0f

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp).scale(scale)
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 11.sp,
                        color = if (selected) PrimaryGreen else UnselectedColor,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

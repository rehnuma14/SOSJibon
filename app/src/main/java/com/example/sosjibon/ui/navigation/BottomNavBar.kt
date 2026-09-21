package com.example.sosjibon.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sosjibon.ui.settings.SettingsViewModel

private val SosRed = Color(0xFFE5484D)
private val SelectedColor = Color(0xFF159A6C)
private val UnselectedColor = Color.Gray

@Composable
fun SosJibonBottomNavBar(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on auth or emergency screens
    val route = currentDestination?.route
    if (route == ROUTE_LANDING || route == ROUTE_LOGIN || route == ROUTE_REGISTER || route == ROUTE_EMERGENCY_SOS) {
        return
    }

    val authUser = FirebaseAuth.getInstance().currentUser
    val authEmail = authUser?.email?.trim()?.lowercase() ?: ""
    val isAdmin = authUser != null && authEmail == "admin@gmail.com"

    if (isAdmin) {
        return
    }

    val navItems = userBottomNavItems

    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar {
            navItems.forEachIndexed { index, screen ->
                if (index == 2) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        enabled = false,
                        icon = {},
                        label = {}
                    )
                }
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val iconColor by animateColorAsState(
                    targetValue = if (selected) SelectedColor else UnselectedColor,
                    animationSpec = tween(durationMillis = 250),
                    label = "NavigationIconColor"
                )

                val scale = if (selected) 1.08f else 1.0f

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
                            modifier = Modifier.size(24.dp).scale(scale)
                        )
                    },
                    label = {
                        Text(
                            text = screen.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(60.dp)
                .clickable {
                    navController.navigate(ROUTE_EMERGENCY_SOS)
                },
            shape = CircleShape,
            color = SosRed,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

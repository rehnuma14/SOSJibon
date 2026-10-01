/*
 * LandingScreen.kt
 * What this file does: App landing page with Login/Register and Emergency SOS guest access.
 *
 * Pseudo-code:
 * 1. Render App Logo and welcome branding.
 * 2. If user clicks "Login / Register Account" -> Navigate to LoginScreen / RegisterScreen.
 * 3. If user clicks "Continue Without Login" -> Navigate ONLY to Emergency SOS Screen.
 * 4. If user clicks "DIRECT EMERGENCY SOS" -> Navigate ONLY to Emergency SOS Screen.
 */

package com.example.sosjibon.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.ui.theme.SOSJIBONTheme

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onSOSClick: () -> Unit
) {
    SOSJIBONTheme(darkTheme = false) {
        val primaryGreen = Color(0xFF159A6C)
        val darkGreen = Color(0xFF087A55)
        val lightGreen = Color(0xFFE8F7F1)
        val emergencyRed = Color(0xFFD92D20)
        val background = Color(0xFFF8FCFA)
        val cardBg = Color.White
        val textDark = Color(0xFF17332A)
        val textGray = Color(0xFF6B7C75)

        val infiniteTransition = rememberInfiniteTransition(label = "landing_animation")
        val orbScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(animation = tween(1600), repeatMode = RepeatMode.Reverse),
            label = "orb_scale"
        )
        val floatingOffset by infiniteTransition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Reverse),
            label = "floating_offset"
        )

        Surface(modifier = Modifier.fillMaxSize(), color = background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.5.dp, primaryGreen.copy(alpha = 0.30f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(color = lightGreen, shape = CircleShape)
                                    .border(2.dp, primaryGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = "SOSJibon",
                                    tint = primaryGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = "SOSJibon", color = textDark, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                Text(text = "Your emergency healthcare companion", color = textGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingMedicalIcon(icon = Icons.Default.MedicalServices, tint = primaryGreen, modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = floatingOffset.dp))
                        FloatingMedicalIcon(icon = Icons.Default.LocalHospital, tint = darkGreen, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-20).dp, y = (-floatingOffset).dp))
                        FloatingMedicalIcon(icon = Icons.Default.Call, tint = primaryGreen, modifier = Modifier.align(Alignment.BottomStart).offset(x = 40.dp, y = floatingOffset.dp))
                        FloatingMedicalIcon(icon = Icons.Default.Person, tint = darkGreen, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-40).dp, y = (-floatingOffset).dp))

                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(orbScale)
                                .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = primaryGreen, spotColor = primaryGreen)
                                .background(color = primaryGreen, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = "Emergency healthcare", tint = Color.White, modifier = Modifier.size(64.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Healthcare when you need it most", color = textDark, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Connect with emergency services, healthcare resources, and essential medical information.", color = textGray, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 12.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Button 1: Login / Register
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Text(text = "Login / Register Account", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Button 2: Continue Without Login (Emergency SOS Only)
                    OutlinedButton(
                        onClick = { onGuestClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, primaryGreen)
                    ) {
                        Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Continue Without Login (Emergency SOS Only)", color = primaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Button 3: Direct Emergency SOS
                    Button(
                        onClick = onSOSClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = emergencyRed),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Emergency SOS", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "DIRECT EMERGENCY SOS", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Stay safe. Stay prepared. Stay connected.", color = textGray, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "© 2026 SOSJibon", color = textGray.copy(alpha = 0.75f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun FloatingMedicalIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .border(width = 1.dp, color = tint.copy(alpha = 0.25f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

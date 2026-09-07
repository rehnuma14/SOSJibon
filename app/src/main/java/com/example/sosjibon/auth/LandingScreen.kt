package com.example.sosjibon.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.ui.theme.SOSJIBONTheme

// ------------------------------------------------------------
// SOSJibon Landing Screen
// ------------------------------------------------------------

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSOSClick: () -> Unit
) {

    // --------------------------------------------------------
    // Theme colors
    // --------------------------------------------------------

    val primaryGreen = Color(0xFF159A6C)
    val darkGreen = Color(0xFF087A55)
    val lightGreen = Color(0xFFE8F7F1)
    val emergencyRed = Color(0xFFD92D20)
    val background = Color(0xFFF8FCFA)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)

    // --------------------------------------------------------
    // Animation
    // --------------------------------------------------------

    val infiniteTransition = rememberInfiniteTransition(
        label = "landing_animation"
    )

    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    var showSOS by remember {
        mutableStateOf(true)
    }

    // --------------------------------------------------------
    // Screen
    // --------------------------------------------------------

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // ------------------------------------------------
            // Main content
            // ------------------------------------------------

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(35.dp))

                // --------------------------------------------
                // App branding card
                // --------------------------------------------

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = primaryGreen.copy(alpha = 0.30f)
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 18.dp,
                                vertical = 15.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // App icon
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(
                                    color = lightGreen,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = primaryGreen,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = "SOSJibon",
                                tint = primaryGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {

                            Text(
                                text = "SOSJibon",
                                color = textDark,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = "Your emergency healthcare companion",
                                color = textGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(42.dp))

                // --------------------------------------------
                // Floating medical icons
                // --------------------------------------------

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(265.dp),
                    contentAlignment = Alignment.Center
                ) {

                    // Top-left
                    FloatingMedicalIcon(
                        icon = Icons.Default.MedicalServices,
                        tint = primaryGreen,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = 24.dp,
                                y = floatingOffset.dp
                            )
                    )

                    // Top-right
                    FloatingMedicalIcon(
                        icon = Icons.Default.LocalHospital,
                        tint = darkGreen,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(
                                x = (-24).dp,
                                y = (-floatingOffset).dp
                            )
                    )

                    // Bottom-left
                    FloatingMedicalIcon(
                        icon = Icons.Default.Call,
                        tint = primaryGreen,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(
                                x = 48.dp,
                                y = floatingOffset.dp
                            )
                    )

                    // Bottom-right
                    FloatingMedicalIcon(
                        icon = Icons.Default.Person,
                        tint = darkGreen,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(
                                x = (-48).dp,
                                y = (-floatingOffset).dp
                            )
                    )

                    // ----------------------------------------
                    // Main medical orb
                    // ----------------------------------------

                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(orbScale)
                            .shadow(
                                elevation = 18.dp,
                                shape = CircleShape,
                                ambientColor = primaryGreen,
                                spotColor = primaryGreen
                            )
                            .background(
                                color = primaryGreen,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(155.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = "Emergency healthcare",
                                tint = Color.White,
                                modifier = Modifier.size(82.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --------------------------------------------
                // Welcome text
                // --------------------------------------------

                Text(
                    text = "Healthcare when you need it most",
                    color = textDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect with emergency services, healthcare resources, and essential medical information — all in one place.",
                    color = textGray,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(25.dp))

                // --------------------------------------------
                // Login + Register
                // --------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = primaryGreen
                        )
                    ) {

                        Text(
                            text = "Login",
                            color = primaryGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen
                        )
                    ) {

                        Text(
                            text = "Register",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // --------------------------------------------
                // Emergency SOS button
                // --------------------------------------------

                AnimatedVisibility(
                    visible = showSOS,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {

                    Button(
                        onClick = onSOSClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = emergencyRed
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 5.dp
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Emergency SOS",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Text(
                            text = "EMERGENCY SOS",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --------------------------------------------
                // Footer
                // --------------------------------------------

                Text(
                    text = "Stay safe. Stay prepared. Stay connected.",
                    color = textGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "© 2026 SOSJibon",
                    color = textGray.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ------------------------------------------------------------
// Floating medical icon
// ------------------------------------------------------------

@Composable
private fun FloatingMedicalIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape
            )
            .background(
                color = Color.White,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = tint.copy(alpha = 0.25f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    SOSJIBONTheme {
        LandingScreen(
            onLoginClick = {},
            onRegisterClick = {},
            onSOSClick = {}
        )
    }
}
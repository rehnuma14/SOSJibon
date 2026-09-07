package com.example.sosjibon.ui.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    onBack: () -> Unit
) {
    val primaryGreen = Color(0xFF159A6C)
    val background = Color(0xFFF8FCFA)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)

    var selectedDeveloper by remember { mutableStateOf<DeveloperItem?>(null) }

    val developers = listOf(
        DeveloperItem(
            name = "Shafiul Islam",
            role = "Lead Developer",
            designation = "Lead Systems & Android Engineer",
            contribution = "Core System Architecture, App Navigation Host, Emergency SOS Engine, and Jetpack Compose UI Framework.",
            email = "shafiul@sosjibon.org",
            linkedin = "linkedin.com/in/shafiul-islam",
            initial = "SI"
        ),
        DeveloperItem(
            name = "Mahmudul Hasan",
            role = "Backend Lead",
            designation = "Cloud & Security Infrastructure Lead",
            contribution = "Firebase Authentication, Cloud Firestore Realtime Sync Engine, and Encrypted Room Database Storage.",
            email = "mahmudul@sosjibon.org",
            linkedin = "linkedin.com/in/mahmudul-hasan",
            initial = "MH"
        ),
        DeveloperItem(
            name = "Nusrat Jahan",
            role = "UI/UX Specialist",
            designation = "Mobile Product & Experience Designer",
            contribution = "Healthcare Vault UI, Vitals Trend Canvas Charts Visualization, Theme Systems & Accessibility Design.",
            email = "nusrat@sosjibon.org",
            linkedin = "linkedin.com/in/nusrat-jahan",
            initial = "NJ"
        ),
        DeveloperItem(
            name = "Tanvir Ahmed",
            role = "QA & Data Lead",
            designation = "Quality Assurance & Health Systems Lead",
            contribution = "Emergency GPS Location Services, First Aid E-Library Content Management, and Integration Testing.",
            email = "tanvir@sosjibon.org",
            linkedin = "linkedin.com/in/tanvir-ahmed",
            initial = "TA"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Developer Team", fontWeight = FontWeight.Bold, color = textDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background)
            )
        },
        containerColor = background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = primaryGreen)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Meet the Creators", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Tap any card below to view full designation, contributions & contact details.", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }

            Text(text = "Engineering Team (2×2)", fontWeight = FontWeight.Bold, color = textDark, fontSize = 16.sp)

            // 2x2 Grid Layout
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GridDeveloperCard(
                        developer = developers[0],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[0] }
                    )
                    GridDeveloperCard(
                        developer = developers[1],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[1] }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GridDeveloperCard(
                        developer = developers[2],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[2] }
                    )
                    GridDeveloperCard(
                        developer = developers[3],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDeveloper = developers[3] }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = primaryGreen.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "SOSJibon Engineering", color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "Built for saving lives", color = textGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    selectedDeveloper?.let { dev ->
        DeveloperDetailModal(developer = dev, onDismiss = { selectedDeveloper = null })
    }
}

@Composable
private fun GridDeveloperCard(
    developer: DeveloperItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryGreen = Color(0xFF159A6C)
    val lightGreen = Color(0xFFE8F7F1)
    val textDark = Color(0xFF17332A)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(lightGreen, CircleShape)
                    .border(2.dp, primaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = developer.initial, fontWeight = FontWeight.Black, color = primaryGreen, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = developer.name,
                fontWeight = FontWeight.ExtraBold,
                color = textDark,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = developer.role,
                color = primaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = lightGreen
            ) {
                Text(
                    text = "Tap for details",
                    color = primaryGreen,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun DeveloperDetailModal(
    developer: DeveloperItem,
    onDismiss: () -> Unit
) {
    val primaryGreen = Color(0xFF159A6C)
    val lightGreen = Color(0xFFE8F7F1)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(lightGreen, CircleShape)
                        .border(1.5.dp, primaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = developer.initial, fontWeight = FontWeight.Black, color = primaryGreen, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = developer.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = textDark)
                    Text(text = developer.role, fontSize = 12.sp, color = primaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = Color(0xFFE9EFEC))

                Text(text = "Designation:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 12.5.sp)
                Text(text = developer.designation, color = textGray, fontSize = 12.sp)

                Text(text = "Key Contribution:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 12.5.sp)
                Text(text = developer.contribution, color = textGray, fontSize = 12.sp, lineHeight = 17.sp)

                HorizontalDivider(color = Color(0xFFE9EFEC))

                Text(text = "Contact & Profiles:", fontWeight = FontWeight.Bold, color = textDark, fontSize = 12.5.sp)
                Text(text = "✉️ Email: ${developer.email}", color = primaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "🔗 LinkedIn: ${developer.linkedin}", color = primaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)) {
                Text(text = "Close")
            }
        }
    )
}

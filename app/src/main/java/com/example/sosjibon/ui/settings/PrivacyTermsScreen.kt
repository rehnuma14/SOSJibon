package com.example.sosjibon.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyTermsScreen(
    onBack: () -> Unit
) {
    val primaryGreen = MaterialTheme.colorScheme.primary
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface
    val pageBg = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = { Text(text = "Privacy Policy & Terms", fontWeight = FontWeight.Bold, color = textDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medical Disclaimer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = "🚨 Emergency Medical Disclaimer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SOSJibon provides first-aid guidance, GPS location broadcasting, and personal medical vault storage. The first aid E-Library is designed for informational and emergency assistance purposes only and does not substitute professional medical advice, diagnosis, or hospital treatment. In case of life-threatening emergencies, always dial your local emergency hotline (e.g. 999 / 911 / 112) immediately.",
                        fontSize = 13.sp,
                        color = textDark,
                        lineHeight = 19.sp
                    )
                }
            }

            // User Privacy Policy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = "🔒 User Data Privacy & Encryption", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your personal healthcare data, vitals trends, emergency contacts, and medical vault records are protected using Firebase Auth and encrypted storage. We do not sell or share your medical records with third-party advertisers. Location access is used strictly when requesting Emergency GPS assistance or viewing nearby healthcare services on the map.",
                        fontSize = 13.sp,
                        color = textDark,
                        lineHeight = 19.sp
                    )
                }
            }

            // Terms of Service Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = "📜 Terms of Service", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "By registering and using SOSJibon Healthcare, you agree to keep your emergency contact numbers updated, maintain valid verification details, and use the Emergency SOS engine responsibly without triggering false alarms. Accounts violating emergency safety standards may be restricted.",
                        fontSize = 13.sp,
                        color = textDark,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

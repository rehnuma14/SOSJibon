package com.example.sosjibon.elibrary.resources.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GuideLanguage { ENGLISH, BENGALI }

@Composable
fun LanguageToggle(
    selected: GuideLanguage,
    onLanguageChange: (GuideLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEDEDED))
            .padding(3.dp)
    ) {
        LanguagePill("EN", selected == GuideLanguage.ENGLISH) { onLanguageChange(GuideLanguage.ENGLISH) }
        LanguagePill("বাং", selected == GuideLanguage.BENGALI) { onLanguageChange(GuideLanguage.BENGALI) }
    }
}

@Composable
private fun LanguagePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Color.White else Color(0xFF616161),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF1565C0) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

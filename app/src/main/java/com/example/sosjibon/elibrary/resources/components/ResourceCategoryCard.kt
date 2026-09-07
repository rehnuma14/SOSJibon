package com.example.sosjibon.elibrary.resources.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.sosjibon.elibrary.resources.data.GuideCategory

private val PrimaryGreen = Color(0xFF159A6C)
private val LightGreenBg = Color(0xFFE8F7F1)
private val DarkForestText = Color(0xFF17332A)

@Composable
fun ResourceCategoryCard(
    category: GuideCategory,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = category.displayName
) {
    val background = if (selected) PrimaryGreen else LightGreenBg
    val textColor = if (selected) Color.White else DarkForestText
    val borderColor = if (selected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.2f)

    Text(
        text = label,
        color = textColor,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

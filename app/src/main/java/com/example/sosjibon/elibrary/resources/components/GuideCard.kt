package com.example.sosjibon.elibrary.resources.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.elibrary.resources.data.EmergencyCondition
import com.example.sosjibon.elibrary.resources.data.SeverityTag

private val PrimaryGreen = Color(0xFF159A6C)
private val EmergencyRed = Color(0xFFD92D20)

private fun severityColor(tag: SeverityTag): Color = when (tag) {
    SeverityTag.RED -> EmergencyRed
    SeverityTag.BLUE -> PrimaryGreen
    SeverityTag.GREEN -> Color(0xFF2E7D32)
}

@Composable
fun GuideCard(
    condition: EmergencyCondition,
    isBookmarked: Boolean,
    onClick: (EmergencyCondition) -> Unit,
    onBookmarkToggle: (EmergencyCondition) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = severityColor(condition.severity)
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val iconBg = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(condition) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = condition.cardIconRes),
                        contentDescription = condition.title,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = { onBookmarkToggle(condition) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark this guide",
                        tint = accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = condition.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = textDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = condition.whatHappened,
                fontSize = 11.5.sp,
                color = textGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

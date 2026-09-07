package com.example.sosjibon.elibrary.resources.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DoDontCard(
    title: String,
    items: List<String>,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = accentColor, fontSize = 15.sp)
            items.forEach { line ->
                Text("• $line", fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

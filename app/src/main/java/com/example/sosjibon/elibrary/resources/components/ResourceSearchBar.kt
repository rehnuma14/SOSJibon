package com.example.sosjibon.elibrary.resources.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResourceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryGreen = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search CPR, bleeding, burns, snake bite...", color = textGray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = primaryGreen) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = primaryGreen)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryGreen,
            focusedLeadingIconColor = primaryGreen,
            unfocusedBorderColor = primaryGreen.copy(alpha = 0.35f),
            focusedContainerColor = cardBg,
            unfocusedContainerColor = cardBg
        ),
        modifier = modifier.fillMaxWidth()
    )
}

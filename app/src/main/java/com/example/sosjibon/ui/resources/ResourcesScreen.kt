package com.example.sosjibon.ui.resources

import androidx.compose.runtime.Composable
import com.example.sosjibon.elibrary.resources.ResourceCenterScreen

@Composable
fun ResourcesScreen(
    onConditionClick: (String) -> Unit = {},
    onStartAssessment: () -> Unit = {}
) {
    ResourceCenterScreen(
        onConditionClick = onConditionClick,
        onStartAssessment = onStartAssessment
    )
}

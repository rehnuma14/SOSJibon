package com.example.sosjibon.elibrary.resources

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.elibrary.location.EmergencyLocationHelper
import com.example.sosjibon.elibrary.resources.components.GuideCard
import com.example.sosjibon.elibrary.resources.components.ResourceCategoryCard
import com.example.sosjibon.elibrary.resources.components.ResourceSearchBar
import com.example.sosjibon.elibrary.resources.data.GuideCategory
import com.example.sosjibon.elibrary.resources.data.SeverityTag
import kotlinx.coroutines.launch

private val PrimaryGreen = Color(0xFF159A6C)
private val LightGreenBg = Color(0xFFE8F7F1)
private val DarkForestText = Color(0xFF17332A)
private val TextGray = Color(0xFF6B7C75)
private val EmergencyRed = Color(0xFFD92D20)
private val PageBackground = Color(0xFFF8FCFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceCenterScreen(
    onConditionClick: (conditionId: String) -> Unit,
    onStartAssessment: () -> Unit,
    viewModel: ResourceViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val severityFilter by viewModel.severityFilter.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val filteredConditions by viewModel.filteredConditions.collectAsState()
    val hasNoResults by viewModel.hasNoSearchResults.collectAsState()

    var countryCode by remember { mutableStateOf<String?>(if (isPreview) "BD" else null) }
    var emergencyNumber by remember {
        mutableStateOf(if (isPreview) "999" else EmergencyLocationHelper.getEmergencyNumber(null))
    }

    if (!isPreview) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                scope.launch {
                    countryCode = EmergencyLocationHelper.getCurrentCountryCode(context)
                    emergencyNumber = EmergencyLocationHelper.getEmergencyNumber(countryCode)
                }
            }
        }
        LaunchedEffect(Unit) {
            if (EmergencyLocationHelper.hasLocationPermission(context)) {
                countryCode = EmergencyLocationHelper.getCurrentCountryCode(context)
                emergencyNumber = EmergencyLocationHelper.getEmergencyNumber(countryCode)
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val pageBg = MaterialTheme.colorScheme.background
    val darkForestText = MaterialTheme.colorScheme.onBackground
    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreenBg = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(lightGreenBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "First Aid & Resources",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = darkForestText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pageBg,
                    titleContentColor = darkForestText
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    ResourceSearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LocationEmergencyBar(
                        countryCode = countryCode,
                        emergencyNumber = emergencyNumber,
                        onCallClick = { EmergencyLocationHelper.launchDialer(context, emergencyNumber) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (hasNoResults) {
                        NoResultsFallback(onStartAssessment = onStartAssessment)
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (searchQuery.isBlank()) {
                        Text("Most Urgent Guides", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkForestText)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(viewModel.mostUrgent) { condition ->
                                Box(modifier = Modifier.width(160.dp)) {
                                    GuideCard(
                                        condition = condition,
                                        isBookmarked = bookmarkedIds.contains(condition.id),
                                        onClick = { onConditionClick(condition.id) },
                                        onBookmarkToggle = { viewModel.toggleBookmark(condition.id) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Guide Categories", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkForestText)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                ResourceCategoryCard(
                                    category = GuideCategory.FIRST_AID,
                                    label = "All",
                                    selected = selectedCategory == null,
                                    accentColor = PrimaryGreen,
                                    onClick = { viewModel.onCategorySelected(null) }
                                )
                            }
                            items(GuideCategory.entries) { category ->
                                ResourceCategoryCard(
                                    category = category,
                                    selected = selectedCategory == category,
                                    accentColor = PrimaryGreen,
                                    onClick = { viewModel.onCategorySelected(category) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        SeverityFilterRow(
                            selected = severityFilter,
                            onSelect = viewModel::onSeverityFilterChanged
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text("All Emergency Guides", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkForestText)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(filteredConditions) { condition ->
                GuideCard(
                    condition = condition,
                    isBookmarked = bookmarkedIds.contains(condition.id),
                    onClick = { onConditionClick(condition.id) },
                    onBookmarkToggle = { viewModel.toggleBookmark(condition.id) }
                )
            }
        }
    }
}

@Composable
private fun LocationEmergencyBar(countryCode: String?, emergencyNumber: String, onCallClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EmergencyRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Detected location", tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = countryCode?.let { "Emergency Location: $it" } ?: "Location Detecting...",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.5.sp
                )
                Text(
                    text = "Hotline: $emergencyNumber",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            FilledIconButton(
                onClick = onCallClick,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = EmergencyRed)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call emergency number")
            }
        }
    }
}

@Composable
private fun NoResultsFallback(onStartAssessment: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("No exact guide match found", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkForestText)
            Text(
                "Not sure what to do? Take a quick symptom assessment to find the right guide.",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Button(
                onClick = onStartAssessment,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start Quick Assessment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeverityFilterRow(selected: SeverityTag?, onSelect: (SeverityTag?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White)
        )
        FilterChip(
            selected = selected == SeverityTag.RED,
            onClick = { onSelect(SeverityTag.RED) },
            label = { Text("Critical") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmergencyRed, selectedLabelColor = Color.White)
        )
        FilterChip(
            selected = selected == SeverityTag.BLUE,
            onClick = { onSelect(SeverityTag.BLUE) },
            label = { Text("Moderate") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White)
        )
        FilterChip(
            selected = selected == SeverityTag.GREEN,
            onClick = { onSelect(SeverityTag.GREEN) },
            label = { Text("Minor") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White)
        )
    }
}

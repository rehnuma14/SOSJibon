package com.example.sosjibon.elibrary.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.elibrary.resources.components.GuideCard
import com.example.sosjibon.elibrary.resources.components.ResourceCategoryCard
import com.example.sosjibon.elibrary.resources.components.ResourceSearchBar
import com.example.sosjibon.elibrary.resources.data.GuideCategory
import com.example.sosjibon.elibrary.resources.data.SeverityTag

private val EmergencyRed = Color(0xFFD92D20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceCenterScreen(
    onConditionClick: (conditionId: String) -> Unit,
    onStartAssessment: () -> Unit = {},
    viewModel: ResourceViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val severityFilter by viewModel.severityFilter.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val savedConditions by viewModel.savedConditions.collectAsState()
    val filteredConditions by viewModel.filteredConditions.collectAsState()

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val minCardWidth = if (maxWidth >= 800.dp) 220.dp else if (maxWidth >= 600.dp) 180.dp else 160.dp

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minCardWidth),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ResourceSearchBar(
                            query = searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // SAVED GUIDES SECTION (BOOKMARKS)
                        if (savedConditions.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Saved Guides (${savedConditions.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = darkForestText
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(savedConditions) { condition ->
                                    Box(modifier = Modifier.width(170.dp)) {
                                        GuideCard(
                                            condition = condition,
                                            isBookmarked = true,
                                            onClick = { onConditionClick(condition.id) },
                                            onBookmarkToggle = { viewModel.toggleBookmark(condition.id) }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text("Guide Categories", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = darkForestText)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                ResourceCategoryCard(
                                    category = GuideCategory.FIRST_AID,
                                    label = "All",
                                    selected = selectedCategory == null,
                                    accentColor = primaryGreen,
                                    onClick = { viewModel.onCategorySelected(null) }
                                )
                            }
                            items(GuideCategory.entries) { category ->
                                ResourceCategoryCard(
                                    category = category,
                                    selected = selectedCategory == category,
                                    accentColor = primaryGreen,
                                    onClick = { viewModel.onCategorySelected(category) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        SeverityFilterRow(
                            selected = severityFilter,
                            primaryGreen = primaryGreen,
                            onSelect = viewModel::onSeverityFilterChanged
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("All First Aid Guides (${filteredConditions.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = darkForestText)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeverityFilterRow(
    selected: SeverityTag?,
    primaryGreen: Color,
    onSelect: (SeverityTag?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryGreen, selectedLabelColor = Color.White)
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
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryGreen, selectedLabelColor = Color.White)
        )
        FilterChip(
            selected = selected == SeverityTag.GREEN,
            onClick = { onSelect(SeverityTag.GREEN) },
            label = { Text("Minor") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White)
        )
    }
}

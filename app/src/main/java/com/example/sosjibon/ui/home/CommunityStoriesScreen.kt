package com.example.sosjibon.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.data.content.CommunityStory
import com.example.sosjibon.data.content.sampleCommunityStories

private val PrimaryRed = Color(0xFFE5484D)
private val BlueAccent = Color(0xFF4D8DFF)
private val GreenAccent = Color(0xFF159A6C)
private val OrangeAccent = Color(0xFFFFB020)
private val PurpleAccent = Color(0xFF9B7BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityStoriesScreen(onBack: () -> Unit = {}) {
    var selectedStoryId by rememberSaveable { mutableStateOf<String?>(null) }
    val activeStory = sampleCommunityStories.find { it.id == selectedStoryId }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val primaryGreen = MaterialTheme.colorScheme.primary

    if (activeStory != null) {
        StoryDetailScreen(story = activeStory, onBack = { selectedStoryId = null })
    } else {
        Scaffold(
            containerColor = pageBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Community Stories",
                            color = textDark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = primaryGreen
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(sampleCommunityStories) { story ->
                    StoryCard(story = story, onClick = { selectedStoryId = story.id })
                }
            }
        }
    }
}

@Composable
private fun StoryCard(story: CommunityStory, onClick: () -> Unit) {
    val keyword = getStoryKeyword(story)
    val accentColor = getKeywordColor(keyword)

    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(accentColor)
            )

            Column(modifier = Modifier.padding(18.dp)) {
                KeywordChip(text = keyword, color = accentColor)

                Spacer(modifier = Modifier.height(11.dp))

                Text(
                    text = story.title,
                    color = textDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(9.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Author",
                        tint = textGray,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "By ${story.authorName}",
                        color = textGray,
                        fontSize = 12.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = story.excerpt,
                    color = textGray,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Read Full Story",
                        color = primaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Read Full Story",
                        tint = primaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoryDetailScreen(story: CommunityStory, onBack: () -> Unit) {
    val keyword = getStoryKeyword(story)
    val accentColor = getKeywordColor(keyword)

    val pageBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Story Details",
                        color = textDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    KeywordChip(text = keyword, color = accentColor)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = story.title,
                        color = textDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 29.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Author",
                            tint = textGray,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Shared by ${story.authorName}",
                            color = textGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = textGray.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = story.excerpt,
                        color = textDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KeywordChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getStoryKeyword(story: CommunityStory): String {
    return when {
        story.title.contains("Accident", ignoreCase = true) -> "Accident Emergency"
        story.title.contains("Snake", ignoreCase = true) -> "Wildlife Hazard"
        story.title.contains("Cardiac", ignoreCase = true) || story.title.contains("Heart", ignoreCase = true) -> "Critical Care"
        story.title.contains("Burn", ignoreCase = true) -> "First Aid"
        else -> "Healthcare Experience"
    }
}

private fun getKeywordColor(keyword: String): Color {
    return when (keyword) {
        "Accident Emergency" -> PrimaryRed
        "Wildlife Hazard" -> OrangeAccent
        "Critical Care" -> PurpleAccent
        "First Aid" -> GreenAccent
        else -> BlueAccent
    }
}

package com.example.sosjibon.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.data.content.CommunityStory
import com.example.sosjibon.data.content.sampleCommunityStories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private val PrimaryRed = Color(0xFFE5484D)
private val BlueAccent = Color(0xFF4D8DFF)
private val GreenAccent = Color(0xFF159A6C)
private val OrangeAccent = Color(0xFFFFB020)
private val PurpleAccent = Color(0xFF9B7BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityStoriesScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUid = currentUser?.uid ?: ""
    val currentName = currentUser?.displayName ?: ""

    var selectedStoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var storyToEdit by remember { mutableStateOf<CommunityStory?>(null) }
    var storyToDelete by remember { mutableStateOf<CommunityStory?>(null) }

    val publishedStories = remember { mutableStateListOf<CommunityStory>().apply { addAll(sampleCommunityStories) } }
    val userPendingStories = remember { mutableStateListOf<CommunityStory>() }

    val activeStory = publishedStories.find { it.id == selectedStoryId } ?: sampleCommunityStories.find { it.id == selectedStoryId }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val primaryGreen = MaterialTheme.colorScheme.primary

    // Real-time Listener for Firestore Community Stories
    DisposableEffect(currentUid) {
        val db = FirebaseFirestore.getInstance()

        // 1. Published & Approved Stories Collection
        val listener1 = db.collection("community_stories")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firestoreStories = snapshot.documents.mapNotNull { doc -> docToStory(doc) }
                    firestoreStories.forEach { fs ->
                        if (publishedStories.none { it.id == fs.id }) {
                            publishedStories.add(0, fs)
                        } else {
                            val idx = publishedStories.indexOfFirst { it.id == fs.id }
                            if (idx != -1) publishedStories[idx] = fs
                        }
                    }
                }
            }

        // 2. Pending Stories Collection (Only for Author's Pending Review status)
        val listener2 = db.collection("pending_community_stories")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    userPendingStories.clear()
                    val pendingItems = snapshot.documents.mapNotNull { doc -> docToStory(doc) }
                        .filter { it.authorUid == currentUid || (currentUid.isBlank() && it.authorName == currentName) }
                    userPendingStories.addAll(pendingItems)
                }
            }

        onDispose {
            listener1.remove()
            listener2.remove()
        }
    }

    if (activeStory != null) {
        StoryDetailScreen(
            story = activeStory,
            currentUid = currentUid,
            currentName = currentName,
            onBack = { selectedStoryId = null },
            onEdit = { storyToEdit = it },
            onDelete = { storyToDelete = it }
        )
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
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showSubmitDialog = true },
                    containerColor = primaryGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Share Story")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Story", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (userPendingStories.isNotEmpty()) {
                    item {
                        Text(
                            text = "⏳ My Pending Submissions (Awaiting Admin Review)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(userPendingStories) { pendingStory ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStoryId = pendingStory.id },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pendingStory.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFD97706).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "⏳ Pending Admin Review",
                                            color = Color(0xFFD97706),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = pendingStory.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )

                                Text(
                                    text = pendingStory.excerpt,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                    }
                }

                items(publishedStories) { story ->
                    val isMyStory = (story.authorUid.isNotBlank() && story.authorUid == currentUid) ||
                        (currentName.isNotBlank() && story.authorName.contains(currentName, ignoreCase = true))

                    StoryCard(
                        story = story,
                        isMyStory = isMyStory,
                        onClick = { selectedStoryId = story.id },
                        onEdit = { storyToEdit = story },
                        onDelete = { storyToDelete = story }
                    )
                }
            }
        }
    }

    // SUBMIT NEW STORY DIALOG
    if (showSubmitDialog) {
        SubmitStoryDialog(
            onDismiss = { showSubmitDialog = false },
            onSubmit = { newStory ->
                showSubmitDialog = false

                // Save story to Firebase Cloud Firestore pending collection
                scope.launch(Dispatchers.IO) {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("pending_community_stories").document(newStory.id).set(newStory).await()
                    } catch (_: Exception) {}
                }

                Toast.makeText(context, "Story submitted! Awaiting Admin approval in Firebase Console.", Toast.LENGTH_LONG).show()
            },
            currentUid = currentUid,
            currentName = currentName,
            primaryGreen = primaryGreen,
            textDark = textDark
        )
    }

    // EDIT STORY DIALOG
    storyToEdit?.let { story ->
        EditStoryDialog(
            story = story,
            onDismiss = { storyToEdit = null },
            onSave = { updated ->
                storyToEdit = null

                // Update in publishedStories list locally
                val idx = publishedStories.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    publishedStories[idx] = updated
                }

                // Sync update to Cloud Firestore
                scope.launch(Dispatchers.IO) {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("community_stories").document(updated.id).set(updated).await()
                        db.collection("pending_community_stories").document(updated.id).set(updated).await()
                    } catch (_: Exception) {}
                }

                Toast.makeText(context, "Story updated successfully! ✓", Toast.LENGTH_SHORT).show()
            },
            primaryGreen = primaryGreen,
            textDark = textDark
        )
    }

    // DELETE CONFIRMATION DIALOG
    storyToDelete?.let { story ->
        AlertDialog(
            onDismissRequest = { storyToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryRed) },
            title = { Text("Delete Your Story?", fontWeight = FontWeight.Bold, color = textDark) },
            text = { Text("Are you sure you want to delete '${story.title}'? This action cannot be undone.", color = textDark) },
            confirmButton = {
                Button(
                    onClick = {
                        val target = storyToDelete
                        storyToDelete = null

                        if (target != null) {
                            publishedStories.removeAll { it.id == target.id }

                            if (selectedStoryId == target.id) {
                                selectedStoryId = null
                            }

                            // Delete from Cloud Firestore
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("community_stories").document(target.id).delete().await()
                                    db.collection("pending_community_stories").document(target.id).delete().await()
                                } catch (_: Exception) {}
                            }

                            Toast.makeText(context, "Story deleted from Community Stories.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Delete Story")
                }
            },
            dismissButton = {
                TextButton(onClick = { storyToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitStoryDialog(
    onDismiss: () -> Unit,
    onSubmit: (CommunityStory) -> Unit,
    currentUid: String,
    currentName: String,
    primaryGreen: Color,
    textDark: Color
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf(currentName.ifBlank { "Verified Member" }) }
    var category by remember { mutableStateOf("Healthcare Experience") }
    var excerpt by remember { mutableStateOf("") }

    val categories = listOf("Accident Emergency", "First Aid", "Critical Care", "Wildlife Hazard", "Healthcare Experience")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Your Experience", fontWeight = FontWeight.Bold, color = textDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Submitted stories are sent to Firebase for Admin approval before public publishing.", fontSize = 11.5.sp, color = Color.Gray)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Story Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Your Name / Role") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryGreen, selectedLabelColor = Color.White)
                        )
                    }
                }

                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Story Details & Experience") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && author.isNotBlank() && excerpt.isNotBlank(),
                onClick = {
                    val story = CommunityStory(
                        id = UUID.randomUUID().toString(),
                        authorUid = currentUid,
                        authorName = author.trim(),
                        title = title.trim(),
                        excerpt = excerpt.trim(),
                        category = category,
                        status = "pending",
                        timestamp = System.currentTimeMillis()
                    )
                    onSubmit(story)
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Submit for Admin Review", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditStoryDialog(
    story: CommunityStory,
    onDismiss: () -> Unit,
    onSave: (CommunityStory) -> Unit,
    primaryGreen: Color,
    textDark: Color
) {
    var title by remember { mutableStateOf(story.title) }
    var author by remember { mutableStateOf(story.authorName) }
    var category by remember { mutableStateOf(story.category) }
    var excerpt by remember { mutableStateOf(story.excerpt) }

    val categories = listOf("Accident Emergency", "First Aid", "Critical Care", "Wildlife Hazard", "Healthcare Experience")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Your Story", fontWeight = FontWeight.Bold, color = textDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Story Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Your Name / Role") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryGreen, selectedLabelColor = Color.White)
                        )
                    }
                }

                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Story Details & Experience") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && author.isNotBlank() && excerpt.isNotBlank(),
                onClick = {
                    val updated = story.copy(
                        authorName = author.trim(),
                        title = title.trim(),
                        excerpt = excerpt.trim(),
                        category = category
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun StoryCard(
    story: CommunityStory,
    isMyStory: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val keyword = story.category.ifBlank { getStoryKeyword(story) }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KeywordChip(text = keyword, color = accentColor)

                    if (isMyStory) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Story", tint = primaryGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Story", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

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
                        text = if (isMyStory) "By ${story.authorName} (You)" else "By ${story.authorName}",
                        color = if (isMyStory) primaryGreen else textGray,
                        fontWeight = if (isMyStory) FontWeight.Bold else FontWeight.Normal,
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
private fun StoryDetailScreen(
    story: CommunityStory,
    currentUid: String,
    currentName: String,
    onBack: () -> Unit,
    onEdit: (CommunityStory) -> Unit,
    onDelete: (CommunityStory) -> Unit
) {
    val keyword = story.category.ifBlank { getStoryKeyword(story) }
    val accentColor = getKeywordColor(keyword)

    val isMyStory = (story.authorUid.isNotBlank() && story.authorUid == currentUid) ||
        (currentName.isNotBlank() && story.authorName.contains(currentName, ignoreCase = true))

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
                actions = {
                    if (isMyStory) {
                        IconButton(onClick = { onEdit(story) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryGreen)
                        }
                        IconButton(onClick = { onDelete(story) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed)
                        }
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
                            text = if (isMyStory) "Shared by ${story.authorName} (You)" else "Shared by ${story.authorName}",
                            color = if (isMyStory) primaryGreen else textGray,
                            fontSize = 13.sp,
                            fontWeight = if (isMyStory) FontWeight.Bold else FontWeight.Medium
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

private fun docToStory(doc: DocumentSnapshot): CommunityStory? {
    try {
        val story = doc.toObject(CommunityStory::class.java)
        if (story != null && story.title.isNotBlank()) {
            return story.copy(id = doc.id)
        }
    } catch (_: Exception) {}

    val title = doc.getString("title") ?: doc.getString("name") ?: ""
    if (title.isBlank()) return null

    val author = doc.getString("authorName") ?: doc.getString("author") ?: doc.getString("user") ?: "Verified Member"
    val authorUid = doc.getString("authorUid") ?: ""
    val excerpt = doc.getString("excerpt") ?: doc.getString("details") ?: doc.getString("description") ?: doc.getString("content") ?: ""
    val category = doc.getString("category") ?: "Healthcare Experience"
    val status = doc.getString("status") ?: "published"

    return CommunityStory(
        id = doc.id,
        authorUid = authorUid,
        authorName = author,
        title = title,
        excerpt = excerpt,
        category = category,
        status = status
    )
}

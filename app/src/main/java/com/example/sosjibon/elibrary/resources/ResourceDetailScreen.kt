package com.example.sosjibon.elibrary.resources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.elibrary.resources.components.DoDontCard
import com.example.sosjibon.elibrary.resources.components.GuideLanguage
import com.example.sosjibon.elibrary.resources.components.GuideStepCard
import com.example.sosjibon.elibrary.resources.components.LanguageToggle
import com.example.sosjibon.elibrary.resources.components.WarningCard
import com.example.sosjibon.elibrary.resources.data.EmergencyCondition
import com.example.sosjibon.elibrary.resources.data.GuideStep
import com.example.sosjibon.elibrary.resources.data.SeverityTag

private enum class ReadingMode { STEPPER, FULL_PAGE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceDetailScreen(
    conditionId: String,
    onBack: () -> Unit,
    onCallEmergency: () -> Unit,
    onRelatedGuideClick: (String) -> Unit = {},
    viewModel: ResourceViewModel = viewModel()
) {
    val condition = viewModel.getCondition(conditionId)
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    var readingMode by remember { mutableStateOf(ReadingMode.STEPPER) }
    var language by remember { mutableStateOf(GuideLanguage.ENGLISH) }
    var stepperIndex by remember { mutableStateOf(0) }

    if (condition == null) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Guide not found.")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
        return
    }

    val accent = when (condition.severity) {
        SeverityTag.RED -> Color(0xFFD32F2F)
        SeverityTag.BLUE -> Color(0xFF1565C0)
        SeverityTag.GREEN -> Color(0xFF2E7D32)
    }
    val isBookmarked = bookmarkedIds.contains(condition.id)
    val displayTitle = if (language == GuideLanguage.BENGALI) condition.titleBn else condition.title

    Scaffold(
        containerColor = Color(0xFFF8FCFA),
        topBar = {
            TopAppBar(
                title = { Text(displayTitle, fontWeight = FontWeight.Bold, color = Color(0xFF17332A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF159A6C)) }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark(condition.id) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                            tint = Color(0xFF159A6C)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FCFA), titleContentColor = Color(0xFF17332A))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCallEmergency,
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                text = { Text("Call Emergency") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageToggle(selected = language, onLanguageChange = { language = it })
                TextButton(onClick = {
                    readingMode = if (readingMode == ReadingMode.STEPPER) ReadingMode.FULL_PAGE else ReadingMode.STEPPER
                    stepperIndex = 0
                }) {
                    Text(if (readingMode == ReadingMode.STEPPER) "Read Full Page" else "Switch to Stepper")
                }
            }

            when (readingMode) {
                ReadingMode.STEPPER -> StepperView(
                    stages = buildStages(
                        condition.whatHappened, condition.recognizeSigns, condition.doThis,
                        condition.dontDoThis, condition.whenToGetHelp, accent,
                        relatedTitles = relatedTitlesFor(condition.relatedConditionIds, viewModel),
                        onRelatedClick = onRelatedGuideClick
                    ),
                    currentIndex = stepperIndex,
                    onNext = { stepperIndex = (stepperIndex + 1).coerceAtMost(4) },
                    onPrevious = { stepperIndex = (stepperIndex - 1).coerceAtLeast(0) }
                )
                ReadingMode.FULL_PAGE -> FullPageView(
                    condition = condition,
                    accent = accent,
                    relatedTitles = relatedTitlesFor(condition.relatedConditionIds, viewModel),
                    onRelatedClick = onRelatedGuideClick
                )
            }
        }
    }
}

private fun relatedTitlesFor(ids: List<String>, viewModel: ResourceViewModel): List<Pair<String, String>> =
    ids.mapNotNull { id -> viewModel.getCondition(id)?.let { id to it.title } }

@Composable
private fun RelatedGuidesRow(relatedTitles: List<Pair<String, String>>, onRelatedClick: (String) -> Unit) {
    if (relatedTitles.isEmpty()) return
    Column {
        Text("See Also", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            relatedTitles.forEach { (id, title) ->
                AssistChip(onClick = { onRelatedClick(id) }, label = { Text(title, fontSize = 12.sp) })
            }
        }
    }
}

private data class Stage(val title: String, val content: @Composable () -> Unit)

@Composable
private fun buildStages(
    whatHappened: String,
    recognizeSigns: List<String>,
    doThis: List<GuideStep>,
    dontDoThis: List<String>,
    whenToGetHelp: String,
    accent: Color,
    relatedTitles: List<Pair<String, String>>,
    onRelatedClick: (String) -> Unit
): List<Stage> = listOf(
    Stage("What Happened") { Text(whatHappened, fontSize = 16.sp) },
    Stage("Recognize the Signs") {
        DoDontCard(title = "Signs to look for", items = recognizeSigns, accentColor = accent, backgroundColor = Color(0xFFF5F5F5))
    },
    Stage("Do This") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            doThis.forEach { step -> GuideStepCard(step = step, accentColor = accent) }
        }
    },
    Stage("Don't Do This") {
        DoDontCard(title = "Avoid these", items = dontDoThis, accentColor = Color(0xFFD32F2F), backgroundColor = Color(0xFFFFEBEE))
    },
    Stage("When to Get Help") {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            WarningCard(message = whenToGetHelp)
            if (relatedTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                RelatedGuidesRow(relatedTitles = relatedTitles, onRelatedClick = onRelatedClick)
            }
        }
    }
)

@Composable
private fun StepperView(stages: List<Stage>, currentIndex: Int, onNext: () -> Unit, onPrevious: () -> Unit) {
    val stage = stages[currentIndex]
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("${currentIndex + 1} / ${stages.size}", fontSize = 12.sp, color = Color(0xFF9E9E9E))
        Spacer(modifier = Modifier.height(4.dp))
        Text(stage.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f)) { stage.content() }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (currentIndex > 0) {
                OutlinedButton(onClick = onPrevious) { Text("Back") }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            if (currentIndex < stages.size - 1) {
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }
}

@Composable
private fun FullPageView(
    condition: EmergencyCondition,
    accent: Color,
    relatedTitles: List<Pair<String, String>>,
    onRelatedClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(condition.whatHappened, fontSize = 15.sp, color = Color(0xFF5F6368))
            Spacer(modifier = Modifier.height(12.dp))
            DoDontCard(title = "Recognize the Signs", items = condition.recognizeSigns, accentColor = accent, backgroundColor = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Do This", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(condition.doThis) { step ->
            GuideStepCard(step = step, accentColor = accent)
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            DoDontCard(title = "Don't Do This", items = condition.dontDoThis, accentColor = Color(0xFFD32F2F), backgroundColor = Color(0xFFFFEBEE))
            Spacer(modifier = Modifier.height(12.dp))
            WarningCard(message = condition.whenToGetHelp)
            if (relatedTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                RelatedGuidesRow(relatedTitles = relatedTitles, onRelatedClick = onRelatedClick)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

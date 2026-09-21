package com.example.sosjibon.elibrary.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.elibrary.resources.components.DoDontCard
import com.example.sosjibon.elibrary.resources.components.WarningCard
import com.example.sosjibon.elibrary.resources.data.EmergencyCondition
import com.example.sosjibon.elibrary.resources.data.GuideStep
import com.example.sosjibon.elibrary.resources.data.SeverityTag
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceDetailScreen(
    conditionId: String,
    onBack: () -> Unit,
    onCallEmergency: () -> Unit = {},
    onRelatedGuideClick: (String) -> Unit = {},
    viewModel: ResourceViewModel = viewModel()
) {
    val condition = viewModel.getCondition(conditionId)
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val scope = rememberCoroutineScope()

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val primaryGreen = MaterialTheme.colorScheme.primary

    if (condition == null) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Guide not found.", color = textDark)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
        return
    }

    val accent = when (condition.severity) {
        SeverityTag.RED -> Color(0xFFD32F2F)
        SeverityTag.BLUE -> primaryGreen
        SeverityTag.GREEN -> Color(0xFF2E7D32)
    }
    val isBookmarked = bookmarkedIds.contains(condition.id)
    val totalSteps = condition.doThis.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { totalSteps })

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = { Text(condition.title, fontWeight = FontWeight.Bold, color = textDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryGreen) }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark(condition.id) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg, titleContentColor = textDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // STEP PROGRESS HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step ${pagerState.currentPage + 1} of $totalSteps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryGreen
                )

                // Animated Pager Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(totalSteps) { page ->
                        val isSelected = pagerState.currentPage == page
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) primaryGreen else primaryGreen.copy(alpha = 0.25f))
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primaryGreen,
                trackColor = primaryGreen.copy(alpha = 0.15f)
            )

            // INTERACTIVE ANIMATED SWIPE CARDS PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val step = condition.doThis.getOrNull(page) ?: GuideStep(
                    stepNumber = page + 1,
                    instruction = condition.whatHappened,
                    imageRes = condition.cardIconRes,
                    imageDescription = condition.title
                )

                SwipeStepCard(
                    step = step,
                    condition = condition,
                    accentColor = accent
                )
            }

            // BOTTOM SWIPE CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, if (pagerState.currentPage > 0) primaryGreen else Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = if (pagerState.currentPage > 0) primaryGreen else Color(0xFF9CA3AF),
                        disabledContainerColor = Color(0xFFF3F4F6),
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Step",
                        tint = if (pagerState.currentPage > 0) primaryGreen else Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Previous",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pagerState.currentPage > 0) primaryGreen else Color(0xFF9CA3AF)
                    )
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < totalSteps - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pagerState.currentPage < totalSteps - 1) primaryGreen else Color(0xFF2E7D32),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < totalSteps - 1) "Next Step" else "Complete ✓",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (pagerState.currentPage < totalSteps - 1) Icons.Default.ChevronRight else Icons.Default.Check,
                        contentDescription = "Next Step",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeStepCard(
    step: GuideStep,
    condition: EmergencyCondition,
    accentColor: Color
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // IMAGE VISUALIZATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = step.imageRes),
                    contentDescription = step.imageDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                )
            }

            // STEP BADGE & TITLE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${step.stepNumber}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "STEP ${step.stepNumber} ACTION",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            // INSTRUCTION TEXT
            Text(
                text = step.instruction,
                fontSize = 15.sp,
                color = textDark,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            // SIGNS TO LOOK FOR ON STEP 1
            if (step.stepNumber == 1 && condition.recognizeSigns.isNotEmpty()) {
                DoDontCard(
                    title = "Signs to Recognize",
                    items = condition.recognizeSigns,
                    accentColor = accentColor
                )
            }

            // DONT DO THIS ON STEP 2 OR LATER
            if (step.stepNumber >= 2 && condition.dontDoThis.isNotEmpty()) {
                DoDontCard(
                    title = "Avoid Doing This",
                    items = condition.dontDoThis,
                    accentColor = Color(0xFFD32F2F)
                )
            }

            // WHEN TO GET HELP
            if (condition.whenToGetHelp.isNotBlank()) {
                WarningCard(message = condition.whenToGetHelp)
            }
        }
    }
}

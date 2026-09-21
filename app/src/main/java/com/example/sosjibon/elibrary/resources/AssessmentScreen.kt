package com.example.sosjibon.elibrary.resources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.elibrary.resources.data.FirstAidContent

private val EmergencyRed = Color(0xFFD92D20)
private val PrimaryGreen = Color(0xFF159A6C)

private sealed class AssessmentStep {
    object Consciousness : AssessmentStep()
    object Breathing : AssessmentStep()
    object MainSymptom : AssessmentStep()
    data class Result(val conditionId: String) : AssessmentStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentScreen(
    onOpenGuide: (conditionId: String) -> Unit,
    onCallEmergency: () -> Unit,
    onExit: () -> Unit
) {
    var step by remember { mutableStateOf<AssessmentStep>(AssessmentStep.Consciousness) }

    val pageBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = pageBg,
        topBar = {
            TopAppBar(
                title = { Text("Symptom Triage Assistant", fontWeight = FontWeight.ExtraBold, color = textDark) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pageBg,
                    titleContentColor = textDark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val current = step) {
                is AssessmentStep.Consciousness -> QuestionCard(
                    stepNumber = "1 of 2",
                    question = "Is the person responding to voice or touch?",
                    optionA = "YES, Responsive",
                    optionB = "NO Response",
                    onOptionA = { step = AssessmentStep.MainSymptom },
                    onOptionB = { step = AssessmentStep.Breathing }
                )

                is AssessmentStep.Breathing -> QuestionCard(
                    stepNumber = "2 of 2",
                    question = "Is the person breathing normally?",
                    optionA = "YES, Breathing Normally",
                    optionB = "NO / Only Gasping",
                    onOptionA = { step = AssessmentStep.Result("unresponsive_breathing") },
                    onOptionB = { step = AssessmentStep.Result("unresponsive_not_breathing") }
                )

                is AssessmentStep.MainSymptom -> SymptomPicker(
                    onPicked = { conditionId -> step = AssessmentStep.Result(conditionId) }
                )

                is AssessmentStep.Result -> ResultView(
                    conditionId = current.conditionId,
                    onOpenGuide = onOpenGuide,
                    onCallEmergency = onCallEmergency,
                    onRestart = { step = AssessmentStep.Consciousness },
                    onExit = onExit
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    stepNumber: String,
    question: String,
    optionA: String,
    optionB: String,
    onOptionA: () -> Unit,
    onOptionB: () -> Unit
) {
    val textDark = MaterialTheme.colorScheme.onBackground
    val cardBg = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("QUESTION $stepNumber", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }

            Text(question, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textDark, lineHeight = 24.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onOptionA,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(optionA, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOptionB,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Text(optionB, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ColumnScope.SymptomPicker(onPicked: (String) -> Unit) {
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    val options = listOf(
        "😮 Choking (can't breathe/speak)" to "choking",
        "🩸 Severe Bleeding" to "bleeding",
        "🔥 Burn Injury" to "burns",
        "❤️ Chest Pain / Pressure" to "cardiac_emergency",
        "🧠 Seizure / Convulsions" to "seizure",
        "🫁 Difficulty Breathing (Wheezing)" to "asthma_attack",
        "🐝 Insect Bite or Sting" to "insect_bites",
        "🐕 Animal Bite" to "animal_bites",
        "🐍 Snake Bite" to "snake_bites",
        "🧪 Suspected Poisoning" to "poisoning",
        "🧠 Face / Arm / Speech Stroke" to "stroke",
        "☀️ Heat Stroke / Confusion" to "heat_stroke",
        "⚡ Electric Shock" to "electric_shock",
        "🩸 Diabetic Emergency" to "diabetes_emergency",
        "🏊 Near-Drowning" to "drowning"
    )

    Text("Select Main Symptom", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Tap the symptom that best describes the situation", fontSize = 12.sp, color = textGray)

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumnOptions(
        options = options,
        onPicked = onPicked,
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun LazyColumnOptions(
    options: List<Pair<String, String>>,
    onPicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textDark = MaterialTheme.colorScheme.onSurface
    val cardBg = MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(options) { (label, id) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPicked(id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = textDark)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultView(
    conditionId: String,
    onOpenGuide: (String) -> Unit,
    onCallEmergency: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val condition = FirstAidContent.getById(conditionId)
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(2.dp, EmergencyRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RECOMMENDED FIRST AID PROTOCOL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = EmergencyRed)
            }

            Text(
                condition?.title ?: "Unresponsive & Not Breathing",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark
            )

            Text(
                "This is first-aid guidance only. If the situation is life-threatening, call emergency services immediately.",
                fontSize = 12.5.sp,
                color = textGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onCallEmergency,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALL EMERGENCY HOTLINE NOW", fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp)
            }

            Button(
                onClick = { onOpenGuide(conditionId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("OPEN STEP-BY-STEP GUIDE", fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp)
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onRestart) { Text("Restart Triage", color = PrimaryGreen, fontWeight = FontWeight.Bold) }
                TextButton(onClick = onExit) { Text("Back to Resources", color = textGray) }
            }
        }
    }
}

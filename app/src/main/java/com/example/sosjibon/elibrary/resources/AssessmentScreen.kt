package com.example.sosjibon.elibrary.resources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sosjibon.elibrary.resources.data.FirstAidContent
import com.example.sosjibon.elibrary.ui.theme.EmergencyRed
import com.example.sosjibon.elibrary.ui.theme.TrustBlue

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

    Scaffold(
        containerColor = Color(0xFFF8FCFA),
        topBar = {
            TopAppBar(
                title = { Text("Quick Assessment", fontWeight = FontWeight.Bold, color = Color(0xFF17332A)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FCFA),
                    titleContentColor = Color(0xFF17332A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            when (val current = step) {
                is AssessmentStep.Consciousness -> QuestionCard(
                    question = "Is the person responding to voice or touch?",
                    optionA = "Yes, responsive",
                    optionB = "No response",
                    onOptionA = { step = AssessmentStep.MainSymptom },
                    onOptionB = { step = AssessmentStep.Breathing }
                )

                is AssessmentStep.Breathing -> QuestionCard(
                    question = "Is the person breathing normally?",
                    optionA = "Yes, breathing normally",
                    optionB = "No / only gasping",
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
    question: String,
    optionA: String,
    optionB: String,
    onOptionA: () -> Unit,
    onOptionB: () -> Unit
) {
    Text(question, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onOptionA,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TrustBlue)
    ) { Text(optionA, fontSize = 16.sp) }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onOptionB,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
    ) { Text(optionB, fontSize = 16.sp) }
}

@Composable
private fun ColumnScope.SymptomPicker(onPicked: (String) -> Unit) {
    val options = listOf(
        "Choking (can't breathe/speak)" to "choking",
        "Severe bleeding" to "bleeding",
        "Burn injury" to "burns",
        "Chest pain / pressure" to "cardiac_emergency",
        "Seizure / convulsions" to "seizure",
        "Difficulty breathing (wheezing)" to "asthma_attack",
        "Insect bite or sting" to "insect_bites",
        "Animal bite" to "animal_bites",
        "Snake bite" to "snake_bites",
        "Suspected poisoning" to "poisoning",
        "Face/arm/speech changes" to "stroke",
        "Very hot, confused (heat exposure)" to "heat_stroke",
        "Electric shock" to "electric_shock",
        "Diabetic and feeling unwell" to "diabetes_emergency",
        "Pulled from water / near-drowning" to "drowning"
    )

    Text("What is the main symptom?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
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
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (label, id) ->
            OutlinedButton(
                onClick = { onPicked(id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label, modifier = Modifier.fillMaxWidth())
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

    Text("Likely situation:", fontSize = 14.sp, color = Color(0xFF5F6368))
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        condition?.title ?: "Unresponsive & Not Breathing",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = EmergencyRed
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Text(
            "This is a guide only, not a medical diagnosis. If the person is " +
                "unconscious, not breathing normally, or you are unsure — call " +
                "emergency services first.",
            modifier = Modifier.padding(12.dp),
            fontSize = 13.sp
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = onCallEmergency,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
    ) { Text("Call Emergency Number Now") }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = { onOpenGuide(conditionId) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TrustBlue)
    ) { Text("Open Step-by-Step Guide") }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = onRestart) { Text("Restart Assessment") }
    TextButton(onClick = onExit) { Text("Back to Resource Center") }
}

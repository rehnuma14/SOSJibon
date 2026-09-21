package com.example.sosjibon.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.ai.AiAssistantController
import com.example.sosjibon.ai.IntentClassifier
import com.example.sosjibon.ui.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlin.math.roundToInt

private val PrimaryGreen = Color(0xFF159A6C)
private val DarkForest = Color(0xFF0D5C3A)
private val EmergencyRed = Color(0xFFD92D20)
private val AiPurple = Color(0xFF7C3AED)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val isEmergencyAlert: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatHeadOverlay(
    onNavigateToSos: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState by settingsViewModel.state.collectAsState()
    val authUser = FirebaseAuth.getInstance().currentUser
    val authEmail = authUser?.email?.trim()?.lowercase() ?: ""
    val isAdmin = authUser != null && authEmail == "admin@gmail.com"

    // Chat bot is ONLY accessible to regular logged-in users (not guests, not admins)
    val isRegularUser = authUser != null && !isAdmin && settingsState.isLoggedIn &&
            settingsState.profile.fullName != "Guest Member" && settingsState.profile.email.isNotBlank()

    if (!isRegularUser) {
        return
    }

    var isChatOpen by remember { mutableStateOf(false) }

    // Offset coordinates for dragging Chat Head
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Infinite pulse animation for Chat Head
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // FLOATING AI CHAT HEAD BUBBLE
        if (!isChatOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .padding(bottom = 88.dp, end = 16.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            ) {
                // Outer Pulsing Glow Circle
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(PrimaryGreen.copy(alpha = 0.45f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Main Floating Orb Button
                Surface(
                    onClick = { isChatOpen = true },
                    shape = CircleShape,
                    color = PrimaryGreen,
                    shadowElevation = 10.dp,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryGreen, AiPurple)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Medical Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // "AI" Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .background(EmergencyRed, RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(10.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("AI", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // FULL INTERACTIVE AI MEDICAL ASSISTANT MODAL SHEET
        if (isChatOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { isChatOpen = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                scrimColor = Color.Black.copy(alpha = 0.45f)
            ) {
                AiChatSheetContent(
                    onClose = { isChatOpen = false },
                    onNavigateToSos = {
                        isChatOpen = false
                        onNavigateToSos()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiChatSheetContent(
    onClose: () -> Unit,
    onNavigateToSos: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                sender = MessageSender.AI,
                text = "Hello! I am SOSJibon AI Healthcare & Triage Assistant 🤖.\nHow can I help you today? Ask about medical symptoms, first-aid steps, or emergency response guidance."
            )
        )
    }

    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
            .padding(horizontal = 16.dp)
    ) {
        // HEADER BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(PrimaryGreen, AiPurple)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SOSJibon AI Assistant",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.5.sp,
                            color = textDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(PrimaryGreen, RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("24/7 AI", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text(
                        text = "Realtime Triage & Healthcare Guidance",
                        fontSize = 11.sp,
                        color = textGray
                    )
                }
            }

            Row {
                IconButton(onClick = {
                    messages.clear()
                    messages.add(
                        AiChatMessage(
                            sender = MessageSender.AI,
                            text = "Chat history cleared. What healthcare question can I assist you with?"
                        )
                    )
                }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = textGray)
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SUGGESTION CHIPS ROW
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val context = LocalContext.current
            SuggestionChipItem("🩺 Symptom Triage") {
                sendUserQuery("Can you help me check symptoms?", messages, context)
            }
            SuggestionChipItem("🩹 First Aid Guide") {
                sendUserQuery("What is the first aid for minor burn or bleeding?", messages, context)
            }
            SuggestionChipItem("🏥 Emergency Call") {
                sendUserQuery("What are the emergency numbers in Bangladesh?", messages, context)
            }
            SuggestionChipItem("💊 Medicine Info") {
                sendUserQuery("What should I do for severe headache?", messages, context)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CHAT MESSAGES LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onNavigateToSos = onNavigateToSos,
                    textDark = textDark
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val context = LocalContext.current
        // INPUT FIELD & SEND BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask medical question (e.g. burn, fever)...", fontSize = 12.5.sp, color = textGray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = textGray.copy(alpha = 0.35f),
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        sendUserQuery(inputText, messages, context)
                        inputText = ""
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = {
                    if (inputText.isNotBlank()) {
                        sendUserQuery(inputText, messages, context)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank(),
                shape = CircleShape,
                color = PrimaryGreen,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionChipItem(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: AiChatMessage,
    onNavigateToSos: () -> Unit,
    textDark: Color
) {
    val isUser = message.sender == MessageSender.USER
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (!isUser && message.isEmergencyAlert) BorderStroke(1.5.dp, EmergencyRed) else null,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SOSJibon AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else textDark,
                    fontWeight = FontWeight.Normal
                )

                if (message.isEmergencyAlert) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, EmergencyRed)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CRITICAL SYMPTOM ALERT", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = EmergencyRed)
                            }
                            Text(
                                text = "If you or the patient are experiencing chest pain, difficulty breathing, or severe bleeding, seek immediate emergency help!",
                                fontSize = 11.sp,
                                color = textDark
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmergencyRed,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call 999", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    onClick = onNavigateToSos,
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkForest,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open SOS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sendUserQuery(query: String, messages: MutableList<AiChatMessage>, context: Context) {
    messages.add(AiChatMessage(sender = MessageSender.USER, text = query))

    val q = query.trim().lowercase()
    val isCritical = q.contains("chest pain") || q.contains("unconscious") || q.contains("heart attack") ||
            q.contains("stroke") || q.contains("bleeding") || q.contains("মাথা ঘোরানো") || q.contains("বুকে ব্যথা") ||
            q.contains("শ্বাসকষ্ট") || q.contains("অজ্ঞান") || q.contains("বিষ")

    var modelResponse: String? = null
    try {
        val classifier = IntentClassifier(context.applicationContext)
        val controller = AiAssistantController(classifier)
        val aiResult = controller.handleQuery(query)
        if (aiResult.message.isNotBlank() && !aiResult.message.contains("could not understand")) {
            modelResponse = aiResult.message
        }
    } catch (_: Exception) {}

    val responseText = modelResponse ?: when {
        q.contains("burn") || q.contains("পোড়া") -> {
            "🔥 **First Aid for Burns:**\n" +
                    "1. Cool the burn immediately with cool running tap water for 10-20 minutes.\n" +
                    "2. Do NOT apply ice, toothpaste, or oil.\n" +
                    "3. Cover loosely with a clean plastic wrap or sterile bandage.\n" +
                    "4. Take paracetamol for pain if needed and seek medical advice for severe blisters."
        }
        q.contains("fever") || q.contains("জ্বর") -> {
            "🌡️ **Fever Guidance:**\n" +
                    "1. Rest in a well-ventilated room and drink plenty of fluids (water, ORS, fruit juice).\n" +
                    "2. Wipe the body with lukewarm water (tepid sponging).\n" +
                    "3. Take Paracetamol (500mg) according to age guidelines.\n" +
                    "4. If fever exceeds 102°F or lasts over 3 days, consult a physician."
        }
        q.contains("number") || q.contains("emergency") || q.contains("কল") || q.contains("নম্বর") -> {
            "🚑 **Emergency Contact Numbers (Bangladesh):**\n" +
                    "• Emergency Service: 999\n" +
                    "• Health Call Center (Shastho Batayon): 16263\n" +
                    "• IEDCR Helpline: 10655\n" +
                    "• National Helpline: 109"
        }
        q.contains("chest pain") || q.contains("বুকে ব্যথা") -> {
            "🚨 **WARNING: Potential Cardiac Emergency!**\n" +
                    "1. Have the person sit down and remain calm.\n" +
                    "2. Loosen tight clothing around chest and neck.\n" +
                    "3. Call 999 or go to the nearest emergency hospital immediately."
        }
        q.contains("bleed") || q.contains("রক্ত") -> {
            "🩸 **First Aid for Bleeding:**\n" +
                    "1. Apply firm, direct pressure on the wound using a clean cloth or bandage.\n" +
                    "2. Elevate the injured limb above heart level if possible.\n" +
                    "3. Maintain pressure continuously for 10-15 minutes without lifting the cloth."
        }
        q.contains("headache") || q.contains("মাথা ব্যথা") -> {
            "💆 **Headache Relief:**\n" +
                    "1. Rest in a quiet, dark room and stay hydrated.\n" +
                    "2. Apply a cold or warm compress to forehead or neck.\n" +
                    "3. Seek emergency medical care if accompanied by sudden neck stiffness, confusion, or speech loss."
        }
        else -> {
            "🩺 **Healthcare Guidance:**\n" +
                    "I am analyzing your query. For any medical symptoms, keep the patient comfortable, monitor vital signs, and consult a qualified medical professional. In case of emergency, call 999 or use the SOS button."
        }
    }

    messages.add(
        AiChatMessage(
            sender = MessageSender.AI,
            text = responseText,
            isEmergencyAlert = isCritical
        )
    )
}
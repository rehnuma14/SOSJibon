package com.example.sosjibon.ui.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.ai.gemini.GeminiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProTipScreen(
    onBack: () -> Unit,
    viewModel: GeminiViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var question by remember {
        mutableStateOf("")
    }

    var selectedLanguage by remember {
        mutableStateOf("English")
    }

    var languageMenuExpanded by remember {
        mutableStateOf(false)
    }

    var isListening by remember {
        mutableStateOf(false)
    }

    var selectedPdfName by remember {
        mutableStateOf<String?>(null)
    }

    val listState = rememberLazyListState()

    /*
     * =========================================================
     * PDF PICKER
     * =========================================================
     */

    val pdfPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                selectedPdfName =
                    uri.lastPathSegment
                        ?.substringAfterLast("/")
                        ?: "Selected PDF"

                viewModel.selectPdf(uri)
            }
        }

    /*
     * =========================================================
     * MICROPHONE PERMISSION STATE
     * =========================================================
     */

    var microphonePermissionGranted by remember {

        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    /*
     * =========================================================
     * SINGLE SPEECH RECOGNIZER
     * =========================================================
     */

    val speechRecognizer =
        remember(context) {

            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        }

    /*
     * =========================================================
     * SPEECH RECOGNITION LISTENER
     * =========================================================
     */

    DisposableEffect(speechRecognizer) {

        if (speechRecognizer != null) {

            speechRecognizer.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(
                        params: Bundle?
                    ) {
                        isListening = true
                    }

                    override fun onBeginningOfSpeech() {
                        isListening = true
                    }

                    override fun onRmsChanged(
                        rmsdB: Float
                    ) {
                        // Not required
                    }

                    override fun onBufferReceived(
                        buffer: ByteArray?
                    ) {
                        // Not required
                    }

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(
                        error: Int
                    ) {
                        isListening = false
                    }

                    override fun onResults(
                        results: Bundle?
                    ) {

                        val matches =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val result =
                            matches
                                ?.firstOrNull()
                                .orEmpty()

                        if (result.isNotBlank()) {
                            question = result
                        }

                        isListening = false
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {
                        // Partial results disabled
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {
                        // Not required
                    }
                }
            )
        }

        onDispose {

            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
            }

            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }

            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {
            }
        }
    }

    /*
     * =========================================================
     * MICROPHONE PERMISSION LAUNCHER
     * =========================================================
     */

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            microphonePermissionGranted = granted

            if (granted) {

                startSpeechRecognition(
                    speechRecognizer = speechRecognizer,
                    language =
                        if (selectedLanguage == "Bangla") {
                            "bn-BD"
                        } else {
                            "en-US"
                        },
                    onListeningChanged = {
                        isListening = it
                    }
                )
            }
        }

    /*
     * =========================================================
     * START LISTENING
     * =========================================================
     */

    fun startListening() {

        if (!microphonePermissionGranted) {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )

            return
        }

        if (speechRecognizer == null) {
            isListening = false
            return
        }

        startSpeechRecognition(
            speechRecognizer = speechRecognizer,
            language =
                if (selectedLanguage == "Bangla") {
                    "bn-BD"
                } else {
                    "en-US"
                },
            onListeningChanged = {
                isListening = it
            }
        )
    }

    /*
     * =========================================================
     * STOP LISTENING
     * =========================================================
     */

    fun stopListening() {

        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }

        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }

        isListening = false
    }

    /*
     * =========================================================
     * MAIN UI
     * =========================================================
     */

    Scaffold(

        modifier = Modifier.fillMaxSize(),

        /*
         * =====================================================
         * TOP BAR
         * =====================================================
         */

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = "Pro Tip",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "AI Health Assistant",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = {

                            stopListening()

                            onBack()
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.ArrowBack,

                            contentDescription =
                                "Back"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            )
        },

        /*
         * =====================================================
         * BOTTOM INPUT AREA
         *
         * IMPORTANT FIX:
         *
         * imePadding() moves this entire input area upward
         * when the keyboard appears.
         * =====================================================
         */

        bottomBar = {

            Surface(
                tonalElevation = 3.dp,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
            ) {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            )
                ) {

                    /*
                     * =================================================
                     * SELECTED PDF
                     * =================================================
                     */

                    if (selectedPdfName != null) {

                        SelectedPdfCard(
                            fileName = selectedPdfName!!,

                            onRemove = {

                                selectedPdfName = null

                                viewModel.clearPdf()
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )
                    }

                    /*
                     * =================================================
                     * LANGUAGE + PDF + VOICE
                     * =================================================
                     */

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        /*
                         * LANGUAGE
                         */

                        Box {

                            Button(
                                onClick = {
                                    languageMenuExpanded = true
                                },

                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .surfaceVariant,

                                            contentColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
                                        )
                            ) {

                                Text(
                                    text = selectedLanguage
                                )
                            }

                            DropdownMenu(
                                expanded =
                                    languageMenuExpanded,

                                onDismissRequest = {
                                    languageMenuExpanded = false
                                }
                            ) {

                                DropdownMenuItem(
                                    text = {
                                        Text("English")
                                    },

                                    onClick = {

                                        selectedLanguage =
                                            "English"

                                        languageMenuExpanded =
                                            false
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("বাংলা")
                                    },

                                    onClick = {

                                        selectedLanguage =
                                            "Bangla"

                                        languageMenuExpanded =
                                            false
                                    }
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )

                        /*
                         * PDF BUTTON
                         */

                        IconButton(
                            onClick = {

                                pdfPicker.launch(
                                    arrayOf(
                                        "application/pdf"
                                    )
                                )
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.AttachFile,

                                contentDescription =
                                    "Upload PDF"
                            )
                        }

                        /*
                         * VOICE BUTTON
                         */

                        IconButton(
                            onClick = {

                                if (isListening) {
                                    stopListening()
                                } else {
                                    startListening()
                                }
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (isListening) {
                                        Icons.Outlined.Stop
                                    } else {
                                        Icons.Outlined.KeyboardVoice
                                    },

                                contentDescription =
                                    if (isListening) {
                                        "Stop listening"
                                    } else {
                                        "Voice input"
                                    },

                                tint =
                                    if (isListening) {
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                    } else {
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                    }
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    /*
                     * =================================================
                     * QUESTION + SEND
                     * =================================================
                     */

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.Bottom
                    ) {

                        OutlinedTextField(

                            value = question,

                            onValueChange = {
                                question = it
                            },

                            modifier =
                                Modifier.weight(1f),

                            placeholder = {
                                Text(
                                    text =
                                        "Ask a health question..."
                                )
                            },

                            maxLines = 4,

                            trailingIcon = {

                                if (question.isNotBlank()) {

                                    IconButton(
                                        onClick = {
                                            question = ""
                                        }
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Outlined.Clear,

                                            contentDescription =
                                                "Clear"
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.size(8.dp)
                        )

                        Button(

                            onClick = {

                                stopListening()

                                viewModel.askGemini(
                                    question =
                                        question.trim()
                                )
                            },

                            enabled =
                                !uiState.isLoading &&
                                        (
                                                question.isNotBlank() ||
                                                        selectedPdfName != null
                                                ),

                            modifier =
                                Modifier.height(56.dp),

                            shape =
                                MaterialTheme.shapes.medium
                        ) {

                            if (uiState.isLoading) {

                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(22.dp),

                                    strokeWidth = 2.dp,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onPrimary
                                )

                            } else {

                                Icon(
                                    imageVector =
                                        Icons.Outlined.Send,

                                    contentDescription =
                                        "Ask Gemini"
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        /*
         * =========================================================
         * MAIN SCROLLABLE CONTENT
         * =========================================================
         */

        LazyColumn(

            state = listState,

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme
                            .colorScheme
                            .background
                    )
                    .padding(innerPadding),

            contentPadding =
                PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            /*
             * =====================================================
             * INTRO CARD
             * =====================================================
             */

            item {

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        androidx.compose.foundation
                            .shape
                            .RoundedCornerShape(18.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Text(
                            text =
                                "How can I help?",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "Ask a health question or upload a medical PDF. I will provide a concise explanation and useful next steps.",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer
                        )
                    }
                }
            }

            /*
             * =====================================================
             * LISTENING CARD
             * =====================================================
             */

            if (isListening) {

                item {

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .errorContainer
                            )
                    ) {

                        Row(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined
                                        .KeyboardVoice,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(10.dp)
                            )

                            Text(
                                text =
                                    "Listening... Speak clearly.",

                                fontWeight =
                                    FontWeight.Medium,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                            )
                        }
                    }
                }
            }

            /*
             * =====================================================
             * ERROR
             * =====================================================
             */

            if (!uiState.error.isNullOrBlank()) {

                item {

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .errorContainer
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                text =
                                    "Unable to get a response",

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(5.dp)
                            )

                            Text(
                                text =
                                    uiState.error!!,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                            )
                        }
                    }
                }
            }

            /*
             * =====================================================
             * GEMINI RESPONSE
             * =====================================================
             */

            if (uiState.response.isNotBlank()) {

                item {

                    GeminiResponseCard(
                        response =
                            uiState.response,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }

            /*
             * =====================================================
             * EMPTY STATE
             * =====================================================
             */

            if (
                uiState.response.isBlank() &&
                uiState.error.isNullOrBlank() &&
                !uiState.isLoading
            ) {

                item {

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    EmptyProTipState()
                }
            }
        }
    }
}


/*
 * =============================================================
 * SPEECH RECOGNITION
 * =============================================================
 *
 * This function DOES NOT create a SpeechRecognizer.
 *
 * It uses the single recognizer owned by ProTipScreen.
 * =============================================================
 */

private fun startSpeechRecognition(
    speechRecognizer: SpeechRecognizer?,
    language: String,
    onListeningChanged: (Boolean) -> Unit
) {

    if (speechRecognizer == null) {

        onListeningChanged(false)

        return
    }

    val intent =
        Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                language
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                language
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                1
            )
        }

    try {

        onListeningChanged(true)

        speechRecognizer.startListening(intent)

    } catch (_: Exception) {

        onListeningChanged(false)

        try {
            speechRecognizer.cancel()
        } catch (_: Exception) {
        }
    }
}


/*
 * =============================================================
 * SELECTED PDF CARD
 * =============================================================
 */

@Composable
private fun SelectedPdfCard(
    fileName: String,
    onRemove: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.AttachFile,

                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.size(8.dp)
            )

            Text(

                text = fileName,

                modifier =
                    Modifier.weight(1f),

                maxLines = 1,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.Medium
            )

            IconButton(
                onClick = onRemove
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Clear,

                    contentDescription =
                        "Remove PDF"
                )
            }
        }
    }
}


/*
 * =============================================================
 * EMPTY STATE
 * =============================================================
 */

@Composable
private fun EmptyProTipState() {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 24.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(

            text =
                "Your health assistant is ready",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.SemiBold
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(

            text =
                "Ask a question, use your voice, or attach a PDF to get started.",

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}
package com.example.sosjibon.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sosjibon.ai.AiUiState
import com.example.sosjibon.ai.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSheet(
    aiViewModel: AiViewModel,
    uiState: AiUiState,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {

    var query by remember {
        mutableStateOf("")
    }

    /*
     * Local AI navigation
     */
    LaunchedEffect(uiState.route) {

        val route = uiState.route

        if (!route.isNullOrBlank()) {

            onNavigate(route)

            aiViewModel.clearRoute()

            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /*
             * HEADER
             */
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null
                )

                Text(
                    text = "SOS Jibon AI",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )

                IconButton(
                    onClick = onDismiss
                ) {

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close AI"
                    )
                }
            }

            /*
             * PRO TIP
             *
             * This opens the Gemini-powered Pro Tip screen.
             */
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onNavigate("pro_tip")
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null
                )

                Text(
                    text = "  Pro Tip"
                )
            }

            Text(
                text = "Use Pro Tip for detailed health questions, explanations and document analysis.",
                style = MaterialTheme.typography.bodyMedium
            )

            /*
             * LOCAL AI RESPONSE
             */
            if (uiState.isLoading) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    CircularProgressIndicator()
                }

            } else if (uiState.message.isNotBlank()) {

                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 60.dp,
                            max = 220.dp
                        )
                )
            }

            /*
             * LOCAL AI QUESTION
             */
            OutlinedTextField(
                value = query,

                onValueChange = {
                    query = it
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Ask SOS Jibon")
                },

                placeholder = {
                    Text(
                        "Example: Open my medical vault"
                    )
                },

                enabled = !uiState.isLoading,

                maxLines = 4
            )

            /*
             * SEND
             */
            Button(
                onClick = {

                    val cleanQuery = query.trim()

                    if (cleanQuery.isNotEmpty()) {

                        aiViewModel.ask(cleanQuery)
                    }
                },

                modifier = Modifier.fillMaxWidth(),

                enabled =
                    query.isNotBlank() &&
                            !uiState.isLoading
            ) {

                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null
                )

                Text(
                    text = " Ask"
                )
            }
        }
    }
}
package com.rahvenz.ai.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rahvenz.ai.assistant.AssistantViewModel
import com.rahvenz.ai.assistant.VoiceState
import com.rahvenz.ai.ui.components.VoiceOrb
import com.rahvenz.ai.ui.theme.BackgroundBlack
import com.rahvenz.ai.ui.theme.Crimson
import com.rahvenz.ai.ui.theme.SurfaceCardLight
import com.rahvenz.ai.ui.theme.TextPrimary
import com.rahvenz.ai.ui.theme.TextSecondary

@Composable
fun AssistantScreen(vm: AssistantViewModel) {
    var input by rememberSaveable { mutableStateOf("") }
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
    val voiceState by vm.voiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.startListening()
        } else {
            showPermissionDialog = true
        }
    }

    val isListening = voiceState is VoiceState.Listening || voiceState is VoiceState.Processing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Voice Assistant", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text("I listen and reply in Hindi.", color = TextSecondary)

        Spacer(Modifier.height(40.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                if (isListening) {
                    vm.stopListening()
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        ) {
            VoiceOrb(active = isListening)
            Spacer(Modifier.height(14.dp))
            Text(
                text = when (voiceState) {
                    VoiceState.Idle -> "Tap the orb and speak in Hindi"
                    VoiceState.Listening -> "Listening… speak now"
                    VoiceState.Processing -> "Processing…"
                    is VoiceState.Error -> "Couldn't hear you. Tap to try again."
                },
                color = if (isListening) Crimson else TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "On some devices recognition uses an online service. The app never records in the background.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Bolo... (type in Hindi)") },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Crimson,
                unfocusedBorderColor = SurfaceCardLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedPlaceholderColor = TextSecondary,
                unfocusedPlaceholderColor = TextSecondary,
            ),
            trailingIcon = {
                IconButton(onClick = {
                    if (input.isNotBlank()) {
                        vm.sendUserText(input)
                        input = ""
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Crimson)
                }
            },
        )

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = { vm.stopAssistant() },
            border = BorderStroke(1.dp, Crimson),
        ) {
            Text("Stop speaking", color = TextPrimary)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            },
            title = { Text("Microphone needed") },
            text = {
                Text("RAHVENZ uses the microphone only while you tap the orb and speak. Nothing is recorded in the background.")
            },
        )
    }
}

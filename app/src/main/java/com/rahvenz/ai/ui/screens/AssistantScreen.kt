package com.rahvenz.ai.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var showVoiceDialog by rememberSaveable { mutableStateOf(false) }
    val voiceState by vm.voiceState.collectAsStateWithLifecycle()
    val callActive by vm.callActive.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.startCall()
        } else {
            showPermissionDialog = true
        }
    }

    val isActive = voiceState is VoiceState.Listening ||
        voiceState is VoiceState.Processing ||
        voiceState is VoiceState.Speaking

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text("RAHVENZ AI", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text(
            text = when {
                !callActive -> "Tap the orb to start a call"
                voiceState is VoiceState.Listening -> "Listening…"
                voiceState is VoiceState.Processing -> "Thinking…"
                voiceState is VoiceState.Speaking -> "Speaking…"
                voiceState is VoiceState.Error -> voiceErrorHint((voiceState as VoiceState.Error).code)
                else -> "Connected — tap the orb to speak"
            },
            color = if (isActive) Crimson else TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(36.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                if (callActive) {
                    vm.retryListening()
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        ) {
            VoiceOrb(active = isActive)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Free on-device voices. Recognition may use an online service on some devices.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.height(28.dp))

        if (callActive) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Crimson)
                    .clickable { vm.endCall() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        OutlinedButton(onClick = { showVoiceDialog = true }) {
            Text("Voice: ${vm.currentVoiceName()}", color = TextPrimary)
        }

        Spacer(Modifier.height(24.dp))
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
    }

    if (showVoiceDialog) {
        val recommended = vm.recommendedVoiceNames()
        val others = vm.otherVoiceNames()
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) { Text("Close") }
            },
            title = { Text("Choose a voice (free)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("★ Recommended Hindi voices", color = Crimson, style = MaterialTheme.typography.titleSmall)
                    if (recommended.isEmpty()) {
                        Text("No Hindi neural voice found.", color = TextSecondary)
                    } else {
                        recommended.forEach { name ->
                            TextButton(onClick = {
                                vm.selectVoice(name)
                                showVoiceDialog = false
                            }) { Text(name, color = TextPrimary) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Other voices", color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                    if (others.isEmpty()) {
                        Text("None.", color = TextSecondary)
                    } else {
                        others.forEach { name ->
                            TextButton(onClick = {
                                vm.selectVoice(name)
                                showVoiceDialog = false
                            }) { Text(name, color = TextPrimary) }
                        }
                    }
                }
            },
        )
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
                Text("RAHVENZ uses the microphone only during a call. Nothing is recorded in the background.")
            },
        )
    }
}

private fun voiceErrorHint(code: Int): String = when (code) {
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error ($code). Turn on mobile data."
    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that ($code). Speak a bit louder."
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission missing ($code)."
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard ($code). Tap and speak right away."
    SpeechRecognizer.ERROR_CLIENT -> "Speech service error ($code). Enable 'Google voice typing' in Settings."
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy ($code). One moment…"
    else -> "Couldn't hear you (code $code)."
}

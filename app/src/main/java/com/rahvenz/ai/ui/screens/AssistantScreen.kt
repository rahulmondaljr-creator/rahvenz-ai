package com.rahvenz.ai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rahvenz.ai.assistant.AssistantViewModel
import com.rahvenz.ai.ui.components.VoiceOrb
import com.rahvenz.ai.ui.theme.BackgroundBlack
import com.rahvenz.ai.ui.theme.Crimson
import com.rahvenz.ai.ui.theme.SurfaceCardLight
import com.rahvenz.ai.ui.theme.TextPrimary
import com.rahvenz.ai.ui.theme.TextSecondary

@Composable
fun AssistantScreen(vm: AssistantViewModel) {
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Voice Assistant", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Text("I listen and reply in Hindi.", color = TextSecondary)

        Spacer(Modifier.height(48.dp))
        VoiceOrb(active = false)
        Spacer(Modifier.height(24.dp))

        Text(
            "Tap-to-talk voice input arrives in Phase 2.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            "For now, type below — I will reply in spoken Hindi.",
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
}

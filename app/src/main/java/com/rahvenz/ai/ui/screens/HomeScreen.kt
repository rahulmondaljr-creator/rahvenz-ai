package com.rahvenz.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rahvenz.ai.assistant.AssistantViewModel
import com.rahvenz.ai.ui.components.VoiceOrb
import com.rahvenz.ai.ui.theme.BackgroundBlack
import com.rahvenz.ai.ui.theme.Crimson
import com.rahvenz.ai.ui.theme.SurfaceCard
import com.rahvenz.ai.ui.theme.SurfaceCardLight
import com.rahvenz.ai.ui.theme.TextPrimary
import com.rahvenz.ai.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun HomeScreen(vm: AssistantViewModel, onNavigate: (String) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var comingSoon by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            text = greetingText(),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
        )
        Text(
            text = "I'm RAHVENZ AI — your Hindi-speaking assistant.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Spacer(Modifier.height(36.dp))
        VoiceOrb(active = true)
        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ask RAHVENZ anything...") },
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
                        onNavigate("chat")
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Crimson)
                }
            },
        )

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureChip(label = "Voice Mode", icon = Icons.Default.Mic, onClick = { onNavigate("assistant") })
            FeatureChip(
                label = "Visual Lens",
                icon = Icons.Default.PhotoCamera,
                onClick = { comingSoon = "Visual Lens arrives in Phase 6 — camera image analysis." },
            )
            FeatureChip(
                label = "Quick Actions",
                icon = Icons.Default.FlashOn,
                onClick = { comingSoon = "Quick Actions arrive in Phase 7." },
            )
        }
    }

    comingSoon?.let { message ->
        AlertDialog(
            onDismissRequest = { comingSoon = null },
            confirmButton = {
                TextButton(onClick = { comingSoon = null }) { Text("OK") }
            },
            title = { Text("Coming soon") },
            text = { Text(message) },
        )
    }
}

private fun greetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Hello"
    }
}

@Composable
private fun FeatureChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceCard,
            contentColor = TextPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Crimson, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

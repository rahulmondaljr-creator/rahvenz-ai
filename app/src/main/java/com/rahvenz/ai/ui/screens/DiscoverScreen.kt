package com.rahvenz.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rahvenz.ai.ui.theme.BackgroundBlack
import com.rahvenz.ai.ui.theme.Crimson
import com.rahvenz.ai.ui.theme.SurfaceCard
import com.rahvenz.ai.ui.theme.TextPrimary
import com.rahvenz.ai.ui.theme.TextSecondary

private data class Capability(val title: String, val status: String)

@Composable
fun DiscoverScreen() {
    val capabilities = listOf(
        Capability("Local commands (time, date, greetings, stop)", "Available now"),
        Capability("Hindi voice recognition — tap the orb and speak", "Available now"),
        Capability("Open apps (Chrome, YouTube, Settings, WhatsApp...)", "Available now"),
        Capability("Gemini & Groq connectors (bring your own key)", "Phase 3"),
        Capability("AI chat with conversation history", "Phase 4"),
        Capability("Accessibility actions (scroll, back, tap)", "Phase 5"),
        Capability("Visual Lens camera analysis", "Phase 6"),
        Capability("Account login, OTP, password reset", "Phase 7"),
        Capability("Security hardening & full test suite", "Phase 8"),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            Text(
                "Discover RAHVENZ",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(capabilities) { cap ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(cap.title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        cap.status,
                        color = if (cap.status == "Available now") Crimson else TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

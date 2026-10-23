package com.rahvenz.ai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rahvenz.ai.ui.theme.Crimson

@Composable
fun VoiceOrb(active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")
    val pulse by transition.animateFloat(
        initialValue = if (active) 1f else 0.95f,
        targetValue = if (active) 1.14f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Box(modifier = modifier.size(170.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(170.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(Crimson.copy(alpha = 0.10f))
        )
        Box(
            Modifier
                .size(140.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(Crimson.copy(alpha = 0.18f))
        )
        Box(
            Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFFF4D6D), Color(0xFF5C0A1A))))
        )
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Microphone",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

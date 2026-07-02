package com.danielcastillo.calendariointeractivo.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danielcastillo.calendariointeractivo.ui.theme.Coral
import com.danielcastillo.calendariointeractivo.ui.theme.Gold
import com.danielcastillo.calendariointeractivo.ui.theme.Ink
import com.danielcastillo.calendariointeractivo.ui.theme.Iris
import com.danielcastillo.calendariointeractivo.ui.theme.Lagoon

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")

    val pulse by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Ink,
                        Color(0xFF102A43),
                        Color(0xFF0F766E),
                        Color(0xFF312E81)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Lagoon.copy(alpha = 0.30f),
                radius = size.minDimension * (0.25f + drift * 0.08f),
                center = Offset(size.width * (0.18f + drift * 0.12f), size.height * 0.18f)
            )

            drawCircle(
                color = Coral.copy(alpha = 0.22f),
                radius = size.minDimension * (0.20f + drift * 0.05f),
                center = Offset(size.width * 0.88f, size.height * (0.20f + drift * 0.12f))
            )

            drawCircle(
                color = Gold.copy(alpha = 0.24f),
                radius = size.minDimension * 0.18f,
                center = Offset(size.width * (0.64f - drift * 0.18f), size.height * 0.84f)
            )

            drawCircle(
                color = Iris.copy(alpha = 0.18f),
                radius = size.minDimension * 0.24f,
                center = Offset(size.width * 0.12f, size.height * 0.88f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(132.dp)
                    .scale(pulse),
                shape = RoundedCornerShape(38.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 24.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = Lagoon,
                        modifier = Modifier.size(68.dp)
                    )
                }
            }

            Text(
                text = "Calendario Interactivo",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Preparando tus eventos...",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
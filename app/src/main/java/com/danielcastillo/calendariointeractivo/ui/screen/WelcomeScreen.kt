package com.danielcastillo.calendariointeractivo.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danielcastillo.calendariointeractivo.ui.theme.Gold
import com.danielcastillo.calendariointeractivo.ui.theme.Ink
import com.danielcastillo.calendariointeractivo.ui.theme.Lagoon

@Composable
fun WelcomeScreen(
    onEnter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Ink,
                        Color(0xFF12323F),
                        Color(0xFF0F766E),
                        Color(0xFF172554)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Lagoon.copy(alpha = 0.24f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.15f, size.height * 0.16f)
            )

            drawCircle(
                color = Gold.copy(alpha = 0.18f),
                radius = size.minDimension * 0.22f,
                center = Offset(size.width * 0.82f, size.height * 0.82f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Surface(
                modifier = Modifier.size(118.dp),
                shape = RoundedCornerShape(34.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 18.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = Lagoon,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Calendario interactivo",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Organiza eventos, comparte planes y recibe recordatorios desde una app moderna y adaptable.",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onEnter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Ink
                ),
                contentPadding = PaddingValues(horizontal = 26.dp, vertical = 15.dp)
            ) {
                Text(
                    text = "Entrar",
                    fontWeight = FontWeight.Black
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null
                )
            }
        }

        Text(
            text = "Creado por Daniel Castillo",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
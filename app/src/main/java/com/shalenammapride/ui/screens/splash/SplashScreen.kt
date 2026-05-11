package com.shalenammapride.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shalenammapride.ui.theme.IndiaGreen
import com.shalenammapride.ui.theme.PureWhite
import com.shalenammapride.ui.theme.Saffron
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Saffron, PureWhite, IndiaGreen),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🏫",
                fontSize = 80.sp
            )
            Text(
                text = "Shale",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 40.sp
                )
            )
            Text(
                text = "ನಮ್ಮ ಹೆಮ್ಮೆ · Namma Pride",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = PureWhite.copy(alpha = 0.9f),
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Building trust through transparency",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PureWhite.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

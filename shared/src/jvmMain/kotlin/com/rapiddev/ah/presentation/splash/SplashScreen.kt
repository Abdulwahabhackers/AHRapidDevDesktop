package com.rapiddev.ah.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val aScale = remember { Animatable(0f) }
    val hScale = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        aScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        delay(100)
        hScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        delay(100)
        subtitleAlpha.animateTo(1f, tween(400))
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A2540),
                        Color(0xFF0B0F1A),
                        Color(0xFF000000)
                    ),
                    center = Offset.Unspecified,
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "H",
                    fontSize = 82.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF22D3EE),
                    modifier = Modifier.scale(aScale.value).alpha(aScale.value)
                )
                Text(
                    text = "A",
                    fontSize = 82.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFF0C674),
                    modifier = Modifier.scale(hScale.value).alpha(hScale.value)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "RapidDev",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                color = Color(0xFFF1F5F9),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .background(
                        color = Color(0xFF22D3EE).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "من الفكرة إلى تطبيق",
                    fontSize = 13.sp,
                    color = Color(0xFF22D3EE),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
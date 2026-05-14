package com.flo.readinglog.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun D20Face(
    roll: Int,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 80.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawPath(
                path = Path().apply {
                    moveTo(cx, 0f)
                    lineTo(size.width, cy)
                    lineTo(cx, size.height)
                    lineTo(0f, cy)
                    close()
                },
                color = color,
            )
        }
        Text(
            text = "$roll",
            fontSize = (sizeDp.value * 0.35f).sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

@Composable
fun SpinningD20(
    isSaving: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 80.dp,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    var displayedRoll by remember { mutableIntStateOf((1..20).random()) }

    LaunchedEffect(isSaving) {
        if (isSaving) {
            while (true) {
                displayedRoll = (1..20).random()
                delay(80)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "d20spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing)),
        label = "rotation",
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "scale",
    )

    D20Face(
        roll = displayedRoll,
        modifier = modifier.graphicsLayer {
            rotationZ = rotation
            scaleX = scale
            scaleY = scale
        },
        sizeDp = sizeDp,
        color = primaryColor,
    )
}

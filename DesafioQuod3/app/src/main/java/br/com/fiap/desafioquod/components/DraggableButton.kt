package br.com.fiap.desafioquod.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale


@Composable
fun DraggableButton(
    bgColor: Color,
    onClick: () -> Unit,
) {
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }

    // Animação de pulsação
    val animatedScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            animatedScale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            animatedScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    val configuration = LocalConfiguration.current
    val largura: Dp = configuration.screenWidthDp.dp
    val altura: Dp = configuration.screenHeightDp.dp
    val larguraElemento: Dp = 75.dp


    Box(
        modifier = Modifier
            .absoluteOffset(x = (largura - larguraElemento), y = (altura - larguraElemento) / 3)
            //.absoluteOffset(x = 200.dp, y = 80.dp)
            .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
            .scale(animatedScale.value)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX.value += dragAmount.x
                    offsetY.value += dragAmount.y
                }
            }
            .size(40.dp)
            .background(bgColor, CircleShape)
            .clip(CircleShape)
            .clickable { onClick() }
            .border(width = 1.dp, PurpleQuod)
            .zIndex(Float.MAX_VALUE),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.q),
            contentDescription = "contentDescription",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview
@Composable
fun PreviewDraggableButton() {
    DraggableButton(bgColor = Color.Blue, onClick = {})
}



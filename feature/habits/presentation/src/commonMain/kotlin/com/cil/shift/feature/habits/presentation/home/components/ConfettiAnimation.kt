package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val id: Int,
    val x: Float,
    val initialY: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val horizontalSpeed: Float,
    val fallSpeed: Float,
    val shape: ConfettiShape
)

enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    SQUARE
}

@Composable
fun ConfettiAnimation(
    isPlaying: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isPlaying) return

    val colors = listOf(
        Color(0xFF4E7CFF), // Blue
        Color(0xFF00D9FF), // Cyan
        Color(0xFF4ECDC4), // Teal
        Color(0xFFFF6B6B), // Red
        Color(0xFFFFA07A), // Orange
        Color(0xFFFFD93D), // Yellow
        Color(0xFF6BCB77), // Green
        Color(0xFFC9B1FF), // Purple
        Color(0xFFFF85A2), // Pink
    )

    val particleCount = 150

    // Generate particles once
    val particles = remember {
        List(particleCount) { index ->
            ConfettiParticle(
                id = index,
                x = Random.nextFloat(),
                initialY = Random.nextFloat() * -0.5f - 0.1f, // Start above screen
                size = Random.nextFloat() * 12f + 6f,
                color = colors[Random.nextInt(colors.size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 720f - 360f, // -360 to 360
                horizontalSpeed = Random.nextFloat() * 0.4f - 0.2f, // Sideways drift
                fallSpeed = Random.nextFloat() * 0.3f + 0.4f, // Fall speed variation
                shape = ConfettiShape.entries[Random.nextInt(ConfettiShape.entries.size)]
            )
        }
    }

    // Animation progress (0 to 1 over 3 seconds)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 4000,
                    easing = LinearEasing
                )
            )
            onAnimationEnd()
        }
    }

    val progress = animationProgress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            // Calculate current position based on progress
            val currentX = (particle.x + particle.horizontalSpeed * progress * sin(progress * 10f + particle.id)) * canvasWidth
            val currentY = (particle.initialY + progress * 1.5f * particle.fallSpeed) * canvasHeight

            // Only draw if particle is visible
            if (currentY > -particle.size && currentY < canvasHeight + particle.size) {
                val currentRotation = particle.rotation + particle.rotationSpeed * progress

                // Fade out near the end
                val alpha = when {
                    progress > 0.8f -> 1f - ((progress - 0.8f) / 0.2f)
                    else -> 1f
                }.coerceIn(0f, 1f)

                val particleColor = particle.color.copy(alpha = alpha)

                rotate(
                    degrees = currentRotation,
                    pivot = Offset(currentX, currentY)
                ) {
                    when (particle.shape) {
                        ConfettiShape.RECTANGLE -> {
                            drawRect(
                                color = particleColor,
                                topLeft = Offset(currentX - particle.size / 2, currentY - particle.size),
                                size = Size(particle.size, particle.size * 2)
                            )
                        }
                        ConfettiShape.CIRCLE -> {
                            drawCircle(
                                color = particleColor,
                                radius = particle.size / 2,
                                center = Offset(currentX, currentY)
                            )
                        }
                        ConfettiShape.SQUARE -> {
                            drawRect(
                                color = particleColor,
                                topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                                size = Size(particle.size, particle.size)
                            )
                        }
                    }
                }
            }
        }
    }
}

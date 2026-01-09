package com.cil.shift.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Coach mark step data
 */
data class CoachMarkStep(
    val id: String,
    val title: String,
    val description: String,
    val position: TooltipPosition = TooltipPosition.BOTTOM
)

enum class TooltipPosition {
    TOP, BOTTOM, LEFT, RIGHT, CENTER
}

/**
 * Coach mark controller to manage the guided tour
 */
class CoachMarkController {
    var isActive by mutableStateOf(false)
        private set

    var currentStepIndex by mutableStateOf(0)
        private set

    private var steps: List<CoachMarkStep> = emptyList()
    private var targetBounds: MutableMap<String, Rect> = mutableMapOf()
    private var onComplete: (() -> Unit)? = null

    val currentStep: CoachMarkStep?
        get() = steps.getOrNull(currentStepIndex)

    val totalSteps: Int
        get() = steps.size

    fun start(steps: List<CoachMarkStep>, onComplete: () -> Unit = {}) {
        this.steps = steps
        this.onComplete = onComplete
        currentStepIndex = 0
        isActive = true
    }

    fun next() {
        if (currentStepIndex < steps.size - 1) {
            currentStepIndex++
        } else {
            finish()
        }
    }

    fun previous() {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    fun skip() {
        finish()
    }

    private fun finish() {
        isActive = false
        currentStepIndex = 0
        onComplete?.invoke()
    }

    fun registerTarget(id: String, bounds: Rect) {
        targetBounds[id] = bounds
    }

    fun getTargetBounds(id: String): Rect? = targetBounds[id]
}

@Composable
fun rememberCoachMarkController(): CoachMarkController {
    return remember { CoachMarkController() }
}

/**
 * Modifier to register a component as a coach mark target
 */
fun Modifier.coachMarkTarget(
    controller: CoachMarkController,
    id: String
): Modifier = this.onGloballyPositioned { coordinates ->
    controller.registerTarget(id, coordinates.boundsInRoot())
}

/**
 * Coach Mark Overlay that shows the guided tour with pulsing circle highlight
 */
@Composable
fun CoachMarkOverlay(
    controller: CoachMarkController,
    strings: CoachMarkStrings,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var screenSize by remember { mutableStateOf(Size.Zero) }

    // Animation spec for smooth transitions between steps
    val transitionSpec = tween<Float>(durationMillis = 400, easing = EaseInOutCubic)

    // Pulsing animation for the highlight circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    AnimatedVisibility(
        visible = controller.isActive,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        val currentStep = controller.currentStep ?: return@AnimatedVisibility
        val targetBounds = controller.getTargetBounds(currentStep.id)

        // Animated spotlight bounds - smooth transition between steps
        val animatedLeft by animateFloatAsState(
            targetValue = targetBounds?.left ?: 0f,
            animationSpec = transitionSpec,
            label = "spotlightLeft"
        )
        val animatedTop by animateFloatAsState(
            targetValue = targetBounds?.top ?: 0f,
            animationSpec = transitionSpec,
            label = "spotlightTop"
        )
        val animatedRight by animateFloatAsState(
            targetValue = targetBounds?.right ?: 0f,
            animationSpec = transitionSpec,
            label = "spotlightRight"
        )
        val animatedBottom by animateFloatAsState(
            targetValue = targetBounds?.bottom ?: 0f,
            animationSpec = transitionSpec,
            label = "spotlightBottom"
        )

        // Animated bounds rect
        val animatedBounds = if (targetBounds != null) {
            Rect(animatedLeft, animatedTop, animatedRight, animatedBottom)
        } else null

        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    screenSize = Size(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* Consume taps on overlay */ }
                }
        ) {
            // Dark overlay with spotlight hole using Path
            Canvas(modifier = Modifier.fillMaxSize()) {
                val overlayColor = Color.Black.copy(alpha = 0.8f)

                if (animatedBounds != null) {
                    val padding = 12.dp.toPx()
                    val spotlightRect = Rect(
                        left = animatedBounds.left - padding,
                        top = animatedBounds.top - padding,
                        right = animatedBounds.right + padding,
                        bottom = animatedBounds.bottom + padding
                    )
                    val cornerRadius = 16.dp.toPx()

                    // Draw overlay with hole using Path and EvenOdd fill
                    val path = Path().apply {
                        fillType = PathFillType.EvenOdd
                        // Full screen rectangle
                        addRect(Rect(0f, 0f, size.width, size.height))
                        // Subtract the spotlight area (rounded rect) - EvenOdd makes this a hole
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                rect = spotlightRect,
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                            )
                        )
                    }

                    drawPath(
                        path = path,
                        color = overlayColor,
                        style = androidx.compose.ui.graphics.drawscope.Fill
                    )

                    // Draw pulsing glow effect around spotlight
                    val glowPadding = padding * pulseScale
                    val glowRect = Rect(
                        left = animatedBounds.left - glowPadding,
                        top = animatedBounds.top - glowPadding,
                        right = animatedBounds.right + glowPadding,
                        bottom = animatedBounds.bottom + glowPadding
                    )

                    // Outer glow
                    drawRoundRect(
                        color = Color(0xFF4E7CFF).copy(alpha = pulseAlpha * 0.5f),
                        topLeft = Offset(glowRect.left - 8.dp.toPx(), glowRect.top - 8.dp.toPx()),
                        size = Size(glowRect.width + 16.dp.toPx(), glowRect.height + 16.dp.toPx()),
                        cornerRadius = CornerRadius(cornerRadius + 8.dp.toPx()),
                        style = Stroke(width = 6.dp.toPx())
                    )

                    // Inner highlight border
                    drawRoundRect(
                        color = Color(0xFF4E7CFF).copy(alpha = 0.9f),
                        topLeft = Offset(spotlightRect.left, spotlightRect.top),
                        size = Size(spotlightRect.width, spotlightRect.height),
                        cornerRadius = CornerRadius(cornerRadius),
                        style = Stroke(width = 3.dp.toPx())
                    )
                } else {
                    // No target - just draw full overlay
                    drawRect(color = overlayColor)
                }
            }

            // Tooltip card
            val tooltipWidthDp = 300.dp
            val tooltipHeightDp = 180.dp
            val bottomNavHeight = 120.dp // Account for bottom nav with extra padding

            // Calculate target tooltip position based on current step bounds
            val (targetTooltipX, targetTooltipY) = if (targetBounds != null) {
                with(density) {
                    val bounds = targetBounds
                    val tooltipWidthPx = tooltipWidthDp.toPx()
                    val tooltipHeightPx = tooltipHeightDp.toPx()
                    val screenWidth = screenSize.width
                    val screenHeight = screenSize.height
                    val bottomNavHeightPx = bottomNavHeight.toPx()
                    val safeBottomPx = bottomNavHeightPx + 24.dp.toPx()
                    val spotlightPadding = 28.dp.toPx() // Extra padding to avoid overlap

                    when (currentStep.position) {
                        TooltipPosition.BOTTOM -> {
                            val x = (bounds.left + bounds.width / 2 - tooltipWidthPx / 2)
                                .coerceIn(16.dp.toPx(), (screenWidth - tooltipWidthPx - 16.dp.toPx()).coerceAtLeast(16.dp.toPx()))
                            var y = bounds.bottom + spotlightPadding
                            if (y + tooltipHeightPx > screenHeight - safeBottomPx) {
                                y = bounds.top - tooltipHeightPx - spotlightPadding
                            }
                            Pair(x, y.coerceAtLeast(60.dp.toPx()))
                        }
                        TooltipPosition.TOP -> {
                            val x = (bounds.left + bounds.width / 2 - tooltipWidthPx / 2)
                                .coerceIn(16.dp.toPx(), (screenWidth - tooltipWidthPx - 16.dp.toPx()).coerceAtLeast(16.dp.toPx()))
                            var y = bounds.top - tooltipHeightPx - spotlightPadding
                            if (y < 60.dp.toPx()) {
                                y = bounds.bottom + spotlightPadding
                            }
                            if (y + tooltipHeightPx > screenHeight - safeBottomPx) {
                                y = screenHeight - safeBottomPx - tooltipHeightPx
                            }
                            Pair(x, y.coerceAtLeast(60.dp.toPx()))
                        }
                        TooltipPosition.CENTER -> {
                            val x = (screenWidth - tooltipWidthPx) / 2
                            val y = (screenHeight - tooltipHeightPx - safeBottomPx) / 2
                            Pair(x.coerceAtLeast(16.dp.toPx()), y.coerceAtLeast(60.dp.toPx()))
                        }
                        else -> {
                            val x = (bounds.left + bounds.width / 2 - tooltipWidthPx / 2)
                                .coerceIn(16.dp.toPx(), (screenWidth - tooltipWidthPx - 16.dp.toPx()).coerceAtLeast(16.dp.toPx()))
                            var y = bounds.bottom + spotlightPadding
                            if (y + tooltipHeightPx > screenHeight - safeBottomPx) {
                                y = bounds.top - tooltipHeightPx - spotlightPadding
                            }
                            Pair(x, y.coerceAtLeast(60.dp.toPx()))
                        }
                    }
                }
            } else {
                // Center position when no bounds
                with(density) {
                    val tooltipWidthPx = tooltipWidthDp.toPx()
                    val tooltipHeightPx = tooltipHeightDp.toPx()
                    val bottomNavHeightPx = bottomNavHeight.toPx()
                    val safeBottomPx = bottomNavHeightPx + 24.dp.toPx()
                    val x = (screenSize.width - tooltipWidthPx) / 2
                    val y = (screenSize.height - tooltipHeightPx - safeBottomPx) / 2
                    Pair(x.coerceAtLeast(16.dp.toPx()), y.coerceAtLeast(60.dp.toPx()))
                }
            }

            // Animate tooltip position
            val animatedTooltipX by animateFloatAsState(
                targetValue = targetTooltipX,
                animationSpec = transitionSpec,
                label = "tooltipX"
            )
            val animatedTooltipY by animateFloatAsState(
                targetValue = targetTooltipY,
                animationSpec = transitionSpec,
                label = "tooltipY"
            )

            // Animate tooltip content with slide-in, scale, and crossfade effect
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(350, easing = EaseOutCubic)) +
                    slideInVertically(
                        animationSpec = tween(400, easing = EaseOutCubic),
                        initialOffsetY = { it / 2 }
                    ) +
                    scaleIn(
                        animationSpec = tween(350, easing = EaseOutCubic),
                        initialScale = 0.8f
                    )) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "tooltipContent"
            ) { step ->
                TooltipCard(
                    step = step,
                    currentIndex = controller.currentStepIndex,
                    totalSteps = controller.totalSteps,
                    strings = strings,
                    onNext = { controller.next() },
                    onPrevious = { controller.previous() },
                    onSkip = { controller.skip() },
                    modifier = Modifier
                        .offset { IntOffset(animatedTooltipX.roundToInt(), animatedTooltipY.roundToInt()) }
                        .width(tooltipWidthDp)
                )
            }
        }
    }
}

@Composable
private fun TooltipCard(
    step: CoachMarkStep,
    currentIndex: Int,
    totalSteps: Int,
    strings: CoachMarkStrings,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with step counter and skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentIndex + 1}/$totalSteps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4E7CFF)
                )

                TextButton(
                    onClick = onSkip,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = strings.skip,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = step.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = step.description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = strings.back,
                            color = Color(0xFF4E7CFF)
                        )
                    }
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4E7CFF)
                    )
                ) {
                    Text(
                        text = if (currentIndex == totalSteps - 1) strings.finish else strings.next,
                        color = Color.White
                    )
                    if (currentIndex < totalSteps - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Localized strings for coach marks
 */
data class CoachMarkStrings(
    val skip: String,
    val next: String,
    val back: String,
    val finish: String
)

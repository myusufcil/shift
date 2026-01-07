package com.cil.shift.core.designsystem.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

// Honey colors
private val HoneyGold = Color(0xFFFFD700)
private val HoneyAmber = Color(0xFFFFA500)
private val HoneyDark = Color(0xFFB8860B)
private val LowHoneyRed = Color(0xFFFF6B6B)

/**
 * Honey counter displayed in the header.
 * Shows current balance with honey emoji.
 */
@Composable
fun HoneyCounter(
    balance: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLow: Boolean = balance < 20,
    isCritical: Boolean = balance < 10
) {
    val backgroundColor = when {
        isCritical -> LowHoneyRed.copy(alpha = 0.2f)
        isLow -> HoneyAmber.copy(alpha = 0.2f)
        else -> HoneyGold.copy(alpha = 0.15f)
    }

    val textColor = when {
        isCritical -> LowHoneyRed
        isLow -> HoneyAmber
        else -> HoneyGold
    }

    // Bounce animation when balance changes
    var previousBalance by remember { mutableStateOf(balance) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(balance) {
        if (balance != previousBalance) {
            isAnimating = true
            delay(300)
            isAnimating = false
            previousBalance = balance
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "honey_bounce"
    )

    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "\uD83C\uDF6F", // Honey pot emoji
            fontSize = 16.sp
        )
        Text(
            text = balance.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Locked feature overlay with honey cost display.
 */
@Composable
fun HoneyLockedOverlay(
    featureName: String,
    cost: Int,
    currentBalance: Int,
    onUnlock: () -> Unit,
    onGetPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = currentBalance >= cost

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = HoneyGold,
                modifier = Modifier.size(48.dp)
            )

            // Feature name
            Text(
                text = featureName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Cost display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Costs",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "\uD83C\uDF6F $cost",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HoneyGold
                )
            }

            // Current balance
            Text(
                text = "Your balance: \uD83C\uDF6F $currentBalance",
                fontSize = 12.sp,
                color = if (canAfford) Color.White.copy(alpha = 0.5f) else LowHoneyRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unlock button (if can afford)
            if (canAfford) {
                Button(
                    onClick = onUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HoneyGold,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(
                        text = "Unlock for $cost \uD83C\uDF6F",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Get Premium link
            TextButton(onClick = onGetPremium) {
                Text(
                    text = "Get Premium for unlimited access",
                    color = HoneyGold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Small lock badge for showing on locked items.
 */
@Composable
fun HoneyLockBadge(
    cost: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = HoneyGold,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "$cost",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = HoneyGold
        )
        Text(
            text = "\uD83C\uDF6F",
            fontSize = 10.sp
        )
    }
}

/**
 * Popup shown when honey is earned.
 */
@Composable
fun HoneyEarnedPopup(
    visible: Boolean,
    amount: Int,
    reason: String,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 2.5 seconds
    LaunchedEffect(visible) {
        if (visible) {
            delay(2500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(HoneyDark, HoneyGold, HoneyAmber)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "\uD83C\uDF6F",
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = "+$amount Honey!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = reason,
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Dialog shown when honey is low.
 */
@Composable
fun LowHoneyDialog(
    visible: Boolean,
    balance: Int,
    onGetPremium: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2942)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "\uD83C\uDF6F",
                    fontSize = 48.sp
                )

                Text(
                    text = "Running low on Honey!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "You have $balance honey left. Complete habits and check in daily to earn more!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onGetPremium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HoneyGold,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Premium - Unlimited Access",
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "I'll earn more honey",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Dialog shown when trying to use a feature without enough honey.
 */
@Composable
fun NotEnoughHoneyDialog(
    visible: Boolean,
    featureName: String,
    cost: Int,
    currentBalance: Int,
    onGetPremium: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val needed = cost - currentBalance

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2942)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = LowHoneyRed,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Need more Honey!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "$featureName costs $cost \uD83C\uDF6F",
                    fontSize = 16.sp,
                    color = HoneyGold
                )

                Text(
                    text = "You have $currentBalance \uD83C\uDF6F\nNeed $needed more",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ways to earn
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ways to earn honey:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Daily check-in: +2 \uD83C\uDF6F",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "• Complete habit: +1 \uD83C\uDF6F",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "• 7-day streak: +10 \uD83C\uDF6F",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onGetPremium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HoneyGold,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Premium - Skip the wait",
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "I'll come back later",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet shown when user doesn't have enough honey.
 * More user-friendly than dialog - doesn't interrupt flow.
 */
/**
 * Localized strings for the honey bottom sheet.
 */
data class HoneyBottomSheetStrings(
    val title: String = "Not Enough Honey!",
    val yourBalance: String = "Your Balance",
    val needed: String = "Needed",
    val howToEarn: String = "How to Earn Honey?",
    val dailyLogin: String = "Daily login: +2 honey",
    val completeHabit: String = "Complete habit: +1 honey",
    val sevenDayStreak: String = "7-day streak: +10 honey",
    val getPremium: String = "Get Premium - Unlimited Access",
    val watchAd: String = "Watch Ad (+5 honey)",
    val later: String = "I'll earn more honey later"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotEnoughHoneyBottomSheet(
    visible: Boolean,
    featureName: String,
    cost: Int,
    currentBalance: Int,
    strings: HoneyBottomSheetStrings = HoneyBottomSheetStrings(),
    onGetPremium: () -> Unit,
    onWatchAd: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val needed = cost - currentBalance

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A2942),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Honey icon
            Text(
                text = "\uD83C\uDF6F",
                fontSize = 48.sp
            )

            // Title
            Text(
                text = strings.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Feature info
            Text(
                text = "$featureName: $cost \uD83C\uDF6F",
                fontSize = 16.sp,
                color = HoneyGold,
                fontWeight = FontWeight.SemiBold
            )

            // Balance info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = strings.yourBalance,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$currentBalance \uD83C\uDF6F",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LowHoneyRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = strings.needed,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "+$needed \uD83C\uDF6F",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = HoneyGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // How to earn honey
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.howToEarn,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ ", color = HoneyGold)
                    Text(strings.dailyLogin, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ ", color = HoneyGold)
                    Text(strings.completeHabit, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ ", color = HoneyGold)
                    Text(strings.sevenDayStreak, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Premium button
            Button(
                onClick = onGetPremium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HoneyGold,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings.getPremium + " ✨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Watch ad button (optional)
            if (onWatchAd != null) {
                OutlinedButton(
                    onClick = onWatchAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HoneyGold
                    )
                ) {
                    Text(
                        text = strings.watchAd,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Dismiss button
            TextButton(onClick = onDismiss) {
                Text(
                    text = strings.later,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

package com.cil.shift.feature.settings.presentation.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.achievement.AchievementManager
import com.cil.shift.core.common.achievement.AchievementTier
import com.cil.shift.core.common.achievement.Achievements
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val achievementManager = koinInject<AchievementManager>()
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val unlockedAchievements by achievementManager.unlockedAchievements.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    // Group achievements by tier
    val achievementsByTier = remember(unlockedAchievements) {
        Achievements.all.groupBy { it.tier }
    }

    val unlockedCount = unlockedAchievements.size
    val totalCount = Achievements.all.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = StringResources.achievements.get(currentLanguage),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4E7CFF),
                                    Color(0xFF6C63FF)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$unlockedCount / $totalCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = StringResources.achievementsUnlocked.get(currentLanguage),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(unlockedCount.toFloat() / totalCount)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            // Tier sections
            AchievementTier.entries.forEach { tier ->
                val achievementsInTier = achievementsByTier[tier] ?: emptyList()
                if (achievementsInTier.isNotEmpty()) {
                    item {
                        val (primaryColor, _) = getTierColors(tier)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = getTierEmoji(tier),
                                fontSize = 20.sp
                            )
                            Text(
                                text = tier.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "(${achievementsInTier.count { unlockedAchievements.contains(it.id) }}/${achievementsInTier.size})",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.5f)
                            )
                        }
                    }

                    items(achievementsInTier) { achievement ->
                        val isUnlocked = unlockedAchievements.contains(achievement.id)
                        val progress = achievementManager.getProgress(achievement.id)

                        AchievementCard(
                            achievement = achievement,
                            isUnlocked = isUnlocked,
                            progress = progress,
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun getTierEmoji(tier: AchievementTier): String {
    return when (tier) {
        AchievementTier.BRONZE -> "🥉"
        AchievementTier.SILVER -> "🥈"
        AchievementTier.GOLD -> "🥇"
        AchievementTier.PLATINUM -> "💎"
        AchievementTier.DIAMOND -> "👑"
    }
}

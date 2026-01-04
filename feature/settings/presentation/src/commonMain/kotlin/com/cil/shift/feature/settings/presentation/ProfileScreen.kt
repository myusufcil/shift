package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.*
import org.koin.compose.koinInject

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val habitRepository = koinInject<HabitRepository>()

    var showLanguageDialog by remember { mutableStateOf(false) }
    val activeHabitsCount by habitRepository.getHabits().collectAsState(initial = emptyList<Habit>())
    var totalStreakDays by remember { mutableStateOf(0) }
    var totalCompletedCount by remember { mutableStateOf(0) }

    // Calculate completed count and longest streak from database
    LaunchedEffect(activeHabitsCount) {
        if (activeHabitsCount.isEmpty()) {
            totalCompletedCount = 0
            totalStreakDays = 0
            return@LaunchedEffect
        }

        var totalCompleted = 0
        var longestStreak = 0

        activeHabitsCount.forEach { habit ->
            // Get all completions for this habit
            val completions = habitRepository.getCompletions(habit.id).first()

            // Count total completed
            val completedForHabit = completions.count { it.isCompleted }
            totalCompleted += completedForHabit

            // Calculate streak for this habit
            if (completions.isNotEmpty()) {
                // Filter only completed ones and sort by date descending (most recent first)
                val completedDates = completions
                    .filter { it.isCompleted }
                    .map { parseDate(it.date) }
                    .filterNotNull()
                    .sortedDescending()

                if (completedDates.isNotEmpty()) {
                    val today = com.cil.shift.core.common.currentDate()

                    // Calculate current streak (must include today or yesterday)
                    var currentStreak = 0
                    val mostRecentDate = completedDates.first()

                    // Check if most recent completion is today or yesterday
                    val daysSinceLastCompletion = mostRecentDate.daysUntil(today)

                    if (daysSinceLastCompletion <= 1) {
                        // Start counting streak from most recent date
                        currentStreak = 1
                        var previousDate = mostRecentDate

                        for (i in 1 until completedDates.size) {
                            val currentDate = completedDates[i]
                            val daysDiff = currentDate.daysUntil(previousDate)

                            // Check if dates are consecutive (1 day apart)
                            if (daysDiff == 1) {
                                currentStreak++
                                previousDate = currentDate
                            } else {
                                // Streak broken
                                break
                            }
                        }

                        // Update longest streak if this one is longer
                        if (currentStreak > longestStreak) {
                            longestStreak = currentStreak
                        }
                    }

                    // Also find the longest historical streak
                    var tempStreak = 1
                    var maxHistoricalStreak = 1

                    for (i in 1 until completedDates.size) {
                        val daysDiff = completedDates[i].daysUntil(completedDates[i - 1])

                        if (daysDiff == 1) {
                            tempStreak++
                            if (tempStreak > maxHistoricalStreak) {
                                maxHistoricalStreak = tempStreak
                            }
                        } else {
                            tempStreak = 1
                        }
                    }

                    if (maxHistoricalStreak > longestStreak) {
                        longestStreak = maxHistoricalStreak
                    }
                }
            }
        }

        totalCompletedCount = totalCompleted
        totalStreakDays = longestStreak
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = StringResources.profile.localized(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628)
                )
            )
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4E7CFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Alex",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "alex@example.com",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = StringResources.activeHabits.localized(),
                        value = activeHabitsCount.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = StringResources.totalStreak.localized(),
                        value = totalStreakDays.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = StringResources.completed.localized(),
                        value = totalCompletedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Settings section
            item {
                Text(
                    text = StringResources.settings.localized().uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.Language,
                        title = StringResources.language.localized(),
                        subtitle = currentLanguage.nativeName,
                        onClick = { showLanguageDialog = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = StringResources.about.localized(),
                        onClick = onNavigateToAbout
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Lock,
                        title = StringResources.privacyPolicy.localized(),
                        onClick = onNavigateToPrivacy
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Description,
                        title = StringResources.termsOfService.localized(),
                        onClick = onNavigateToTerms
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Help,
                        title = StringResources.helpSupport.localized(),
                        onClick = { /* TODO */ }
                    )
                }
            }

            // Danger zone
            item {
                Text(
                    text = StringResources.dangerZone.localized().uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF6B6B).copy(alpha = 0.8f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.Delete,
                        title = StringResources.deleteAccount.localized(),
                        onClick = { /* TODO */ },
                        isDanger = true
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = StringResources.signOut.localized(),
                        onClick = { /* TODO */ },
                        isDanger = true
                    )
                }
            }

            // Version info
            item {
                Text(
                    text = StringResources.version.localized() + " 1.0.0",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { language ->
                    localizationManager.setLanguage(language)
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A2942))
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .height(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E7CFF)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isDanger: Boolean = false,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A2942))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDanger) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDanger) Color(0xFFFF6B6B) else Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    var showRestartDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }

    if (showRestartDialog && selectedLanguage != null) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF1A2942),
            title = {
                Text(
                    text = if (selectedLanguage == Language.TURKISH) {
                        "Dil Değiştirildi"
                    } else if (selectedLanguage == Language.SPANISH) {
                        "Idioma Cambiado"
                    } else {
                        "Language Changed"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (selectedLanguage == Language.TURKISH) {
                        "Dil değişikliğinin tam olarak uygulanması için lütfen uygulamayı yeniden başlatın."
                    } else if (selectedLanguage == Language.SPANISH) {
                        "Por favor, reinicie la aplicación para que el cambio de idioma se aplique por completo."
                    } else {
                        "Please restart the app for the language change to take full effect."
                    },
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        onDismiss()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF4E7CFF)
                    )
                ) {
                    Text(
                        if (selectedLanguage == Language.TURKISH) {
                            "Tamam"
                        } else if (selectedLanguage == Language.SPANISH) {
                            "OK"
                        } else {
                            "OK"
                        }
                    )
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1A2942),
            title = {
                Text(
                    text = StringResources.selectLanguage.localized(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Language.entries.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (language == currentLanguage)
                                        Color(0xFF4E7CFF).copy(alpha = 0.2f)
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    if (language != currentLanguage) {
                                        onLanguageSelected(language)
                                        selectedLanguage = language
                                        showRestartDialog = true
                                    }
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = language.nativeName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = language.displayName,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            if (language == currentLanguage) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4E7CFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF4E7CFF)
                    )
                ) {
                    Text(StringResources.cancel.localized())
                }
            }
        )
    }
}

/**
 * Helper function to parse date string in YYYY-MM-DD format to LocalDate.
 */
private fun parseDate(dateString: String): LocalDate? {
    return try {
        val parts = dateString.split("-")
        if (parts.size != 3) return null

        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null

        LocalDate(year, month, day)
    } catch (e: Exception) {
        null
    }
}

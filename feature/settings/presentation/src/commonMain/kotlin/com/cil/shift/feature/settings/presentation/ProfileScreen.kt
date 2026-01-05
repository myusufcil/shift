package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthState
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.core.common.purchase.PurchaseManager
import com.cil.shift.core.common.purchase.PremiumState
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.core.common.theme.AppTheme
import com.cil.shift.core.common.theme.ThemeManager
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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
    onNavigateToAchievements: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val habitRepository = koinInject<HabitRepository>()
    val themeManager = koinInject<ThemeManager>()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val authManager = koinInject<AuthManager>()
    val authState by authManager.authState.collectAsState()
    val purchaseManager = koinInject<PurchaseManager>()
    val premiumState by purchaseManager.premiumState.collectAsState()
    val onboardingPreferences = koinInject<OnboardingPreferences>()
    val scope = rememberCoroutineScope()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var pendingLanguage by remember { mutableStateOf<Language?>(null) }
    var pendingTheme by remember { mutableStateOf<AppTheme?>(null) }
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
                        color = textColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
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
                val user = (authState as? AuthState.Authenticated)?.user
                val onboardingName = onboardingPreferences.getUserName().ifBlank { null }
                val displayName = user?.displayName
                    ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: onboardingName
                    ?: if (currentLanguage == Language.TURKISH) "Misafir" else "Guest"
                val email = user?.email
                val initial = displayName.firstOrNull()?.uppercase() ?: "?"

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
                            text = initial,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = displayName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    if (email != null) {
                        Text(
                            text = email,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    } else {
                        // Not logged in - show login prompt
                        TextButton(onClick = onNavigateToLogin) {
                            Text(
                                text = if (currentLanguage == Language.TURKISH) "Giriş Yap / Kayıt Ol" else "Sign In / Sign Up",
                                fontSize = 14.sp,
                                color = Color(0xFF4E7CFF)
                            )
                        }
                    }
                }
            }

            // Premium banner (if not premium)
            if (premiumState !is PremiumState.Premium) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                )
                            )
                            .clickable { onNavigateToPremium() }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == Language.TURKISH) "Premium'a Yukselt" else "Upgrade to Premium",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLanguage == Language.TURKISH) "Tum ozelliklerin kilidini ac" else "Unlock all features",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "⭐",
                                fontSize = 32.sp
                            )
                        }
                    }
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
                        cardColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = StringResources.totalStreak.localized(),
                        value = totalStreakDays.toString(),
                        cardColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = StringResources.completed.localized(),
                        value = totalCompletedCount.toString(),
                        cardColor = cardColor,
                        textColor = textColor,
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
                    color = textColor.copy(alpha = 0.5f),
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
                        icon = Icons.Default.Palette,
                        title = StringResources.theme.localized(),
                        subtitle = when (currentTheme) {
                            AppTheme.DARK -> StringResources.dark.localized()
                            AppTheme.LIGHT -> StringResources.light.localized()
                            AppTheme.SYSTEM -> StringResources.systemDefault.localized()
                        },
                        onClick = { showThemeDialog = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.EmojiEvents,
                        title = StringResources.achievements.localized(),
                        onClick = onNavigateToAchievements
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


            // Version info
            item {
                Text(
                    text = StringResources.version.localized() + " 1.0.0",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.4f),
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
                    if (language != currentLanguage) {
                        pendingLanguage = language
                        showLanguageDialog = false
                        showRestartDialog = true
                    } else {
                        showLanguageDialog = false
                    }
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = currentTheme,
                onThemeSelected = { theme ->
                    if (theme != currentTheme) {
                        pendingTheme = theme
                        showThemeDialog = false
                        showRestartDialog = true
                    } else {
                        showThemeDialog = false
                    }
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = {
                    showRestartDialog = false
                    pendingLanguage = null
                    pendingTheme = null
                },
                title = {
                    Text(
                        text = if (currentLanguage == Language.TURKISH) "Uygulama Yeniden Başlatılacak" else "App Will Restart",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (currentLanguage == Language.TURKISH)
                            "Bu değişikliğin uygulanması için uygulama kapatılacak. Devam etmek istiyor musunuz?"
                        else
                            "The app will close to apply this change. Do you want to continue?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                // Apply the pending change and wait for it to be saved
                                pendingLanguage?.let {
                                    localizationManager.setLanguage(it)
                                }
                                pendingTheme?.let {
                                    themeManager.setTheme(it)
                                }
                                // Small delay to ensure settings are persisted
                                kotlinx.coroutines.delay(100)
                                // Exit the app
                                kotlin.system.exitProcess(0)
                            }
                        }
                    ) {
                        Text(
                            text = if (currentLanguage == Language.TURKISH) "Tamam" else "OK",
                            color = Color(0xFF4E7CFF)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRestartDialog = false
                            pendingLanguage = null
                            pendingTheme = null
                        }
                    ) {
                        Text(
                            text = if (currentLanguage == Language.TURKISH) "İptal" else "Cancel"
                        )
                    }
                }
            )
        }

        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = {
                    Text(
                        text = if (currentLanguage == Language.TURKISH) "Çıkış Yap" else "Sign Out",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (currentLanguage == Language.TURKISH)
                            "Hesabınızdan çıkış yapmak istediğinize emin misiniz?"
                        else
                            "Are you sure you want to sign out?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                // Logout from RevenueCat first to clear purchase sync
                                purchaseManager.logout()
                                // Then sign out from Firebase Auth
                                authManager.signOut()
                                showSignOutDialog = false
                            }
                        }
                    ) {
                        Text(
                            text = if (currentLanguage == Language.TURKISH) "Çıkış Yap" else "Sign Out",
                            color = Color(0xFFFF6B6B)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text(
                            text = if (currentLanguage == Language.TURKISH) "İptal" else "Cancel"
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
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
            color = textColor.copy(alpha = 0.6f),
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
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
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
                tint = if (isDanger) Color(0xFFFF6B6B) else textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDanger) Color(0xFFFF6B6B) else textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.3f),
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
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                text = StringResources.selectLanguage.localized(),
                color = textColor,
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
                                color = textColor
                            )
                            Text(
                                text = language.displayName,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.6f)
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
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4E7CFF)
                )
            ) {
                Text(StringResources.cancel.localized())
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                text = StringResources.selectTheme.localized(),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    val themeName = when (theme) {
                        AppTheme.DARK -> StringResources.dark.localized()
                        AppTheme.LIGHT -> StringResources.light.localized()
                        AppTheme.SYSTEM -> StringResources.systemDefault.localized()
                    }
                    val themeDescription = when (theme) {
                        AppTheme.DARK -> StringResources.darkThemeDescription.localized()
                        AppTheme.LIGHT -> StringResources.lightThemeDescription.localized()
                        AppTheme.SYSTEM -> StringResources.systemThemeDescription.localized()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (theme == currentTheme)
                                    Color(0xFF4E7CFF).copy(alpha = 0.2f)
                                else
                                    Color.Transparent
                            )
                            .clickable {
                                if (theme != currentTheme) {
                                    onThemeSelected(theme)
                                }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = themeName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                            Text(
                                text = themeDescription,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                        if (theme == currentTheme) {
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
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4E7CFF)
                )
            ) {
                Text(StringResources.cancel.localized())
            }
        }
    )
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

package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
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
    val uriHandler = LocalUriHandler.current
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
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
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
                        text = StringResources.profile.get(currentLanguage),
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
                    ?: StringResources.guest.get(currentLanguage)
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
                                text = StringResources.signInSignUp.get(currentLanguage),
                                fontSize = 14.sp,
                                color = Color(0xFF4E7CFF)
                            )
                        }
                    }

                    // Premium badge (if premium)
                    if (premiumState is PremiumState.Premium) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "⭐",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Premium",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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
                                    text = StringResources.upgradeToPremium.get(currentLanguage),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = StringResources.unlockAllFeatures.get(currentLanguage),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "🍯",
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }

            // Stats
            item(key = "stats_$currentLanguage") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = StringResources.activeHabits.get(currentLanguage),
                        value = activeHabitsCount.size.toString(),
                        cardColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = StringResources.totalStreak.get(currentLanguage),
                        value = totalStreakDays.toString(),
                        cardColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = StringResources.completed.get(currentLanguage),
                        value = totalCompletedCount.toString(),
                        cardColor = cardColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Settings section
            item(key = "settings_header_$currentLanguage") {
                Text(
                    text = StringResources.settings.get(currentLanguage).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }

            item(key = "settings_items_$currentLanguage") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Language,
                        iconColor = Color(0xFF4E7CFF),
                        title = StringResources.language.get(currentLanguage),
                        subtitle = currentLanguage.nativeName,
                        onClick = { showLanguageDialog = true }
                    )
                    ThemeToggleMenuItem(
                        currentTheme = currentTheme,
                        currentLanguage = currentLanguage,
                        onThemeToggle = { isDark ->
                            themeManager.setTheme(if (isDark) AppTheme.DARK else AppTheme.LIGHT)
                        },
                        onMoreOptions = { showThemeDialog = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.EmojiEvents,
                        iconColor = Color(0xFFFFD700),
                        title = StringResources.achievements.get(currentLanguage),
                        onClick = onNavigateToAchievements
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.Info,
                        iconColor = Color(0xFF4ECDC4),
                        title = StringResources.about.get(currentLanguage),
                        onClick = onNavigateToAbout
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.Lock,
                        iconColor = Color(0xFF9B59B6),
                        title = StringResources.privacyPolicy.get(currentLanguage),
                        onClick = onNavigateToPrivacy
                    )
                    ProfileMenuItem(
                        icon = Icons.Outlined.Description,
                        iconColor = Color(0xFF3498DB),
                        title = StringResources.termsOfService.get(currentLanguage),
                        onClick = onNavigateToTerms
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        iconColor = Color(0xFFE67E22),
                        title = StringResources.helpSupport.get(currentLanguage),
                        onClick = {
                            uriHandler.openUri("mailto:contact.shiftapp@gmail.com?subject=Shift App Support")
                        }
                    )
                }
            }

            // Account section - only show when logged in
            if (authState is AuthState.Authenticated) {
                val user = (authState as AuthState.Authenticated).user

                item {
                    Text(
                        text = StringResources.account.get(currentLanguage).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.5f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show user info card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardColor)
                                .border(
                                    width = 1.dp,
                                    color = textColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4E7CFF).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: '?').uppercase().toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E7CFF)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName ?: user.email?.substringBefore("@") ?: "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                                Text(
                                    text = user.email ?: "",
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.5f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4ECDC4),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = StringResources.signOut.localized(),
                            onClick = { showSignOutDialog = true },
                            isDanger = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileMenuItem(
                            icon = Icons.Default.Delete,
                            title = StringResources.deleteAccount.localized(),
                            onClick = { showDeleteAccountDialog = true },
                            isDanger = true
                        )
                    }
                }
            }

        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { language ->
                    // Apply language immediately - no restart needed
                    localizationManager.setLanguage(language)
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = currentTheme,
                onThemeSelected = { theme ->
                    // Apply theme immediately - no restart needed
                    themeManager.setTheme(theme)
                    showThemeDialog = false
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
                        text = StringResources.appWillRestart.get(currentLanguage),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = StringResources.appWillRestartMessage.get(currentLanguage)
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
                                // Close dialog - app will apply changes on next restart
                                showRestartDialog = false
                            }
                        }
                    ) {
                        Text(
                            text = StringResources.ok.get(currentLanguage),
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
                            text = StringResources.cancel.get(currentLanguage)
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
                        text = StringResources.signOut.get(currentLanguage),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = StringResources.signOutConfirmMessage.get(currentLanguage)
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
                            text = StringResources.signOut.get(currentLanguage),
                            color = Color(0xFFFF6B6B)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text(
                            text = StringResources.cancel.get(currentLanguage)
                        )
                    }
                }
            )
        }

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isDeleting) showDeleteAccountDialog = false
                },
                title = {
                    Text(
                        text = StringResources.deleteAccount.get(currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )
                },
                text = {
                    Text(
                        text = StringResources.deleteAccountConfirmMessage.get(currentLanguage)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isDeleting = true
                                // Logout from RevenueCat first
                                purchaseManager.logout()
                                // Delete the Firebase account
                                val result = authManager.deleteAccount()
                                isDeleting = false
                                showDeleteAccountDialog = false
                                // Account deleted, user is automatically signed out
                            }
                        },
                        enabled = !isDeleting
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = StringResources.delete.get(currentLanguage),
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAccountDialog = false },
                        enabled = !isDeleting
                    ) {
                        Text(
                            text = StringResources.cancel.get(currentLanguage)
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
    iconColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val effectiveIconColor = when {
        isDanger -> Color(0xFFFF6B6B)
        iconColor != null -> iconColor
        else -> Color(0xFF4E7CFF)
    }

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
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(effectiveIconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = effectiveIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
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
            tint = textColor.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ThemeToggleMenuItem(
    currentTheme: AppTheme,
    currentLanguage: Language,
    onThemeToggle: (isDark: Boolean) -> Unit,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val isDark = currentTheme == AppTheme.DARK ||
                 (currentTheme == AppTheme.SYSTEM && isSystemInDarkTheme())
    val iconColor = Color(0xFFE91E63)

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
            .clickable(onClick = onMoreOptions)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = StringResources.theme.get(currentLanguage),
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = StringResources.theme.get(currentLanguage),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = when (currentTheme) {
                        AppTheme.DARK -> StringResources.dark.get(currentLanguage)
                        AppTheme.LIGHT -> StringResources.light.get(currentLanguage)
                        AppTheme.SYSTEM -> StringResources.systemDefault.get(currentLanguage)
                    },
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sun icon for light mode
            Text(
                text = "☀️",
                fontSize = 14.sp,
                color = if (!isDark) Color(0xFFFFD700) else textColor.copy(alpha = 0.3f)
            )

            Switch(
                checked = isDark,
                onCheckedChange = { onThemeToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4E7CFF),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFFFD700)
                ),
                modifier = Modifier.height(24.dp)
            )

            // Moon icon for dark mode
            Text(
                text = "🌙",
                fontSize = 14.sp,
                color = if (isDark) Color(0xFF4E7CFF) else textColor.copy(alpha = 0.3f)
            )
        }
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
                text = StringResources.selectLanguage.get(currentLanguage),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            val scrollState = rememberScrollState()
            val showTopFade = scrollState.value > 0
            val showBottomFade = scrollState.value < scrollState.maxValue

            Box(
                modifier = Modifier.heightIn(max = 450.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = language.nativeName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                                Text(
                                    text = language.displayName,
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                            if (language == currentLanguage) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4E7CFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Top fade indicator
                if (showTopFade) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(cardColor, cardColor.copy(alpha = 0f))
                                )
                            )
                    )
                }

                // Bottom fade indicator with scroll hint
                if (showBottomFade) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(cardColor.copy(alpha = 0f), cardColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↓",
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = 0.5f)
                        )
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
                Text(StringResources.cancel.get(currentLanguage))
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

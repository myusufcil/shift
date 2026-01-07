package com.cil.shift.feature.settings.presentation.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthState
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.purchase.PurchaseManager
import com.cil.shift.core.common.purchase.PurchaseResult
import com.cil.shift.core.common.purchase.PremiumState
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purchaseManager = koinInject<PurchaseManager>()
    val localizationManager = koinInject<LocalizationManager>()
    val authManager = koinInject<AuthManager>()
    val authState by authManager.authState.collectAsState()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val premiumState by purchaseManager.premiumState.collectAsState()
    val packages by purchaseManager.packages.collectAsState()
    val sdkError by purchaseManager.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var debugInfo by remember { mutableStateOf("Initializing...") }

    // Load offerings on screen open
    LaunchedEffect(Unit) {
        debugInfo = "Loading offerings..."
        try {
            purchaseManager.initialize()
            debugInfo = "Offerings loaded: ${purchaseManager.packages.value}"
        } catch (e: Exception) {
            debugInfo = "Error: ${e.message}"
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    val strings = remember(currentLanguage) { PremiumStrings.get(currentLanguage) }

    // Auto-select yearly as default
    LaunchedEffect(packages) {
        if (selectedPackage == null) {
            selectedPackage = packages?.yearly ?: packages?.monthly
        }
    }

    // If already premium, show success state
    if (premiumState is PremiumState.Premium) {
        PremiumActiveScreen(
            onNavigateBack = onNavigateBack,
            strings = strings,
            textColor = textColor,
            backgroundColor = backgroundColor
        )
        return
    }

    // If not logged in, show login required screen
    if (authState !is AuthState.Authenticated) {
        LoginRequiredScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToLogin = onNavigateToLogin,
            strings = strings,
            textColor = textColor,
            backgroundColor = backgroundColor
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                purchaseManager.restorePurchases { result ->
                                    isLoading = false
                                    when (result) {
                                        is PurchaseResult.Success -> {
                                            // Will auto-update UI
                                        }
                                        is PurchaseResult.Error -> {
                                            errorMessage = result.message
                                        }
                                        is PurchaseResult.Cancelled -> {}
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = strings.restore,
                            color = Color(0xFF4E7CFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.subtitle,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Features list
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem(
                    icon = Icons.Default.Sync,
                    title = strings.featureSync,
                    description = strings.featureSyncDesc,
                    textColor = textColor
                )
                FeatureItem(
                    icon = Icons.Default.TrendingUp,
                    title = strings.featureStats,
                    description = strings.featureStatsDesc,
                    textColor = textColor
                )
                FeatureItem(
                    icon = Icons.Default.Palette,
                    title = strings.featureThemes,
                    description = strings.featureThemesDesc,
                    textColor = textColor
                )
                FeatureItem(
                    icon = Icons.Default.Notifications,
                    title = strings.featureReminders,
                    description = strings.featureRemindersDesc,
                    textColor = textColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Debug: Show state info
            Text(
                text = "packages=${packages != null}\nstate=$premiumState\nerror=$sdkError",
                fontSize = 10.sp,
                color = Color(0xFFFF6B6B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Package selection
            if (packages != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    packages?.yearly?.let { yearly ->
                        PackageCard(
                            packageItem = yearly,
                            isSelected = selectedPackage == yearly,
                            isBestValue = true,
                            label = strings.yearly,
                            onClick = { selectedPackage = yearly },
                            textColor = textColor,
                            cardColor = cardColor
                        )
                    }

                    packages?.monthly?.let { monthly ->
                        PackageCard(
                            packageItem = monthly,
                            isSelected = selectedPackage == monthly,
                            isBestValue = false,
                            label = strings.monthly,
                            onClick = { selectedPackage = monthly },
                            textColor = textColor,
                            cardColor = cardColor
                        )
                    }

                    packages?.lifetime?.let { lifetime ->
                        PackageCard(
                            packageItem = lifetime,
                            isSelected = selectedPackage == lifetime,
                            isBestValue = false,
                            label = strings.lifetime,
                            onClick = { selectedPackage = lifetime },
                            textColor = textColor,
                            cardColor = cardColor
                        )
                    }
                }
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    fontSize = 13.sp,
                    color = Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Purchase button
            Button(
                onClick = {
                    selectedPackage?.let { pkg ->
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            purchaseManager.purchase(pkg) { result ->
                                isLoading = false
                                when (result) {
                                    is PurchaseResult.Success -> {
                                        // Will auto-update UI
                                    }
                                    is PurchaseResult.Error -> {
                                        errorMessage = result.message
                                    }
                                    is PurchaseResult.Cancelled -> {}
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF)
                ),
                enabled = !isLoading && selectedPackage != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = strings.subscribe,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms
            Text(
                text = strings.terms,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF4E7CFF).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4E7CFF),
                modifier = Modifier.size(24.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PackageCard(
    packageItem: Package,
    isSelected: Boolean,
    isBestValue: Boolean,
    label: String,
    onClick: () -> Unit,
    textColor: Color,
    cardColor: Color
) {
    val borderColor = if (isSelected) Color(0xFF4E7CFF) else textColor.copy(alpha = 0.1f)
    val product = packageItem.storeProduct

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    if (isBestValue) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4ECDC4))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BEST VALUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = product.localizedDescription ?: "",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }

            Text(
                text = product.price.formatted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF4E7CFF) else textColor
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4E7CFF),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumActiveScreen(
    onNavigateBack: () -> Unit,
    strings: PremiumStrings,
    textColor: Color,
    backgroundColor: Color
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.alreadyPremium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.enjoyFeatures,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginRequiredScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    strings: PremiumStrings,
    textColor: Color,
    backgroundColor: Color
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4E7CFF), Color(0xFF6B5CE7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.loginRequired,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.loginRequiredDesc,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF)
                )
            ) {
                Text(
                    text = strings.loginButton,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class PremiumStrings(
    val title: String,
    val subtitle: String,
    val featureSync: String,
    val featureSyncDesc: String,
    val featureStats: String,
    val featureStatsDesc: String,
    val featureThemes: String,
    val featureThemesDesc: String,
    val featureReminders: String,
    val featureRemindersDesc: String,
    val monthly: String,
    val yearly: String,
    val lifetime: String,
    val subscribe: String,
    val restore: String,
    val terms: String,
    val alreadyPremium: String,
    val enjoyFeatures: String,
    val loginRequired: String,
    val loginRequiredDesc: String,
    val loginButton: String
) {
    companion object {
        fun get(language: Language): PremiumStrings {
            return when (language) {
                Language.TURKISH -> PremiumStrings(
                    title = "Shift Premium",
                    subtitle = "Tum ozelliklerin kilidini acin",
                    featureSync = "Bulut Senkronizasyonu",
                    featureSyncDesc = "Verileriniz tum cihazlarinizda guvenle saklanir",
                    featureStats = "Detayli Istatistikler",
                    featureStatsDesc = "Aliskanlik trendlerinizi analiz edin",
                    featureThemes = "Ozel Temalar",
                    featureThemesDesc = "Uygulamayi kendi tarziniza uyarlayin",
                    featureReminders = "Akilli Hatirlaticilar",
                    featureRemindersDesc = "Kisisellestirilmis bildirimler alin",
                    monthly = "Aylik",
                    yearly = "Yillik",
                    lifetime = "Omur Boyu",
                    subscribe = "Premium'a Abone Ol",
                    restore = "Satin Alimlari Geri Yukle",
                    terms = "Abonelik otomatik olarak yenilenir. Istediginiz zaman iptal edebilirsiniz.",
                    alreadyPremium = "Zaten Premium Uyesiniz!",
                    enjoyFeatures = "Tum premium ozelliklerden yararlanabilirsiniz",
                    loginRequired = "Giris Yapmaniz Gerekiyor",
                    loginRequiredDesc = "Premium satin almak icin once hesabiniza giris yapin. Bu sayede satin aliminiz hesabiniza baglanir ve tum cihazlarinizda kullanabilirsiniz.",
                    loginButton = "Giris Yap"
                )
                else -> PremiumStrings(
                    title = "Shift Premium",
                    subtitle = "Unlock all features",
                    featureSync = "Cloud Sync",
                    featureSyncDesc = "Your data is safely stored across all your devices",
                    featureStats = "Detailed Statistics",
                    featureStatsDesc = "Analyze your habit trends",
                    featureThemes = "Custom Themes",
                    featureThemesDesc = "Personalize the app to your style",
                    featureReminders = "Smart Reminders",
                    featureRemindersDesc = "Get personalized notifications",
                    monthly = "Monthly",
                    yearly = "Yearly",
                    lifetime = "Lifetime",
                    subscribe = "Subscribe to Premium",
                    restore = "Restore Purchases",
                    terms = "Subscription automatically renews. You can cancel anytime.",
                    alreadyPremium = "You're Already Premium!",
                    enjoyFeatures = "Enjoy all premium features",
                    loginRequired = "Login Required",
                    loginRequiredDesc = "Please sign in to purchase Premium. This ensures your purchase is linked to your account and available on all your devices.",
                    loginButton = "Sign In"
                )
            }
        }
    }
}

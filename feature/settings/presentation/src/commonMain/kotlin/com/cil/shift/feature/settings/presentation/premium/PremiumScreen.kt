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
import androidx.compose.material.icons.filled.Star
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
    val purchaseError by purchaseManager.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    val strings = remember(currentLanguage) { PremiumStrings.get(currentLanguage) }

    // Combine local error with purchase manager error and localize
    val rawErrorMessage = localErrorMessage ?: purchaseError
    val errorMessage = PremiumStrings.localizeError(rawErrorMessage, strings)

    // Load offerings and check status on screen open
    LaunchedEffect(Unit) {
        localErrorMessage = null
        purchaseManager.clearError()
        purchaseManager.initialize()
    }

    // Auto-select yearly as default
    LaunchedEffect(packages) {
        if (selectedPackage == null) {
            selectedPackage = packages?.yearly ?: packages?.monthly
        }
    }

    // Navigate back when premium state is confirmed
    LaunchedEffect(premiumState) {
        if (premiumState is PremiumState.Premium) {
            onNavigateBack()
        }
    }

    // Show loading while checking premium status
    if (premiumState is PremiumState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF4E7CFF),
                modifier = Modifier.size(40.dp)
            )
        }
        return
    }

    // If already premium, show nothing (will navigate back via LaunchedEffect)
    if (premiumState is PremiumState.Premium) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
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
                                        is PurchaseResult.Success -> {}
                                        is PurchaseResult.Error -> {
                                            localErrorMessage = result.message
                                        }
                                        is PurchaseResult.Cancelled -> {}
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = strings.restore,
                            color = Color(0xFF4E7CFF),
                            fontSize = 14.sp
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium badge - smaller
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = strings.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.subtitle,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Features list - more compact, no sync
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

            Spacer(modifier = Modifier.height(20.dp))

            // Package selection
            if (packages != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
            } else {
                // Packages loading/unavailable
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    color = Color(0xFF4E7CFF),
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    color = Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Purchase button
            Button(
                onClick = {
                    selectedPackage?.let { pkg ->
                        scope.launch {
                            isLoading = true
                            localErrorMessage = null
                            purchaseManager.purchase(pkg) { result ->
                                isLoading = false
                                when (result) {
                                    is PurchaseResult.Success -> {}
                                    is PurchaseResult.Error -> {
                                        localErrorMessage = result.message
                                    }
                                    is PurchaseResult.Cancelled -> {}
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF)
                ),
                enabled = !isLoading && selectedPackage != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = strings.subscribe,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Terms
            Text(
                text = strings.terms,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF4E7CFF).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4E7CFF),
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = description,
                fontSize = 12.sp,
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
    val bgColor = if (isSelected) Color(0xFF4E7CFF).copy(alpha = 0.05f) else cardColor
    val product = packageItem.storeProduct

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(bgColor)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color(0xFF4E7CFF) else textColor
                    )
                    if (isBestValue) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4ECDC4))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Text(
                text = product.price.formatted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF4E7CFF) else textColor,
                maxLines = 1
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
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
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
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = strings.loginRequired,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.loginRequiredDesc,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF)
                )
            ) {
                Text(
                    text = strings.loginButton,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class PremiumStrings(
    val title: String,
    val subtitle: String,
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
    val loginRequired: String,
    val loginRequiredDesc: String,
    val loginButton: String,
    val errorNetwork: String,
    val errorCredentials: String,
    val errorPurchaseFailed: String,
    val errorRestoreFailed: String,
    val errorDeviceNotAllowed: String,
    val errorGeneric: String,
    val noPurchasesToRestore: String
) {
    companion object {
        fun get(language: Language): PremiumStrings {
            return when (language) {
                Language.TURKISH -> PremiumStrings(
                    title = "Shift Premium",
                    subtitle = "Tum ozelliklerin kilidini acin",
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
                    restore = "Geri Yukle",
                    terms = "Abonelik otomatik olarak yenilenir. Istediginiz zaman iptal edebilirsiniz.",
                    loginRequired = "Giris Yapmaniz Gerekiyor",
                    loginRequiredDesc = "Premium satin almak icin once hesabiniza giris yapin.",
                    loginButton = "Giris Yap",
                    errorNetwork = "Baglanti hatasi. Lutfen internet baglantinizi kontrol edin.",
                    errorCredentials = "Kimlik dogrulama hatasi. Lutfen tekrar deneyin.",
                    errorPurchaseFailed = "Satin alma basarisiz. Lutfen tekrar deneyin.",
                    errorRestoreFailed = "Geri yukleme basarisiz. Lutfen tekrar deneyin.",
                    errorDeviceNotAllowed = "Bu cihaz satin alma icin desteklenmiyor.",
                    errorGeneric = "Bir hata olustu. Lutfen tekrar deneyin.",
                    noPurchasesToRestore = "Geri yuklenecek satin alma bulunamadi."
                )
                else -> PremiumStrings(
                    title = "Shift Premium",
                    subtitle = "Unlock all features",
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
                    restore = "Restore",
                    terms = "Subscription automatically renews. You can cancel anytime.",
                    loginRequired = "Login Required",
                    loginRequiredDesc = "Please sign in to purchase Premium.",
                    loginButton = "Sign In",
                    errorNetwork = "Connection error. Please check your internet connection.",
                    errorCredentials = "Authentication error. Please try again.",
                    errorPurchaseFailed = "Purchase failed. Please try again.",
                    errorRestoreFailed = "Restore failed. Please try again.",
                    errorDeviceNotAllowed = "This device is not supported for purchases.",
                    errorGeneric = "An error occurred. Please try again.",
                    noPurchasesToRestore = "No purchases found to restore."
                )
            }
        }

        /**
         * Localizes RevenueCat error messages to user-friendly strings
         */
        fun localizeError(error: String?, strings: PremiumStrings): String? {
            if (error == null) return null
            val lowerError = error.lowercase()
            return when {
                lowerError.contains("network") || lowerError.contains("internet") || lowerError.contains("connection") -> strings.errorNetwork
                lowerError.contains("credential") || lowerError.contains("authentication") || lowerError.contains("auth") -> strings.errorCredentials
                lowerError.contains("device") && lowerError.contains("not allowed") -> strings.errorDeviceNotAllowed
                lowerError.contains("no purchases") || lowerError.contains("nothing to restore") -> strings.noPurchasesToRestore
                lowerError.contains("purchase") && (lowerError.contains("fail") || lowerError.contains("error")) -> strings.errorPurchaseFailed
                lowerError.contains("restore") && lowerError.contains("fail") -> strings.errorRestoreFailed
                else -> strings.errorGeneric
            }
        }
    }
}

package com.cil.shift.feature.onboarding.presentation.walkthrough

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductWalkthroughScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val strings = remember(currentLanguage) { ProductWalkthroughStrings.get(currentLanguage) }

    val pages = remember(currentLanguage) {
        listOf(
            ProductPage(
                emoji = "🍯",
                title = strings.welcomeTitle,
                subtitle = strings.welcomeSubtitle,
                description = strings.welcomeDescription,
                accentColor = Color(0xFFFFD700),
                features = listOf(
                    FeatureItem("✨", strings.featureSimple),
                    FeatureItem("📱", strings.featureCrossPlatform),
                    FeatureItem("🌍", strings.featureMultiLanguage)
                )
            ),
            ProductPage(
                emoji = "📝",
                title = strings.habitsTitle,
                subtitle = strings.habitsSubtitle,
                description = strings.habitsDescription,
                accentColor = Color(0xFF4E7CFF),
                features = listOf(
                    FeatureItem("⏰", strings.featureReminders),
                    FeatureItem("📊", strings.featureTracking),
                    FeatureItem("🎯", strings.featureGoals)
                )
            ),
            ProductPage(
                emoji = "📈",
                title = strings.statisticsTitle,
                subtitle = strings.statisticsSubtitle,
                description = strings.statisticsDescription,
                accentColor = Color(0xFF4ECDC4),
                features = listOf(
                    FeatureItem("📉", strings.featureCharts),
                    FeatureItem("🔥", strings.featureStreaks),
                    FeatureItem("📅", strings.featureHistory)
                )
            ),
            ProductPage(
                emoji = "🏆",
                title = strings.achievementsTitle,
                subtitle = strings.achievementsSubtitle,
                description = strings.achievementsDescription,
                accentColor = Color(0xFFE91E63),
                features = listOf(
                    FeatureItem("🥇", strings.featureBadges),
                    FeatureItem("⭐", strings.featureLevels),
                    FeatureItem("🎮", strings.featureGamification)
                )
            ),
            ProductPage(
                emoji = "🚀",
                title = strings.getStartedTitle,
                subtitle = strings.getStartedSubtitle,
                description = strings.getStartedDescription,
                accentColor = Color(0xFF9B59B6),
                features = listOf(
                    FeatureItem("💪", strings.featureStartNow),
                    FeatureItem("🌟", strings.featureTransform),
                    FeatureItem("❤️", strings.featureEnjoy)
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onComplete) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = textColor
                        )
                    }
                },
                actions = {
                    if (pagerState.currentPage < pages.size - 1) {
                        TextButton(onClick = onComplete) {
                            Text(
                                text = strings.skip,
                                color = textColor.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                ProductPageContent(
                    page = pages[pageIndex],
                    isCurrentPage = pagerState.currentPage == pageIndex,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Page indicator
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val color by animateColorAsState(
                        targetValue = if (isSelected) pages[index].accentColor else textColor.copy(alpha = 0.3f),
                        label = "indicatorColor"
                    )
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (isSelected) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }

            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button (only show if not first page)
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        )
                    ) {
                        Text(
                            text = strings.back,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Next/Finish button
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(if (pagerState.currentPage > 0) 1f else 2f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].accentColor
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) strings.getStarted else strings.next,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductPageContent(
    page: ProductPage,
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    val scale by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0.9f,
        animationSpec = tween(300),
        label = "scale"
    )

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji with gradient background
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.3f),
                            page.accentColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = page.emoji,
                fontSize = 72.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = page.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = page.subtitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = page.accentColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 15.sp,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feature cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            page.features.forEach { feature ->
                FeatureCard(
                    feature = feature,
                    accentColor = page.accentColor,
                    cardColor = cardColor,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: FeatureItem,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = feature.emoji,
                fontSize = 20.sp
            )
        }

        Text(
            text = feature.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private data class ProductPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color,
    val features: List<FeatureItem>
)

private data class FeatureItem(
    val emoji: String,
    val text: String
)

private data class ProductWalkthroughStrings(
    // Navigation
    val close: String,
    val skip: String,
    val back: String,
    val next: String,
    val getStarted: String,
    // Page 1: Welcome
    val welcomeTitle: String,
    val welcomeSubtitle: String,
    val welcomeDescription: String,
    val featureSimple: String,
    val featureCrossPlatform: String,
    val featureMultiLanguage: String,
    // Page 2: Habits
    val habitsTitle: String,
    val habitsSubtitle: String,
    val habitsDescription: String,
    val featureReminders: String,
    val featureTracking: String,
    val featureGoals: String,
    // Page 3: Statistics
    val statisticsTitle: String,
    val statisticsSubtitle: String,
    val statisticsDescription: String,
    val featureCharts: String,
    val featureStreaks: String,
    val featureHistory: String,
    // Page 4: Achievements
    val achievementsTitle: String,
    val achievementsSubtitle: String,
    val achievementsDescription: String,
    val featureBadges: String,
    val featureLevels: String,
    val featureGamification: String,
    // Page 5: Get Started
    val getStartedTitle: String,
    val getStartedSubtitle: String,
    val getStartedDescription: String,
    val featureStartNow: String,
    val featureTransform: String,
    val featureEnjoy: String
) {
    companion object {
        fun get(language: Language): ProductWalkthroughStrings {
            return when (language) {
                Language.TURKISH -> ProductWalkthroughStrings(
                    close = "Kapat",
                    skip = "Atla",
                    back = "Geri",
                    next = "Ileri",
                    getStarted = "Basla",
                    welcomeTitle = "Shift'e\nHos Geldiniz",
                    welcomeSubtitle = "Aliskanlik Takip Uygulamasi",
                    welcomeDescription = "Hayatinizi donusturun, hedeflerinize ulasin ve her gun daha iyi bir versiyon olun.",
                    featureSimple = "Basit ve sezgisel arayuz",
                    featureCrossPlatform = "iOS ve Android destegi",
                    featureMultiLanguage = "11 dil destegi",
                    habitsTitle = "Aliskanlik\nOlusturun",
                    habitsSubtitle = "Kolayca takip edin",
                    habitsDescription = "Gunluk, haftalik veya ozel aliskanliklar olusturun ve ilerlemenizi takip edin.",
                    featureReminders = "Akilli hatirlaticilar",
                    featureTracking = "Detayli ilerleme takibi",
                    featureGoals = "Kisisel hedefler belirleyin",
                    statisticsTitle = "Istatistiklerinizi\nGorun",
                    statisticsSubtitle = "Veriye dayali ilerleme",
                    statisticsDescription = "Grafikler ve istatistiklerle ilerlemenizi analiz edin, guclu ve zayif yonlerinizi kesfedin.",
                    featureCharts = "Detayli grafikler",
                    featureStreaks = "Seri takibi",
                    featureHistory = "Gecmis kayitlari",
                    achievementsTitle = "Basarilar\nKazanin",
                    achievementsSubtitle = "Oyunlastirma deneyimi",
                    achievementsDescription = "Hedeflerinize ulastikca rozetler ve oduller kazanin, motivasyonunuzu yuksek tutun.",
                    featureBadges = "47+ farkli rozet",
                    featureLevels = "Seviye sistemi",
                    featureGamification = "Eglenceli deneyim",
                    getStartedTitle = "Hazir\nMisiniz?",
                    getStartedSubtitle = "Yolculugunuz basliyor",
                    getStartedDescription = "Simdi ilk aliskanliginizi olusturun ve hayatinizi donusturmeye baslayin!",
                    featureStartNow = "Hemen baslayin",
                    featureTransform = "Hayatinizi donusturun",
                    featureEnjoy = "Keyfini cikarin"
                )
                Language.SPANISH -> ProductWalkthroughStrings(
                    close = "Cerrar",
                    skip = "Omitir",
                    back = "Atras",
                    next = "Siguiente",
                    getStarted = "Comenzar",
                    welcomeTitle = "Bienvenido\na Shift",
                    welcomeSubtitle = "Seguimiento de habitos",
                    welcomeDescription = "Transforma tu vida, alcanza tus metas y conviertete en una mejor version cada dia.",
                    featureSimple = "Interfaz simple e intuitiva",
                    featureCrossPlatform = "Soporte iOS y Android",
                    featureMultiLanguage = "Soporte para 11 idiomas",
                    habitsTitle = "Crea\nHabitos",
                    habitsSubtitle = "Seguimiento facil",
                    habitsDescription = "Crea habitos diarios, semanales o personalizados y sigue tu progreso.",
                    featureReminders = "Recordatorios inteligentes",
                    featureTracking = "Seguimiento detallado",
                    featureGoals = "Establece metas personales",
                    statisticsTitle = "Ve tus\nEstadisticas",
                    statisticsSubtitle = "Progreso basado en datos",
                    statisticsDescription = "Analiza tu progreso con graficos y estadisticas, descubre tus fortalezas.",
                    featureCharts = "Graficos detallados",
                    featureStreaks = "Seguimiento de rachas",
                    featureHistory = "Historial completo",
                    achievementsTitle = "Gana\nLogros",
                    achievementsSubtitle = "Experiencia gamificada",
                    achievementsDescription = "Gana insignias y recompensas al alcanzar tus objetivos, manten la motivacion.",
                    featureBadges = "47+ insignias diferentes",
                    featureLevels = "Sistema de niveles",
                    featureGamification = "Experiencia divertida",
                    getStartedTitle = "Estas\nListo?",
                    getStartedSubtitle = "Tu viaje comienza",
                    getStartedDescription = "Crea tu primer habito ahora y comienza a transformar tu vida!",
                    featureStartNow = "Comienza ahora",
                    featureTransform = "Transforma tu vida",
                    featureEnjoy = "Disfruta el viaje"
                )
                Language.FRENCH -> ProductWalkthroughStrings(
                    close = "Fermer",
                    skip = "Passer",
                    back = "Retour",
                    next = "Suivant",
                    getStarted = "Commencer",
                    welcomeTitle = "Bienvenue\nsur Shift",
                    welcomeSubtitle = "Suivi des habitudes",
                    welcomeDescription = "Transformez votre vie, atteignez vos objectifs et devenez une meilleure version chaque jour.",
                    featureSimple = "Interface simple et intuitive",
                    featureCrossPlatform = "Support iOS et Android",
                    featureMultiLanguage = "Support de 11 langues",
                    habitsTitle = "Creez des\nHabitudes",
                    habitsSubtitle = "Suivi facile",
                    habitsDescription = "Creez des habitudes quotidiennes, hebdomadaires ou personnalisees et suivez vos progres.",
                    featureReminders = "Rappels intelligents",
                    featureTracking = "Suivi detaille",
                    featureGoals = "Definissez vos objectifs",
                    statisticsTitle = "Consultez vos\nStatistiques",
                    statisticsSubtitle = "Progres bases sur les donnees",
                    statisticsDescription = "Analysez vos progres avec des graphiques et statistiques, decouvrez vos forces.",
                    featureCharts = "Graphiques detailles",
                    featureStreaks = "Suivi des series",
                    featureHistory = "Historique complet",
                    achievementsTitle = "Gagnez des\nSucces",
                    achievementsSubtitle = "Experience ludique",
                    achievementsDescription = "Gagnez des badges et recompenses en atteignant vos objectifs, restez motive.",
                    featureBadges = "47+ badges differents",
                    featureLevels = "Systeme de niveaux",
                    featureGamification = "Experience amusante",
                    getStartedTitle = "Pret a\nCommencer?",
                    getStartedSubtitle = "Votre voyage commence",
                    getStartedDescription = "Creez votre premiere habitude maintenant et commencez a transformer votre vie!",
                    featureStartNow = "Commencez maintenant",
                    featureTransform = "Transformez votre vie",
                    featureEnjoy = "Profitez du voyage"
                )
                Language.GERMAN -> ProductWalkthroughStrings(
                    close = "Schliessen",
                    skip = "Uberspringen",
                    back = "Zuruck",
                    next = "Weiter",
                    getStarted = "Starten",
                    welcomeTitle = "Willkommen\nbei Shift",
                    welcomeSubtitle = "Gewohnheiten verfolgen",
                    welcomeDescription = "Transformieren Sie Ihr Leben, erreichen Sie Ihre Ziele und werden Sie jeden Tag besser.",
                    featureSimple = "Einfache, intuitive Oberflache",
                    featureCrossPlatform = "iOS und Android Unterstutzung",
                    featureMultiLanguage = "11 Sprachen unterstutzt",
                    habitsTitle = "Gewohnheiten\nErstellen",
                    habitsSubtitle = "Einfache Verfolgung",
                    habitsDescription = "Erstellen Sie tagliche, wochentliche oder benutzerdefinierte Gewohnheiten und verfolgen Sie Ihren Fortschritt.",
                    featureReminders = "Intelligente Erinnerungen",
                    featureTracking = "Detaillierte Verfolgung",
                    featureGoals = "Personliche Ziele setzen",
                    statisticsTitle = "Statistiken\nAnzeigen",
                    statisticsSubtitle = "Datenbasierter Fortschritt",
                    statisticsDescription = "Analysieren Sie Ihren Fortschritt mit Diagrammen und Statistiken.",
                    featureCharts = "Detaillierte Diagramme",
                    featureStreaks = "Serien-Verfolgung",
                    featureHistory = "Vollstandiger Verlauf",
                    achievementsTitle = "Erfolge\nFreischalten",
                    achievementsSubtitle = "Spielerische Erfahrung",
                    achievementsDescription = "Verdienen Sie Abzeichen und Belohnungen beim Erreichen Ihrer Ziele.",
                    featureBadges = "47+ verschiedene Abzeichen",
                    featureLevels = "Levelsystem",
                    featureGamification = "Spassige Erfahrung",
                    getStartedTitle = "Bereit zu\nStarten?",
                    getStartedSubtitle = "Ihre Reise beginnt",
                    getStartedDescription = "Erstellen Sie jetzt Ihre erste Gewohnheit und beginnen Sie, Ihr Leben zu verandern!",
                    featureStartNow = "Jetzt starten",
                    featureTransform = "Leben verandern",
                    featureEnjoy = "Geniessen Sie es"
                )
                Language.PORTUGUESE -> ProductWalkthroughStrings(
                    close = "Fechar",
                    skip = "Pular",
                    back = "Voltar",
                    next = "Proximo",
                    getStarted = "Comecar",
                    welcomeTitle = "Bem-vindo\nao Shift",
                    welcomeSubtitle = "Rastreador de habitos",
                    welcomeDescription = "Transforme sua vida, alcance seus objetivos e seja uma versao melhor a cada dia.",
                    featureSimple = "Interface simples e intuitiva",
                    featureCrossPlatform = "Suporte iOS e Android",
                    featureMultiLanguage = "Suporte a 11 idiomas",
                    habitsTitle = "Crie\nHabitos",
                    habitsSubtitle = "Acompanhamento facil",
                    habitsDescription = "Crie habitos diarios, semanais ou personalizados e acompanhe seu progresso.",
                    featureReminders = "Lembretes inteligentes",
                    featureTracking = "Rastreamento detalhado",
                    featureGoals = "Defina metas pessoais",
                    statisticsTitle = "Veja suas\nEstatisticas",
                    statisticsSubtitle = "Progresso baseado em dados",
                    statisticsDescription = "Analise seu progresso com graficos e estatisticas, descubra seus pontos fortes.",
                    featureCharts = "Graficos detalhados",
                    featureStreaks = "Rastreamento de sequencias",
                    featureHistory = "Historico completo",
                    achievementsTitle = "Ganhe\nConquistas",
                    achievementsSubtitle = "Experiencia gamificada",
                    achievementsDescription = "Ganhe insignias e recompensas ao alcancar seus objetivos, mantenha a motivacao.",
                    featureBadges = "47+ insignias diferentes",
                    featureLevels = "Sistema de niveis",
                    featureGamification = "Experiencia divertida",
                    getStartedTitle = "Pronto para\nComecar?",
                    getStartedSubtitle = "Sua jornada comeca",
                    getStartedDescription = "Crie seu primeiro habito agora e comece a transformar sua vida!",
                    featureStartNow = "Comece agora",
                    featureTransform = "Transforme sua vida",
                    featureEnjoy = "Aproveite a jornada"
                )
                Language.ARABIC -> ProductWalkthroughStrings(
                    close = "اغلاق",
                    skip = "تخطي",
                    back = "رجوع",
                    next = "التالي",
                    getStarted = "ابدا",
                    welcomeTitle = "مرحبا بك\nفي Shift",
                    welcomeSubtitle = "تتبع العادات",
                    welcomeDescription = "حول حياتك، حقق اهدافك وكن نسخة افضل كل يوم.",
                    featureSimple = "واجهة بسيطة وسهلة",
                    featureCrossPlatform = "دعم iOS و Android",
                    featureMultiLanguage = "دعم 11 لغة",
                    habitsTitle = "انشئ\nعادات",
                    habitsSubtitle = "تتبع سهل",
                    habitsDescription = "انشئ عادات يومية او اسبوعية او مخصصة وتتبع تقدمك.",
                    featureReminders = "تذكيرات ذكية",
                    featureTracking = "تتبع مفصل",
                    featureGoals = "حدد اهدافك الشخصية",
                    statisticsTitle = "شاهد\nاحصائياتك",
                    statisticsSubtitle = "تقدم مبني على البيانات",
                    statisticsDescription = "حلل تقدمك بالرسوم البيانية والاحصائيات، اكتشف نقاط قوتك.",
                    featureCharts = "رسوم بيانية مفصلة",
                    featureStreaks = "تتبع السلاسل",
                    featureHistory = "سجل كامل",
                    achievementsTitle = "احصل على\nانجازات",
                    achievementsSubtitle = "تجربة ممتعة",
                    achievementsDescription = "احصل على شارات ومكافات عند تحقيق اهدافك، حافظ على حماسك.",
                    featureBadges = "47+ شارة مختلفة",
                    featureLevels = "نظام المستويات",
                    featureGamification = "تجربة ممتعة",
                    getStartedTitle = "هل انت\nجاهز؟",
                    getStartedSubtitle = "رحلتك تبدا",
                    getStartedDescription = "انشئ عادتك الاولى الان وابدا بتحويل حياتك!",
                    featureStartNow = "ابدا الان",
                    featureTransform = "حول حياتك",
                    featureEnjoy = "استمتع بالرحلة"
                )
                Language.RUSSIAN -> ProductWalkthroughStrings(
                    close = "Закрыть",
                    skip = "Пропустить",
                    back = "Назад",
                    next = "Далее",
                    getStarted = "Начать",
                    welcomeTitle = "Добро пожаловать\nв Shift",
                    welcomeSubtitle = "Отслеживание привычек",
                    welcomeDescription = "Преобразите свою жизнь, достигайте целей и становитесь лучше каждый день.",
                    featureSimple = "Простой и интуитивный интерфейс",
                    featureCrossPlatform = "Поддержка iOS и Android",
                    featureMultiLanguage = "Поддержка 11 языков",
                    habitsTitle = "Создавайте\nПривычки",
                    habitsSubtitle = "Легкое отслеживание",
                    habitsDescription = "Создавайте ежедневные, еженедельные или пользовательские привычки и отслеживайте прогресс.",
                    featureReminders = "Умные напоминания",
                    featureTracking = "Детальное отслеживание",
                    featureGoals = "Устанавливайте личные цели",
                    statisticsTitle = "Смотрите\nСтатистику",
                    statisticsSubtitle = "Прогресс на основе данных",
                    statisticsDescription = "Анализируйте прогресс с помощью графиков и статистики, находите сильные стороны.",
                    featureCharts = "Подробные графики",
                    featureStreaks = "Отслеживание серий",
                    featureHistory = "Полная история",
                    achievementsTitle = "Получайте\nДостижения",
                    achievementsSubtitle = "Игровой опыт",
                    achievementsDescription = "Получайте значки и награды за достижение целей, сохраняйте мотивацию.",
                    featureBadges = "47+ разных значков",
                    featureLevels = "Система уровней",
                    featureGamification = "Увлекательный опыт",
                    getStartedTitle = "Готовы\nНачать?",
                    getStartedSubtitle = "Ваш путь начинается",
                    getStartedDescription = "Создайте свою первую привычку сейчас и начните преображать свою жизнь!",
                    featureStartNow = "Начните сейчас",
                    featureTransform = "Преобразите жизнь",
                    featureEnjoy = "Наслаждайтесь путем"
                )
                Language.HINDI -> ProductWalkthroughStrings(
                    close = "बंद करें",
                    skip = "छोड़ें",
                    back = "वापस",
                    next = "अगला",
                    getStarted = "शुरू करें",
                    welcomeTitle = "Shift में\nस्वागत है",
                    welcomeSubtitle = "आदत ट्रैकर",
                    welcomeDescription = "अपना जीवन बदलें, लक्ष्य प्राप्त करें और हर दिन बेहतर बनें।",
                    featureSimple = "सरल और सहज इंटरफ़ेस",
                    featureCrossPlatform = "iOS और Android समर्थन",
                    featureMultiLanguage = "11 भाषाओं का समर्थन",
                    habitsTitle = "आदतें\nबनाएं",
                    habitsSubtitle = "आसान ट्रैकिंग",
                    habitsDescription = "दैनिक, साप्ताहिक या कस्टम आदतें बनाएं और अपनी प्रगति ट्रैक करें।",
                    featureReminders = "स्मार्ट रिमाइंडर",
                    featureTracking = "विस्तृत ट्रैकिंग",
                    featureGoals = "व्यक्तिगत लक्ष्य सेट करें",
                    statisticsTitle = "आंकड़े\nदेखें",
                    statisticsSubtitle = "डेटा-आधारित प्रगति",
                    statisticsDescription = "चार्ट और आंकड़ों के साथ अपनी प्रगति का विश्लेषण करें।",
                    featureCharts = "विस्तृत चार्ट",
                    featureStreaks = "स्ट्रीक ट्रैकिंग",
                    featureHistory = "पूर्ण इतिहास",
                    achievementsTitle = "उपलब्धियां\nअर्जित करें",
                    achievementsSubtitle = "गेमिफाइड अनुभव",
                    achievementsDescription = "लक्ष्य प्राप्त करने पर बैज और पुरस्कार अर्जित करें।",
                    featureBadges = "47+ अलग-अलग बैज",
                    featureLevels = "लेवल सिस्टम",
                    featureGamification = "मज़ेदार अनुभव",
                    getStartedTitle = "शुरू करने\nको तैयार?",
                    getStartedSubtitle = "आपकी यात्रा शुरू होती है",
                    getStartedDescription = "अभी अपनी पहली आदत बनाएं और अपना जीवन बदलना शुरू करें!",
                    featureStartNow = "अभी शुरू करें",
                    featureTransform = "जीवन बदलें",
                    featureEnjoy = "आनंद लें"
                )
                Language.JAPANESE -> ProductWalkthroughStrings(
                    close = "閉じる",
                    skip = "スキップ",
                    back = "戻る",
                    next = "次へ",
                    getStarted = "始める",
                    welcomeTitle = "Shiftへ\nようこそ",
                    welcomeSubtitle = "習慣トラッカー",
                    welcomeDescription = "人生を変え、目標を達成し、毎日より良い自分になりましょう。",
                    featureSimple = "シンプルで直感的なUI",
                    featureCrossPlatform = "iOSとAndroidに対応",
                    featureMultiLanguage = "11言語をサポート",
                    habitsTitle = "習慣を\n作成",
                    habitsSubtitle = "簡単なトラッキング",
                    habitsDescription = "毎日、毎週、またはカスタム習慣を作成し、進捗を追跡します。",
                    featureReminders = "スマートリマインダー",
                    featureTracking = "詳細なトラッキング",
                    featureGoals = "個人目標を設定",
                    statisticsTitle = "統計を\n表示",
                    statisticsSubtitle = "データに基づく進捗",
                    statisticsDescription = "チャートと統計で進捗を分析し、強みを発見しましょう。",
                    featureCharts = "詳細なチャート",
                    featureStreaks = "ストリーク追跡",
                    featureHistory = "完全な履歴",
                    achievementsTitle = "実績を\n獲得",
                    achievementsSubtitle = "ゲーミフィケーション体験",
                    achievementsDescription = "目標達成でバッジと報酬を獲得し、モチベーションを維持。",
                    featureBadges = "47以上のバッジ",
                    featureLevels = "レベルシステム",
                    featureGamification = "楽しい体験",
                    getStartedTitle = "始める\n準備は？",
                    getStartedSubtitle = "旅が始まります",
                    getStartedDescription = "今すぐ最初の習慣を作成して、人生を変え始めましょう！",
                    featureStartNow = "今すぐ始める",
                    featureTransform = "人生を変える",
                    featureEnjoy = "楽しんで"
                )
                Language.CHINESE -> ProductWalkthroughStrings(
                    close = "关闭",
                    skip = "跳过",
                    back = "返回",
                    next = "下一步",
                    getStarted = "开始",
                    welcomeTitle = "欢迎使用\nShift",
                    welcomeSubtitle = "习惯追踪器",
                    welcomeDescription = "改变你的生活，实现目标，每天成为更好的自己。",
                    featureSimple = "简洁直观的界面",
                    featureCrossPlatform = "支持iOS和Android",
                    featureMultiLanguage = "支持11种语言",
                    habitsTitle = "创建\n习惯",
                    habitsSubtitle = "轻松追踪",
                    habitsDescription = "创建每日、每周或自定义习惯，追踪你的进度。",
                    featureReminders = "智能提醒",
                    featureTracking = "详细追踪",
                    featureGoals = "设定个人目标",
                    statisticsTitle = "查看\n统计",
                    statisticsSubtitle = "数据驱动的进度",
                    statisticsDescription = "通过图表和统计分析你的进度，发现你的优势。",
                    featureCharts = "详细图表",
                    featureStreaks = "连续记录追踪",
                    featureHistory = "完整历史",
                    achievementsTitle = "获得\n成就",
                    achievementsSubtitle = "游戏化体验",
                    achievementsDescription = "达成目标时获得徽章和奖励，保持动力。",
                    featureBadges = "47+不同徽章",
                    featureLevels = "等级系统",
                    featureGamification = "有趣的体验",
                    getStartedTitle = "准备好\n开始了吗？",
                    getStartedSubtitle = "你的旅程开始了",
                    getStartedDescription = "现在就创建你的第一个习惯，开始改变你的生活！",
                    featureStartNow = "立即开始",
                    featureTransform = "改变生活",
                    featureEnjoy = "享受旅程"
                )
                else -> ProductWalkthroughStrings(
                    close = "Close",
                    skip = "Skip",
                    back = "Back",
                    next = "Next",
                    getStarted = "Get Started",
                    welcomeTitle = "Welcome to\nShift",
                    welcomeSubtitle = "Habit Tracking App",
                    welcomeDescription = "Transform your life, achieve your goals, and become a better version of yourself every day.",
                    featureSimple = "Simple and intuitive interface",
                    featureCrossPlatform = "iOS and Android support",
                    featureMultiLanguage = "11 language support",
                    habitsTitle = "Create\nHabits",
                    habitsSubtitle = "Easy tracking",
                    habitsDescription = "Create daily, weekly, or custom habits and track your progress effortlessly.",
                    featureReminders = "Smart reminders",
                    featureTracking = "Detailed progress tracking",
                    featureGoals = "Set personal goals",
                    statisticsTitle = "View Your\nStatistics",
                    statisticsSubtitle = "Data-driven progress",
                    statisticsDescription = "Analyze your progress with charts and statistics, discover your strengths.",
                    featureCharts = "Detailed charts",
                    featureStreaks = "Streak tracking",
                    featureHistory = "Complete history",
                    achievementsTitle = "Earn\nAchievements",
                    achievementsSubtitle = "Gamified experience",
                    achievementsDescription = "Earn badges and rewards as you reach your goals, stay motivated.",
                    featureBadges = "47+ different badges",
                    featureLevels = "Level system",
                    featureGamification = "Fun experience",
                    getStartedTitle = "Ready to\nStart?",
                    getStartedSubtitle = "Your journey begins",
                    getStartedDescription = "Create your first habit now and start transforming your life!",
                    featureStartNow = "Start now",
                    featureTransform = "Transform your life",
                    featureEnjoy = "Enjoy the journey"
                )
            }
        }
    }
}

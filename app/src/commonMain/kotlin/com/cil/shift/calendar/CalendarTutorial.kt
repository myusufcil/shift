package com.cil.shift.calendar

import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.designsystem.components.CoachMarkStep
import com.cil.shift.core.designsystem.components.TooltipPosition

/**
 * Coach mark target IDs for the Calendar screen
 */
object CalendarTutorialTargets {
    const val VIEW_MODE_BUTTON = "calendar_view_mode"
    const val DATE_NAVIGATION = "calendar_date_nav"
    const val CALENDAR_GRID = "calendar_grid"
    const val ADD_EVENT_AREA = "calendar_add_event"
}

/**
 * Get localized coach mark steps for the Calendar screen tutorial
 */
fun getCalendarTutorialSteps(language: Language): List<CoachMarkStep> {
    return when (language) {
        Language.TURKISH -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "Görünüm Modu",
                description = "1 gün, 3 gün, hafta veya ay görünümü arasında geçiş yapın.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Tarih Navigasyonu",
                description = "Okları kullanarak tarihleri gezin. Bugüne dönmek için butona tıklayın.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Takvim",
                description = "Etkinlikleriniz burada gösterilir. Yeni etkinlik eklemek için bir hücreye tıklayın.",
                position = TooltipPosition.TOP
            )
        )
        Language.SPANISH -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "Modo de Vista",
                description = "Cambia entre vista de 1 día, 3 días, semana o mes.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Navegación",
                description = "Usa las flechas para navegar. Toca el botón para volver a hoy.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Calendario",
                description = "Tus eventos se muestran aquí. Toca una celda para agregar eventos.",
                position = TooltipPosition.TOP
            )
        )
        Language.FRENCH -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "Mode d'Affichage",
                description = "Basculez entre 1 jour, 3 jours, semaine ou mois.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Navigation",
                description = "Utilisez les flèches pour naviguer. Touchez pour revenir à aujourd'hui.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Calendrier",
                description = "Vos événements s'affichent ici. Touchez une cellule pour ajouter.",
                position = TooltipPosition.TOP
            )
        )
        Language.GERMAN -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "Ansichtsmodus",
                description = "Wechseln Sie zwischen 1-Tag, 3-Tage, Woche oder Monat.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Navigation",
                description = "Verwenden Sie Pfeile zum Navigieren. Tippen Sie für heute.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Kalender",
                description = "Ihre Termine werden hier angezeigt. Tippen Sie zum Hinzufügen.",
                position = TooltipPosition.TOP
            )
        )
        Language.PORTUGUESE -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "Modo de Vista",
                description = "Alterne entre 1 dia, 3 dias, semana ou mês.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Navegação",
                description = "Use as setas para navegar. Toque para voltar para hoje.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Calendário",
                description = "Seus eventos são mostrados aqui. Toque para adicionar novos.",
                position = TooltipPosition.TOP
            )
        )
        Language.ARABIC -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "وضع العرض",
                description = "التبديل بين عرض يوم واحد أو 3 أيام أو أسبوع أو شهر.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "التنقل",
                description = "استخدم الأسهم للتنقل. انقر للعودة إلى اليوم.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "التقويم",
                description = "يتم عرض أحداثك هنا. انقر على خلية لإضافة حدث.",
                position = TooltipPosition.TOP
            )
        )
        Language.RUSSIAN -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "Режим Просмотра",
                description = "Переключайтесь между 1 день, 3 дня, неделя или месяц.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Навигация",
                description = "Используйте стрелки для навигации. Нажмите для сегодня.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Календарь",
                description = "Ваши события показаны здесь. Нажмите для добавления.",
                position = TooltipPosition.TOP
            )
        )
        Language.HINDI -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "दृश्य मोड",
                description = "1 दिन, 3 दिन, सप्ताह या महीने के बीच स्विच करें।",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "नेविगेशन",
                description = "नेविगेट करने के लिए तीरों का उपयोग करें। आज के लिए टैप करें।",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "कैलेंडर",
                description = "आपके इवेंट यहां दिखाए जाते हैं। जोड़ने के लिए सेल पर टैप करें।",
                position = TooltipPosition.TOP
            )
        )
        Language.JAPANESE -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "表示モード",
                description = "1日、3日、週、月表示を切り替えます。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "ナビゲーション",
                description = "矢印で移動。タップして今日に戻ります。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "カレンダー",
                description = "イベントがここに表示されます。セルをタップして追加。",
                position = TooltipPosition.TOP
            )
        )
        Language.CHINESE -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "视图模式",
                description = "在1天、3天、周或月视图之间切换。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "导航",
                description = "使用箭头导航。点击返回今天。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "日历",
                description = "您的事件显示在此处。点击单元格添加事件。",
                position = TooltipPosition.TOP
            )
        )
        else -> listOf(
            CoachMarkStep(
                id = CalendarTutorialTargets.VIEW_MODE_BUTTON,
                title = "View Mode",
                description = "Switch between 1-day, 3-day, week or month view.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.DATE_NAVIGATION,
                title = "Navigation",
                description = "Use arrows to navigate dates. Tap to return to today.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = CalendarTutorialTargets.CALENDAR_GRID,
                title = "Calendar",
                description = "Your events are shown here. Tap a cell to add new events.",
                position = TooltipPosition.TOP
            )
        )
    }
}

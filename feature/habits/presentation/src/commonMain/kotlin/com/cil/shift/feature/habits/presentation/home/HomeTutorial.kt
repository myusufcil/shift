package com.cil.shift.feature.habits.presentation.home

import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.designsystem.components.CoachMarkStep
import com.cil.shift.core.designsystem.components.TooltipPosition

/**
 * Coach mark target IDs for the Home screen
 */
object HomeTutorialTargets {
    const val GREETING_HEADER = "greeting_header"
    const val NOTIFICATION_BUTTON = "notification_button"
    const val WEEKLY_CALENDAR = "weekly_calendar"
    const val DAILY_PROGRESS = "daily_progress"
    const val FIRST_HABIT = "first_habit"
    const val ADD_HABIT_BUTTON = "add_habit_button"
}

/**
 * Get localized coach mark steps for the Home screen tutorial
 * @param language The language for localization
 * @param hasHabits Whether the user has any habits (if false, habit card step is skipped)
 */
fun getHomeTutorialSteps(language: Language, hasHabits: Boolean = true): List<CoachMarkStep> {
    val allSteps = when (language) {
        Language.TURKISH -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Merhaba!",
                description = "Burada gunun tarihi ve kisisellestirilmis karsilamaniz yer aliyor.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Bildirimler",
                description = "Tum bildirimlerinizi ve hatirlaticilari buradan gorebilirsiniz.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Haftalik Takvim",
                description = "Gunlere tiklayarak o gune ait aliskanliklarinizi gorebilirsiniz.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Gunluk Ilerleme",
                description = "Bugun tamamladiginiz aliskanlik sayisini gorsel olarak takip edin.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Aliskanlik Karti",
                description = "Kartlara tiklayin ve tamamlayin. Siralamayi degistirmek icin basili tutup surukleyin.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "Yeni Aliskanlik",
                description = "Yeni aliskanliklar olusturmak icin bu butona tiklayin.",
                position = TooltipPosition.TOP
            )
        )
        Language.SPANISH -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Hola!",
                description = "Aqui veras la fecha de hoy y tu saludo personalizado.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Notificaciones",
                description = "Puedes ver todas tus notificaciones y recordatorios aqui.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Calendario Semanal",
                description = "Toca los dias para ver tus habitos de ese dia.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Progreso Diario",
                description = "Sigue visualmente cuantos habitos has completado hoy.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Tarjeta de Habito",
                description = "Toca para completar. Manten presionado y arrastra para reordenar.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "Nuevo Habito",
                description = "Toca este boton para crear nuevos habitos.",
                position = TooltipPosition.TOP
            )
        )
        Language.FRENCH -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Bonjour!",
                description = "Ici vous verrez la date du jour et votre salut personnalise.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Notifications",
                description = "Vous pouvez voir toutes vos notifications et rappels ici.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Calendrier Hebdo",
                description = "Touchez les jours pour voir vos habitudes de ce jour.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Progres Quotidien",
                description = "Suivez visuellement combien d'habitudes vous avez completees aujourd'hui.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Carte d'Habitude",
                description = "Touchez pour completer. Maintenez et glissez pour reorganiser.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "Nouvelle Habitude",
                description = "Touchez ce bouton pour creer de nouvelles habitudes.",
                position = TooltipPosition.TOP
            )
        )
        Language.GERMAN -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Hallo!",
                description = "Hier sehen Sie das heutige Datum und Ihre personliche Begrüssung.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Benachrichtigungen",
                description = "Alle Ihre Benachrichtigungen und Erinnerungen finden Sie hier.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Wochenkalender",
                description = "Tippen Sie auf die Tage, um Ihre Gewohnheiten fur diesen Tag zu sehen.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Tagesfortschritt",
                description = "Verfolgen Sie visuell, wie viele Gewohnheiten Sie heute abgeschlossen haben.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Gewohnheitskarte",
                description = "Tippen zum Abschliessen. Halten und ziehen zum Neuordnen.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "Neue Gewohnheit",
                description = "Tippen Sie hier, um neue Gewohnheiten zu erstellen.",
                position = TooltipPosition.TOP
            )
        )
        Language.PORTUGUESE -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Ola!",
                description = "Aqui voce vera a data de hoje e sua saudacao personalizada.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Notificacoes",
                description = "Voce pode ver todas as suas notificacoes e lembretes aqui.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Calendario Semanal",
                description = "Toque nos dias para ver seus habitos daquele dia.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Progresso Diario",
                description = "Acompanhe visualmente quantos habitos voce completou hoje.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Cartao de Habito",
                description = "Toque para completar. Segure e arraste para reordenar.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "Novo Habito",
                description = "Toque neste botao para criar novos habitos.",
                position = TooltipPosition.TOP
            )
        )
        Language.ARABIC -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "مرحبا!",
                description = "هنا سترى تاريخ اليوم وتحيتك الشخصية.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "الاشعارات",
                description = "يمكنك رؤية جميع اشعاراتك وتذكيراتك هنا.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "التقويم الاسبوعي",
                description = "انقر على الايام لرؤية عاداتك لذلك اليوم.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "التقدم اليومي",
                description = "تتبع بصريا عدد العادات التي اكملتها اليوم.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "بطاقة العادة",
                description = "انقر للإكمال. اضغط مطولا واسحب لإعادة الترتيب.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "عادة جديدة",
                description = "انقر على هذا الزر لإنشاء عادات جديدة.",
                position = TooltipPosition.TOP
            )
        )
        Language.RUSSIAN -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Привет!",
                description = "Здесь вы увидите сегодняшнюю дату и персонализированное приветствие.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Уведомления",
                description = "Все ваши уведомления и напоминания можно посмотреть здесь.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Недельный Календарь",
                description = "Нажмите на дни, чтобы увидеть привычки за этот день.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Дневной Прогресс",
                description = "Отслеживайте визуально, сколько привычек вы выполнили сегодня.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Карточка Привычки",
                description = "Нажмите для выполнения. Удерживайте и перетащите для изменения порядка.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "Новая Привычка",
                description = "Нажмите эту кнопку, чтобы создать новые привычки.",
                position = TooltipPosition.TOP
            )
        )
        Language.HINDI -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "नमस्ते!",
                description = "यहां आप आज की तारीख और अपना व्यक्तिगत अभिवादन देखेंगे।",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "सूचनाएं",
                description = "आप यहां अपनी सभी सूचनाएं और रिमाइंडर देख सकते हैं।",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "साप्ताहिक कैलेंडर",
                description = "उस दिन की आदतें देखने के लिए दिनों पर टैप करें।",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "दैनिक प्रगति",
                description = "आज आपने कितनी आदतें पूरी कीं, इसे विज़ुअल रूप से ट्रैक करें।",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "आदत कार्ड",
                description = "पूरा करने के लिए टैप करें। क्रम बदलने के लिए होल्ड करके खींचें।",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "नई आदत",
                description = "नई आदतें बनाने के लिए इस बटन पर टैप करें।",
                position = TooltipPosition.TOP
            )
        )
        Language.JAPANESE -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "こんにちは!",
                description = "ここには今日の日付とパーソナライズされた挨拶が表示されます。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "通知",
                description = "すべての通知とリマインダーはここで確認できます。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "週間カレンダー",
                description = "日をタップしてその日の習慣を確認できます。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "本日の進捗",
                description = "今日完了した習慣の数を視覚的に追跡します。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "習慣カード",
                description = "タップして完了。長押しでドラッグして並べ替え。",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "新しい習慣",
                description = "このボタンをタップして新しい習慣を作成します。",
                position = TooltipPosition.TOP
            )
        )
        Language.CHINESE -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "你好！",
                description = "这里显示今天的日期和个性化问候语。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "通知",
                description = "您可以在这里查看所有通知和提醒。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "周历",
                description = "点击日期查看当天的习惯。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "每日进度",
                description = "直观地追踪您今天完成了多少习惯。",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "习惯卡片",
                description = "点击完成。长按拖动重新排序。",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "新习惯",
                description = "点击此按钮创建新习惯。",
                position = TooltipPosition.TOP
            )
        )
        else -> listOf(
            CoachMarkStep(
                id = HomeTutorialTargets.GREETING_HEADER,
                title = "Welcome!",
                description = "Here you'll see today's date and your personalized greeting.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.NOTIFICATION_BUTTON,
                title = "Notifications",
                description = "You can see all your notifications and reminders here.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.WEEKLY_CALENDAR,
                title = "Weekly Calendar",
                description = "Tap on days to see your habits for that day.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.DAILY_PROGRESS,
                title = "Daily Progress",
                description = "Visually track how many habits you've completed today.",
                position = TooltipPosition.BOTTOM
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.FIRST_HABIT,
                title = "Habit Card",
                description = "Tap to complete. Long press and drag to reorder.",
                position = TooltipPosition.TOP
            ),
            CoachMarkStep(
                id = HomeTutorialTargets.ADD_HABIT_BUTTON,
                title = "New Habit",
                description = "Tap this button to create new habits.",
                position = TooltipPosition.TOP
            )
        )
    }

    // Filter out habit card step if user has no habits
    return if (hasHabits) {
        allSteps
    } else {
        allSteps.filter { it.id != HomeTutorialTargets.FIRST_HABIT }
    }
}

package com.cil.shift.core.common.achievement

/**
 * Represents an achievement/badge that users can unlock
 */
data class Achievement(
    val id: String,
    val type: AchievementType,
    val title: String,
    val titleTr: String,
    val description: String,
    val descriptionTr: String,
    val icon: String,
    val requiredValue: Int,
    val tier: AchievementTier = AchievementTier.BRONZE
)

/**
 * Achievement tiers based on difficulty
 */
enum class AchievementTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND
}

/**
 * Types of achievements
 */
enum class AchievementType {
    STREAK,           // Consecutive days streak
    TOTAL_COMPLETIONS, // Total habits completed
    HABITS_CREATED,   // Number of habits created
    PERFECT_WEEK,     // All habits completed for a week
    PERFECT_MONTH,    // All habits completed for a month
    EARLY_BIRD,       // Complete habits before 8am
    NIGHT_OWL,        // Complete habits after 10pm
    WEEKEND_WARRIOR,  // Complete all habits on weekends
    FIRST_STEP,       // First habit completed
    DEDICATED,        // Use app for X days
    VARIETY,          // Have X different habit categories
    HYDRATION_HERO,   // Drink water X times
    FITNESS_FANATIC,  // Complete fitness habits X times
    MINDFULNESS_MASTER // Complete meditation/mindfulness X times
}

/**
 * Represents an unlocked achievement for a user
 */
data class UnlockedAchievement(
    val achievementId: String,
    val unlockedAt: Long, // Epoch milliseconds
    val currentProgress: Int = 0
)

/**
 * All available achievements in the app
 */
object Achievements {
    val all: List<Achievement> = listOf(
        // Streak achievements
        Achievement(
            id = "streak_3",
            type = AchievementType.STREAK,
            title = "Getting Started",
            titleTr = "Basladik",
            description = "Maintain a 3-day streak",
            descriptionTr = "3 gun ust uste devam et",
            icon = "fire",
            requiredValue = 3,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "streak_7",
            type = AchievementType.STREAK,
            title = "Week Warrior",
            titleTr = "Hafta Savasçisi",
            description = "Maintain a 7-day streak",
            descriptionTr = "7 gun ust uste devam et",
            icon = "fire",
            requiredValue = 7,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "streak_14",
            type = AchievementType.STREAK,
            title = "Two Week Champion",
            titleTr = "Iki Hafta Sampiyonu",
            description = "Maintain a 14-day streak",
            descriptionTr = "14 gun ust uste devam et",
            icon = "fire",
            requiredValue = 14,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "streak_30",
            type = AchievementType.STREAK,
            title = "Monthly Master",
            titleTr = "Aylik Usta",
            description = "Maintain a 30-day streak",
            descriptionTr = "30 gun ust uste devam et",
            icon = "fire",
            requiredValue = 30,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "streak_60",
            type = AchievementType.STREAK,
            title = "Habit Hero",
            titleTr = "Aliskanlik Kahramani",
            description = "Maintain a 60-day streak",
            descriptionTr = "60 gun ust uste devam et",
            icon = "fire",
            requiredValue = 60,
            tier = AchievementTier.PLATINUM
        ),
        Achievement(
            id = "streak_100",
            type = AchievementType.STREAK,
            title = "Century Club",
            titleTr = "Yuzyil Kulubu",
            description = "Maintain a 100-day streak",
            descriptionTr = "100 gun ust uste devam et",
            icon = "fire",
            requiredValue = 100,
            tier = AchievementTier.DIAMOND
        ),
        Achievement(
            id = "streak_365",
            type = AchievementType.STREAK,
            title = "Year of Excellence",
            titleTr = "Mukemmellik Yili",
            description = "Maintain a 365-day streak",
            descriptionTr = "365 gun ust uste devam et",
            icon = "fire",
            requiredValue = 365,
            tier = AchievementTier.DIAMOND
        ),

        // Total completions achievements
        Achievement(
            id = "completions_1",
            type = AchievementType.FIRST_STEP,
            title = "First Step",
            titleTr = "Ilk Adim",
            description = "Complete your first habit",
            descriptionTr = "Ilk aliskanligini tamamla",
            icon = "check",
            requiredValue = 1,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "completions_10",
            type = AchievementType.TOTAL_COMPLETIONS,
            title = "Getting Momentum",
            titleTr = "Ivme Kazanmak",
            description = "Complete 10 habits",
            descriptionTr = "10 aliskanlik tamamla",
            icon = "check",
            requiredValue = 10,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "completions_50",
            type = AchievementType.TOTAL_COMPLETIONS,
            title = "Habit Builder",
            titleTr = "Aliskanlik Insaatcisi",
            description = "Complete 50 habits",
            descriptionTr = "50 aliskanlik tamamla",
            icon = "check",
            requiredValue = 50,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "completions_100",
            type = AchievementType.TOTAL_COMPLETIONS,
            title = "Century Achiever",
            titleTr = "Yuz Basari",
            description = "Complete 100 habits",
            descriptionTr = "100 aliskanlik tamamla",
            icon = "check",
            requiredValue = 100,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "completions_500",
            type = AchievementType.TOTAL_COMPLETIONS,
            title = "Habit Machine",
            titleTr = "Aliskanlik Makinesi",
            description = "Complete 500 habits",
            descriptionTr = "500 aliskanlik tamamla",
            icon = "check",
            requiredValue = 500,
            tier = AchievementTier.PLATINUM
        ),
        Achievement(
            id = "completions_1000",
            type = AchievementType.TOTAL_COMPLETIONS,
            title = "Legendary",
            titleTr = "Efsanevi",
            description = "Complete 1000 habits",
            descriptionTr = "1000 aliskanlik tamamla",
            icon = "check",
            requiredValue = 1000,
            tier = AchievementTier.DIAMOND
        ),

        // Habits created achievements
        Achievement(
            id = "created_1",
            type = AchievementType.HABITS_CREATED,
            title = "Habit Creator",
            titleTr = "Aliskanlik Yaraticisi",
            description = "Create your first habit",
            descriptionTr = "Ilk aliskanligini olustur",
            icon = "plus",
            requiredValue = 1,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "created_5",
            type = AchievementType.HABITS_CREATED,
            title = "Habit Planner",
            titleTr = "Aliskanlik Planlaycisi",
            description = "Create 5 habits",
            descriptionTr = "5 aliskanlik olustur",
            icon = "plus",
            requiredValue = 5,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "created_10",
            type = AchievementType.HABITS_CREATED,
            title = "Life Designer",
            titleTr = "Yasam Tasarimcisi",
            description = "Create 10 habits",
            descriptionTr = "10 aliskanlik olustur",
            icon = "plus",
            requiredValue = 10,
            tier = AchievementTier.GOLD
        ),

        // Perfect week/month
        Achievement(
            id = "perfect_week",
            type = AchievementType.PERFECT_WEEK,
            title = "Perfect Week",
            titleTr = "Mukemmel Hafta",
            description = "Complete all habits for 7 days straight",
            descriptionTr = "7 gun boyunca tum aliskanliklari tamamla",
            icon = "star",
            requiredValue = 1,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "perfect_month",
            type = AchievementType.PERFECT_MONTH,
            title = "Perfect Month",
            titleTr = "Mukemmel Ay",
            description = "Complete all habits for 30 days straight",
            descriptionTr = "30 gun boyunca tum aliskanliklari tamamla",
            icon = "star",
            requiredValue = 1,
            tier = AchievementTier.DIAMOND
        ),

        // Time-based achievements
        Achievement(
            id = "early_bird_10",
            type = AchievementType.EARLY_BIRD,
            title = "Early Bird",
            titleTr = "Erken Kalkan",
            description = "Complete 10 habits before 8 AM",
            descriptionTr = "Sabah 8'den once 10 aliskanlik tamamla",
            icon = "sunrise",
            requiredValue = 10,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "night_owl_10",
            type = AchievementType.NIGHT_OWL,
            title = "Night Owl",
            titleTr = "Gece Kusu",
            description = "Complete 10 habits after 10 PM",
            descriptionTr = "Gece 10'dan sonra 10 aliskanlik tamamla",
            icon = "moon",
            requiredValue = 10,
            tier = AchievementTier.SILVER
        ),

        // App usage
        Achievement(
            id = "dedicated_7",
            type = AchievementType.DEDICATED,
            title = "Dedicated User",
            titleTr = "Sadik Kullanici",
            description = "Use the app for 7 days",
            descriptionTr = "Uygulamayi 7 gun kullan",
            icon = "heart",
            requiredValue = 7,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "dedicated_30",
            type = AchievementType.DEDICATED,
            title = "Committed",
            titleTr = "Kararli",
            description = "Use the app for 30 days",
            descriptionTr = "Uygulamayi 30 gun kullan",
            icon = "heart",
            requiredValue = 30,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "dedicated_100",
            type = AchievementType.DEDICATED,
            title = "Lifestyle Change",
            titleTr = "Yasam Tarzi Degisikligi",
            description = "Use the app for 100 days",
            descriptionTr = "Uygulamayi 100 gun kullan",
            icon = "heart",
            requiredValue = 100,
            tier = AchievementTier.GOLD
        )
    )

    fun getById(id: String): Achievement? = all.find { it.id == id }

    fun getByType(type: AchievementType): List<Achievement> = all.filter { it.type == type }
}

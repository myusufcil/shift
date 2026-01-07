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
        ),
        Achievement(
            id = "dedicated_365",
            type = AchievementType.DEDICATED,
            title = "Year Long Journey",
            titleTr = "Bir Yillik Yolculuk",
            description = "Use the app for 365 days",
            descriptionTr = "Uygulamayi 365 gun kullan",
            icon = "heart",
            requiredValue = 365,
            tier = AchievementTier.DIAMOND
        ),

        // Weekend Warrior achievements
        Achievement(
            id = "weekend_warrior_4",
            type = AchievementType.WEEKEND_WARRIOR,
            title = "Weekend Starter",
            titleTr = "Hafta Sonu Baslangici",
            description = "Complete all habits on 4 weekends",
            descriptionTr = "4 hafta sonu tum aliskanliklari tamamla",
            icon = "sun",
            requiredValue = 4,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "weekend_warrior_12",
            type = AchievementType.WEEKEND_WARRIOR,
            title = "Weekend Warrior",
            titleTr = "Hafta Sonu Savascisi",
            description = "Complete all habits on 12 weekends",
            descriptionTr = "12 hafta sonu tum aliskanliklari tamamla",
            icon = "sun",
            requiredValue = 12,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "weekend_warrior_52",
            type = AchievementType.WEEKEND_WARRIOR,
            title = "Weekend Legend",
            titleTr = "Hafta Sonu Efsanesi",
            description = "Complete all habits on 52 weekends",
            descriptionTr = "52 hafta sonu tum aliskanliklari tamamla",
            icon = "sun",
            requiredValue = 52,
            tier = AchievementTier.GOLD
        ),

        // Variety achievements
        Achievement(
            id = "variety_3",
            type = AchievementType.VARIETY,
            title = "Diverse Habits",
            titleTr = "Cesitli Aliskanliklar",
            description = "Have habits in 3 different categories",
            descriptionTr = "3 farkli kategoride aliskanlik olustur",
            icon = "grid",
            requiredValue = 3,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "variety_5",
            type = AchievementType.VARIETY,
            title = "Well Rounded",
            titleTr = "Cok Yonlu",
            description = "Have habits in 5 different categories",
            descriptionTr = "5 farkli kategoride aliskanlik olustur",
            icon = "grid",
            requiredValue = 5,
            tier = AchievementTier.SILVER
        ),

        // Early Bird achievements
        Achievement(
            id = "early_bird_50",
            type = AchievementType.EARLY_BIRD,
            title = "Morning Person",
            titleTr = "Sabah Insani",
            description = "Complete 50 habits before 8 AM",
            descriptionTr = "Sabah 8'den once 50 aliskanlik tamamla",
            icon = "sunrise",
            requiredValue = 50,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "early_bird_100",
            type = AchievementType.EARLY_BIRD,
            title = "Dawn Champion",
            titleTr = "Safak Sampiyonu",
            description = "Complete 100 habits before 8 AM",
            descriptionTr = "Sabah 8'den once 100 aliskanlik tamamla",
            icon = "sunrise",
            requiredValue = 100,
            tier = AchievementTier.PLATINUM
        ),

        // Night Owl achievements
        Achievement(
            id = "night_owl_50",
            type = AchievementType.NIGHT_OWL,
            title = "Night Person",
            titleTr = "Gece Insani",
            description = "Complete 50 habits after 10 PM",
            descriptionTr = "Gece 10'dan sonra 50 aliskanlik tamamla",
            icon = "moon",
            requiredValue = 50,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "night_owl_100",
            type = AchievementType.NIGHT_OWL,
            title = "Midnight Master",
            titleTr = "Gece Yarisi Ustasi",
            description = "Complete 100 habits after 10 PM",
            descriptionTr = "Gece 10'dan sonra 100 aliskanlik tamamla",
            icon = "moon",
            requiredValue = 100,
            tier = AchievementTier.PLATINUM
        ),

        // Perfect weeks
        Achievement(
            id = "perfect_week_4",
            type = AchievementType.PERFECT_WEEK,
            title = "Monthly Perfectionist",
            titleTr = "Aylik Mukemmeliyetci",
            description = "Complete 4 perfect weeks",
            descriptionTr = "4 mukemmel hafta tamamla",
            icon = "star",
            requiredValue = 4,
            tier = AchievementTier.PLATINUM
        ),
        Achievement(
            id = "perfect_week_12",
            type = AchievementType.PERFECT_WEEK,
            title = "Quarterly Champion",
            titleTr = "Ceyreklik Sampiyon",
            description = "Complete 12 perfect weeks",
            descriptionTr = "12 mukemmel hafta tamamla",
            icon = "star",
            requiredValue = 12,
            tier = AchievementTier.DIAMOND
        ),

        // Hydration Hero
        Achievement(
            id = "hydration_10",
            type = AchievementType.HYDRATION_HERO,
            title = "Hydration Starter",
            titleTr = "Hidrasyon Baslangici",
            description = "Complete water drinking habit 10 times",
            descriptionTr = "Su icme aliskanligini 10 kez tamamla",
            icon = "droplet",
            requiredValue = 10,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "hydration_50",
            type = AchievementType.HYDRATION_HERO,
            title = "Hydration Hero",
            titleTr = "Hidrasyon Kahramani",
            description = "Complete water drinking habit 50 times",
            descriptionTr = "Su icme aliskanligini 50 kez tamamla",
            icon = "droplet",
            requiredValue = 50,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "hydration_100",
            type = AchievementType.HYDRATION_HERO,
            title = "Hydration Master",
            titleTr = "Hidrasyon Ustasi",
            description = "Complete water drinking habit 100 times",
            descriptionTr = "Su icme aliskanligini 100 kez tamamla",
            icon = "droplet",
            requiredValue = 100,
            tier = AchievementTier.GOLD
        ),

        // Fitness Fanatic
        Achievement(
            id = "fitness_10",
            type = AchievementType.FITNESS_FANATIC,
            title = "Fitness Starter",
            titleTr = "Fitness Baslangici",
            description = "Complete fitness habits 10 times",
            descriptionTr = "Fitness aliskanliklarini 10 kez tamamla",
            icon = "dumbbell",
            requiredValue = 10,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "fitness_50",
            type = AchievementType.FITNESS_FANATIC,
            title = "Fitness Fanatic",
            titleTr = "Fitness Fanatigi",
            description = "Complete fitness habits 50 times",
            descriptionTr = "Fitness aliskanliklarini 50 kez tamamla",
            icon = "dumbbell",
            requiredValue = 50,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "fitness_100",
            type = AchievementType.FITNESS_FANATIC,
            title = "Fitness Legend",
            titleTr = "Fitness Efsanesi",
            description = "Complete fitness habits 100 times",
            descriptionTr = "Fitness aliskanliklarini 100 kez tamamla",
            icon = "dumbbell",
            requiredValue = 100,
            tier = AchievementTier.GOLD
        ),

        // Mindfulness Master
        Achievement(
            id = "mindfulness_10",
            type = AchievementType.MINDFULNESS_MASTER,
            title = "Mindful Beginner",
            titleTr = "Farkindaliginiz Baslangici",
            description = "Complete meditation habits 10 times",
            descriptionTr = "Meditasyon aliskanliklarini 10 kez tamamla",
            icon = "peace",
            requiredValue = 10,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "mindfulness_50",
            type = AchievementType.MINDFULNESS_MASTER,
            title = "Mindfulness Practitioner",
            titleTr = "Farkindalik Uygulayicisi",
            description = "Complete meditation habits 50 times",
            descriptionTr = "Meditasyon aliskanliklarini 50 kez tamamla",
            icon = "peace",
            requiredValue = 50,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "mindfulness_100",
            type = AchievementType.MINDFULNESS_MASTER,
            title = "Mindfulness Master",
            titleTr = "Farkindalik Ustasi",
            description = "Complete meditation habits 100 times",
            descriptionTr = "Meditasyon aliskanliklarini 100 kez tamamla",
            icon = "peace",
            requiredValue = 100,
            tier = AchievementTier.GOLD
        )
    )

    fun getById(id: String): Achievement? = all.find { it.id == id }

    fun getByType(type: AchievementType): List<Achievement> = all.filter { it.type == type }
}

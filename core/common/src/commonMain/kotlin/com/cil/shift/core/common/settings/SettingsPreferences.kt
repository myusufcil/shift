package com.cil.shift.core.common.settings

interface SettingsPreferences {
    fun isNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)

    fun getDailyReminderTime(): String?
    fun setDailyReminderTime(time: String?)

    fun isHapticFeedbackEnabled(): Boolean
    fun setHapticFeedbackEnabled(enabled: Boolean)

    fun isSoundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)
}

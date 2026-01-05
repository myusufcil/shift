package com.cil.shift.core.common.settings

import android.content.Context
import android.content.SharedPreferences

class AndroidSettingsPreferences(context: Context) : SettingsPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_settings_prefs",
        Context.MODE_PRIVATE
    )

    override fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override fun getDailyReminderTime(): String? {
        return prefs.getString(KEY_DAILY_REMINDER_TIME, null)
    }

    override fun setDailyReminderTime(time: String?) {
        prefs.edit().putString(KEY_DAILY_REMINDER_TIME, time).apply()
    }

    override fun isHapticFeedbackEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, true)
    }

    override fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, enabled).apply()
    }

    override fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    override fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_REMINDER_TIME = "daily_reminder_time"
        private const val KEY_HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
    }
}

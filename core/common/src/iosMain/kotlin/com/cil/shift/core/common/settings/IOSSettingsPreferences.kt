package com.cil.shift.core.common.settings

import platform.Foundation.NSUserDefaults

class IOSSettingsPreferences : SettingsPreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun isNotificationsEnabled(): Boolean {
        // Default to true if not set
        return if (userDefaults.objectForKey(KEY_NOTIFICATIONS_ENABLED) != null) {
            userDefaults.boolForKey(KEY_NOTIFICATIONS_ENABLED)
        } else {
            true
        }
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_NOTIFICATIONS_ENABLED)
        userDefaults.synchronize()
    }

    override fun getDailyReminderTime(): String? {
        return userDefaults.stringForKey(KEY_DAILY_REMINDER_TIME)
    }

    override fun setDailyReminderTime(time: String?) {
        userDefaults.setObject(time, KEY_DAILY_REMINDER_TIME)
        userDefaults.synchronize()
    }

    override fun isHapticFeedbackEnabled(): Boolean {
        return if (userDefaults.objectForKey(KEY_HAPTIC_FEEDBACK_ENABLED) != null) {
            userDefaults.boolForKey(KEY_HAPTIC_FEEDBACK_ENABLED)
        } else {
            true
        }
    }

    override fun setHapticFeedbackEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_HAPTIC_FEEDBACK_ENABLED)
        userDefaults.synchronize()
    }

    override fun isSoundEnabled(): Boolean {
        return if (userDefaults.objectForKey(KEY_SOUND_ENABLED) != null) {
            userDefaults.boolForKey(KEY_SOUND_ENABLED)
        } else {
            true
        }
    }

    override fun setSoundEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_SOUND_ENABLED)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_REMINDER_TIME = "daily_reminder_time"
        private const val KEY_HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
    }
}

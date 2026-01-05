package com.cil.shift.core.common.theme

import platform.Foundation.NSUserDefaults

class IOSThemePreferences : ThemePreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getTheme(): String? {
        return userDefaults.stringForKey(KEY_THEME)
    }

    override fun setTheme(theme: String) {
        userDefaults.setObject(theme, KEY_THEME)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_THEME = "app_theme"
    }
}

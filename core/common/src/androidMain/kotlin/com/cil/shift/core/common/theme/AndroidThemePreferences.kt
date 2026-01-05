package com.cil.shift.core.common.theme

import android.content.Context
import android.content.SharedPreferences

class AndroidThemePreferences(context: Context) : ThemePreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_theme_prefs",
        Context.MODE_PRIVATE
    )

    override fun getTheme(): String? {
        return prefs.getString(KEY_THEME, null)
    }

    override fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).commit()
    }

    companion object {
        private const val KEY_THEME = "app_theme"
    }
}

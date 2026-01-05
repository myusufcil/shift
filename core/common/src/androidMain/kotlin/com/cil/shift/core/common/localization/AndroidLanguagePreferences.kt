package com.cil.shift.core.common.localization

import android.content.Context
import android.content.SharedPreferences

class AndroidLanguagePreferences(context: Context) : LanguagePreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_language_prefs",
        Context.MODE_PRIVATE
    )

    override fun getLanguageCode(): String? {
        return prefs.getString(KEY_LANGUAGE, null)
    }

    override fun setLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).commit()
    }

    companion object {
        private const val KEY_LANGUAGE = "language_code"
    }
}

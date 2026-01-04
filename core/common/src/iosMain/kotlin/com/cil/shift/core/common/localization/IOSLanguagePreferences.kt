package com.cil.shift.core.common.localization

import platform.Foundation.NSUserDefaults

class IOSLanguagePreferences : LanguagePreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getLanguageCode(): String? {
        return userDefaults.stringForKey(KEY_LANGUAGE)
    }

    override fun setLanguageCode(code: String) {
        userDefaults.setObject(code, KEY_LANGUAGE)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_LANGUAGE = "language_code"
    }
}

package com.cil.shift.core.common.onboarding

import android.content.Context
import android.content.SharedPreferences

class AndroidOnboardingPreferences(context: Context) : OnboardingPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_onboarding_prefs",
        Context.MODE_PRIVATE
    )

    override fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

package com.cil.shift.core.common.onboarding

import platform.Foundation.NSUserDefaults

class IOSOnboardingPreferences : OnboardingPreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun isOnboardingCompleted(): Boolean {
        return userDefaults.boolForKey(KEY_ONBOARDING_COMPLETED)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        userDefaults.setBool(completed, KEY_ONBOARDING_COMPLETED)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

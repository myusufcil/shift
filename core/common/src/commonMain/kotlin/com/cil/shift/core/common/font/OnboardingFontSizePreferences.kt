package com.cil.shift.core.common.font

import com.cil.shift.core.common.onboarding.OnboardingPreferences

/**
 * FontSizePreferences implementation that delegates to OnboardingPreferences.
 */
class OnboardingFontSizePreferences(
    private val onboardingPreferences: OnboardingPreferences
) : FontSizePreferences {

    override fun getFontSize(): FontSize {
        return FontSize.fromString(onboardingPreferences.getFontSize())
    }

    override fun setFontSize(fontSize: FontSize) {
        onboardingPreferences.setFontSize(fontSize.name)
    }
}

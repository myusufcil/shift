package com.cil.shift.core.common.haptic

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

/**
 * iOS implementation of HapticFeedbackManager
 */
class IOSHapticFeedbackManager : HapticFeedbackManager {

    private val lightGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val mediumGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    private val heavyGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    private val notificationGenerator = UINotificationFeedbackGenerator()

    override fun performHaptic(type: HapticType) {
        when (type) {
            HapticType.LIGHT -> {
                lightGenerator.prepare()
                lightGenerator.impactOccurred()
            }
            HapticType.MEDIUM -> {
                mediumGenerator.prepare()
                mediumGenerator.impactOccurred()
            }
            HapticType.HEAVY -> {
                heavyGenerator.prepare()
                heavyGenerator.impactOccurred()
            }
            HapticType.SUCCESS -> {
                notificationGenerator.prepare()
                notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            }
            HapticType.ERROR -> {
                notificationGenerator.prepare()
                notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
            }
        }
    }
}

private val hapticManagerInstance: HapticFeedbackManager by lazy {
    IOSHapticFeedbackManager()
}

actual fun getHapticFeedbackManager(): HapticFeedbackManager {
    return hapticManagerInstance
}

package com.cil.shift.core.common.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants

/**
 * Android implementation of HapticFeedbackManager
 */
class AndroidHapticFeedbackManager(private val context: Context) : HapticFeedbackManager {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun performHaptic(type: HapticType) {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    HapticType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    HapticType.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    HapticType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)
                }
                vib.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val duration = when (type) {
                    HapticType.LIGHT -> 10L
                    HapticType.MEDIUM -> 20L
                    HapticType.HEAVY -> 30L
                    HapticType.SUCCESS -> 25L
                    HapticType.ERROR -> 50L
                }
                vib.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val duration = when (type) {
                    HapticType.LIGHT -> 10L
                    HapticType.MEDIUM -> 20L
                    HapticType.HEAVY -> 30L
                    HapticType.SUCCESS -> 25L
                    HapticType.ERROR -> 50L
                }
                vib.vibrate(duration)
            }
        }
    }
}

// Singleton holder for the haptic manager
private var hapticManagerInstance: HapticFeedbackManager? = null

fun initHapticFeedback(context: Context) {
    hapticManagerInstance = AndroidHapticFeedbackManager(context)
}

actual fun getHapticFeedbackManager(): HapticFeedbackManager {
    return hapticManagerInstance ?: object : HapticFeedbackManager {
        override fun performHaptic(type: HapticType) {
            // No-op if not initialized
        }
    }
}

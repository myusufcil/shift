package com.cil.shift.core.common.haptic

/**
 * Types of haptic feedback
 */
enum class HapticType {
    /** Light tap feedback - for toggles, selections */
    LIGHT,
    /** Medium feedback - for confirmations */
    MEDIUM,
    /** Heavy feedback - for important actions */
    HEAVY,
    /** Success feedback - for completed actions */
    SUCCESS,
    /** Error feedback - for failed actions */
    ERROR
}

/**
 * Interface for haptic feedback
 */
interface HapticFeedbackManager {
    fun performHaptic(type: HapticType)
}

/**
 * Expect function to get platform-specific haptic feedback manager
 */
expect fun getHapticFeedbackManager(): HapticFeedbackManager

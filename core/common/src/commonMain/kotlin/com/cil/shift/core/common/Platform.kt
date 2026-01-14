package com.cil.shift.core.common

/**
 * Platform detection utility
 */
expect val isIOS: Boolean
expect val isAndroid: Boolean

/**
 * App version - must be initialized at app startup on Android
 */
object AppVersionHolder {
    var version: String = "1.0.0"
}

val appVersion: String
    get() = AppVersionHolder.version

package com.cil.shift.core.common

import platform.Foundation.NSBundle

actual val isIOS: Boolean = true
actual val isAndroid: Boolean = false

// Initialize iOS version from bundle
private val iosAppVersion: String by lazy {
    NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0.0"
}

fun initIOSAppVersion() {
    AppVersionHolder.version = iosAppVersion
}

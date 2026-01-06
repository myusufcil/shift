package com.cil.shift.core.common.widget

import android.content.Context
import android.content.Intent

/**
 * Android implementation of WidgetNotifier.
 * Sends a broadcast to update all widgets.
 */
actual object WidgetNotifier {

    private var appContext: Context? = null

    const val ACTION_UPDATE_WIDGETS = "com.cil.shift.ACTION_UPDATE_WIDGETS"

    /**
     * Initialize with application context. Call once from Application.onCreate()
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun notifyWidgetsToUpdate() {
        appContext?.let { context ->
            android.util.Log.d("WidgetNotifier", "Sending widget update broadcast")
            val intent = Intent(ACTION_UPDATE_WIDGETS)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } ?: run {
            android.util.Log.w("WidgetNotifier", "Context not initialized - call WidgetNotifier.initialize() first")
        }
    }
}

package com.cil.shift.core.common.widget

/**
 * Platform-specific widget notifier.
 * Updates widgets when habit data changes.
 */
expect object WidgetNotifier {
    /**
     * Notify widgets to refresh their data.
     * On Android: Updates all Glance widgets
     * On iOS: Reloads widget timelines (if supported)
     */
    fun notifyWidgetsToUpdate()
}

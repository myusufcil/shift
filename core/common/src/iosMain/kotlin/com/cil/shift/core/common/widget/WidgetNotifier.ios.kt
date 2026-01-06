package com.cil.shift.core.common.widget

/**
 * iOS implementation of WidgetNotifier.
 * On iOS, widget updates are handled through App Groups and UserDefaults.
 * The widget extension reads from shared UserDefaults and has its own timeline refresh.
 * This is a no-op as iOS widgets refresh based on their own timeline configuration.
 */
actual object WidgetNotifier {
    actual fun notifyWidgetsToUpdate() {
        // iOS widgets are updated through App Groups shared UserDefaults
        // The widget extension reads data on its own timeline
        // No direct API call needed from KMP code
        println("iOS: Widget data updated in shared storage")
    }
}

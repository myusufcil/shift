package com.cil.shift.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility object to update all Shift widgets.
 * Call this whenever habit data changes in the main app.
 */
object WidgetUpdater {

    /**
     * Updates all Shift habit widgets.
     * Safe to call from any context - runs in background.
     */
    fun updateAllWidgets(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("WidgetUpdater", "Updating all widgets...")
                HabitWidget().updateAll(context)
                android.util.Log.d("WidgetUpdater", "All widgets updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("WidgetUpdater", "Error updating widgets", e)
            }
        }
    }

    /**
     * Check if any Shift widgets exist on the home screen.
     */
    suspend fun hasWidgets(context: Context): Boolean {
        return try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(HabitWidget::class.java)
            glanceIds.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}

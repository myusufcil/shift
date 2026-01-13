package com.cil.shift.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cil.shift.MainActivity
import com.cil.shift.core.database.DatabaseDriverFactory
import com.cil.shift.feature.habits.data.database.HabitsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

/**
 * Single Habit Widget - Shows one habit with large toggle button
 */
class SingleHabitWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(80.dp, 80.dp),    // Small
            DpSize(150.dp, 150.dp),  // Medium
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load habit data before provideContent (since we're in suspend context)
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id) as androidx.datastore.preferences.core.Preferences
        val habitId: String? = prefs[stringPreferencesKey("habit_id_${id.hashCode()}")]

        val habit = if (habitId != null) {
            loadHabit(context, habitId)
        } else {
            // If no habit selected, show first one
            loadFirstHabit(context)
        }

        provideContent {
            val size = LocalSize.current

            if (habit != null) {
                if (size.width < 120.dp) {
                    SmallSingleHabitWidget(habit)
                } else {
                    LargeSingleHabitWidget(habit)
                }
            } else {
                EmptyStateWidget()
            }
        }
    }

    private suspend fun loadHabit(context: Context, habitId: String): SingleWidgetHabit? = withContext(Dispatchers.IO) {
        var driver: app.cash.sqldelight.db.SqlDriver? = null
        try {
            val driverFactory = DatabaseDriverFactory(context)
            driver = driverFactory.createDriverWithSchema(
                HabitsDatabase.Schema,
                "shift_v4.db"
            )
            val database = HabitsDatabase(driver)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            val habit = database.habitQueries.getById(habitId).executeAsOneOrNull() ?: return@withContext null
            val completion = database.habitQueries.getCompletion(habitId, today).executeAsOneOrNull()

            val isCompleted = completion?.is_completed == 1L
            val currentValue = completion?.current_value?.toInt() ?: 0
            val targetValue = habit.target_value?.toInt()
            val progress = if (targetValue != null && targetValue > 0) {
                (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
            } else if (isCompleted) 1f else 0f

            val streak = calculateStreak(database, habitId, today)

            SingleWidgetHabit(
                id = habit.id,
                name = habit.name,
                icon = habit.icon,
                color = habit.color,
                isCompleted = isCompleted,
                streak = streak,
                progress = progress,
                targetValue = targetValue,
                currentValue = currentValue,
                date = today
            )
        } catch (e: Exception) {
            android.util.Log.e("SingleHabitWidget", "Error loading habit", e)
            null
        } finally {
            driver?.close()
        }
    }

    private suspend fun loadFirstHabit(context: Context): SingleWidgetHabit? = withContext(Dispatchers.IO) {
        var driver: app.cash.sqldelight.db.SqlDriver? = null
        try {
            val driverFactory = DatabaseDriverFactory(context)
            driver = driverFactory.createDriverWithSchema(
                HabitsDatabase.Schema,
                "shift_v4.db"
            )
            val database = HabitsDatabase(driver)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            val habit = database.habitQueries.getAll().executeAsList()
                .filter { it.is_archived == 0L }
                .firstOrNull() ?: return@withContext null

            val completion = database.habitQueries.getCompletion(habit.id, today).executeAsOneOrNull()

            val isCompleted = completion?.is_completed == 1L
            val currentValue = completion?.current_value?.toInt() ?: 0
            val targetValue = habit.target_value?.toInt()
            val progress = if (targetValue != null && targetValue > 0) {
                (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
            } else if (isCompleted) 1f else 0f

            val streak = calculateStreak(database, habit.id, today)

            SingleWidgetHabit(
                id = habit.id,
                name = habit.name,
                icon = habit.icon,
                color = habit.color,
                isCompleted = isCompleted,
                streak = streak,
                progress = progress,
                targetValue = targetValue,
                currentValue = currentValue,
                date = today
            )
        } catch (e: Exception) {
            android.util.Log.e("SingleHabitWidget", "Error loading first habit", e)
            null
        } finally {
            driver?.close()
        }
    }

    private fun calculateStreak(database: HabitsDatabase, habitId: String, fromDate: String): Int {
        var streak = 0
        var checkDate = LocalDate.parse(fromDate)

        for (i in 0 until 30) {
            val completion = database.habitQueries.getCompletion(habitId, checkDate.toString()).executeAsOneOrNull()

            if (completion?.is_completed == 1L) {
                streak++
            } else if (i > 0) {
                break
            }
            checkDate = checkDate.minus(1, DateTimeUnit.DAY)
        }
        return streak
    }

    companion object {
        val habitIdKey = ActionParameters.Key<String>("habitId")
        val dateKey = ActionParameters.Key<String>("date")
    }
}

data class SingleWidgetHabit(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val isCompleted: Boolean,
    val streak: Int = 0,
    val progress: Float = 0f,
    val targetValue: Int? = null,
    val currentValue: Int = 0,
    val date: String
)

// ============================================
// SMALL SINGLE HABIT WIDGET
// ============================================
@Composable
private fun SmallSingleHabitWidget(habit: SingleWidgetHabit) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.color))
    } catch (e: Exception) {
        Color(0xFF4E7CFF)
    }

    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val completedColor = ColorProvider(Color(0xFF4ECDC4))
    val buttonColor = if (habit.isCompleted) completedColor else ColorProvider(habitColor)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<SingleHabitToggleAction>(
                actionParametersOf(
                    SingleHabitWidget.habitIdKey to habit.id,
                    SingleHabitWidget.dateKey to habit.date
                )
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large toggle button
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .background(buttonColor)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (habit.isCompleted) "✓" else getIconEmoji(habit.icon),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = if (habit.isCompleted) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Streak
            if (habit.streak > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", style = TextStyle(fontSize = 10.sp))
                    Text(
                        text = "${habit.streak}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFF9500)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ============================================
// LARGE SINGLE HABIT WIDGET
// ============================================
@Composable
private fun LargeSingleHabitWidget(habit: SingleWidgetHabit) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.color))
    } catch (e: Exception) {
        Color(0xFF4E7CFF)
    }

    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val cardColor = ColorProvider(Color(0xFF16213E))
    val textColor = ColorProvider(Color.White)
    val subtextColor = ColorProvider(Color(0xFFAAAAAA))
    val completedColor = ColorProvider(Color(0xFF4ECDC4))
    val buttonColor = if (habit.isCompleted) completedColor else ColorProvider(habitColor)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Top
        ) {
            // Header with name
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = GlanceModifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(ColorProvider(habitColor))
                        .cornerRadius(2.dp)
                ) {}

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = habit.name,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )

                // Streak badge
                if (habit.streak > 0) {
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(Color(0xFFFF9500).copy(alpha = 0.2f)))
                            .cornerRadius(8.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", style = TextStyle(fontSize = 10.sp))
                            Text(
                                text = "${habit.streak}",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFFF9500)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Large toggle button
            Box(
                modifier = GlanceModifier
                    .size(72.dp)
                    .background(buttonColor)
                    .cornerRadius(20.dp)
                    .clickable(actionRunCallback<SingleHabitToggleAction>(
                        actionParametersOf(
                            SingleHabitWidget.habitIdKey to habit.id,
                            SingleHabitWidget.dateKey to habit.date
                        )
                    )),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (habit.isCompleted) "✓" else getIconEmoji(habit.icon),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = if (habit.isCompleted) 36.sp else 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Status text
            Text(
                text = if (habit.isCompleted) "Completed!" else "Tap to complete",
                style = TextStyle(
                    color = if (habit.isCompleted) completedColor else subtextColor,
                    fontSize = 12.sp
                )
            )

            // Progress for measurable habits
            if (habit.targetValue != null) {
                Spacer(modifier = GlanceModifier.height(8.dp))

                // Progress bar
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(cardColor)
                        .cornerRadius(3.dp)
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .width((habit.progress * 120).dp)
                            .background(if (habit.isCompleted) completedColor else ColorProvider(habitColor))
                            .cornerRadius(3.dp)
                    ) {}
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "${habit.currentValue}/${habit.targetValue}",
                    style = TextStyle(
                        color = subtextColor,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ============================================
// EMPTY STATE
// ============================================
@Composable
private fun EmptyStateWidget() {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "✨",
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Add habit",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF4E7CFF)),
                    fontSize = 12.sp
                )
            )
        }
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        "water", "wat", "hydration" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "pill", "med", "medicine" -> "💊"
        "journal", "jou", "write" -> "✍️"
        "meditation", "mindfulness" -> "🧘"
        "books", "book", "boo", "read", "reading" -> "📚"
        "running", "run" -> "🏃"
        "walking", "wal", "walk" -> "🚶"
        "gym", "dumbbell", "dum", "fitness", "workout", "exercise" -> "🏋️"
        "yoga", "yog" -> "🧘"
        "sleep", "sle", "rest" -> "😴"
        "coffee", "cof" -> "☕"
        "music", "mus" -> "🎵"
        "art", "palette", "draw", "paint" -> "🎨"
        "fire", "fir" -> "🔥"
        "check", "che", "task" -> "✅"
        "star", "goal" -> "⭐"
        "heart", "health" -> "❤️"
        "brain", "study", "learn" -> "🧠"
        "money", "save", "finance" -> "💰"
        "phone", "screen", "digital" -> "📱"
        "sun", "morning" -> "☀️"
        "moon", "night", "evening" -> "🌙"
        "food", "eat", "meal" -> "🍽️"
        "no_smoking", "quit" -> "🚭"
        "language", "speak" -> "🗣️"
        "code", "programming" -> "💻"
        else -> if (icon.any { it.code >= 0x1F300 }) icon else "✓"
    }
}

// ============================================
// TOGGLE ACTION
// ============================================
class SingleHabitToggleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[SingleHabitWidget.habitIdKey] ?: return
        val date = parameters[SingleHabitWidget.dateKey] ?: return

        android.util.Log.d("SingleHabitWidget", "Toggle: $habitId on $date")

        withContext(Dispatchers.IO) {
            var driver: app.cash.sqldelight.db.SqlDriver? = null
            try {
                val driverFactory = DatabaseDriverFactory(context)
                driver = driverFactory.createDriverWithSchema(
                    HabitsDatabase.Schema,
                    "shift_v4.db"
                )
                val database = HabitsDatabase(driver)

                val existing = database.habitQueries
                    .getCompletion(habitId, date)
                    .executeAsOneOrNull()

                if (existing != null) {
                    val newStatus = if (existing.is_completed == 1L) 0L else 1L
                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = newStatus,
                        current_value = existing.current_value ?: 0L,
                        note = existing.note
                    )
                } else {
                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = 1L,
                        current_value = 0L,
                        note = null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SingleHabitWidget", "Error toggling habit", e)
            } finally {
                driver?.close()
            }
        }

        // Update this widget
        SingleHabitWidget().update(context, glanceId)

        // Also update main habit widgets
        HabitWidget().updateAll(context)

        // Notify app to refresh
        val intent = Intent(HabitWidget.ACTION_HABIT_TOGGLED)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}

// ============================================
// WIDGET RECEIVER
// ============================================
class SingleHabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SingleHabitWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            com.cil.shift.core.common.widget.WidgetNotifier.ACTION_UPDATE_WIDGETS,
            HabitWidget.ACTION_HABIT_TOGGLED -> {
                android.util.Log.d("SingleHabitWidget", "Received update broadcast")
                GlobalScope.launch {
                    try {
                        SingleHabitWidget().updateAll(context)
                    } catch (e: Exception) {
                        android.util.Log.e("SingleHabitWidget", "Error updating widgets", e)
                    }
                }
            }
        }
    }
}

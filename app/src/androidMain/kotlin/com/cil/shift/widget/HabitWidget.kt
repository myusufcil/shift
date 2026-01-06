package com.cil.shift.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
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

class HabitWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val habits = loadHabits(context)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        provideContent {
            HabitWidgetContent(
                habits = habits,
                today = today,
                onHabitClick = { habitId ->
                    actionRunCallback<ToggleHabitAction>(
                        actionParametersOf(habitIdKey to habitId, dateKey to today)
                    )
                }
            )
        }
    }

    private suspend fun loadHabits(context: Context): List<WidgetHabit> = withContext(Dispatchers.IO) {
        var driver: app.cash.sqldelight.db.SqlDriver? = null
        try {
            android.util.Log.d("HabitWidget", "Loading habits...")
            val driverFactory = DatabaseDriverFactory(context)
            driver = driverFactory.createDriverWithSchema(
                HabitsDatabase.Schema,
                "shift_v4.db"
            )
            val database = HabitsDatabase(driver)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            val habits = database.habitQueries.getAll().executeAsList()
            val completions = database.habitQueries.getCompletionsForDate(today).executeAsList()

            android.util.Log.d("HabitWidget", "Found ${habits.size} habits, ${completions.size} completions for $today")
            completions.forEach { c ->
                android.util.Log.d("HabitWidget", "  Completion: ${c.habit_id} = ${c.is_completed}")
            }

            habits.filter { it.is_archived == 0L }.take(5).map { habit ->
                val completion = completions.find { it.habit_id == habit.id }
                val isCompleted = completion?.is_completed == 1L
                val currentValue = completion?.current_value?.toInt() ?: 0
                val targetValue = habit.target_value?.toInt()
                val progress = if (targetValue != null && targetValue > 0) {
                    (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
                } else if (isCompleted) 1f else 0f

                // Calculate streak (simplified - just count consecutive days)
                val streak = calculateStreak(database, habit.id, today)

                WidgetHabit(
                    id = habit.id,
                    name = habit.name,
                    icon = habit.icon,
                    color = habit.color,
                    isCompleted = isCompleted,
                    streak = streak,
                    progress = progress,
                    targetValue = targetValue,
                    currentValue = currentValue
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitWidget", "Error loading habits", e)
            e.printStackTrace()
            emptyList()
        } finally {
            driver?.close()
        }
    }

    private fun calculateStreak(database: HabitsDatabase, habitId: String, fromDate: String): Int {
        var streak = 0
        var checkDate = LocalDate.parse(fromDate)

        for (i in 0 until 30) { // Check last 30 days
            val completion = database.habitQueries.getCompletion(habitId, checkDate.toString()).executeAsOneOrNull()

            if (completion?.is_completed == 1L) {
                streak++
            } else if (i > 0) { // Allow today to be incomplete
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

data class WidgetHabit(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val isCompleted: Boolean,
    val streak: Int = 0,
    val progress: Float = 0f, // For measurable habits (0.0 to 1.0)
    val targetValue: Int? = null,
    val currentValue: Int = 0
)

@Composable
private fun HabitWidgetContent(
    habits: List<WidgetHabit>,
    today: String,
    onHabitClick: (String) -> Unit
) {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val cardColor = ColorProvider(Color(0xFF16213E))
    val textColor = ColorProvider(Color.White)
    val subtextColor = ColorProvider(Color(0xFFAAAAAA))
    val accentColor = ColorProvider(Color(0xFF00D9FF))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(24.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Header with progress
            val completedCount = habits.count { it.isCompleted }
            val totalCount = habits.size
            val progressPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shift",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "$completedCount/$totalCount",
                    style = TextStyle(
                        color = subtextColor,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                // Progress badge
                Box(
                    modifier = GlanceModifier
                        .background(if (progressPercent == 100) ColorProvider(Color(0xFF4ECDC4)) else ColorProvider(Color(0xFF333355)))
                        .cornerRadius(10.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$progressPercent%",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            if (habits.isEmpty()) {
                // Empty state
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No habits yet",
                            style = TextStyle(
                                color = subtextColor,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Tap to add habits",
                            style = TextStyle(
                                color = accentColor,
                                fontSize = 12.sp
                            ),
                            modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                        )
                    }
                }
            } else {
                // Habit list
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    habits.forEach { habit ->
                        HabitItem(
                            habit = habit,
                            onClick = { onHabitClick(habit.id) }
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitItem(
    habit: WidgetHabit,
    onClick: () -> Unit
) {
    val cardColor = ColorProvider(Color(0xFF16213E))
    val textColor = ColorProvider(Color.White)
    val subtextColor = ColorProvider(Color(0xFFAAAAAA))
    val completedColor = ColorProvider(Color(0xFF4ECDC4))
    val streakColor = ColorProvider(Color(0xFFFF9500))
    val checkColor = if (habit.isCompleted) completedColor else ColorProvider(Color(0xFF333355))

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(cardColor)
            .cornerRadius(12.dp)
            .padding(10.dp)
            .clickable(actionRunCallback<ToggleHabitAction>(
                actionParametersOf(
                    HabitWidget.habitIdKey to habit.id,
                    HabitWidget.dateKey to Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                )
            )),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = GlanceModifier
                .size(26.dp)
                .background(checkColor)
                .cornerRadius(7.dp),
            contentAlignment = Alignment.Center
        ) {
            if (habit.isCompleted) {
                Text(
                    text = "✓",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        // Icon
        Text(
            text = getIconEmoji(habit.icon),
            style = TextStyle(fontSize = 18.sp)
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Name and details
        Column(
            modifier = GlanceModifier.defaultWeight(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = habit.name,
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            // Show progress for measurable habits or streak
            if (habit.targetValue != null) {
                Text(
                    text = "${habit.currentValue}/${habit.targetValue}",
                    style = TextStyle(
                        color = subtextColor,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Streak badge
        if (habit.streak > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥",
                    style = TextStyle(fontSize = 12.sp)
                )
                Text(
                    text = "${habit.streak}",
                    style = TextStyle(
                        color = streakColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        "water", "wat", "hydration" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "pill", "med" -> "💊"
        "journal", "jou" -> "✍️"
        "meditation", "me" -> "🧘"
        "books", "book", "boo", "read" -> "📚"
        "running", "run" -> "🏃"
        "walking", "wal" -> "🚶"
        "gym", "dumbbell", "dum", "fitness", "workout" -> "🏋️"
        "yoga", "yog" -> "🧘"
        "sleep", "sle" -> "😴"
        "coffee", "cof" -> "☕"
        "music", "mus" -> "🎵"
        "art", "palette" -> "🎨"
        "fire", "fir" -> "🔥"
        "check", "che" -> "✅"
        else -> if (icon.any { it.code >= 0x1F300 }) icon else "✓"
    }
}

class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitWidget.habitIdKey]
        val date = parameters[HabitWidget.dateKey]

        android.util.Log.d("HabitWidget", "Toggle action triggered - habitId: $habitId, date: $date")

        if (habitId == null || date == null) {
            android.util.Log.e("HabitWidget", "Missing parameters - habitId: $habitId, date: $date")
            return
        }

        withContext(Dispatchers.IO) {
            var driver: app.cash.sqldelight.db.SqlDriver? = null
            try {
                val driverFactory = DatabaseDriverFactory(context)
                driver = driverFactory.createDriverWithSchema(
                    HabitsDatabase.Schema,
                    "shift_v4.db"
                )
                val database = HabitsDatabase(driver)

                // Check current completion status
                val existing = database.habitQueries
                    .getCompletion(habitId, date)
                    .executeAsOneOrNull()

                android.util.Log.d("HabitWidget", "Existing completion: $existing")

                if (existing != null) {
                    // Toggle completion
                    val newStatus = if (existing.is_completed == 1L) 0L else 1L
                    android.util.Log.d("HabitWidget", "Toggling from ${existing.is_completed} to $newStatus")
                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = newStatus,
                        current_value = existing.current_value ?: 0L,
                        note = existing.note
                    )
                } else {
                    // Create new completion
                    android.util.Log.d("HabitWidget", "Creating new completion as completed")
                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = 1L,
                        current_value = 0L,
                        note = null
                    )
                }

                android.util.Log.d("HabitWidget", "Toggle completed successfully")
            } catch (e: Exception) {
                android.util.Log.e("HabitWidget", "Error toggling habit", e)
                e.printStackTrace()
            } finally {
                // Close the driver to ensure changes are flushed
                driver?.close()
            }
        }

        // Update widget after database changes are complete
        android.util.Log.d("HabitWidget", "Updating widget...")
        HabitWidget().update(context, glanceId)
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Handle custom broadcast to update widgets
        if (intent.action == com.cil.shift.core.common.widget.WidgetNotifier.ACTION_UPDATE_WIDGETS) {
            android.util.Log.d("HabitWidget", "Received widget update broadcast")
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    HabitWidget().updateAll(context)
                    android.util.Log.d("HabitWidget", "Widgets updated via broadcast")
                } catch (e: Exception) {
                    android.util.Log.e("HabitWidget", "Error updating widgets via broadcast", e)
                }
            }
        }
    }
}

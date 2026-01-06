package com.cil.shift.widget

import android.content.Context
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
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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
        try {
            val driverFactory = DatabaseDriverFactory(context)
            val driver = driverFactory.createDriverWithSchema(
                HabitsDatabase.Schema,
                "shift_v4.db"
            )
            val database = HabitsDatabase(driver)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            val habits = database.habitQueries.getAll().executeAsList()
            val completions = database.habitQueries.getCompletionsForDate(today).executeAsList()

            habits.filter { it.is_archived == 0L }.take(5).map { habit ->
                val isCompleted = completions.any { completion ->
                    completion.habit_id == habit.id && completion.is_completed == 1L
                }
                WidgetHabit(
                    id = habit.id,
                    name = habit.name,
                    icon = habit.icon,
                    color = habit.color,
                    isCompleted = isCompleted
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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
    val isCompleted: Boolean
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
            // Header
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "Today's Habits",
                    style = TextStyle(
                        color = subtextColor,
                        fontSize = 12.sp
                    )
                )
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
    val completedColor = ColorProvider(Color(0xFF4ECDC4))
    val checkColor = if (habit.isCompleted) completedColor else ColorProvider(Color(0xFF333355))

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(cardColor)
            .cornerRadius(12.dp)
            .padding(12.dp)
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
                .size(28.dp)
                .background(checkColor)
                .cornerRadius(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (habit.isCompleted) {
                Text(
                    text = "✓",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Icon
        Text(
            text = getIconEmoji(habit.icon),
            style = TextStyle(fontSize = 20.sp)
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Name
        Text(
            text = habit.name,
            style = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
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
        val habitId = parameters[HabitWidget.habitIdKey] ?: return
        val date = parameters[HabitWidget.dateKey] ?: return

        withContext(Dispatchers.IO) {
            try {
                val driverFactory = DatabaseDriverFactory(context)
                val driver = driverFactory.createDriverWithSchema(
                    HabitsDatabase.Schema,
                    "shift_v4.db"
                )
                val database = HabitsDatabase(driver)

                // Check current completion status
                val existing = database.habitQueries
                    .getCompletion(habitId, date)
                    .executeAsOneOrNull()

                if (existing != null) {
                    // Toggle completion
                    val newStatus = if (existing.is_completed == 1L) 0L else 1L
                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = newStatus,
                        current_value = existing.current_value ?: 0L,
                        note = existing.note
                    )
                } else {
                    // Create new completion
                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = 1L,
                        current_value = 0L,
                        note = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Update widget
        HabitWidget().update(context, glanceId)
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}

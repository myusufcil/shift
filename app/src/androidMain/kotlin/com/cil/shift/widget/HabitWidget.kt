package com.cil.shift.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.appwidget.GlanceAppWidgetManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
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

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),   // Small 2x2
            DpSize(250.dp, 110.dp),   // Medium 4x2 (wider)
            DpSize(250.dp, 180.dp),   // Large 4x4 (wider)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val habits = loadHabits(context)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        provideContent {
            val size = LocalSize.current

            when {
                size.width < 180.dp && size.height < 150.dp -> SmallWidget(habits)
                size.height < 160.dp -> MediumWidget(habits, today)
                else -> LargeWidget(habits, today)
            }
        }
    }

    private suspend fun loadHabits(context: Context): List<WidgetHabit> = withContext(Dispatchers.IO) {
        var driver: app.cash.sqldelight.db.SqlDriver? = null
        try {
            val driverFactory = DatabaseDriverFactory(context)
            driver = driverFactory.createDriverWithSchema(
                HabitsDatabase.Schema,
                "shift_v4.db"
            )
            val database = HabitsDatabase(driver)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            val habits = database.habitQueries.getAll().executeAsList()
            val completions = database.habitQueries.getCompletionsForDate(today).executeAsList()

            habits.filter { it.is_archived == 0L }.take(10).map { habit ->
                val completion = completions.find { it.habit_id == habit.id }
                val isCompleted = completion?.is_completed == 1L
                val currentValue = completion?.current_value?.toInt() ?: 0
                val targetValue = habit.target_value?.toInt()
                val progress = if (targetValue != null && targetValue > 0) {
                    (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
                } else if (isCompleted) 1f else 0f

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
            emptyList()
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
        val incrementKey = ActionParameters.Key<Int>("increment")
        const val ACTION_HABIT_TOGGLED = "com.cil.shift.ACTION_HABIT_TOGGLED"
    }
}

data class WidgetHabit(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val isCompleted: Boolean,
    val streak: Int = 0,
    val progress: Float = 0f,
    val targetValue: Int? = null,
    val currentValue: Int = 0
)

// ============================================
// SMALL WIDGET (2x2) - Daily Summary
// ============================================
@Composable
private fun SmallWidget(habits: List<WidgetHabit>) {
    val completedCount = habits.count { it.isCompleted }
    val totalCount = habits.size
    val progressPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val accentColor = if (progressPercent == 100) ColorProvider(Color(0xFF4ECDC4)) else ColorProvider(Color(0xFF4E7CFF))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator (simulated with text)
            Box(
                modifier = GlanceModifier
                    .size(56.dp)
                    .background(ColorProvider(Color(0xFF16213E)))
                    .cornerRadius(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$progressPercent%",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "$completedCount/$totalCount",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFAAAAAA)),
                    fontSize = 12.sp
                )
            )

            // Streak if any
            val maxStreak = habits.maxOfOrNull { it.streak } ?: 0
            if (maxStreak > 0) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", style = TextStyle(fontSize = 10.sp))
                    Text(
                        text = "$maxStreak",
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
// MEDIUM WIDGET (4x2) - Habit List
// ============================================
@Composable
private fun MediumWidget(habits: List<WidgetHabit>, today: String) {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val cardColor = ColorProvider(Color(0xFF16213E))
    val completedCount = habits.count { it.isCompleted }
    val totalCount = habits.size
    val progressPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .padding(12.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
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
                        color = ColorProvider(Color(0xFF4E7CFF)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(if (progressPercent == 100) ColorProvider(Color(0xFF4ECDC4)) else ColorProvider(Color(0xFF333355)))
                        .cornerRadius(8.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (habits.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap to add habits",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF4E7CFF)),
                            fontSize = 12.sp
                        ),
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                    )
                }
            } else {
                // Habit chips in a row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    habits.take(4).forEach { habit ->
                        HabitChip(habit, today)
                        Spacer(modifier = GlanceModifier.width(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitChip(habit: WidgetHabit, today: String) {
    val checkColor = if (habit.isCompleted)
        ColorProvider(Color(0xFF4ECDC4))
    else
        ColorProvider(Color(0xFF333355))

    Box(
        modifier = GlanceModifier
            .background(ColorProvider(Color(0xFF16213E)))
            .cornerRadius(12.dp)
            .padding(8.dp)
            .clickable(actionRunCallback<ToggleHabitAction>(
                actionParametersOf(
                    HabitWidget.habitIdKey to habit.id,
                    HabitWidget.dateKey to today
                )
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(checkColor)
                    .cornerRadius(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (habit.isCompleted) "✓" else getIconEmoji(habit.icon),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = if (habit.isCompleted) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            if (habit.streak > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", style = TextStyle(fontSize = 8.sp))
                    Text(
                        text = "${habit.streak}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFF9500)),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ============================================
// LARGE WIDGET (4x4) - Detailed View
// ============================================
@Composable
private fun LargeWidget(habits: List<WidgetHabit>, today: String) {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val completedCount = habits.count { it.isCompleted }
    val totalCount = habits.size
    val progressPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
    val totalStreak = habits.sumOf { it.streak }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(24.dp)
            .padding(16.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Header with stats
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shift",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF4E7CFF)),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Today's Progress",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF888888)),
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.defaultWeight())

                // Stats badges
                Row {
                    StatBadge(
                        value = "$progressPercent%",
                        color = if (progressPercent == 100) Color(0xFF4ECDC4) else Color(0xFF4E7CFF)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    if (totalStreak > 0) {
                        StatBadge(value = "🔥$totalStreak", color = Color(0xFFFF9500))
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Progress bar
            ProgressBar(progressPercent)

            Spacer(modifier = GlanceModifier.height(12.dp))

            if (habits.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✨", style = TextStyle(fontSize = 32.sp))
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = "No habits yet",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFAAAAAA)),
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "Tap to add your first habit",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF4E7CFF)),
                                fontSize = 12.sp
                            ),
                            modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                        )
                    }
                }
            } else {
                // Habit list (widget scroll is limited, show top habits)
                Column(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                ) {
                    habits.take(8).forEach { habit ->
                        LargeHabitItem(habit, today)
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }

                    // Motivational message at bottom
                    if (progressPercent == 100) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(ColorProvider(Color(0xFF4ECDC4).copy(alpha = 0.2f)))
                                .cornerRadius(8.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎉 All done!",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF4ECDC4)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(value: String, color: Color) {
    Box(
        modifier = GlanceModifier
            .background(ColorProvider(color.copy(alpha = 0.2f)))
            .cornerRadius(8.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun ProgressBar(percent: Int) {
    val backgroundColor = ColorProvider(Color(0xFF16213E))
    val progressColor = when {
        percent == 100 -> ColorProvider(Color(0xFF4ECDC4))
        percent >= 50 -> ColorProvider(Color(0xFF4E7CFF))
        else -> ColorProvider(Color(0xFFFF6B6B))
    }

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(8.dp)
            .background(backgroundColor)
            .cornerRadius(4.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxHeight()
                .width((percent * 2).dp.coerceAtMost(200.dp)) // Approximate width
                .background(progressColor)
                .cornerRadius(4.dp)
        ) {}
    }
}

@Composable
private fun LargeHabitItem(habit: WidgetHabit, today: String) {
    val cardColor = ColorProvider(Color(0xFF16213E))
    val textColor = ColorProvider(Color.White)
    val subtextColor = ColorProvider(Color(0xFFAAAAAA))
    val completedColor = ColorProvider(Color(0xFF4ECDC4))
    val checkColor = if (habit.isCompleted) completedColor else ColorProvider(Color(0xFF333355))
    val isMeasurable = habit.targetValue != null && habit.targetValue > 0

    // Parse habit color
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.color))
    } catch (e: Exception) {
        Color(0xFF4E7CFF)
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(cardColor)
            .cornerRadius(12.dp)
            .padding(10.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored indicator
        Box(
            modifier = GlanceModifier
                .width(4.dp)
                .height(36.dp)
                .background(ColorProvider(habitColor))
                .cornerRadius(2.dp)
        ) {}

        Spacer(modifier = GlanceModifier.width(10.dp))

        if (isMeasurable) {
            // +/- buttons for measurable habits
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Minus button
                Box(
                    modifier = GlanceModifier
                        .size(28.dp)
                        .background(ColorProvider(Color(0xFF333355)))
                        .cornerRadius(8.dp)
                        .clickable(actionRunCallback<IncrementHabitAction>(
                            actionParametersOf(
                                HabitWidget.habitIdKey to habit.id,
                                HabitWidget.dateKey to today,
                                HabitWidget.incrementKey to -getIncrementAmount(habit.targetValue!!)
                            )
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(6.dp))

                // Plus button
                Box(
                    modifier = GlanceModifier
                        .size(28.dp)
                        .background(if (habit.isCompleted) completedColor else ColorProvider(habitColor))
                        .cornerRadius(8.dp)
                        .clickable(actionRunCallback<IncrementHabitAction>(
                            actionParametersOf(
                                HabitWidget.habitIdKey to habit.id,
                                HabitWidget.dateKey to today,
                                HabitWidget.incrementKey to getIncrementAmount(habit.targetValue!!)
                            )
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else {
            // Checkbox for simple habits
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(checkColor)
                    .cornerRadius(8.dp)
                    .clickable(actionRunCallback<ToggleHabitAction>(
                        actionParametersOf(
                            HabitWidget.habitIdKey to habit.id,
                            HabitWidget.dateKey to today
                        )
                    )),
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
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        // Icon
        Text(
            text = getIconEmoji(habit.icon),
            style = TextStyle(fontSize = 18.sp)
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Name and progress
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = habit.name,
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            if (isMeasurable) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${habit.currentValue}/${habit.targetValue}",
                        style = TextStyle(
                            color = subtextColor,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    // Mini progress indicator
                    Box(
                        modifier = GlanceModifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(ColorProvider(Color(0xFF333355)))
                            .cornerRadius(2.dp)
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxHeight()
                                .width((habit.progress * 40).dp)
                                .background(if (habit.isCompleted) completedColor else ColorProvider(habitColor))
                                .cornerRadius(2.dp)
                        ) {}
                    }
                }
            }
        }

        // Streak
        if (habit.streak > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔥", style = TextStyle(fontSize = 12.sp))
                Text(
                    text = "${habit.streak}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFF9500)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// Calculate smart increment based on target value
private fun getIncrementAmount(targetValue: Int): Int {
    return when {
        targetValue >= 1000 -> 250  // For water (2000ml) -> +250ml
        targetValue >= 100 -> 10   // For larger values -> +10
        targetValue >= 10 -> 1    // For medium values -> +1
        else -> 1
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
        "meditation", "med", "mindfulness" -> "🧘"
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
// FORCE UPDATE HELPER
// ============================================
private suspend fun forceUpdateAllWidgets(context: Context) {
    try {
        // Small delay to ensure database transaction is committed
        kotlinx.coroutines.delay(50)

        // Method 1: Use Glance's update mechanism first
        val glanceManager = GlanceAppWidgetManager(context)
        val glanceIds = glanceManager.getGlanceIds(HabitWidget::class.java)
        android.util.Log.d("HabitWidget", "Updating ${glanceIds.size} Glance widgets")

        glanceIds.forEach { glanceId ->
            try {
                HabitWidget().update(context, glanceId)
            } catch (e: Exception) {
                android.util.Log.e("HabitWidget", "Error updating glance widget $glanceId", e)
            }
        }

        // Method 2: Send APPWIDGET_UPDATE broadcast as backup
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HabitWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        android.util.Log.d("HabitWidget", "Found ${appWidgetIds.size} native widget IDs")

        if (appWidgetIds.isNotEmpty()) {
            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            updateIntent.component = componentName
            context.sendBroadcast(updateIntent)
        }

        // Additional delay then update again
        kotlinx.coroutines.delay(100)
        glanceIds.forEach { glanceId ->
            HabitWidget().update(context, glanceId)
        }
    } catch (e: Exception) {
        android.util.Log.e("HabitWidget", "Error in forceUpdateAllWidgets", e)
    }
}

// ============================================
// TOGGLE ACTION
// ============================================
class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitWidget.habitIdKey] ?: run {
            android.util.Log.e("HabitWidget", "Missing habitId parameter")
            return
        }
        val date = parameters[HabitWidget.dateKey] ?: run {
            android.util.Log.e("HabitWidget", "Missing date parameter")
            return
        }

        android.util.Log.d("HabitWidget", "Toggle action started: habitId=$habitId, date=$date")

        try {
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

                    android.util.Log.d("HabitWidget", "Existing completion: $existing")

                    if (existing != null) {
                        val newStatus = if (existing.is_completed == 1L) 0L else 1L
                        android.util.Log.d("HabitWidget", "Updating status to: $newStatus")
                        database.habitQueries.upsertCompletion(
                            habit_id = habitId,
                            date = date,
                            is_completed = newStatus,
                            current_value = existing.current_value ?: 0L,
                            note = existing.note
                        )
                    } else {
                        android.util.Log.d("HabitWidget", "Creating new completion")
                        database.habitQueries.upsertCompletion(
                            habit_id = habitId,
                            date = date,
                            is_completed = 1L,
                            current_value = 0L,
                            note = null
                        )
                    }
                    android.util.Log.d("HabitWidget", "Database update complete")
                } finally {
                    driver?.close()
                }
            }

            // Force widget update using native AppWidgetManager
            android.util.Log.d("HabitWidget", "Force updating widgets...")
            forceUpdateAllWidgets(context)
            android.util.Log.d("HabitWidget", "Widgets force updated")

            // Notify app to refresh (if running)
            val intent = Intent(HabitWidget.ACTION_HABIT_TOGGLED)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
            android.util.Log.d("HabitWidget", "Broadcast sent")
        } catch (e: Exception) {
            android.util.Log.e("HabitWidget", "Error in toggle action", e)
        }
    }
}

// ============================================
// INCREMENT ACTION (for measurable habits)
// ============================================
class IncrementHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitWidget.habitIdKey] ?: return
        val date = parameters[HabitWidget.dateKey] ?: return
        val increment = parameters[HabitWidget.incrementKey] ?: return

        android.util.Log.d("HabitWidget", "Increment action: habitId=$habitId, increment=$increment")

        try {
            withContext(Dispatchers.IO) {
                var driver: app.cash.sqldelight.db.SqlDriver? = null
                try {
                    val driverFactory = DatabaseDriverFactory(context)
                    driver = driverFactory.createDriverWithSchema(
                        HabitsDatabase.Schema,
                        "shift_v4.db"
                    )
                    val database = HabitsDatabase(driver)

                    // Get habit to know target value
                    val habit = database.habitQueries.getById(habitId).executeAsOneOrNull()
                    val targetValue = habit?.target_value?.toInt() ?: 100

                    val existing = database.habitQueries
                        .getCompletion(habitId, date)
                        .executeAsOneOrNull()

                    val currentValue = existing?.current_value?.toInt() ?: 0
                    val newValue = (currentValue + increment).coerceIn(0, targetValue * 2) // Allow up to 2x target
                    val isCompleted = if (newValue >= targetValue) 1L else 0L

                    android.util.Log.d("HabitWidget", "Value: $currentValue -> $newValue, completed=$isCompleted")

                    database.habitQueries.upsertCompletion(
                        habit_id = habitId,
                        date = date,
                        is_completed = isCompleted,
                        current_value = newValue.toLong(),
                        note = existing?.note
                    )
                } finally {
                    driver?.close()
                }
            }

            // Force widget update
            forceUpdateAllWidgets(context)

            // Notify app
            val intent = Intent(HabitWidget.ACTION_HABIT_TOGGLED)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("HabitWidget", "Error in increment action", e)
        }
    }
}

// ============================================
// WIDGET RECEIVER
// ============================================
class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            com.cil.shift.core.common.widget.WidgetNotifier.ACTION_UPDATE_WIDGETS,
            HabitWidget.ACTION_HABIT_TOGGLED -> {
                android.util.Log.d("HabitWidget", "Received update broadcast")
                kotlinx.coroutines.GlobalScope.launch {
                    try {
                        val manager = GlanceAppWidgetManager(context)
                        val glanceIds = manager.getGlanceIds(HabitWidget::class.java)
                        glanceIds.forEach { id ->
                            HabitWidget().update(context, id)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HabitWidget", "Error updating widgets", e)
                    }
                }
            }
        }
    }
}

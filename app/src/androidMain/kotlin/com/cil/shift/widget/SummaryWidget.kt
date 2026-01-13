package com.cil.shift.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import kotlinx.datetime.*

/**
 * Weekly Summary Widget - Shows weekly progress at a glance
 */
class SummaryWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(280.dp, 140.dp),
            DpSize(320.dp, 160.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stats = loadStats(context)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFF1A1A2E)))
                    .cornerRadius(20.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(16.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shift",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF4E7CFF)),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Bu Hafta",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF888888)),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        // Current streak
                        if (stats.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = GlanceModifier
                                    .background(ColorProvider(Color(0xFFFF9500).copy(alpha = 0.2f)))
                                    .cornerRadius(10.dp)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "🔥", style = TextStyle(fontSize = 14.sp))
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                Text(
                                    text = "${stats.currentStreak} gün seri",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFFFF9500)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Weekly calendar with percentages
                    WeeklyCalendar(stats.weekData)

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // Week summary
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val weekAvgColor = when {
                            stats.weekAverage >= 80 -> Color(0xFF4ECDC4)
                            stats.weekAverage >= 50 -> Color(0xFF4E7CFF)
                            else -> Color(0xFFFF9500)
                        }
                        Text(
                            text = "Haftalık ortalama: %${stats.weekAverage}",
                            style = TextStyle(
                                color = ColorProvider(weekAvgColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadStats(context: Context): WidgetStats = withContext(Dispatchers.IO) {
        var driver: app.cash.sqldelight.db.SqlDriver? = null
        try {
            val driverFactory = DatabaseDriverFactory(context)
            driver = driverFactory.createDriverWithSchema(
                HabitsDatabase.Schema,
                "shift_v4.db"
            )
            val database = HabitsDatabase(driver)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // Get all active habits
            val habits = database.habitQueries.getAll().executeAsList()
                .filter { it.is_archived == 0L }

            val totalHabits = habits.size
            val activeHabitIds = habits.map { it.id }.toSet()

            // Calculate current streak (consecutive days with 100% completion)
            var currentStreak = 0
            var checkDate = today
            for (i in 0 until 365) {
                val dateStr = checkDate.toString()
                val completions = database.habitQueries.getCompletionsForDate(dateStr).executeAsList()
                // Only count completions for active habits
                val completed = completions.count { it.is_completed == 1L && it.habit_id in activeHabitIds }

                if (totalHabits > 0 && completed >= totalHabits) {
                    currentStreak++
                } else if (i > 0) {
                    break
                }
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            }

            // Weekly data (Monday to Sunday)
            val weekData = mutableListOf<DayData>()
            val startOfWeek = today.minus((today.dayOfWeek.ordinal).toLong(), DateTimeUnit.DAY)

            for (i in 0 until 7) {
                val date = startOfWeek.plus(i.toLong(), DateTimeUnit.DAY)
                val dateStr = date.toString()
                val dayCompletions = database.habitQueries.getCompletionsForDate(dateStr).executeAsList()
                // Only count completions for active habits
                val dayCompleted = dayCompletions.count { it.is_completed == 1L && it.habit_id in activeHabitIds }
                val dayPercent = if (totalHabits > 0) (dayCompleted * 100 / totalHabits) else 0
                val isToday = date == today
                val isFuture = date > today

                weekData.add(DayData(
                    dayName = getDayName(date.dayOfWeek),
                    percent = dayPercent,
                    completed = dayCompleted,
                    total = totalHabits,
                    isToday = isToday,
                    isFuture = isFuture
                ))
            }

            // Week average (excluding future days)
            val pastDays = weekData.filter { !it.isFuture }
            val weekAverage = if (pastDays.isNotEmpty()) {
                pastDays.sumOf { it.percent } / pastDays.size
            } else 0

            WidgetStats(
                currentStreak = currentStreak,
                weekData = weekData,
                weekAverage = weekAverage
            )
        } catch (e: Exception) {
            android.util.Log.e("SummaryWidget", "Error loading stats", e)
            WidgetStats()
        } finally {
            driver?.close()
        }
    }

    private fun getDayName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Pzt"
            DayOfWeek.TUESDAY -> "Sal"
            DayOfWeek.WEDNESDAY -> "Çar"
            DayOfWeek.THURSDAY -> "Per"
            DayOfWeek.FRIDAY -> "Cum"
            DayOfWeek.SATURDAY -> "Cmt"
            DayOfWeek.SUNDAY -> "Paz"
            else -> ""
        }
    }
}

data class WidgetStats(
    val currentStreak: Int = 0,
    val weekData: List<DayData> = emptyList(),
    val weekAverage: Int = 0
)

data class DayData(
    val dayName: String,
    val percent: Int,
    val completed: Int,
    val total: Int,
    val isToday: Boolean,
    val isFuture: Boolean
)

@Composable
private fun WeeklyCalendar(weekData: List<DayData>) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        weekData.forEach { day ->
            DayBox(day)
            if (day != weekData.last()) {
                Spacer(modifier = GlanceModifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun DayBox(day: DayData) {
    val backgroundColor = when {
        day.isFuture -> Color(0xFF252540)
        day.percent == 100 -> Color(0xFF4ECDC4)
        day.percent >= 50 -> Color(0xFF4E7CFF)
        day.percent > 0 -> Color(0xFFFF9500)
        else -> Color(0xFF333355)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = GlanceModifier.width(38.dp)
    ) {
        // Day name
        Text(
            text = day.dayName,
            style = TextStyle(
                color = ColorProvider(if (day.isToday) Color.White else Color(0xFF888888)),
                fontSize = 10.sp,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Day box with percentage
        Box(
            modifier = GlanceModifier
                .size(width = 38.dp, height = 44.dp)
                .background(ColorProvider(backgroundColor))
                .cornerRadius(8.dp)
                .then(
                    if (day.isToday) {
                        GlanceModifier.background(ColorProvider(backgroundColor))
                    } else {
                        GlanceModifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (day.isFuture) {
                    Text(
                        text = "—",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF555555)),
                            fontSize = 14.sp
                        )
                    )
                } else if (day.percent == 100) {
                    Text(
                        text = "✓",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    // Show percentage
                    Text(
                        text = "%${day.percent}",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    // Show completed/total
                    Text(
                        text = "${day.completed}/${day.total}",
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

// Widget Receiver
class SummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SummaryWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            com.cil.shift.core.common.widget.WidgetNotifier.ACTION_UPDATE_WIDGETS,
            HabitWidget.ACTION_HABIT_TOGGLED -> {
                GlobalScope.launch {
                    try {
                        val manager = GlanceAppWidgetManager(context)
                        manager.getGlanceIds(SummaryWidget::class.java).forEach { id ->
                            SummaryWidget().update(context, id)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SummaryWidget", "Error updating widget", e)
                    }
                }
            }
        }
    }
}

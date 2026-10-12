package com.example.ironquest

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.workoutHistoryDataStore by preferencesDataStore(
    name = "workout_history"
)

data class WorkoutRecord(
    val timestamp: Long,
    val xp: Int,
    val pullUps: Int,
    val dips: Int,
    val pushUps: Int
)

object WorkoutHistoryData {

    private val RECORDS_KEY =
        stringSetPreferencesKey("records")

    fun getHistory(context: Context) =
        context.workoutHistoryDataStore.data.map { preferences ->

            val records = preferences[RECORDS_KEY] ?: emptySet()

            records.mapNotNull { record ->

                val parts = record.split("|")

                if (parts.size != 5) {
                    null
                } else {
                    WorkoutRecord(
                        timestamp = parts[0].toLongOrNull()
                            ?: return@mapNotNull null,
                        xp = parts[1].toIntOrNull()
                            ?: return@mapNotNull null,
                        pullUps = parts[2].toIntOrNull()
                            ?: return@mapNotNull null,
                        dips = parts[3].toIntOrNull()
                            ?: return@mapNotNull null,
                        pushUps = parts[4].toIntOrNull()
                            ?: return@mapNotNull null
                    )
                }
            }.sortedByDescending {
                it.timestamp
            }
        }

    suspend fun clearHistory(context: Context) {
        context.workoutHistoryDataStore.edit { preferences -> preferences.clear() }
    }

    suspend fun addWorkout(
        context: Context,
        xp: Int,
        pullUps: Int,
        dips: Int,
        pushUps: Int
    ) {
        context.workoutHistoryDataStore.edit { preferences ->

            val records =
                preferences[RECORDS_KEY]?.toMutableSet()
                    ?: mutableSetOf()

            records.add(
                "${System.currentTimeMillis()}|$xp|$pullUps|$dips|$pushUps"
            )

            preferences[RECORDS_KEY] = records
        }
    }
}

@Composable
fun WorkoutHistoryScreen(
    language: AppLanguage = AppLanguage.RU,
    context: Context,
    onBack: () -> Unit
) {
    val s = strings(language)
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondaryText = Color(0xFF8194AD)

    val history by WorkoutHistoryData
        .getHistory(context)
        .collectAsState(initial = emptyList())
    var analyticsPeriod by remember { mutableStateOf(HistoryPeriod.THIRTY_DAYS) }
    var exerciseFilter by remember { mutableStateOf(HistoryExerciseFilter.ALL) }
    val periodStart = when (analyticsPeriod) {
        HistoryPeriod.SEVEN_DAYS -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        HistoryPeriod.THIRTY_DAYS -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        else -> 0L
    }
    val analyticsHistory = history.filter { it.timestamp >= periodStart }
    val visibleHistory = analyticsHistory
        .filter { exerciseFilter == HistoryExerciseFilter.ALL || exerciseFilter.reps(it) > 0 }
        .let { records ->
            if (exerciseFilter == HistoryExerciseFilter.ALL) records else records.sortedByDescending { exerciseFilter.reps(it) }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF223148)
                )
            ) {
                Text(
                    text = "←",
                    color = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.icon_history), null, Modifier.height(30.dp))
                Text(s.history, color = text, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(1.dp))
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (history.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        panel,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = s.historyEmptyTitle,
                    color = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = s.historyEmptyText,
                    color = secondaryText,
                    fontSize = 14.sp
                )
            }

        } else {

            Text(
                text = s.progress,
                color = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryPeriod.values().forEach { period ->
                    val selected = analyticsPeriod == period
                    Text(
                        text = period.label(s),
                        color = if (selected) Color(0xFF080F1B) else secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(if (selected) accent else panel, RoundedCornerShape(8.dp))
                            .clickable { analyticsPeriod = period }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }

            Text(
                text = s.exerciseFilter,
                color = secondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryExerciseFilter.values().forEach { filter ->
                    val selected = exerciseFilter == filter
                    Text(
                        text = filter.label(s),
                        color = if (selected) Color(0xFF080F1B) else secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(if (selected) accent else panel, RoundedCornerShape(8.dp))
                            .clickable { exerciseFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }

            HistoryAnalytics(history = visibleHistory, filter = exerciseFilter, strings = s, panel = panel, accent = accent, text = text, secondaryText = secondaryText)

            Spacer(Modifier.height(12.dp))

            ProgressGraph(
                history = visibleHistory,
                filter = exerciseFilter,
                appStrings = s,
                panel = panel,
                accent = accent,
                secondaryText = secondaryText
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text(
                text = s.workouts,
                color = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            visibleHistory.forEachIndexed { index, workout ->

                WorkoutHistoryCard(
                    language = language,
                    workout = workout,
                    panel = panel,
                    accent = accent,
                    text = text,
                    secondaryText = secondaryText
                )

                if (index < visibleHistory.lastIndex) {
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }
        }
    }
}

enum class HistoryPeriod {
    SEVEN_DAYS,
    THIRTY_DAYS,
    ALL_TIME;

    fun label(strings: AppStrings): String = when (this) {
        SEVEN_DAYS -> strings.sevenDays
        THIRTY_DAYS -> strings.thirtyDays
        ALL_TIME -> strings.allTime
    }
}

enum class HistoryExerciseFilter {
    ALL,
    PULL_UPS,
    DIPS,
    PUSH_UPS;

    fun label(strings: AppStrings): String = when (this) {
        ALL -> strings.allExercises
        PULL_UPS -> strings.pullUps
        DIPS -> strings.dips
        PUSH_UPS -> strings.pushUps
    }

    fun reps(record: WorkoutRecord): Int = when (this) {
        ALL -> record.pullUps + record.dips + record.pushUps
        PULL_UPS -> record.pullUps
        DIPS -> record.dips
        PUSH_UPS -> record.pushUps
    }
}

@Composable
private fun HistoryAnalytics(
    history: List<WorkoutRecord>,
    filter: HistoryExerciseFilter,
    strings: AppStrings,
    panel: Color,
    accent: Color,
    text: Color,
    secondaryText: Color
) {
    val totalReps = history.sumOf { filter.reps(it) }
    val totalXp = history.sumOf { it.xp }
    val average = if (history.isEmpty()) 0 else totalReps / history.size
    val best = history.maxOfOrNull { filter.reps(it) } ?: 0

    Column(
        modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)
    ) {
        Text(strings.progressSummary, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsValue(strings.workoutCount, history.size.toString(), text, secondaryText, Modifier.weight(1f))
            AnalyticsValue(strings.repetitions, totalReps.toString(), text, secondaryText, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsValue(strings.average, "$average", text, secondaryText, Modifier.weight(1f))
            AnalyticsValue(strings.record, "$best", text, secondaryText, Modifier.weight(1f))
            AnalyticsValue(strings.experience, "+$totalXp XP", text, secondaryText, Modifier.weight(1f))
        }
        if (history.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(strings.noWorkoutsPeriod, color = secondaryText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AnalyticsValue(
    label: String,
    value: String,
    text: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = secondaryText, fontSize = 11.sp)
        Text(value, color = text, fontSize = 19.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ProgressGraph(
    history: List<WorkoutRecord>,
    filter: HistoryExerciseFilter = HistoryExerciseFilter.ALL,
    appStrings: AppStrings = strings(AppLanguage.RU),
    panel: Color,
    accent: Color,
    secondaryText: Color
) {
    val graphHistory = history.take(7).reversed()
    val values = graphHistory.map { filter.reps(it) }
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val dateFormat = remember { SimpleDateFormat("dd.MM", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(15.dp)
    ) {
        Text(appStrings.repsByWorkout, color = secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(appStrings.lastWorkoutsMax(values.size, maxValue), color = secondaryText, fontSize = 11.sp)
        Spacer(Modifier.height(14.dp))

        if (graphHistory.isEmpty()) {
            Text(appStrings.graphEmpty, color = secondaryText, fontSize = 13.sp, modifier = Modifier.padding(vertical = 28.dp))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                graphHistory.forEach { workout ->
                    val value = filter.reps(workout)
                    Column(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(value.toString(), color = secondaryText, fontSize = 10.sp, maxLines = 1)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight((value.toFloat() / maxValue).coerceAtLeast(0.06f))
                                .background(accent, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                graphHistory.forEach { workout ->
                    Text(
                        dateFormat.format(Date(workout.timestamp)),
                        color = secondaryText,
                        fontSize = 9.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryCard(
    language: AppLanguage = AppLanguage.RU,
    workout: WorkoutRecord,
    panel: Color,
    accent: Color,
    text: Color,
    secondaryText: Color
) {
    val s = strings(language)
    val date = SimpleDateFormat(
        "dd.MM.yyyy • HH:mm",
        Locale.getDefault()
    ).format(
        Date(workout.timestamp)
    )

    val totalReps =
        workout.pullUps +
                workout.dips +
                workout.pushUps

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                panel,
                RoundedCornerShape(6.dp)
            )
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = date,
                color = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "+${workout.xp} XP",
                color = accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text = s.totalRepsLine(totalReps),
            color = secondaryText,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_pullups), null, Modifier.height(24.dp))
            Text(s.pullUpsLine(workout.pullUps), color = text, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_dips), null, Modifier.height(24.dp))
            Text(s.dipsLine(workout.dips), color = text, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_pushups), null, Modifier.height(24.dp))
            Text(s.pushUpsLine(workout.pushUps), color = text, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

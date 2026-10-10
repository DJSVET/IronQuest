package com.example.ironquest

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
    context: Context,
    onBack: () -> Unit
) {
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondaryText = Color(0xFF8194AD)

    val history by WorkoutHistoryData
        .getHistory(context)
        .collectAsState(initial = emptyList())

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
                Text("ИСТОРИЯ", color = text, fontSize = 22.sp, fontWeight = FontWeight.Black)
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
                    text = "Тренировок пока нет",
                    color = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Проведи первую тренировку,\nи она появится здесь.",
                    color = secondaryText,
                    fontSize = 14.sp
                )
            }

        } else {

            Text(
                text = "ПРОГРЕСС",
                color = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            HistoryAnalytics(history = history, panel = panel, accent = accent, text = text, secondaryText = secondaryText)

            Spacer(Modifier.height(12.dp))

            ProgressGraph(
                history = history,
                panel = panel,
                accent = accent,
                secondaryText = secondaryText
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text(
                text = "ТРЕНИРОВКИ",
                color = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            history.forEachIndexed { index, workout ->

                WorkoutHistoryCard(
                    workout = workout,
                    panel = panel,
                    accent = accent,
                    text = text,
                    secondaryText = secondaryText
                )

                if (index < history.lastIndex) {
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryAnalytics(
    history: List<WorkoutRecord>,
    panel: Color,
    accent: Color,
    text: Color,
    secondaryText: Color
) {
    val total = history.sumOf { it.pullUps + it.dips + it.pushUps }
    val average = if (history.isEmpty()) 0 else total / history.size
    val best = history.maxOfOrNull { it.pullUps + it.dips + it.pushUps } ?: 0
    val latest = history.firstOrNull()?.let { it.pullUps + it.dips + it.pushUps } ?: 0
    val previous = history.getOrNull(1)?.let { it.pullUps + it.dips + it.pushUps }
    val change = previous?.let { latest - it }

    Column(
        modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(6.dp)).padding(14.dp)
    ) {
        Text("ОБЩАЯ АНАЛИТИКА", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AnalyticsValue("Тренировок", history.size.toString(), text, secondaryText)
            AnalyticsValue("Повторений", total.toString(), text, secondaryText)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AnalyticsValue("В среднем", "$average", text, secondaryText)
            AnalyticsValue("Рекорд", "$best", text, secondaryText)
        }
        if (change != null) {
            Spacer(Modifier.height(10.dp))
            val changeText = if (change > 0) "+$change повторений" else "$change повторений"
            Text("Последняя тренировка: $changeText к предыдущей", color = if (change >= 0) accent else secondaryText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AnalyticsValue(label: String, value: String, text: Color, secondaryText: Color) {
    Column {
        Text(label, color = secondaryText, fontSize = 11.sp)
        Text(value, color = text, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ProgressGraph(
    history: List<WorkoutRecord>,
    panel: Color,
    accent: Color,
    secondaryText: Color
) {
    val graphHistory = history
        .take(10)
        .reversed()

    val values = graphHistory.map {
        it.pullUps + it.dips + it.pushUps
    }

    val maxValue = (values.maxOrNull() ?: 1)
        .coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                panel,
                RoundedCornerShape(6.dp)
            )
            .padding(15.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Повторения",
                color = secondaryText,
                fontSize = 13.sp
            )

            Text(
                text = "Последние ${values.size}",
                color = secondaryText,
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {

            val graphWidth = size.width
            val graphHeight = size.height

            if (values.isNotEmpty()) {

                val stepX =
                    if (values.size == 1) {
                        0f
                    } else {
                        graphWidth / (values.size - 1)
                    }

                val points = values.mapIndexed { index, value ->

                    val x = index * stepX

                    val normalized =
                        value.toFloat() / maxValue.toFloat()

                    val y =
                        graphHeight - (normalized * graphHeight)

                    androidx.compose.ui.geometry.Offset(
                        x,
                        y
                    )
                }

                val linePath = Path()

                points.forEachIndexed { index, point ->

                    if (index == 0) {
                        linePath.moveTo(
                            point.x,
                            point.y
                        )
                    } else {
                        linePath.lineTo(
                            point.x,
                            point.y
                        )
                    }
                }

                drawPath(
                    path = linePath,
                    color = accent,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 6f
                    )
                )

                points.forEach { point ->

                    drawCircle(
                        color = accent,
                        radius = 8f,
                        center = point
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = graphHistory.firstOrNull()?.let { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it.timestamp)) } ?: "—",
                color = secondaryText,
                fontSize = 11.sp
            )
            Text("Максимум: $maxValue повторений", color = secondaryText, fontSize = 11.sp)
            Text(
                text = graphHistory.lastOrNull()?.let { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it.timestamp)) } ?: "—",
                color = secondaryText,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun WorkoutHistoryCard(
    workout: WorkoutRecord,
    panel: Color,
    accent: Color,
    text: Color,
    secondaryText: Color
) {
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
            text = "Всего повторений: $totalReps",
            color = secondaryText,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_pullups), null, Modifier.height(24.dp))
            Text("Подтягивания: ${workout.pullUps}", color = text, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_dips), null, Modifier.height(24.dp))
            Text("Брусья: ${workout.dips}", color = text, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_pushups), null, Modifier.height(24.dp))
            Text("Отжимания: ${workout.pushUps}", color = text, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
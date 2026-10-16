package com.example.ironquest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryPeriod(val days: Int?) { SEVEN_DAYS(7), THIRTY_DAYS(30), ALL_TIME(null) }

/** Летопись тренировок: аналитика, график и список боёв. */
@Composable
fun WorkoutHistoryScreen(history: List<WorkoutRecord>, custom: List<Exercise>, onBack: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var period by rememberSaveable { mutableStateOf(HistoryPeriod.THIRTY_DAYS) }
    var exerciseId by rememberSaveable { mutableStateOf<String?>(null) }
    val now = System.currentTimeMillis()
    val inPeriod = history.filter { r -> period.days?.let { r.timestamp >= now - it * 86_400_000L } ?: true }
    val usedExercises = remember(history) { history.flatMap { it.entries.keys }.distinct() }
    val filtered = if (exerciseId == null) inPeriod else inPeriod.filter { it.entries.containsKey(exerciseId) }
    fun value(r: WorkoutRecord): Int = exerciseId?.let { r.total(it) } ?: r.repEquivalent(custom)
    val selectedEx = exerciseId?.let { exerciseById(it, custom) }

    Column(Modifier.fillMaxSize()) {
        PixelTopBar(tr("ЛЕТОПИСЬ", "CHRONICLE"), "scroll", onBack = onBack)
        if (history.isEmpty()) {
            EmptyState("scroll", tr("ЛЕТОПИСЬ ПУСТА", "NO RECORDS"), tr("Проведи первую тренировку, и она появится здесь.", "Complete your first workout and it will appear here."),
                Modifier.padding(16.dp))
            return@Column
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PixelChip(tr("7 дней", "7 days"), period == HistoryPeriod.SEVEN_DAYS, { period = HistoryPeriod.SEVEN_DAYS }, Modifier.weight(1f))
                    PixelChip(tr("30 дней", "30 days"), period == HistoryPeriod.THIRTY_DAYS, { period = HistoryPeriod.THIRTY_DAYS }, Modifier.weight(1f))
                    PixelChip(tr("Всё", "All"), period == HistoryPeriod.ALL_TIME, { period = HistoryPeriod.ALL_TIME }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PixelChip(tr("Все", "All"), exerciseId == null, { exerciseId = null }, color = pal.accent2)
                    usedExercises.forEach { id ->
                        val ex = exerciseById(id, custom)
                        PixelChip(ex?.name(lang) ?: id, exerciseId == id, { exerciseId = id }, icon = "ex:${ex?.icon ?: "dumbbell"}", color = pal.accent2)
                    }
                }
            }
            item {
                val total = filtered.sumOf { value(it) }
                val avg = if (filtered.isEmpty()) 0 else total / filtered.size
                val best = filtered.maxOfOrNull { value(it) } ?: 0
                val xp = filtered.sumOf { it.xp }
                val minutes = filtered.sumOf { it.durationSec } / 60
                PixelPanel(Modifier.fillMaxWidth()) {
                    TitleText(tr("СВОДКА", "SUMMARY"), size = 9.sp, color = pal.accent)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Metric("sword", tr("Битвы", "Battles"), "${filtered.size}", Modifier.weight(1f))
                        Metric("muscle", if (selectedEx != null) selectedEx.unit.short.text() else tr("Объём", "Volume"), "$total", Modifier.weight(1f))
                        Metric("star", "XP", "+$xp", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Metric("hourglass", tr("Среднее", "Average"), "$avg", Modifier.weight(1f))
                        Metric("crown", tr("Рекорд", "Record"), "$best", Modifier.weight(1f))
                        Metric("stopwatch", tr("Минут", "Minutes"), "$minutes", Modifier.weight(1f))
                    }
                }
            }
            item {
                val graph = filtered.take(10).reversed()
                PixelPanel(Modifier.fillMaxWidth()) {
                    TitleText(tr("ГРАФИК", "CHART"), size = 9.sp, color = pal.accent)
                    Spacer(Modifier.height(4.dp))
                    PText(tr("Последние ${graph.size} тренировок", "Last ${graph.size} workouts"), color = pal.muted, size = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    if (graph.isEmpty()) PText(tr("За этот период тренировок нет.", "No workouts in this period."), color = pal.muted)
                    else PixelBarChart(graph.map { value(it) }, graph.map { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it.timestamp)) })
                }
            }
            item { SectionHeader(tr("БИТВЫ", "BATTLES"), "sword") }
            items(filtered, key = { it.timestamp }) { r -> HistoryCard(r, custom, Modifier.animateItem()) }
        }
    }
}

@Composable
private fun Metric(icon: String, label: String, value: String, modifier: Modifier) {
    val pal = LocalPalette.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        PixelIcon(icon, 22.dp)
        Spacer(Modifier.width(6.dp))
        Column {
            PText(label, color = pal.muted, size = 12.sp, maxLines = 1)
            PText(value, bold = true, size = 17.sp, maxLines = 1)
        }
    }
}

@Composable
private fun PixelBarChart(values: List<Int>, labels: List<String>) {
    val pal = LocalPalette.current
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, v ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                PText("$v", size = 11.sp, color = pal.muted, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                val frac = v / max.toFloat()
                val isLast = i == values.lastIndex
                Canvas(Modifier.fillMaxWidth().height((110 * frac).coerceAtLeast(6f).dp)) {
                    val p = 2.dp.toPx()
                    val c = if (isLast) pal.gold else pal.accent
                    stepRect(pal.border, 0f, 0f, size.width, size.height, p)
                    drawRect(c, Offset(p, p), Size(size.width - 2 * p, size.height - 2 * p))
                    drawRect(lerp(c, Color.White, 0.4f), Offset(p, p), Size(size.width - 2 * p, p))
                    drawRect(lerp(c, Color.Black, 0.3f), Offset(size.width - 2 * p, p), Size(p, size.height - 2 * p))
                }
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEach { l -> PText(l, size = 10.sp, color = pal.muted, maxLines = 1, align = TextAlign.Center, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun HistoryCard(r: WorkoutRecord, custom: List<Exercise>, modifier: Modifier = Modifier) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val date = SimpleDateFormat("dd.MM.yyyy · HH:mm", Locale.getDefault()).format(Date(r.timestamp))
    PixelPanel(modifier.fillMaxWidth(), padding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                PText(date, bold = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PText(weekdayFull(dayOfWeek(dayKey(r.timestamp)), lang), color = pal.muted, size = 13.sp)
                    if (r.durationSec > 0) PText(" · ${r.durationSec / 60} " + tr("мин", "min"), color = pal.muted, size = 13.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                TitleText("+${r.xp} XP", size = 9.sp, color = pal.accent)
                if (r.coins > 0) Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelIcon("coin", 12.dp); Spacer(Modifier.width(3.dp)); PText("+${r.coins}", size = 13.sp, color = pal.gold, bold = true)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        r.entries.forEach { (id, sets) ->
            val ex = exerciseById(id, custom)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Box(Modifier.width(30.dp)) { PixelIcon("ex:${ex?.icon ?: "dumbbell"}", 24.dp) }
                Spacer(Modifier.width(6.dp))
                PText(ex?.name(lang) ?: id, size = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                PText(sets.joinToString(" · ") { formatAmount(it, ex?.unit ?: ExerciseUnit.REPS, lang) }, color = pal.muted, size = 14.sp, maxLines = 1)
                Spacer(Modifier.width(6.dp))
                PText("= " + formatAmount(sets.sum(), ex?.unit ?: ExerciseUnit.REPS, lang), color = pal.accent, size = 14.sp, bold = true)
            }
        }
    }
}

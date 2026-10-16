package com.example.ironquest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private fun defaultAmount(unit: ExerciseUnit) = when (unit) {
    ExerciseUnit.REPS -> 10
    ExerciseUnit.SECONDS -> 30
    ExerciseUnit.MINUTES -> 10
}

/** Экран тренировки: подходы записываются степпером в один тап, есть таймеры отдыха и секундомер. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutScreen(
    state: PlayerState,
    history: List<WorkoutRecord>,
    perks: Perks,
    isSaving: Boolean,
    onFinish: (Map<String, List<Int>>, Int) -> Unit,
    onBack: () -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val context = LocalContext.current
    val haptic = rememberHaptic()
    val custom = state.customExercises
    val startedAt = remember { System.currentTimeMillis() }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }

    val order = remember {
        mutableStateListOf<String>().apply {
            addAll(allExercises(custom).filter { it.id in state.selectedExercises && !it.archived }.map { it.id })
            if (isEmpty()) addAll(DEFAULT_SELECTED_EXERCISES)
        }
    }
    val sets = remember { mutableStateMapOf<String, SnapshotStateList<Int>>() }
    val inputs = remember { mutableStateMapOf<String, Int>() }
    fun setsOf(id: String): List<Int> = sets[id] ?: emptyList()
    fun mutableSets(id: String): SnapshotStateList<Int> = sets.getOrPut(id) { mutableStateListOf() }
    fun lastSets(id: String): List<Int>? = history.firstOrNull { it.entries.containsKey(id) }?.entries?.get(id)

    var restSeconds by remember { mutableIntStateOf(AppPreferences.restTimerSeconds(context)) }
    var restUntil by remember { mutableLongStateOf(0L) }
    var showExit by remember { mutableStateOf(false) }
    var showFinish by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var editSet by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var typeFor by remember { mutableStateOf<String?>(null) }

    val totalXp = order.sumOf { id ->
        val ex = exerciseById(id, custom) ?: return@sumOf 0.0
        ex.xpFor(setsOf(id).sum()).toDouble()
    }.let { base ->
        var x = (base * (1 + perks.xp / 100.0)).roundToInt()
        if (state.count(Consumable.XP_POTION) > 0) x *= 2
        x
    }
    val totalSets = order.sumOf { setsOf(it).size }

    BackHandler(enabled = !isSaving) { if (totalSets == 0) onBack() else showExit = true }

    // Вибрация в конце отдыха
    LaunchedEffect(restUntil) {
        if (restUntil > 0) {
            val wait = restUntil - System.currentTimeMillis()
            if (wait > 0) delay(wait)
            if (restUntil > 0) {
                haptic(HapticFeedbackType.LongPress)
                restUntil = 0
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            PixelTopBar(tr("ТРЕНИРОВКА", "WORKOUT"), "sword", onBack = { if (totalSets == 0) onBack() else showExit = true }) {
                val elapsed = ((now - startedAt) / 1000).toInt()
                Row(Modifier.insetFrame().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    PixelIcon("stopwatch", 18.dp)
                    Spacer(Modifier.width(6.dp))
                    TitleText("%02d:%02d".format(elapsed / 60, elapsed % 60), size = 10.sp, shadow = false)
                }
            }
            // сводка
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).panelFrame().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val xpShown by animateIntAsState(totalXp, tween(300), label = "xp")
                PixelIcon("star", 22.dp)
                Spacer(Modifier.width(6.dp))
                TitleText("+$xpShown XP", size = 11.sp, color = pal.accent)
                Spacer(Modifier.weight(1f))
                PText(tr("подходов: $totalSets", "sets: $totalSets"), color = pal.muted, size = 14.sp)
                if (state.count(Consumable.XP_POTION) > 0) { Spacer(Modifier.width(6.dp)); Tag("×2", pal.accent2) }
                if (state.count(Consumable.LUCK_CLOVER) > 0) { Spacer(Modifier.width(4.dp)); PixelIcon("clover", 18.dp) }
            }
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(order, key = { it }) { id ->
                    val ex = exerciseById(id, custom) ?: return@items
                    ExerciseCard(
                        exercise = ex,
                        sets = setsOf(id),
                        value = inputs[id] ?: lastSets(id)?.firstOrNull() ?: defaultAmount(ex.unit),
                        last = lastSets(id),
                        onValue = { inputs[id] = it.coerceIn(0, 9999) },
                        onAddSet = { v ->
                            if (v > 0) {
                                mutableSets(id).add(v)
                                haptic(HapticFeedbackType.LongPress)
                                if (restSeconds > 0) restUntil = System.currentTimeMillis() + restSeconds * 1000L
                            }
                        },
                        onEditSet = { index -> editSet = id to index },
                        onType = { typeFor = id },
                        onRemove = { order.remove(id); sets.remove(id) },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "add") {
                    PixelButton(tr("ДОБАВИТЬ УПРАЖНЕНИЕ", "ADD EXERCISE"), { showPicker = true }, Modifier.fillMaxWidth(),
                        color = pal.panelHi, textColor = pal.text, icon = "plus", fontSize = 9.sp)
                }
                item(key = "rest") {
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        PixelIcon("hourglass", 18.dp)
                        Spacer(Modifier.width(6.dp))
                        PText(tr("Отдых после подхода:", "Rest after a set:"), color = pal.muted, size = 14.sp, modifier = Modifier.weight(1f))
                        listOf(0, 30, 60, 90, 120).forEach { s ->
                            Spacer(Modifier.width(4.dp))
                            Box(
                                Modifier
                                    .heightIn(min = 36.dp)
                                    .clickable { restSeconds = s; AppPreferences.setRestTimerSeconds(context, s) }
                                    .then(if (restSeconds == s) Modifier.panelFrame(pal.accent2) else Modifier.insetFrame())
                                    .padding(horizontal = 7.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) { PText(if (s == 0) tr("выкл", "off") else "$s", size = 13.sp, bold = true, color = if (restSeconds == s) pal.bg else pal.text) }
                        }
                    }
                }
            }
        }

        // нижняя панель: таймер отдыха + кнопка завершения
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .drawBehind { drawRect(pal.bg.copy(alpha = 0.92f)) }
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            AnimatedVisibility(restUntil > now, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                val left = ((restUntil - now) / 1000).toInt().coerceAtLeast(0)
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).panelFrame(pal.panelHi).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PixelIcon("hourglass", 22.dp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        PText(tr("ОТДЫХ", "REST"), size = 13.sp, bold = true, color = pal.accent2)
                        PixelBar(if (restSeconds > 0) left / restSeconds.toFloat() else 0f, Modifier.fillMaxWidth(), color = pal.accent2, height = 10.dp, segments = 0)
                    }
                    Spacer(Modifier.width(10.dp))
                    TitleText("%d:%02d".format(left / 60, left % 60), size = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    PText("+15", bold = true, modifier = Modifier.clickable { restUntil += 15_000 }.padding(6.dp))
                    PixelIconButton("close", { restUntil = 0 }, size = 38.dp, iconSize = 16.dp, contentDescription = tr("Пропустить отдых", "Skip rest"))
                }
            }
            PixelButton(
                text = if (isSaving) tr("СОХРАНЕНИЕ...", "SAVING...") else tr("ЗАВЕРШИТЬ · +$totalXp XP", "FINISH · +$totalXp XP"),
                onClick = { showFinish = true },
                enabled = totalSets > 0 && !isSaving,
                modifier = Modifier.fillMaxWidth(),
                icon = "trophy",
                height = 58.dp,
            )
        }
    }

    if (showExit) {
        ConfirmDialog(
            tr("ПОКИНУТЬ БОЙ?", "LEAVE BATTLE?"),
            tr("Записанные подходы не сохранятся.", "Logged sets will not be saved."),
            tr("ВЫЙТИ", "LEAVE"), onConfirm = { showExit = false; onBack() }, onDismiss = { showExit = false },
            icon = "skull", danger = true, cancel = tr("ОСТАТЬСЯ", "STAY"),
        )
    }
    if (showFinish) {
        PixelDialog({ showFinish = false }, tr("ЗАВЕРШИТЬ?", "FINISH?"), "trophy") {
            order.filter { setsOf(it).isNotEmpty() }.forEach { id ->
                val ex = exerciseById(id, custom) ?: return@forEach
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    PixelIcon("ex:${ex.icon}", 24.dp)
                    Spacer(Modifier.width(8.dp))
                    PText(ex.name(lang), modifier = Modifier.weight(1f), maxLines = 1)
                    PText(setsOf(id).joinToString(" · ") { formatAmount(it, ex.unit, lang) }, color = pal.muted, maxLines = 1)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon("star", 22.dp)
                Spacer(Modifier.width(6.dp))
                TitleText("+$totalXp XP", color = pal.accent, size = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelButton(tr("ЕЩЁ ПОДХОД", "ONE MORE"), { showFinish = false }, Modifier.weight(1f), color = pal.panelHi, textColor = pal.text, fontSize = 8.sp, height = 48.dp)
                PixelButton(tr("ГОТОВО", "DONE"), {
                    showFinish = false
                    onFinish(order.associateWith { setsOf(it).toList() }.filterValues { it.isNotEmpty() }, ((System.currentTimeMillis() - startedAt) / 1000).toInt())
                }, Modifier.weight(1f), fontSize = 9.sp, height = 48.dp)
            }
        }
    }
    if (showPicker) {
        PixelDialog({ showPicker = false }, tr("ВЫБЕРИ УПРАЖНЕНИЕ", "PICK EXERCISE"), "book") {
            val available = allExercises(custom).filter { !it.archived && it.id !in order }
            if (available.isEmpty()) PText(tr("Все упражнения уже в тренировке.", "All exercises are already added."), color = pal.muted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                available.forEach { ex ->
                    Column(
                        Modifier
                            .widthIn(min = 92.dp)
                            .clickable { order.add(ex.id); showPicker = false }
                            .panelFrame()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PixelIcon("ex:${ex.icon}", 36.dp)
                        Spacer(Modifier.height(4.dp))
                        PText(ex.name(lang), size = 13.sp, align = TextAlign.Center, maxLines = 2, modifier = Modifier.widthIn(max = 96.dp))
                    }
                }
            }
        }
    }
    editSet?.let { (id, index) ->
        val list = sets[id]
        val ex = exerciseById(id, custom)
        if (ex == null || list == null || index !in list.indices) {
            LaunchedEffect(Unit) { editSet = null }
        } else {
            var v by remember(id, index) { mutableIntStateOf(list[index]) }
            PixelDialog({ editSet = null }, tr("ПОДХОД ${index + 1}", "SET ${index + 1}"), "ex:${ex.icon}") {
                AmountStepper(v, ex.unit, { v = it.coerceIn(0, 9999) }, onType = null)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PixelButton(tr("УДАЛИТЬ", "DELETE"), { list.removeAt(index); editSet = null }, Modifier.weight(1f), color = pal.danger, textColor = Color.White, fontSize = 8.sp, height = 48.dp, icon = "trash")
                    PixelButton(tr("ОК", "OK"), { if (v > 0) list[index] = v else list.removeAt(index); editSet = null }, Modifier.weight(1f), fontSize = 9.sp, height = 48.dp)
                }
            }
        }
    }
    typeFor?.let { id ->
        val ex = exerciseById(id, custom)
        var text by remember(id) { mutableStateOf("") }
        PixelDialog({ typeFor = null }, ex?.name(lang)?.uppercase() ?: "", "pencil") {
            PText(when (ex?.unit) { ExerciseUnit.SECONDS -> tr("Секунды", "Seconds"); ExerciseUnit.MINUTES -> tr("Минуты", "Minutes"); else -> tr("Повторения", "Reps") }, color = pal.muted)
            Spacer(Modifier.height(6.dp))
            PixelTextField(text, { t -> if (t.all { it.isDigit() } && t.length <= 4) text = t }, Modifier.fillMaxWidth(), placeholder = "0", numeric = true, big = true)
            Spacer(Modifier.height(14.dp))
            PixelButton(tr("ГОТОВО", "DONE"), { text.toIntOrNull()?.let { inputs[id] = it }; typeFor = null }, Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ExerciseCard(
    exercise: Exercise,
    sets: List<Int>,
    value: Int,
    last: List<Int>?,
    onValue: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onEditSet: (Int) -> Unit,
    onType: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var stopwatchStart by remember { mutableLongStateOf(0L) }
    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(stopwatchStart) {
        while (stopwatchStart > 0) { tick = System.currentTimeMillis(); delay(250) }
    }
    val total = sets.sum()
    Column(modifier.fillMaxWidth().panelFrame().padding(12.dp).animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).insetFrame(), contentAlignment = Alignment.Center) { PixelIcon("ex:${exercise.icon}", 40.dp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                TitleText(exercise.name(lang).uppercase(), size = 9.sp, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    exercise.stats.entries.sortedByDescending { it.value }.forEach { (s, w) ->
                        PixelIcon(s.icon, 14.dp)
                        PText("×$w ", size = 12.sp, color = s.color, bold = true)
                    }
                }
                if (last != null) PText(tr("Прошлый раз: ", "Last time: ") + last.joinToString(" · ") { formatAmount(it, exercise.unit, lang) },
                    color = pal.muted, size = 13.sp, maxLines = 1)
            }
            if (sets.isEmpty()) PixelIconButton("close", onRemove, size = 36.dp, iconSize = 14.dp, contentDescription = tr("Убрать", "Remove"))
        }
        if (sets.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sets.forEachIndexed { i, v ->
                    var appear by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { appear = true }
                    AnimatedVisibility(appear, enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) {
                        Row(
                            Modifier
                                .heightIn(min = 36.dp)
                                .combinedClickable(onClick = { onEditSet(i) }, onLongClick = { onEditSet(i) })
                                .pixelFrame(pal.accent, lerp(pal.accent, Color.White, 0.35f), lerp(pal.accent, Color.Black, 0.35f), pal.border, 2.dp)
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PText("${i + 1}:", size = 12.sp, color = pal.onAccent.copy(alpha = 0.7f))
                            Spacer(Modifier.width(3.dp))
                            PText(formatAmount(v, exercise.unit, lang), size = 15.sp, bold = true, color = pal.onAccent)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        val running = stopwatchStart > 0
        val shown = if (running) ((tick - stopwatchStart) / 1000).toInt().coerceAtLeast(0) else value
        AmountStepper(shown, exercise.unit, onValue, onType)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (exercise.unit == ExerciseUnit.SECONDS) {
                PixelButton(
                    if (running) tr("СТОП", "STOP") else tr("ТАЙМЕР", "TIMER"),
                    {
                        if (running) {
                            val secs = ((System.currentTimeMillis() - stopwatchStart) / 1000).toInt()
                            stopwatchStart = 0
                            onValue(secs)
                        } else stopwatchStart = System.currentTimeMillis()
                    },
                    Modifier.weight(1f),
                    color = if (running) pal.danger else pal.panelHi,
                    textColor = if (running) Color.White else pal.text,
                    icon = "stopwatch", fontSize = 8.sp, height = 48.dp,
                )
            }
            PixelButton(
                tr("ЗАПИСАТЬ", "LOG SET"),
                { if (!running) onAddSet(value) },
                Modifier.weight(1.4f),
                enabled = !running && value > 0,
                icon = "check", fontSize = 8.sp, height = 48.dp,
            )
        }
        if (total > 0) {
            Spacer(Modifier.height(6.dp))
            Row {
                PText(tr("Итого: ", "Total: "), color = pal.muted, size = 14.sp)
                PText(formatAmount(total, exercise.unit, lang) + if (exercise.unit == ExerciseUnit.REPS) " " + exercise.unit.short.text() else "",
                    color = pal.accent, size = 14.sp, bold = true)
            }
        }
    }
}

/** Степпер: [−] значение [+], с быстрыми прибавками. */
@Composable
private fun AmountStepper(value: Int, unit: ExerciseUnit, onValue: (Int) -> Unit, onType: (() -> Unit)?) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val step = unit.step
    Row(verticalAlignment = Alignment.CenterVertically) {
        PixelIconButton("minus", { onValue((value - step).coerceAtLeast(0)) }, size = 52.dp, iconSize = 22.dp, contentDescription = "-$step")
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(52.dp)
                .then(if (onType != null) Modifier.clickable(role = Role.Button) { onType() } else Modifier)
                .insetFrame(),
            contentAlignment = Alignment.Center,
        ) {
            val scale = remember { Animatable(1f) }
            LaunchedEffect(value) { scale.snapTo(1.12f); scale.animateTo(1f, spring(dampingRatio = 0.5f)) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }) {
                TitleText(formatAmount(value, unit, lang), size = 16.sp)
                if (unit == ExerciseUnit.REPS) {
                    Spacer(Modifier.width(6.dp))
                    PText(unit.short.text(), color = pal.muted, size = 13.sp)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        PixelIconButton("plus", { onValue(value + step) }, size = 52.dp, iconSize = 22.dp, contentDescription = "+$step")
    }
    Spacer(Modifier.height(6.dp))
    val quick = when (unit) {
        ExerciseUnit.REPS -> listOf(1, 5, 10)
        ExerciseUnit.SECONDS -> listOf(10, 30, 60)
        ExerciseUnit.MINUTES -> listOf(1, 5, 10)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        quick.forEach { q ->
            Box(
                Modifier.weight(1f).heightIn(min = 36.dp).clickable { onValue(value + q) }.insetFrame().padding(4.dp),
                contentAlignment = Alignment.Center,
            ) { PText("+" + formatAmount(q, unit, lang), size = 14.sp, bold = true, color = pal.accent) }
        }
        Box(
            Modifier.weight(1f).heightIn(min = 36.dp).clickable { onValue(0) }.insetFrame().padding(4.dp),
            contentAlignment = Alignment.Center,
        ) { PText(tr("сброс", "reset"), size = 14.sp, color = pal.muted) }
    }
}

/* ================================== РЕЗУЛЬТАТ ================================== */

@Composable
fun WorkoutResultScreen(
    reward: WorkoutReward,
    state: PlayerState,
    totalXp: Int,
    newAchievements: List<Achievement>,
    onContinue: () -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val haptic = rememberHaptic()
    val title = remember { Animatable(-200f) }
    var counting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        title.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 300f))
        counting = true
        haptic(HapticFeedbackType.LongPress)
    }
    val xpShown by animateIntAsState(if (counting) reward.xp else 0, tween(1200, easing = FastOutSlowInEasing), label = "xp")
    val coinsShown by animateIntAsState(if (counting) reward.coins else 0, tween(1000), label = "c")
    val levelInfo = getLevelInfo(totalXp)
    val leveled = reward.levelAfter > reward.levelBefore

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        TitleText(tr("ПОБЕДА!", "VICTORY!"), size = 24.sp, color = pal.gold, modifier = Modifier.graphicsLayer { translationY = title.value }, align = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        PText(tr("Тренировка завершена", "Workout complete"), color = pal.muted, size = 16.sp)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
            RaysBackground(pal.gold.copy(alpha = 0.18f), Modifier.fillMaxSize())
            HeroView(state.equipped, state.heroLook, pixel = 6.dp, celebrate = true)
        }
        // XP и монеты
        PixelPanel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon("star", 34.dp)
                Spacer(Modifier.width(10.dp))
                TitleText("+$xpShown XP", size = 18.sp, color = pal.accent, modifier = Modifier.weight(1f))
                PixelIcon("coin", 28.dp)
                Spacer(Modifier.width(6.dp))
                TitleText("+$coinsShown", size = 14.sp, color = pal.gold)
            }
            val bonuses = buildList {
                if (reward.usedPotion) add(tr("Зелье ×2 XP", "Potion ×2 XP"))
                if (reward.perks.xp > 0) add(tr("Экипировка +${reward.perks.xp}% XP", "Gear +${reward.perks.xp}% XP"))
                if (reward.usedMagnet) add(tr("Магнит ×3 монет", "Magnet ×3 coins"))
                if (reward.perks.coins > 0) add(tr("+${reward.perks.coins}% монет", "+${reward.perks.coins}% coins"))
                if (reward.usedClover) add(tr("Клевер удачи", "Luck clover"))
            }
            if (bonuses.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                PText(bonuses.joinToString(" · "), color = pal.accent2, size = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelBadge(levelInfo.level)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    if (leveled) {
                        val t = rememberInfiniteTransition(label = "lvl")
                        val a by t.animateFloat(0.5f, 1f, infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "a")
                        TitleText(tr("НОВЫЙ УРОВЕНЬ!", "LEVEL UP!"), size = 10.sp, color = pal.gold.copy(alpha = a))
                        PText(tr("+${(reward.levelAfter - reward.levelBefore) * 25} монет за уровень", "+${(reward.levelAfter - reward.levelBefore) * 25} coins for level"), color = pal.gold, size = 13.sp)
                    } else PText("${levelInfo.currentXp} / ${levelInfo.requiredXp} XP", color = pal.muted, size = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    PixelBar(levelInfo.progress, Modifier.fillMaxWidth(), height = 14.dp)
                }
            }
        }
        // прирост характеристик
        if (reward.statGains.any { it.value > 0 }) {
            Spacer(Modifier.height(12.dp))
            PixelPanel(Modifier.fillMaxWidth()) {
                TitleText(tr("ПРИРОСТ", "GAINS"), size = 9.sp, color = pal.muted)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat.values().forEach { s ->
                        val g = reward.statGains[s] ?: 0
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PixelIcon(s.icon, 26.dp, disabled = g == 0)
                            PText(s.short.text(), size = 12.sp, color = pal.muted)
                            PText("+$g", size = 15.sp, bold = true, color = if (g > 0) s.color else pal.muted)
                        }
                    }
                }
            }
        }
        // добыча
        Spacer(Modifier.height(12.dp))
        LootReveal(reward.item)
        // достижения
        if (newAchievements.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            PixelPanel(Modifier.fillMaxWidth()) {
                TitleText(tr("НОВЫЕ ДОСТИЖЕНИЯ", "NEW ACHIEVEMENTS"), size = 9.sp, color = pal.gold)
                Spacer(Modifier.height(8.dp))
                newAchievements.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                        PixelIcon(achievementIcon(a.icon), 28.dp)
                        Spacer(Modifier.width(8.dp))
                        PText(a.title(lang), modifier = Modifier.weight(1f), bold = true)
                        PText("+${a.rewardXp} XP", color = pal.accent, size = 14.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        PixelButton(tr("ПРОДОЛЖИТЬ", "CONTINUE"), onContinue, Modifier.fillMaxWidth(), icon = "castle", height = 58.dp)
        Spacer(Modifier.height(12.dp))
    }
}

/** Сундук с добычей: трясётся, открывается по тапу, предмет вылетает с лучами. */
@Composable
private fun LootReveal(item: ItemDefinition?) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val haptic = rememberHaptic()
    var opened by remember { mutableStateOf(false) }
    val shake = rememberInfiniteTransition(label = "shake")
    val rot by shake.animateFloat(-6f, 6f, infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse), label = "r")
    val pop = remember { Animatable(0f) }
    LaunchedEffect(opened) { if (opened) pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 220f)) }

    PixelPanel(Modifier.fillMaxWidth()) {
        TitleText(tr("ДОБЫЧА", "LOOT"), size = 9.sp, color = pal.muted)
        Spacer(Modifier.height(10.dp))
        if (item == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon("chest", 40.dp, disabled = true)
                Spacer(Modifier.width(12.dp))
                PText(tr("В этот раз ничего не выпало. Больше повторений — выше шанс!", "Nothing dropped this time. More reps — better odds!"), color = pal.muted)
            }
            return@PixelPanel
        }
        Box(Modifier.fillMaxWidth().height(170.dp), contentAlignment = Alignment.Center) {
            if (!opened) {
                Column(
                    Modifier.clickable { opened = true; haptic(HapticFeedbackType.LongPress) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PixelIcon("chest", 88.dp, modifier = Modifier.graphicsLayer { rotationZ = rot })
                    Spacer(Modifier.height(8.dp))
                    PText(tr("Нажми, чтобы открыть", "Tap to open"), color = pal.gold, bold = true)
                }
            } else {
                RaysBackground(item.rarity.color.copy(alpha = 0.35f), Modifier.fillMaxSize())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value; translationY = (1 - pop.value) * 60f },
                ) {
                    Box(
                        Modifier.size(92.dp).pixelFrame(pal.panelLo, item.rarity.color, lerp(item.rarity.color, Color.Black, 0.5f), pal.border, 3.dp),
                        contentAlignment = Alignment.Center,
                    ) { ItemIcon(item, 70.dp) }
                    Spacer(Modifier.height(8.dp))
                    PText(item.name(lang), bold = true, size = 17.sp, align = TextAlign.Center)
                    PText(item.rarity.label(lang), color = item.rarity.color, size = 14.sp, bold = true)
                }
            }
        }
    }
}

/** Вращающиеся пиксельные лучи. */
@Composable
fun RaysBackground(color: Color, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "rays")
    val angle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "ang")
    Canvas(modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r = maxOf(size.width, size.height)
        val p = 6.dp.toPx()
        for (k in 0 until 12) {
            val a = Math.toRadians((angle + k * 30).toDouble())
            var d = p * 3
            while (d < r) {
                val w = p * (1 + d / r * 3)
                drawRect(color.copy(alpha = color.alpha * (1 - d / r)), Offset((cx + cos(a) * d).toFloat() - w / 2, (cy + sin(a) * d).toFloat() - w / 2), Size(w, w))
                d += p * 1.5f
            }
        }
    }
}

@Suppress("unused")
private val dummyDp: Dp = 0.dp

package com.example.ironquest

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** Каталог упражнений: выбор для тренировки и создание своих. */
@Composable
fun ExercisesScreen(
    state: PlayerState,
    onBack: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onSaveCustom: (Exercise) -> Unit,
    onArchive: (Exercise) -> Unit,
    onStartWorkout: () -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var editing by remember { mutableStateOf<Exercise?>(null) }
    var creating by remember { mutableStateOf(false) }
    var archiving by remember { mutableStateOf<Exercise?>(null) }
    val custom = state.customExercises.filter { !it.archived }
    val selected = state.selectedExercises

    Column(Modifier.fillMaxSize()) {
        PixelTopBar(tr("УПРАЖНЕНИЯ", "EXERCISES"), "book", onBack = onBack)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                PText(tr("Отметь упражнения, которые появятся в тренировке. Каждое качает свои характеристики.",
                    "Pick the exercises shown in your workout. Each one trains its own attributes."), color = pal.muted)
                Spacer(Modifier.height(10.dp))
                PixelButton(tr("СОЗДАТЬ СВОЁ", "CREATE CUSTOM"), { creating = true }, Modifier.fillMaxWidth(), icon = "pencil", fontSize = 10.sp)
            }
            if (custom.isNotEmpty()) {
                item { SectionHeader(tr("МОИ УПРАЖНЕНИЯ", "MY EXERCISES"), "pencil") }
                items(custom, key = { it.id }) { ex ->
                    ExerciseRow(ex, ex.id in selected, onToggle = { onToggle(ex.id, it) }, onEdit = { editing = ex }, onDelete = { archiving = ex })
                }
            }
            item { SectionHeader(tr("БАЗОВЫЕ", "BUILT-IN"), "dumbbell") }
            items(BUILTIN_EXERCISES, key = { it.id }) { ex ->
                ExerciseRow(ex, ex.id in selected, onToggle = { onToggle(ex.id, it) })
            }
        }
        Box(Modifier.navigationBarsPadding().padding(16.dp)) {
            PixelButton(tr("В БОЙ · ${selected.size}", "FIGHT · ${selected.size}"), onStartWorkout, Modifier.fillMaxWidth(), icon = "sword", enabled = selected.isNotEmpty())
        }
    }

    if (creating || editing != null) {
        ExerciseEditorDialog(editing, onDismiss = { creating = false; editing = null }, onSave = { onSaveCustom(it); creating = false; editing = null })
    }
    archiving?.let { ex ->
        ConfirmDialog(
            tr("УДАЛИТЬ УПРАЖНЕНИЕ?", "DELETE EXERCISE?"),
            tr("«${ex.name(lang)}» исчезнет из списка. История и характеристики сохранятся.", "\"${ex.name(lang)}\" will be removed. History and attributes are kept."),
            tr("УДАЛИТЬ", "DELETE"), onConfirm = { onArchive(ex); archiving = null }, onDismiss = { archiving = null }, icon = "trash", danger = true,
        )
    }
}

@Composable
private fun ExerciseRow(ex: Exercise, selected: Boolean, onToggle: (Boolean) -> Unit, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val haptic = rememberHaptic()
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox) { haptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onToggle(!selected) }
            .pixelFrame(if (selected) lerp(pal.panel, pal.accent, 0.12f) else pal.panel, if (selected) pal.accent else lerp(pal.panel, Color.White, 0.14f), lerp(pal.panel, Color.Black, 0.35f), pal.border)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(50.dp).insetFrame(), contentAlignment = Alignment.Center) { PixelIcon("ex:${ex.icon}", 38.dp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            PText(ex.name(lang), bold = true, size = 16.sp, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ex.stats.entries.sortedByDescending { it.value }.forEach { (s, w) ->
                    PixelIcon(s.icon, 14.dp)
                    PText("×$w ", size = 12.sp, color = s.color, bold = true)
                }
                PText(" · " + ex.unit.short.text(), size = 12.sp, color = pal.muted)
            }
            if (ex.muscles.isNotEmpty()) PText(ex.muscles.joinToString(", ") { it.title.get(lang) }, color = pal.muted, size = 12.sp, maxLines = 1)
        }
        if (onEdit != null) PixelIconButton("pencil", onEdit, size = 38.dp, iconSize = 18.dp, contentDescription = tr("Изменить", "Edit"))
        if (onDelete != null) { Spacer(Modifier.width(4.dp)); PixelIconButton("trash", onDelete, size = 38.dp, iconSize = 18.dp, contentDescription = tr("Удалить", "Delete")) }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(32.dp).then(if (selected) Modifier.panelFrame(pal.accent) else Modifier.insetFrame()),
            contentAlignment = Alignment.Center,
        ) { if (selected) PixelIcon("check", 20.dp) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseEditorDialog(initial: Exercise?, onDismiss: () -> Unit, onSave: (Exercise) -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var name by remember { mutableStateOf(initial?.title?.get(lang) ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "dumbbell") }
    var unit by remember { mutableStateOf(initial?.unit ?: ExerciseUnit.REPS) }
    var difficulty by remember { mutableStateOf(initial?.difficulty ?: Difficulty.NORMAL) }
    val initialSorted = initial?.stats?.entries?.sortedByDescending { it.value }
    var primary by remember { mutableStateOf(initialSorted?.getOrNull(0)?.key ?: Stat.STR) }
    var secondary by remember { mutableStateOf(initialSorted?.getOrNull(1)?.key) }
    var muscles by remember { mutableStateOf(initial?.muscles ?: emptySet()) }

    val preview = Exercise(
        id = initial?.id ?: "custom_${System.currentTimeMillis()}",
        title = LText(name.trim(), name.trim()),
        icon = icon, unit = unit,
        stats = Exercise.customStats(primary, secondary, difficulty),
        muscles = muscles,
        xpPerUnit = Exercise.baseXp(unit, difficulty),
        custom = true, difficulty = difficulty,
    )
    val sample = when (unit) { ExerciseUnit.REPS -> 10; ExerciseUnit.SECONDS -> 60; ExerciseUnit.MINUTES -> 10 }

    PixelDialog(onDismiss, if (initial == null) tr("НОВОЕ УПРАЖНЕНИЕ", "NEW EXERCISE") else tr("ИЗМЕНИТЬ", "EDIT"), "pencil") {
        Label(tr("Название", "Name"))
        PixelTextField(name, { if (it.length <= 28) name = it }, Modifier.fillMaxWidth(), placeholder = tr("Например: Австралийские подтягивания", "e.g. Australian pull-ups"))
        Label(tr("Иконка", "Icon"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            EXERCISE_ICON_CHOICES.forEach { k ->
                Box(
                    Modifier.size(46.dp).clickable(role = Role.RadioButton) { icon = k }
                        .then(if (icon == k) Modifier.panelFrame(pal.accent) else Modifier.insetFrame()),
                    contentAlignment = Alignment.Center,
                ) { PixelIcon("ex:$k", 32.dp) }
            }
        }
        Label(tr("Что считаем", "Measured in"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PixelChip(tr("Повторения", "Reps"), unit == ExerciseUnit.REPS, { unit = ExerciseUnit.REPS })
            PixelChip(tr("Секунды", "Seconds"), unit == ExerciseUnit.SECONDS, { unit = ExerciseUnit.SECONDS })
            PixelChip(tr("Минуты", "Minutes"), unit == ExerciseUnit.MINUTES, { unit = ExerciseUnit.MINUTES })
        }
        Label(tr("Сложность", "Difficulty"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Difficulty.values().forEach { d -> PixelChip(d.title.text(), difficulty == d, { difficulty = d }) }
        }
        Label(tr("Главная характеристика", "Main attribute"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Stat.values().forEach { s -> PixelChip(s.short.text(), primary == s, { primary = s; if (secondary == s) secondary = null }, icon = s.icon, color = s.color) }
        }
        Label(tr("Дополнительная (необязательно)", "Secondary (optional)"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PixelChip(tr("Нет", "None"), secondary == null, { secondary = null })
            Stat.values().filter { it != primary }.forEach { s -> PixelChip(s.short.text(), secondary == s, { secondary = s }, icon = s.icon, color = s.color) }
        }
        Label(tr("Мышцы", "Muscles"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Muscle.values().forEach { m ->
                PixelChip(m.title.text(), m in muscles, { muscles = if (m in muscles) muscles - m else muscles + m }, icon = "mu:${m.icon}", color = pal.accent2)
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().insetFrame().padding(10.dp)) {
            PText(tr("За ", "For ") + formatAmount(sample, unit, lang) + (if (unit == ExerciseUnit.REPS) " " + unit.short.text() else "") + ":", color = pal.muted, size = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon("star", 16.dp); Spacer(Modifier.width(4.dp))
                PText("+${preview.xpFor(sample).roundToInt()} XP", color = pal.accent, bold = true)
                preview.statPoints(sample).forEach { (s, v) ->
                    Spacer(Modifier.width(10.dp)); PixelIcon(s.icon, 16.dp); Spacer(Modifier.width(3.dp))
                    PText("+$v", color = s.color, bold = true)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        PixelButton(tr("СОХРАНИТЬ", "SAVE"), {
            val n = name.trim()
            onSave(preview.copy(title = if (initial != null && initial.title.get(lang) == n) initial.title else LText(n, n)))
        }, Modifier.fillMaxWidth(), enabled = name.isNotBlank(), icon = "check")
    }
}

@Composable
private fun Label(text: String) {
    Spacer(Modifier.height(12.dp))
    PText(text, color = LocalPalette.current.muted, size = 14.sp, bold = true)
    Spacer(Modifier.height(6.dp))
}

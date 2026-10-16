package com.example.ironquest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HERO_PHRASES = listOf(
    LText("Ещё один подход!", "One more set!"),
    LText("Сегодня я сильнее, чем вчера.", "Stronger than yesterday."),
    LText("Боль — это слабость, покидающая тело!", "Pain is weakness leaving the body!"),
    LText("Где мой турник?", "Where's my pull-up bar?"),
    LText("Готов к приключениям!", "Ready for adventure!"),
    LText("Пропустить? Не в этой жизни.", "Skip? Not in this life."),
    LText("Чувствую новый уровень...", "I can feel a new level..."),
    LText("Добыча сама себя не выбьет!", "Loot won't farm itself!"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: PlayerState,
    history: List<WorkoutRecord>,
    level: LevelInfo,
    stats: HeroStats,
    streak: Int,
    bestStreak: Int,
    achievements: List<Achievement>,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenSlot: (ItemSlot) -> Unit,
    onSaveRestDays: (Set<Int>) -> Unit,
    onSaveHero: (String, HeroLook) -> Unit,
    onGoShop: () -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var showRest by remember { mutableStateOf(false) }
    var showHero by remember { mutableStateOf(false) }
    val heroName = state.heroName.ifBlank { tr("Герой", "Hero") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 32.dp)
    ) {
        // ---------- HUD
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelBadge(level.level)
            Spacer(Modifier.width(10.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable(role = Role.Button) { showHero = true }
                    .padding(vertical = 4.dp)
            ) {
                TitleText(heroName, size = 11.sp, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PText(rankTitle(level.level).text(), color = pal.accent2, size = 14.sp, bold = true)
                    Spacer(Modifier.width(6.dp))
                    PixelIcon("pencil", 14.dp, alpha = 0.7f)
                }
            }
            StreakBadge(streak, history.any { dayKey(it.timestamp) == dayKey(System.currentTimeMillis()) })
            Spacer(Modifier.width(6.dp))
            PixelIconButton("gear", onOpenSettings, contentDescription = tr("Настройки", "Settings"), size = 44.dp, iconSize = 24.dp)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon("star", 18.dp)
            Spacer(Modifier.width(6.dp))
            PText(tr("ОПЫТ", "XP"), color = pal.muted, size = 13.sp, bold = true)
            Spacer(Modifier.weight(1f))
            PText("${level.currentXp} / ${level.requiredXp}", color = pal.text, size = 14.sp, bold = true)
            Spacer(Modifier.width(10.dp))
            CoinCounter(state.coins)
        }
        Spacer(Modifier.height(6.dp))
        PixelBar(level.progress, Modifier.fillMaxWidth(), color = pal.accent, height = 16.dp, segments = 10)

        // ---------- Сцена героя
        Spacer(Modifier.height(14.dp))
        HeroStage(state, stats, achievements, onOpenSlot)

        // ---------- Активные бонусы
        val boosts = Consumable.values().filter { it != Consumable.STREAK_FREEZE && state.count(it) > 0 }
        if (boosts.isNotEmpty() || stats.perks != Perks()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                boosts.forEach { c -> StatPill(c.icon, "${c.title.text()} ×${state.count(c)}", pal.text) }
                if (stats.perks.xp > 0) StatPill("star", "+${stats.perks.xp}% XP", pal.accent)
                if (stats.perks.coins > 0) StatPill("coin", "+${stats.perks.coins}% " + tr("монет", "coins"), pal.gold)
                if (stats.perks.luck > 0) StatPill("clover", "+${stats.perks.luck}% " + tr("удачи", "luck"), pal.good)
            }
        }

        // ---------- Неделя и серия
        SectionHeader(tr("НЕДЕЛЯ ГЕРОЯ", "HERO WEEK"), "calendar") {
            Row(
                Modifier.clickable(role = Role.Button) { showRest = true }.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PixelIcon("moon", 16.dp)
                Spacer(Modifier.width(4.dp))
                PText(tr("Выходные ${state.restDays.size}/${state.restDaysUnlocked}", "Rest ${state.restDays.size}/${state.restDaysUnlocked}"),
                    color = pal.accent2, size = 14.sp, bold = true)
            }
        }
        WeekCard(state, history, streak, bestStreak, onRest = { showRest = true })

        // ---------- Характеристики
        SectionHeader(tr("ХАРАКТЕРИСТИКИ", "ATTRIBUTES"), "fist") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon("bolt", 16.dp)
                Spacer(Modifier.width(4.dp))
                TitleText("${stats.power}", size = 10.sp, color = pal.gold)
            }
        }
        AttributesCard(stats, state.customExercises)

        // ---------- Сравнение тренировок
        SectionHeader(tr("ПОСЛЕДНЯЯ БИТВА", "LAST BATTLE"), "sword")
        ComparisonCard(history, state.customExercises)

        // ---------- Быстрые ссылки
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton(tr("ЛЕТОПИСЬ", "CHRONICLE"), onOpenHistory, Modifier.weight(1f), color = pal.panelHi, textColor = pal.text, icon = "scroll", fontSize = 9.sp)
            PixelButton(tr("УПРАЖНЕНИЯ", "EXERCISES"), onOpenExercises, Modifier.weight(1f), color = pal.panelHi, textColor = pal.text, icon = "book", fontSize = 9.sp)
        }
    }

    if (showRest) RestDaysDialog(state, onDismiss = { showRest = false }, onSave = { onSaveRestDays(it); showRest = false }, onGoShop = { showRest = false; onGoShop() })
    if (showHero) HeroEditDialog(state, onDismiss = { showHero = false }, onSave = { n, l -> onSaveHero(n, l); showHero = false })
}

@Composable
fun LevelBadge(level: Int) {
    val pal = LocalPalette.current
    Box(
        Modifier
            .size(54.dp)
            .pixelFrame(pal.accent, androidx.compose.ui.graphics.lerp(pal.accent, Color.White, 0.4f), androidx.compose.ui.graphics.lerp(pal.accent, Color.Black, 0.4f), pal.border, 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PText("LV", color = pal.onAccent, size = 11.sp, bold = true)
            TitleText("$level", color = pal.onAccent, size = if (level >= 100) 9.sp else 13.sp, shadow = false, maxLines = 1)
        }
    }
}

@Composable
private fun StreakBadge(streak: Int, trainedToday: Boolean) {
    val pal = LocalPalette.current
    Row(
        Modifier.heightIn(min = 44.dp).insetFrame().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PixelIcon(if (streak > 0) "flame" else "flame_off", 24.dp)
        Spacer(Modifier.width(4.dp))
        TitleText("$streak", size = 11.sp, color = if (trainedToday) pal.gold else pal.text, shadow = false)
    }
}

/* ---------------------------------------------------------------- сцена */

@Composable
private fun HeroStage(state: PlayerState, stats: HeroStats, achievements: List<Achievement>, onOpenSlot: (ItemSlot) -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var phrase by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(phrase) {
        if (phrase != null) {
            delay(2400)
            phrase = null
        }
    }
    val equipped = state.equipped
    Box(
        Modifier
            .fillMaxWidth()
            .height(318.dp)
            .panelFrame()
            .padding(3.dp)
    ) {
        HeroScenery(pal, Modifier.fillMaxSize())
        // слоты экипировки
        Column(Modifier.align(Alignment.CenterStart).padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ItemSlot.HEAD, ItemSlot.BODY, ItemSlot.HANDS, ItemSlot.TRINKET).forEach { slot ->
                EquipSlot(slot, equipped[slot]) { onOpenSlot(slot) }
            }
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ItemSlot.WEAPON, ItemSlot.BACK, ItemSlot.FEET).forEach { slot ->
                EquipSlot(slot, equipped[slot]) { onOpenSlot(slot) }
            }
            Box(
                Modifier
                    .size(52.dp)
                    .insetFrame(pal.panelLo),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PixelIcon("bolt", 18.dp)
                    PText("${stats.power}", size = 12.sp, bold = true, color = pal.gold, maxLines = 1)
                }
            }
        }
        // любимые достижения
        val favs = achievements.filter { it.id in state.favoriteAchievements }
        if (favs.isNotEmpty()) {
            Row(
                Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                favs.forEach { a ->
                    Box(Modifier.size(34.dp).insetFrame(pal.panelLo), contentAlignment = Alignment.Center) {
                        PixelIcon(achievementIcon(a.icon), 22.dp, disabled = !a.unlocked)
                    }
                }
            }
        }
        // герой
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)) {
            HeroView(equipped, state.heroLook, pixel = 6.dp, onTap = { phrase = HERO_PHRASES.random().get(lang) })
        }
        // реплика
        AnimatedVisibility(
            visible = phrase != null,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 52.dp, start = 70.dp, end = 70.dp),
            enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut(),
        ) {
            Box(Modifier.pixelFrame(Color(0xFFF4F0E6), Color.White, Color(0xFFC9C2B0), pal.border, 2.dp).padding(horizontal = 10.dp, vertical = 6.dp)) {
                PText(phrase ?: "", color = Color(0xFF231C30), size = 14.sp, bold = true, align = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun EquipSlot(slot: ItemSlot, item: ItemDefinition?, onClick: () -> Unit) {
    val pal = LocalPalette.current
    val haptic = rememberHaptic()
    Box(
        Modifier
            .size(52.dp)
            .clickable(role = Role.Button) { haptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onClick() }
            .then(
                if (item != null) Modifier.pixelFrame(pal.panelLo, item.rarity.color, androidx.compose.ui.graphics.lerp(item.rarity.color, Color.Black, 0.5f), pal.border, 2.dp)
                else Modifier.insetFrame(pal.panelLo.copy(alpha = 0.85f))
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (item != null) ItemIcon(item, 36.dp)
        else PixelIcon("item:${slot.emptyIcon}:iron", 30.dp, disabled = true, alpha = 0.6f)
    }
}

/* ---------------------------------------------------------------- неделя */

@Composable
private fun WeekCard(state: PlayerState, history: List<WorkoutRecord>, streak: Int, best: Int, onRest: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val week = remember(history, state.restDays, state.frozenDays) { currentWeek(history, state.restDays, state.frozenDays) }
    val todayRest = week.firstOrNull { it.isToday }?.state == DayState.REST
    PixelPanel(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            week.forEach { d -> DayCell(d, Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon("hourglass", 18.dp)
            Spacer(Modifier.width(8.dp))
            PText(lastWorkoutLabel(history, lang), color = pal.text, size = 15.sp, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon("flame", 18.dp)
            Spacer(Modifier.width(8.dp))
            PText(tr("Серия: ${daysRu(streak)} · рекорд: $best", "Streak: $streak d · best: $best"), color = pal.muted, size = 15.sp, modifier = Modifier.weight(1f))
            if (state.count(Consumable.STREAK_FREEZE) > 0) {
                StatPill("snowflake", "${state.count(Consumable.STREAK_FREEZE)}", pal.accent2)
            }
        }
        if (todayRest) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().insetFrame().clickable { onRest() }.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PixelIcon("moon", 24.dp)
                Spacer(Modifier.width(10.dp))
                PText(tr("Сегодня выходной — серия в безопасности. Можно и потренироваться!", "Rest day — your streak is safe. Training is still welcome!"),
                    color = pal.accent2, size = 14.sp)
            }
        }
    }
}

private fun daysRu(n: Int) = "$n ${plural(n, "день", "дня", "дней")}"

@Composable
private fun DayCell(d: WeekDay, modifier: Modifier) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val fill = when (d.state) {
        DayState.DONE -> pal.accent
        DayState.REST -> pal.panelHi
        DayState.FROZEN -> Color(0xFF3B8FB0)
        DayState.MISSED -> pal.panelLo
        DayState.TODAY -> pal.inset
        DayState.FUTURE -> pal.inset
    }
    val border = if (d.isToday) pal.gold else pal.border
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        PText(weekdayShort(d.dow, lang), size = 12.sp, bold = true, color = if (d.isToday) pal.gold else pal.muted)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pixelFrame(fill, androidx.compose.ui.graphics.lerp(fill, Color.White, 0.25f), androidx.compose.ui.graphics.lerp(fill, Color.Black, 0.35f), border, 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (d.state) {
                DayState.DONE -> PixelIcon("check", 20.dp)
                DayState.REST -> PixelIcon("moon", 18.dp)
                DayState.FROZEN -> PixelIcon("snowflake", 18.dp)
                DayState.MISSED -> PixelIcon("close", 14.dp, alpha = 0.55f)
                DayState.TODAY -> PixelIcon("sword", 16.dp, alpha = 0.6f)
                DayState.FUTURE -> Unit
            }
        }
    }
}

fun lastWorkoutLabel(history: List<WorkoutRecord>, lang: AppLanguage): String {
    val last = history.firstOrNull() ?: return tr(lang, "Последняя тренировка: пока нет", "Last workout: none yet")
    val today = dayKey(System.currentTimeMillis())
    val day = dayKey(last.timestamp)
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(last.timestamp))
    val label = when (day) {
        today -> tr(lang, "сегодня, $time", "today, $time")
        shiftDay(today, -1) -> tr(lang, "вчера, $time", "yesterday, $time")
        else -> {
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(last.timestamp))
            var n = 0
            var cur = today
            while (cur > day && n < 400) { cur = shiftDay(cur, -1); n++ }
            tr(lang, "$date ($n ${plural(n, "день", "дня", "дней")} назад)", "$date ($n days ago)")
        }
    }
    return tr(lang, "Последняя тренировка: $label", "Last workout: $label")
}

/* ---------------------------------------------------------------- характеристики */

@Composable
private fun AttributesCard(stats: HeroStats, custom: List<Exercise>) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var expanded by remember { mutableStateOf<Stat?>(null) }
    PixelPanel(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        Stat.values().forEachIndexed { i, stat ->
            val lv = stats.levels[stat] ?: statLevel(0)
            val bonus = stats.gearBonus[stat] ?: 0
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { expanded = if (expanded == stat) null else stat }
                    .padding(vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).insetFrame(), contentAlignment = Alignment.Center) { PixelIcon(stat.icon, 26.dp) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TitleText(stat.title.text(), size = 9.sp, color = stat.color, maxLines = 1, modifier = Modifier.weight(1f))
                            TitleText("${lv.level}", size = 12.sp)
                            if (bonus > 0) {
                                Spacer(Modifier.width(4.dp))
                                PText("+$bonus", color = pal.accent2, size = 15.sp, bold = true)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        PixelBar(lv.progress, Modifier.fillMaxWidth(), color = stat.color, height = 12.dp, segments = 8)
                        Spacer(Modifier.height(2.dp))
                        PText("${lv.current} / ${lv.required} " + tr("очков", "pts"), color = pal.muted, size = 12.sp)
                    }
                }
                AnimatedVisibility(expanded == stat, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    val top = allExercises(custom).filter { (it.stats[stat] ?: 0) > 0 && !it.archived }
                        .sortedByDescending { it.stats[stat] ?: 0 }.take(5)
                    Column(Modifier.padding(top = 8.dp, start = 50.dp)) {
                        PText(stat.hint.text(), color = pal.muted, size = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        top.forEach { ex ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                PixelIcon("ex:${ex.icon}", 20.dp)
                                Spacer(Modifier.width(6.dp))
                                PText(ex.name(lang), size = 14.sp, modifier = Modifier.weight(1f))
                                PText("×${ex.stats[stat]}", color = stat.color, size = 14.sp, bold = true)
                            }
                        }
                    }
                }
            }
            if (i < Stat.values().size - 1) Spacer(Modifier.height(2.dp))
        }
        Spacer(Modifier.height(10.dp))
        TitleText(tr("МЫШЦЫ", "MUSCLES"), size = 8.sp, color = pal.muted)
        Spacer(Modifier.height(8.dp))
        val muscles = Muscle.values().toList()
        muscles.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { m ->
                    val lv = stats.muscles[m] ?: statLevel(0)
                    Row(
                        Modifier.weight(1f).insetFrame().padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PixelIcon("mu:${m.icon}", 26.dp)
                        Spacer(Modifier.width(4.dp))
                        Column {
                            PText(m.title.text(), size = 12.sp, maxLines = 1, color = pal.muted)
                            PText("LV ${lv.level}", size = 14.sp, bold = true)
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- сравнение */

@Composable
private fun ComparisonCard(history: List<WorkoutRecord>, custom: List<Exercise>) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    PixelPanel(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        if (history.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon("skull", 32.dp)
                Spacer(Modifier.width(10.dp))
                PText(tr("Заверши первую тренировку, чтобы начать отслеживать прогресс.", "Finish your first workout to start tracking progress."), color = pal.muted)
            }
            return@PixelPanel
        }
        val latest = history[0]
        val prev = history.getOrNull(1)
        val lt = latest.repEquivalent(custom)
        val pt = prev?.repEquivalent(custom)
        val diff = if (pt != null) lt - pt else 0
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                PText(tr("Объём", "Volume"), color = pal.muted, size = 13.sp)
                TitleText("$lt", size = 16.sp)
                PText(if (pt != null) tr("Прошлая: $pt", "Previous: $pt") else tr("Следующая покажет разницу", "Next one shows the difference"), color = pal.muted, size = 13.sp)
            }
            val (icon, color, text) = when {
                pt == null -> Triple("sparkle", pal.accent2, tr("НОВОЕ", "NEW"))
                diff > 0 -> Triple("arrow_up", pal.good, "+$diff")
                diff < 0 -> Triple("arrow_down", pal.danger, "$diff")
                else -> Triple("check", pal.muted, "=")
            }
            Column(horizontalAlignment = Alignment.End) {
                PixelIcon(icon, 28.dp)
                TitleText(text, size = 11.sp, color = color)
            }
        }
        Spacer(Modifier.height(10.dp))
        latest.entries.forEach { (id, sets) ->
            val ex = exerciseById(id, custom)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                PixelIcon("ex:${ex?.icon ?: "dumbbell"}", 22.dp)
                Spacer(Modifier.width(8.dp))
                PText(ex?.name(lang) ?: id, size = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                PText(sets.joinToString(" · ") { formatAmount(it, ex?.unit ?: ExerciseUnit.REPS, lang) }, color = pal.muted, size = 14.sp, maxLines = 1)
            }
        }
    }
}

/* ---------------------------------------------------------------- диалоги */

@Composable
fun RestDaysDialog(state: PlayerState, onDismiss: () -> Unit, onSave: (Set<Int>) -> Unit, onGoShop: (() -> Unit)? = null) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var selected by remember { mutableStateOf(state.restDays.take(state.restDaysUnlocked).toSet()) }
    PixelDialog(onDismiss, tr("ВЫХОДНЫЕ", "REST DAYS"), "moon") {
        PText(tr("В выходной день серия не сгорает, даже если ты не тренировался. Тренировка в выходной всё равно засчитывается.",
            "On a rest day your streak won't break even if you skip. Training on a rest day still counts."), color = pal.muted)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(MAX_REST_DAYS) { i ->
                PixelIcon("moon", 22.dp, disabled = i >= state.restDaysUnlocked)
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.weight(1f))
            PText(tr("Выбрано ${selected.size} из ${state.restDaysUnlocked}", "Selected ${selected.size} of ${state.restDaysUnlocked}"), bold = true, color = pal.accent2)
        }
        Spacer(Modifier.height(12.dp))
        WEEK_ORDER.forEach { dow ->
            val on = dow in selected
            val canAdd = selected.size < state.restDaysUnlocked
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable(role = Role.Checkbox) {
                        selected = if (on) selected - dow else if (canAdd) selected + dow else selected
                    }
                    .then(if (on) Modifier.panelFrame(pal.panelHi) else Modifier.insetFrame())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PText(weekdayFull(dow, lang), modifier = Modifier.weight(1f), bold = on, color = if (on || canAdd) pal.text else pal.muted)
                if (on) PixelIcon("moon", 22.dp) else PixelIcon("sword", 18.dp, alpha = 0.5f)
            }
        }
        if (state.restDaysUnlocked < MAX_REST_DAYS) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().then(if (onGoShop != null) Modifier.clickable { onGoShop() } else Modifier).insetFrame().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PixelIcon("shop", 22.dp)
                Spacer(Modifier.width(8.dp))
                PText(tr("Больше выходных (до 5 в неделю) — в лавке, раздел «Улучшения».", "More rest days (up to 5 a week) are in the shop, Upgrades tab."),
                    color = pal.gold, size = 14.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        PixelButton(tr("СОХРАНИТЬ", "SAVE"), { onSave(selected) }, Modifier.fillMaxWidth(), icon = "check")
    }
}

@Composable
fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    numeric: Boolean = false,
    big: Boolean = false,
) {
    val pal = LocalPalette.current
    Box(modifier.heightIn(min = 48.dp).insetFrame().padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) PText(placeholder, color = pal.muted.copy(alpha = 0.7f), size = if (big) 22.sp else 17.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(pal.accent),
            keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            textStyle = TextStyle(color = pal.text, fontFamily = if (big) TitleFont else BodyFont, fontSize = if (big) 20.sp else 17.sp,
                textAlign = if (big) TextAlign.Center else TextAlign.Start),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroEditDialog(state: PlayerState, onDismiss: () -> Unit, onSave: (String, HeroLook) -> Unit) {
    val pal = LocalPalette.current
    var name by remember { mutableStateOf(state.heroName) }
    var look by remember { mutableStateOf(state.heroLook) }
    PixelDialog(onDismiss, tr("ТВОЙ ГЕРОЙ", "YOUR HERO"), "castle") {
        Box(Modifier.fillMaxWidth().height(170.dp).insetFrame(pal.panelLo), contentAlignment = Alignment.Center) {
            HeroView(state.equipped, look, pixel = 5.dp, onTap = {})
        }
        Spacer(Modifier.height(12.dp))
        PText(tr("Имя", "Name"), color = pal.muted, size = 14.sp)
        Spacer(Modifier.height(4.dp))
        PixelTextField(name, { if (it.length <= 16) name = it }, Modifier.fillMaxWidth(), placeholder = tr("Герой", "Hero"))
        Spacer(Modifier.height(12.dp))
        PText(tr("Волосы", "Hair"), color = pal.muted, size = 14.sp)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HAIR_COLORS.forEachIndexed { i, (c, _) -> Swatch(Color(c), look.hair == i) { look = look.copy(hair = i) } }
        }
        Spacer(Modifier.height(10.dp))
        PText(tr("Кожа", "Skin"), color = pal.muted, size = 14.sp)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SKIN_COLORS.forEachIndexed { i, (c, _) -> Swatch(Color(c), look.skin == i) { look = look.copy(skin = i) } }
        }
        Spacer(Modifier.height(16.dp))
        PixelButton(tr("СОХРАНИТЬ", "SAVE"), { onSave(name.trim(), look) }, Modifier.fillMaxWidth(), icon = "check")
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val pal = LocalPalette.current
    val s by animateFloatAsState(if (selected) 1.1f else 1f, tween(150), label = "sw")
    Box(
        Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = s; scaleY = s }
            .clickable(role = Role.RadioButton) { onClick() }
            .pixelFrame(color, androidx.compose.ui.graphics.lerp(color, Color.White, 0.3f), androidx.compose.ui.graphics.lerp(color, Color.Black, 0.3f), if (selected) pal.gold else pal.border, 3.dp)
    )
}

@Suppress("unused")
private fun Modifier.solid(c: Color) = this.background(c)

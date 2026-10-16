package com.example.ironquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { IronQuestApp() }
    }
}

enum class Screen(val isTab: Boolean = false) {
    HOME(true), INVENTORY(true), SHOP(true), ACHIEVEMENTS(true),
    WORKOUT, RESULT, HISTORY, EXERCISES, SETTINGS
}

@Composable
fun IronQuestApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var language by remember { mutableStateOf(AppPreferences.getLanguage(context)) }
    var haptics by remember { mutableStateOf(AppPreferences.hapticsEnabled(context)) }

    LaunchedEffect(Unit) { UpdateCheckScheduler.schedule(context.applicationContext) }

    val stateFlow = remember { PlayerData.state(context.applicationContext) }
    val historyFlow = remember { WorkoutHistoryData.getHistory(context.applicationContext) }
    val state by stateFlow.collectAsState(initial = PlayerState())
    val historyOrNull by historyFlow.collectAsState(initial = null)
    val history = historyOrNull ?: emptyList()

    val equipped = remember(state.equippedIds) { state.equipped }
    val level = getLevelInfo(state.xp)
    val heroStats = remember(history, state.customExercises, equipped, level.level) {
        computeHeroStats(history, state.customExercises, equipped, level.level)
    }
    val streak = remember(history, state.restDays, state.frozenDays) { calculateStreak(history, state.restDays, state.frozenDays) }
    val best = remember(history, state.restDays, state.frozenDays) { bestStreak(history, state.restDays, state.frozenDays) }
    val achievements = remember(history, state, heroStats, best) {
        getAchievements(
            AchievementInput(
                history = history, custom = state.customExercises, totalXp = state.xp, bestStreak = best,
                ownedItems = state.items, counters = state.counters, stats = heroStats,
                equippedCount = equipped.size, ownedThemes = state.ownedThemes.size,
                restDaysUnlocked = state.restDaysUnlocked, restDays = state.restDays,
            )
        )
    }
    val toasts = remember { mutableStateListOf<GameToast>() }
    val sessionAchievements = remember { mutableStateListOf<Achievement>() }

    // Выдаём награды за новые достижения (один раз за каждое).
    val unlockedIds = achievements.filter { it.unlocked }.map { it.id }
    LaunchedEffect(unlockedIds, state.loaded, historyOrNull != null) {
        if (!state.loaded || historyOrNull == null) return@LaunchedEffect
        if (unlockedIds.all { it in state.claimedAchievements }) return@LaunchedEffect
        val fresh = PlayerData.awardUnlockedAchievements(context, achievements)
        sessionAchievements.addAll(fresh)
        if (fresh.size > 3) {
            toasts.add(GameToast(System.nanoTime(), "trophy", tr(language, "ОТКРЫТО ДОСТИЖЕНИЙ: ${fresh.size}", "${fresh.size} ACHIEVEMENTS"),
                "+${fresh.sumOf { it.rewardXp }} XP · +${fresh.sumOf { it.rewardCoins }} " + tr(language, "монет", "coins"), Color(0xFFFFD75A)))
        } else fresh.forEach { a ->
            toasts.add(GameToast(System.nanoTime(), achievementIcon(a.icon), tr(language, "ДОСТИЖЕНИЕ!", "ACHIEVEMENT!"),
                "${a.title(language)} · +${a.rewardXp} XP" + if (a.rewardCoins > 0) " · +${a.rewardCoins}🪙" else "", Color(0xFFFFD75A)))
        }
    }

    // Заморозки серии срабатывают автоматически при запуске.
    var freezesChecked by remember { mutableStateOf(false) }
    LaunchedEffect(state.loaded, historyOrNull != null) {
        if (!freezesChecked && state.loaded && historyOrNull != null) {
            freezesChecked = true
            val used = PlayerData.applyStreakFreezes(context, history)
            if (used > 0) toasts.add(GameToast(System.nanoTime(), "snowflake", tr(language, "СЕРИЯ СПАСЕНА!", "STREAK SAVED!"),
                tr(language, "Потрачено заморозок: $used", "Freezes used: $used"), Color(0xFF8FE8FF)))
        }
    }

    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var slotFilter by remember { mutableStateOf<ItemSlot?>(null) }
    var reward by remember { mutableStateOf<WorkoutReward?>(null) }
    var resultAchievementsFrom by remember { mutableStateOf(0) }
    var saving by remember { mutableStateOf(false) }

    BackHandler(enabled = screen != Screen.HOME && screen != Screen.WORKOUT) {
        screen = when (screen) {
            Screen.EXERCISES, Screen.HISTORY, Screen.SETTINGS, Screen.RESULT -> Screen.HOME
            else -> Screen.HOME
        }
    }

    fun toast(icon: String, title: String, sub: String, color: Color = Color(0xFFB8FF5C)) {
        toasts.add(GameToast(System.nanoTime(), icon, title, sub, color))
    }

    ProvideGameTheme(state.activeTheme, language, haptics) {
        val pal = LocalPalette.current
        MaterialTheme(colorScheme = darkColorScheme(primary = pal.accent, background = pal.bg, surface = pal.panel)) {
            Box(Modifier.fillMaxSize().gameBackground()) {
                if (!state.loaded || historyOrNull == null) {
                    LoadingScreen()
                    return@Box
                }
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = {
                                if (targetState.isTab && initialState.isTab) {
                                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                                } else if (!targetState.isTab) {
                                    (slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(200))) togetherWith fadeOut(tween(150))
                                } else {
                                    (slideInHorizontally(tween(260)) { -it / 3 } + fadeIn(tween(200))) togetherWith fadeOut(tween(150))
                                }
                            },
                            label = "screens",
                        ) { s ->
                            when (s) {
                                Screen.HOME -> HomeScreen(
                                    state = state, history = history, level = level, stats = heroStats, streak = streak,
                                    bestStreak = best, achievements = achievements,
                                    onOpenSettings = { screen = Screen.SETTINGS },
                                    onOpenHistory = { screen = Screen.HISTORY },
                                    onOpenExercises = { screen = Screen.EXERCISES },
                                    onOpenSlot = { slot -> slotFilter = slot; screen = Screen.INVENTORY },
                                    onSaveRestDays = { days -> scope.launch { PlayerData.setRestDays(context, days) } },
                                    onSaveHero = { name, look ->
                                        scope.launch { PlayerData.setHeroName(context, name); PlayerData.setHeroLook(context, look) }
                                    },
                                    onGoShop = { screen = Screen.SHOP },
                                )
                                Screen.INVENTORY -> InventoryScreen(
                                    state = state, stats = heroStats, slotFilter = slotFilter,
                                    onSlotFilter = { slotFilter = it },
                                    onEquip = { scope.launch { PlayerData.equipItem(context, it) } },
                                    onUnequip = { scope.launch { PlayerData.unequipItem(context, it) } },
                                    onToggleLock = { scope.launch { PlayerData.toggleItemLock(context, it) } },
                                    onSell = { item, count ->
                                        scope.launch {
                                            val got = PlayerData.sellItem(context, item, count)
                                            if (got > 0) toast("coin", tr(language, "ПРОДАНО", "SOLD"), "${item.name(language)} · +$got🪙", pal.gold)
                                        }
                                    },
                                    onSellDuplicates = {
                                        scope.launch {
                                            val got = PlayerData.sellDuplicates(context)
                                            toast("coin", tr(language, "ДУБЛИКАТЫ ПРОДАНЫ", "DUPLICATES SOLD"), "+$got🪙", pal.gold)
                                        }
                                    },
                                )
                                Screen.SHOP -> ShopScreen(
                                    state = state,
                                    onToast = { icon, title, sub -> toast(icon, title, sub, pal.gold) },
                                )
                                Screen.ACHIEVEMENTS -> AchievementsScreen(
                                    state = state, achievements = achievements,
                                    onToggleFavorite = { a -> scope.launch { PlayerData.toggleFavoriteAchievement(context, a.id) } },
                                    onEquipSet = { set ->
                                        scope.launch {
                                            PlayerData.equipSet(context, set)
                                            toast(set.pieces.first().iconKey(), tr(language, "СЕТ НАДЕТ", "SET EQUIPPED"), set.title.get(language))
                                        }
                                    },
                                )
                                Screen.WORKOUT -> WorkoutScreen(
                                    state = state, history = history, perks = heroStats.perks, isSaving = saving,
                                    onBack = { if (!saving) screen = Screen.HOME },
                                    onFinish = { entries, duration ->
                                        if (!saving) {
                                            saving = true
                                            scope.launch {
                                                try {
                                                    resultAchievementsFrom = sessionAchievements.size
                                                    reward = PlayerData.completeWorkout(context, entries, duration, state, heroStats.perks)
                                                    screen = Screen.RESULT
                                                } finally {
                                                    saving = false
                                                }
                                            }
                                        }
                                    },
                                )
                                Screen.RESULT -> {
                                    val r = reward
                                    if (r == null) {
                                        LaunchedEffect(Unit) { screen = Screen.HOME }
                                    } else {
                                        WorkoutResultScreen(
                                            reward = r, state = state, totalXp = state.xp,
                                            newAchievements = sessionAchievements.drop(resultAchievementsFrom),
                                            onContinue = { screen = Screen.HOME; reward = null },
                                        )
                                    }
                                }
                                Screen.HISTORY -> WorkoutHistoryScreen(
                                    history = history, custom = state.customExercises,
                                    onBack = { screen = Screen.HOME },
                                )
                                Screen.EXERCISES -> ExercisesScreen(
                                    state = state,
                                    onBack = { screen = Screen.HOME },
                                    onToggle = { id, on ->
                                        scope.launch {
                                            val ids = if (on) state.selectedExercises + id else state.selectedExercises - id
                                            PlayerData.setSelectedExercises(context, ids)
                                        }
                                    },
                                    onSaveCustom = { ex ->
                                        scope.launch {
                                            PlayerData.saveCustomExercise(context, ex)
                                            PlayerData.setSelectedExercises(context, state.selectedExercises + ex.id)
                                        }
                                    },
                                    onArchive = { ex -> scope.launch { PlayerData.archiveCustomExercise(context, ex.id) } },
                                    onStartWorkout = { screen = Screen.WORKOUT },
                                )
                                Screen.SETTINGS -> SettingsScreen(
                                    state = state,
                                    haptics = haptics,
                                    onBack = { screen = Screen.HOME },
                                    onLanguageChange = { l -> AppPreferences.setLanguage(context, l); language = l },
                                    onHapticsChange = { h -> AppPreferences.setHapticsEnabled(context, h); haptics = h },
                                    onSaveRestDays = { days -> scope.launch { PlayerData.setRestDays(context, days) } },
                                    onResetProgress = {
                                        scope.launch {
                                            PlayerData.resetProgress(context)
                                            WorkoutHistoryData.clearHistory(context)
                                            sessionAchievements.clear()
                                            screen = Screen.HOME
                                        }
                                    },
                                )
                            }
                        }
                    }
                    if (screen.isTab) {
                        val today = dayKey(System.currentTimeMillis())
                        BottomNav(
                            current = screen,
                            trainedToday = history.any { dayKey(it.timestamp) == today },
                            shopBadge = state.loaded && state.dailyChestDay != today,
                            achievementsBadge = state.favoriteAchievements.isEmpty() && achievements.any { it.unlocked } && false,
                            onSelect = { screen = it; if (it != Screen.INVENTORY) slotFilter = null },
                            onWorkout = { screen = Screen.WORKOUT },
                        )
                    }
                }
                ToastHost(toasts)
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    val pal = LocalPalette.current
    val t = rememberInfiniteTransition(label = "load")
    val bob by t.animateFloat(0f, -8f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "b")
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        PixelIcon("sword", 64.dp, modifier = Modifier.graphicsLayer { translationY = bob * density })
        Spacer(Modifier.height(16.dp))
        TitleText("IRONQUEST", size = 18.sp, color = pal.accent)
        Spacer(Modifier.height(8.dp))
        PText(tr("Загрузка подземелья...", "Loading the dungeon..."), color = pal.muted)
    }
}

/** Иконка достижения → ключ для PixelIcon. */
fun achievementIcon(icon: String): String = when {
    icon.startsWith("ui:") -> icon.removePrefix("ui:")
    else -> icon
}

@Composable
private fun BottomNav(
    current: Screen,
    trainedToday: Boolean,
    shopBadge: Boolean,
    achievementsBadge: Boolean,
    onSelect: (Screen) -> Unit,
    onWorkout: () -> Unit,
) {
    val pal = LocalPalette.current
    val haptic = rememberHaptic()
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val p = 3.dp.toPx()
                drawRect(pal.border, Offset(0f, 0f), Size(size.width, size.height))
                drawRect(pal.panel, Offset(0f, p), Size(size.width, size.height - p))
                drawRect(lerp(pal.panel, Color.White, 0.14f), Offset(0f, p), Size(size.width, p))
            }
            .navigationBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            NavItem("castle", tr("ГЕРОЙ", "HERO"), current == Screen.HOME, Modifier.weight(1f)) { onSelect(Screen.HOME) }
            NavItem("backpack", tr("СУМКА", "BAG"), current == Screen.INVENTORY, Modifier.weight(1f)) { onSelect(Screen.INVENTORY) }
            Spacer(Modifier.weight(1.2f))
            NavItem("shop", tr("ЛАВКА", "SHOP"), current == Screen.SHOP, Modifier.weight(1f), badge = shopBadge) { onSelect(Screen.SHOP) }
            NavItem("trophy", tr("НАГРАДЫ", "AWARDS"), current == Screen.ACHIEVEMENTS, Modifier.weight(1f), badge = achievementsBadge) { onSelect(Screen.ACHIEVEMENTS) }
        }
        // центральная кнопка тренировки
        val pulse = rememberInfiniteTransition(label = "fight")
        val scale by pulse.animateFloat(1f, if (trainedToday) 1f else 1.07f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "s")
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clickable(role = Role.Button) { haptic(HapticFeedbackType.LongPress); onWorkout() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(70.dp)
                    .pixelFrame(pal.accent, lerp(pal.accent, Color.White, 0.4f), lerp(pal.accent, Color.Black, 0.4f), pal.border, 4.dp),
                contentAlignment = Alignment.Center,
            ) { PixelIcon("sword", 40.dp) }
            Spacer(Modifier.height(2.dp))
            TitleText(tr("В БОЙ", "FIGHT"), size = 8.sp, color = pal.accent, align = TextAlign.Center)
        }
    }
}

@Composable
private fun NavItem(icon: String, label: String, selected: Boolean, modifier: Modifier, badge: Boolean = false, onClick: () -> Unit) {
    val pal = LocalPalette.current
    val haptic = rememberHaptic()
    val lift by androidx.compose.animation.core.animateDpAsState(if (selected) (-4).dp else 0.dp, label = "lift")
    Column(
        modifier
            .height(64.dp)
            .clickable(role = Role.Tab) { haptic(HapticFeedbackType.TextHandleMove); onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.offset(y = lift)) {
            PixelIcon(icon, 28.dp, disabled = !selected, alpha = if (selected) 1f else 0.9f)
            if (badge) Box(Modifier.align(Alignment.TopEnd).size(8.dp).drawBehind { drawRect(pal.danger) })
        }
        Spacer(Modifier.height(4.dp))
        PText(label, size = 12.sp, bold = true, color = if (selected) pal.accent else pal.muted, maxLines = 1)
    }
}

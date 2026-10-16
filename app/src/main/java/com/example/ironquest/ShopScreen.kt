package com.example.ironquest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.random.Random

private enum class ShopTab { CHESTS, GOODS, UPGRADES, THEMES }

private data class Opening(val case: ShopCase, val item: ItemDefinition, val free: Boolean, val id: Long = System.nanoTime())

@Composable
fun ShopScreen(state: PlayerState, onToast: (String, String, String) -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf(ShopTab.CHESTS) }
    var opening by remember { mutableStateOf<Opening?>(null) }
    var showRest by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val today = dayKey(System.currentTimeMillis())

    fun open(case: ShopCase, free: Boolean) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                when (val r = PlayerData.openCase(context, case, free)) {
                    is CaseOpenResult.Success -> opening = Opening(case, r.item, free)
                    else -> onToast("coin", tr(lang, "НЕ ХВАТАЕТ МОНЕТ", "NOT ENOUGH COINS"), tr(lang, "Тренируйся и продавай дубликаты", "Train and sell duplicates"))
                }
            } finally {
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        PixelTopBar(tr("ЛАВКА", "SHOP"), "shop") { CoinCounter(state.coins) }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixelChip(tr("Сундуки", "Chests"), tab == ShopTab.CHESTS, { tab = ShopTab.CHESTS }, icon = "chest")
            PixelChip(tr("Товары", "Goods"), tab == ShopTab.GOODS, { tab = ShopTab.GOODS }, icon = "potion")
            PixelChip(tr("Улучшения", "Upgrades"), tab == ShopTab.UPGRADES, { tab = ShopTab.UPGRADES }, icon = "moon")
            PixelChip(tr("Темы", "Themes"), tab == ShopTab.THEMES, { tab = ShopTab.THEMES }, icon = "palette")
        }
        AnimatedContent(tab, transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) }, label = "tab", modifier = Modifier.weight(1f)) { t ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (t) {
                    ShopTab.CHESTS -> {
                        DailyChestCard(available = state.dailyChestDay != today, enabled = !busy) { open(ShopCase.BASIC, true) }
                        ShopCase.values().forEach { c -> CaseCard(c, state.coins, enabled = !busy) { open(c, false) } }
                    }
                    ShopTab.GOODS -> {
                        val (deal, price) = remember(today) { dailyDeal(today) }
                        DailyDealCard(deal, price, bought = state.dailyDealDay == today, coins = state.coins) {
                            scope.launch {
                                if (PlayerData.buyDailyDeal(context, deal, price)) onToast(deal.iconKey(), tr(lang, "КУПЛЕНО", "BOUGHT"), deal.name(lang))
                            }
                        }
                        Consumable.values().forEach { c ->
                            ConsumableCard(c, state.count(c), state.coins) {
                                scope.launch {
                                    if (PlayerData.buyConsumable(context, c)) onToast(c.icon, tr(lang, "КУПЛЕНО", "BOUGHT"), c.title.get(lang))
                                }
                            }
                        }
                    }
                    ShopTab.UPGRADES -> {
                        RestDaysUpgradeCard(state, onBuy = {
                            scope.launch {
                                if (PlayerData.buyRestDay(context)) onToast("moon", tr(lang, "НОВЫЙ ВЫХОДНОЙ!", "NEW REST DAY!"), tr(lang, "Выбери день в настройке выходных", "Pick the day in rest settings"))
                            }
                        }, onConfigure = { showRest = true })
                        InfoCard("snowflake", tr("Заморозка серии", "Streak freeze"),
                            tr("Если ты пропустишь рабочий день, заморозка сработает при следующем запуске и серия не сгорит. Купить — во вкладке «Товары».",
                                "If you miss a training day, a freeze is used on next launch and your streak survives. Buy in the Goods tab."))
                    }
                    ShopTab.THEMES -> {
                        AppTheme.values().forEach { theme ->
                            ThemeCard(theme, owned = theme.name in state.ownedThemes, active = state.activeTheme == theme, coins = state.coins,
                                onBuy = { scope.launch { if (PlayerData.buyTheme(context, theme)) onToast("palette", tr(lang, "ТЕМА КУПЛЕНА", "THEME BOUGHT"), theme.title.get(lang)) } },
                                onApply = { scope.launch { PlayerData.applyTheme(context, theme) } })
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showRest) RestDaysDialog(state, onDismiss = { showRest = false }, onSave = { days -> scope.launch { PlayerData.setRestDays(context, days) }; showRest = false })

    opening?.let { o ->
        CaseOpeningOverlay(
            opening = o,
            coins = state.coins,
            onClose = { opening = null },
            onAgain = { opening = null; open(o.case, false) },
        )
    }
}

/* ------------------------------------------------------------------ карточки */

@Composable
private fun OddsBar(odds: IntArray) {
    val pal = LocalPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
            .drawBehind {
                val p = 2.dp.toPx()
                stepRect(pal.border, 0f, 0f, size.width, size.height, p)
                var x = p
                val w = size.width - 2 * p
                ItemRarity.values().forEachIndexed { i, r ->
                    val seg = w * odds[i] / 100f
                    if (seg > 0) drawRect(r.color, Offset(x, p), Size(seg, size.height - 2 * p))
                    x += seg
                }
            }
    )
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ItemRarity.values().forEachIndexed { i, r ->
            if (odds[i] > 0) Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(r.color))
                Spacer(Modifier.width(3.dp))
                PText("${odds[i]}%", size = 12.sp, color = pal.muted)
            }
        }
    }
}

@Composable
private fun CaseCard(case: ShopCase, coins: Int, enabled: Boolean, onOpen: () -> Unit) {
    val pal = LocalPalette.current
    val canBuy = coins >= case.price
    val t = rememberInfiniteTransition(label = "case")
    val bob by t.animateFloat(0f, -4f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "bob")
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).insetFrame(), contentAlignment = Alignment.Center) {
                PixelIcon("chest:${case.material}", 56.dp, modifier = Modifier.graphicsLayer { translationY = bob * density })
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleText(case.title.text(), size = 10.sp)
                Spacer(Modifier.height(4.dp))
                PText(case.desc.text(), color = pal.muted, size = 14.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        OddsBar(case.odds)
        Spacer(Modifier.height(10.dp))
        PixelButton(tr("ОТКРЫТЬ", "OPEN"), onOpen, Modifier.fillMaxWidth(), enabled = enabled && canBuy, height = 50.dp) {
            TitleText(tr("ОТКРЫТЬ", "OPEN"), color = if (canBuy) pal.onAccent else pal.muted, size = 10.sp, shadow = false)
            Spacer(Modifier.width(10.dp))
            PixelIcon("coin", 18.dp)
            Spacer(Modifier.width(4.dp))
            TitleText("${case.price}", color = if (canBuy) pal.onAccent else pal.muted, size = 10.sp, shadow = false)
        }
    }
}

@Composable
private fun DailyChestCard(available: Boolean, enabled: Boolean, onOpen: () -> Unit) {
    val pal = LocalPalette.current
    val now = Calendar.getInstance()
    val minsLeft = (24 * 60) - (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE))
    PixelPanel(Modifier.fillMaxWidth(), fill = if (available) lerp(pal.panel, pal.accent, 0.12f) else pal.panel) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon("gift", 52.dp, disabled = !available)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleText(tr("ДАР ДНЯ", "DAILY GIFT"), size = 10.sp, color = pal.accent)
                Spacer(Modifier.height(4.dp))
                PText(
                    if (available) tr("Бесплатный сундук новичка раз в день!", "A free rookie chest every day!")
                    else tr("Следующий через ${minsLeft / 60} ч ${minsLeft % 60} мин", "Next in ${minsLeft / 60}h ${minsLeft % 60}m"),
                    color = pal.muted, size = 14.sp,
                )
            }
        }
        if (available) {
            Spacer(Modifier.height(10.dp))
            PixelButton(tr("ЗАБРАТЬ", "CLAIM"), onOpen, Modifier.fillMaxWidth(), enabled = enabled, icon = "gift", height = 50.dp)
        }
    }
}

@Composable
private fun DailyDealCard(item: ItemDefinition, price: Int, bought: Boolean, coins: Int, onBuy: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    PixelPanel(Modifier.fillMaxWidth(), fill = lerp(pal.panel, item.rarity.color, 0.1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(68.dp).pixelFrame(pal.panelLo, item.rarity.color, lerp(item.rarity.color, Color.Black, 0.5f), pal.border, 3.dp),
                contentAlignment = Alignment.Center,
            ) { ItemIcon(item, 50.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TitleText(tr("ТОВАР ДНЯ", "DEAL OF THE DAY"), size = 8.sp, color = pal.gold, modifier = Modifier.weight(1f))
                    Tag("-40%", pal.danger, textColor = Color.White)
                }
                Spacer(Modifier.height(4.dp))
                PText(item.name(lang), bold = true)
                PText(item.rarity.label(lang) + (item.slot?.let { " · " + it.title.get(lang) } ?: ""), color = item.rarity.color, size = 13.sp)
                if (item.stats.isNotEmpty()) PText(item.stats.entries.joinToString("  ") { "+${it.value} ${it.key.short.get(lang)}" }, color = pal.accent2, size = 13.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        PixelButton(if (bought) tr("КУПЛЕНО СЕГОДНЯ", "BOUGHT TODAY") else tr("КУПИТЬ", "BUY"), onBuy, Modifier.fillMaxWidth(),
            enabled = !bought && coins >= price, height = 50.dp) {
            if (bought) TitleText(tr("КУПЛЕНО", "BOUGHT"), color = pal.muted, size = 10.sp, shadow = false)
            else {
                TitleText(tr("КУПИТЬ", "BUY"), color = if (coins >= price) pal.onAccent else pal.muted, size = 10.sp, shadow = false)
                Spacer(Modifier.width(10.dp)); PixelIcon("coin", 18.dp); Spacer(Modifier.width(4.dp))
                TitleText("$price", color = if (coins >= price) pal.onAccent else pal.muted, size = 10.sp, shadow = false)
            }
        }
    }
}

@Composable
private fun ConsumableCard(c: Consumable, owned: Int, coins: Int, onBuy: () -> Unit) {
    val pal = LocalPalette.current
    val full = owned >= c.maxStack
    val can = coins >= c.price && !full
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(60.dp).insetFrame(), contentAlignment = Alignment.Center) { PixelIcon(c.icon, 42.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleText(c.title.text().uppercase(), size = 9.sp)
                Spacer(Modifier.height(4.dp))
                PText(c.desc.text(), color = pal.muted, size = 14.sp)
                Spacer(Modifier.height(2.dp))
                PText(tr("В запасе: $owned / ${c.maxStack}", "Owned: $owned / ${c.maxStack}"), color = pal.accent2, size = 13.sp, bold = true)
            }
        }
        Spacer(Modifier.height(10.dp))
        PixelButton("", onBuy, Modifier.fillMaxWidth(), enabled = can, height = 48.dp) {
            if (full) TitleText(tr("МАКСИМУМ", "MAXED"), color = pal.muted, size = 9.sp, shadow = false)
            else {
                TitleText(tr("КУПИТЬ", "BUY"), color = if (can) pal.onAccent else pal.muted, size = 9.sp, shadow = false)
                Spacer(Modifier.width(10.dp)); PixelIcon("coin", 18.dp); Spacer(Modifier.width(4.dp))
                TitleText("${c.price}", color = if (can) pal.onAccent else pal.muted, size = 9.sp, shadow = false)
            }
        }
    }
}

@Composable
private fun RestDaysUpgradeCard(state: PlayerState, onBuy: () -> Unit, onConfigure: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val price = REST_DAY_PRICES[state.restDaysUnlocked]
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon("moon", 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleText(tr("ВЫХОДНЫЕ ДНИ", "REST DAYS"), size = 10.sp)
                Spacer(Modifier.height(4.dp))
                PText(tr("В выходной серия не сгорает. Открыто ${state.restDaysUnlocked} из $MAX_REST_DAYS в неделю.",
                    "Your streak won't break on a rest day. Unlocked ${state.restDaysUnlocked} of $MAX_REST_DAYS per week."), color = pal.muted, size = 14.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(MAX_REST_DAYS) { i ->
                val on = i < state.restDaysUnlocked
                Box(
                    Modifier.weight(1f).height(44.dp).then(if (on) Modifier.panelFrame(pal.panelHi) else Modifier.insetFrame()),
                    contentAlignment = Alignment.Center,
                ) { if (on) PixelIcon("moon", 24.dp) else PixelIcon("lock", 20.dp, alpha = 0.7f) }
            }
        }
        Spacer(Modifier.height(8.dp))
        PText(tr("Сейчас: ", "Current: ") + if (state.restDays.isEmpty()) tr("не выбраны", "none") else WEEK_ORDER.filter { it in state.restDays }.joinToString(", ") { weekdayShort(it, lang) },
            color = pal.accent2, size = 14.sp, bold = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton(tr("НАСТРОИТЬ", "CONFIGURE"), onConfigure, Modifier.weight(1f), color = pal.panelHi, textColor = pal.text, fontSize = 8.sp, height = 48.dp, icon = "calendar")
            if (price != null) {
                PixelButton("", onBuy, Modifier.weight(1f), enabled = state.coins >= price, height = 48.dp) {
                    TitleText("+1", color = if (state.coins >= price) pal.onAccent else pal.muted, size = 10.sp, shadow = false)
                    Spacer(Modifier.width(8.dp)); PixelIcon("coin", 18.dp); Spacer(Modifier.width(4.dp))
                    TitleText("$price", color = if (state.coins >= price) pal.onAccent else pal.muted, size = 10.sp, shadow = false)
                }
            } else {
                Box(Modifier.weight(1f).height(48.dp).insetFrame(), contentAlignment = Alignment.Center) {
                    PText(tr("Все открыты", "All unlocked"), color = pal.good, bold = true)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(icon: String, title: String, text: String) {
    val pal = LocalPalette.current
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon(icon, 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleText(title.uppercase(), size = 9.sp)
                Spacer(Modifier.height(4.dp))
                PText(text, color = pal.muted, size = 14.sp)
            }
        }
    }
}

@Composable
private fun ThemeCard(theme: AppTheme, owned: Boolean, active: Boolean, coins: Int, onBuy: () -> Unit, onApply: () -> Unit) {
    val pal = LocalPalette.current
    val tp = theme.palette
    Column(Modifier.fillMaxWidth().pixelFrame(tp.panel, tp.panelHi, tp.panelLo, pal.border).padding(12.dp)) {
        Box(Modifier.fillMaxWidth().height(86.dp).insetFrame(tp.panelLo).padding(2.dp)) {
            HeroScenery(tp, Modifier.fillMaxSize(), cell = 3.dp)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                TitleText(theme.title.text().uppercase(), size = 10.sp, color = tp.text)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(tp.bg, tp.panel, tp.accent, tp.accent2, tp.gold).forEach { c ->
                        Box(Modifier.size(18.dp).pixelFrame(c, lerp(c, Color.White, 0.3f), lerp(c, Color.Black, 0.3f), pal.border, 2.dp))
                    }
                }
            }
            when {
                active -> Tag(tr("АКТИВНА", "ACTIVE"), tp.accent, textColor = tp.onAccent)
                owned -> PixelButton(tr("ВКЛ", "USE"), onApply, Modifier.width(110.dp), color = tp.accent, textColor = tp.onAccent, height = 46.dp, fontSize = 9.sp)
                else -> PixelButton("", onBuy, Modifier.width(130.dp), color = tp.accent, textColor = tp.onAccent, enabled = coins >= theme.price, height = 46.dp) {
                    PixelIcon("coin", 18.dp); Spacer(Modifier.width(6.dp))
                    TitleText("${theme.price}", color = if (coins >= theme.price) tp.onAccent else pal.muted, size = 10.sp, shadow = false)
                }
            }
        }
    }
}

/* ------------------------------------------------------------- открытие сундука */

private const val REEL_SIZE = 48
private const val WIN_INDEX = 41

/**
 * Рулетка как в CS: лента предметов быстро прокручивается и замедляется на выпавшем предмете.
 * Предмет уже сохранён до начала анимации — закрытие окна ничего не ломает.
 */
@Composable
private fun CaseOpeningOverlay(opening: Opening, coins: Int, onClose: () -> Unit, onAgain: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val haptic = rememberHaptic()
    val density = LocalDensity.current
    val reel = remember(opening.id) {
        val rnd = Random(opening.id)
        List(REEL_SIZE) { i -> if (i == WIN_INDEX) opening.item else randomItemOf(opening.case.rollRarity(rnd), rnd) }
    }
    val tileDp = 88.dp
    val gapDp = 8.dp
    val stepPx = with(density) { (tileDp + gapDp).toPx() }
    val offset = remember(opening.id) { Animatable(0f) }
    var revealed by remember(opening.id) { mutableStateOf(false) }
    val jitter = remember(opening.id) { (Random(opening.id).nextFloat() - 0.5f) * 0.7f }
    var containerWidth by remember { mutableIntStateOf(0) }

    LaunchedEffect(opening.id, containerWidth) {
        if (containerWidth == 0) return@LaunchedEffect
        val target = WIN_INDEX * stepPx + stepPx / 2 + jitter * (stepPx - 20) - containerWidth / 2f
        offset.animateTo(target, tween(5200, easing = CubicBezierEasing(0.08f, 0.6f, 0.12f, 1f)))
        haptic(HapticFeedbackType.LongPress)
        revealed = true
    }
    // «щелчки» при прохождении каждого предмета через центр
    LaunchedEffect(opening.id, containerWidth) {
        var lastIndex = -1
        snapshotFlow { ((offset.value + containerWidth / 2f) / stepPx).toInt() }.collect { idx ->
            if (idx != lastIndex && !revealed) {
                lastIndex = idx
                haptic(HapticFeedbackType.TextHandleMove)
            }
        }
    }
    BackHandler { if (revealed) onClose() }

    Dialog(onDismissRequest = { if (revealed) onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).statusBarsPadding().navigationBarsPadding(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp).panelFrame().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TitleText(opening.case.title.text(), size = 11.sp, color = pal.accent, align = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                AnimatedContent(revealed, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "reveal") { done ->
                    if (!done) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BoxWithConstraints(
                                Modifier
                                    .fillMaxWidth()
                                    .height(tileDp + 28.dp)
                                    .clipToBounds()
                                    .drawBehind { drawRect(pal.panelLo) }
                            ) {
                                val w = with(density) { maxWidth.roundToPx() }
                                LaunchedEffect(w) { containerWidth = w }
                                Row(
                                    Modifier
                                        .wrapContentWidth(Alignment.Start, unbounded = true)
                                        .requiredWidth((tileDp + gapDp) * REEL_SIZE)
                                        .align(Alignment.CenterStart)
                                        .offset { IntOffset(-offset.value.roundToInt(), 0) }
                                        .padding(vertical = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(gapDp),
                                ) {
                                    reel.forEach { item -> ReelTile(item, tileDp) }
                                }
                                // центральный указатель
                                Box(Modifier.align(Alignment.Center).width(4.dp).fillMaxHeight().background(pal.gold))
                                Box(Modifier.align(Alignment.TopCenter).size(14.dp).drawBehind {
                                    val p = size.width / 7
                                    for (r in 0 until 4) drawRect(pal.gold, Offset(r * p, r * p), Size(size.width - 2 * r * p, p))
                                })
                                // затемнение краёв
                                Box(Modifier.align(Alignment.CenterStart).width(40.dp).fillMaxHeight().background(pal.panelLo.copy(alpha = 0.6f)))
                                Box(Modifier.align(Alignment.CenterEnd).width(40.dp).fillMaxHeight().background(pal.panelLo.copy(alpha = 0.6f)))
                            }
                            Spacer(Modifier.height(14.dp))
                            PText(tr("Крутим ленту удачи...", "Spinning the reel of fortune..."), color = pal.muted)
                            Spacer(Modifier.height(10.dp))
                            PText(tr("ПРОПУСТИТЬ", "SKIP"), color = pal.accent2, bold = true, modifier = Modifier.clickable {
                                revealed = true
                            }.padding(12.dp))
                        }
                    } else {
                        RevealPanel(opening, coins, onClose, onAgain)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReelTile(item: ItemDefinition, size: androidx.compose.ui.unit.Dp) {
    val pal = LocalPalette.current
    Box(
        Modifier
            .size(size)
            .pixelFrame(lerp(pal.panel, item.rarity.color, 0.15f), item.rarity.color, lerp(item.rarity.color, Color.Black, 0.5f), pal.border, 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        ItemIcon(item, size * 0.66f)
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(6.dp).padding(horizontal = 3.dp).background(item.rarity.color))
    }
}

@Composable
private fun RevealPanel(opening: Opening, coins: Int, onClose: () -> Unit, onAgain: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val item = opening.item
    val pop = remember { Animatable(0.2f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 260f)) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            RaysBackground(item.rarity.color.copy(alpha = 0.45f), Modifier.fillMaxSize())
            Box(
                Modifier
                    .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                    .size(120.dp)
                    .pixelFrame(pal.panelLo, item.rarity.color, lerp(item.rarity.color, Color.Black, 0.5f), pal.border, 4.dp),
                contentAlignment = Alignment.Center,
            ) { ItemIcon(item, 92.dp) }
        }
        TitleText(item.rarity.label(lang), color = item.rarity.color, size = 11.sp, align = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        PText(item.name(lang), size = 20.sp, bold = true, align = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        PText(item.description(lang), color = pal.muted, align = TextAlign.Center, size = 14.sp)
        if (item.stats.isNotEmpty() || !item.perks.isEmpty) {
            Spacer(Modifier.height(6.dp))
            PText(itemBonusText(item, lang), color = pal.accent2, align = TextAlign.Center, size = 14.sp, bold = true)
        }
        gearSetById(item.setId)?.let { set ->
            Spacer(Modifier.height(4.dp))
            PText(tr("Часть сета «${set.title.ru}»", "Part of the ${set.title.en} set"), color = pal.gold, size = 14.sp)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton(tr("ЗАБРАТЬ", "TAKE"), onClose, Modifier.weight(1f), height = 50.dp, fontSize = 9.sp, icon = "check")
            if (!opening.free) {
                val can = coins >= opening.case.price
                PixelButton("", onAgain, Modifier.weight(1f), color = pal.panelHi, textColor = pal.text, enabled = can, height = 50.dp) {
                    TitleText(tr("ЕЩЁ", "AGAIN"), color = if (can) pal.text else pal.muted, size = 9.sp, shadow = false)
                    Spacer(Modifier.width(6.dp)); PixelIcon("coin", 16.dp); Spacer(Modifier.width(2.dp))
                    TitleText("${opening.case.price}", color = if (can) pal.text else pal.muted, size = 9.sp, shadow = false)
                }
            }
        }
    }
}

fun itemBonusText(item: ItemDefinition, lang: AppLanguage): String {
    val parts = mutableListOf<String>()
    item.stats.forEach { (s, v) -> parts += "+$v ${s.short.get(lang)}" }
    if (item.perks.xp > 0) parts += "+${item.perks.xp}% XP"
    if (item.perks.coins > 0) parts += "+${item.perks.coins}% " + tr(lang, "монет", "coins")
    if (item.perks.luck > 0) parts += "+${item.perks.luck}% " + tr(lang, "удачи", "luck")
    return parts.joinToString("  ")
}

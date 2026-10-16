package com.example.ironquest

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class AchTab { PROGRESS, ALL, DONE, SETS }

@Composable
fun AchievementsScreen(
    state: PlayerState,
    achievements: List<Achievement>,
    onToggleFavorite: (Achievement) -> Unit,
    onEquipSet: (GearSet) -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var tab by rememberSaveable { mutableStateOf(AchTab.PROGRESS) }
    var category by rememberSaveable { mutableStateOf<AchievementCategory?>(null) }
    val unlocked = achievements.count { it.unlocked }
    val list = achievements
        .filter { it.category != AchievementCategory.SET }
        .filter { category == null || it.category == category }
        .let { l ->
            when (tab) {
                AchTab.PROGRESS -> l.filter { !it.unlocked }.sortedByDescending { it.fraction }
                AchTab.DONE -> l.filter { it.unlocked }
                else -> l.sortedWith(compareBy<Achievement> { it.unlocked }.thenByDescending { it.fraction })
            }
        }

    Column(Modifier.fillMaxSize()) {
        PixelTopBar(tr("НАГРАДЫ", "AWARDS"), "trophy") {
            Row(Modifier.insetFrame().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                TitleText("$unlocked/${achievements.size}", size = 10.sp, color = pal.gold, shadow = false)
            }
        }
        PixelBar(unlocked / achievements.size.toFloat(), Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = pal.gold, height = 12.dp, segments = 20)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PixelChip(tr("В процессе", "In progress"), tab == AchTab.PROGRESS, { tab = AchTab.PROGRESS }, icon = "hourglass")
            PixelChip(tr("Открытые", "Unlocked"), tab == AchTab.DONE, { tab = AchTab.DONE }, icon = "check")
            PixelChip(tr("Все", "All"), tab == AchTab.ALL, { tab = AchTab.ALL })
            PixelChip(tr("Сеты", "Sets"), tab == AchTab.SETS, { tab = AchTab.SETS }, icon = "shield")
        }
        if (tab != AchTab.SETS) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PixelChip(tr("Любые", "Any"), category == null, { category = null }, color = pal.accent2)
                AchievementCategory.values().filter { it != AchievementCategory.SET }.forEach { c ->
                    PixelChip(c.title.text(), category == c, { category = c }, color = pal.accent2)
                }
            }
        }
        PText(tr("★ Отметь до 3 любимых — они появятся рядом с героем", "★ Star up to 3 favorites — they appear next to your hero"),
            color = pal.muted, size = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        if (tab == AchTab.SETS) {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(GEAR_SETS, key = { it.id }) { set -> SetCard(set, state, onEquipSet) }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (list.isEmpty()) item {
                    EmptyState("trophy", tr("ПУСТО", "EMPTY"),
                        if (tab == AchTab.DONE) tr("Пока ничего не открыто — вперёд, к первой тренировке!", "Nothing yet — go do your first workout!")
                        else tr("Всё открыто в этой категории!", "Everything unlocked in this category!"))
                }
                items(list, key = { it.id }) { a ->
                    AchievementCard(a, a.id in state.favoriteAchievements, Modifier.animateItem()) { onToggleFavorite(a) }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(a: Achievement, favorite: Boolean, modifier: Modifier = Modifier, onFavorite: () -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val glow = rememberInfiniteTransition(label = "glow")
    val g by glow.animateFloat(0f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "g")
    val border = if (a.unlocked) lerp(pal.gold, pal.gold.lighter(0.4f), g) else lerp(pal.panel, Color.White, 0.12f)
    Row(
        modifier
            .fillMaxWidth()
            .pixelFrame(if (a.unlocked) lerp(pal.panel, pal.gold, 0.08f) else pal.panel, border, lerp(pal.panel, Color.Black, 0.35f), pal.border)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(54.dp).insetFrame(), contentAlignment = Alignment.Center) {
            PixelIcon(achievementIcon(a.icon), 38.dp, disabled = !a.unlocked)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            PText(a.title(lang), bold = true, size = 16.sp, color = if (a.unlocked) pal.gold else pal.text, maxLines = 1)
            PText(a.description(lang), color = pal.muted, size = 14.sp, maxLines = 2)
            Spacer(Modifier.height(6.dp))
            if (!a.unlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelBar(a.fraction, Modifier.weight(1f), color = pal.accent, height = 10.dp, segments = 0)
                    Spacer(Modifier.width(8.dp))
                    PText("${a.progress}/${a.target}", size = 13.sp, bold = true, color = pal.text)
                }
                Spacer(Modifier.height(4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PText(if (a.unlocked) tr("ПОЛУЧЕНО: ", "EARNED: ") else tr("НАГРАДА: ", "REWARD: "), size = 12.sp, color = pal.muted, bold = true)
                PText("+${a.rewardXp} XP", size = 13.sp, color = pal.accent, bold = true)
                if (a.rewardCoins > 0) {
                    Spacer(Modifier.width(6.dp))
                    PixelIcon("coin", 12.dp)
                    PText("${a.rewardCoins}", size = 13.sp, color = pal.gold, bold = true)
                }
            }
        }
        Box(
            Modifier.size(44.dp).clickable(role = Role.Checkbox) { onFavorite() },
            contentAlignment = Alignment.Center,
        ) { PixelIcon("star", 24.dp, disabled = !favorite, alpha = if (favorite) 1f else 0.7f) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetCard(set: GearSet, state: PlayerState, onEquip: (GearSet) -> Unit) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val pieces = set.pieces
    val owned = pieces.count { (state.items[it.id] ?: 0) > 0 }
    val complete = owned == pieces.size
    val wearing = pieces.all { state.equippedIds[it.slot] == it.id }
    val top = pieces.maxOfOrNull { it.rarity.ordinal }?.let { ItemRarity.values()[it] } ?: ItemRarity.COMMON
    PixelPanel(Modifier.fillMaxWidth(), fill = if (complete) lerp(pal.panel, top.color, 0.1f) else pal.panel) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                TitleText(set.title.text().uppercase(), size = 10.sp, color = top.color)
                Spacer(Modifier.height(4.dp))
                PText(tr("Собрано $owned из ${pieces.size}", "Collected $owned of ${pieces.size}"), color = pal.muted, size = 14.sp)
            }
            if (wearing) Tag(tr("НАДЕТ", "WORN"), pal.accent, textColor = pal.onAccent)
        }
        Spacer(Modifier.height(8.dp))
        PixelBar(owned / pieces.size.toFloat(), Modifier.fillMaxWidth(), color = top.color, height = 10.dp, segments = pieces.size)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            pieces.forEach { p ->
                val have = (state.items[p.id] ?: 0) > 0
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(62.dp)) {
                    Box(
                        Modifier.size(52.dp).pixelFrame(pal.panelLo, if (have) p.rarity.color else pal.panelHi, pal.panelLo.darker(0.3f), pal.border, 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (have) ItemIcon(p, 38.dp) else PixelIcon(p.iconKey(), 38.dp, modifier = Modifier.graphicsLayer { alpha = 0.25f }, disabled = true)
                    }
                    PText(p.name(lang), size = 11.sp, color = if (have) pal.text else pal.muted, maxLines = 2)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon("sparkle", 16.dp)
            Spacer(Modifier.width(6.dp))
            PText(tr("Бонус сета: ", "Set bonus: ") + setBonusText(set, lang), color = pal.accent2, size = 14.sp)
        }
        if (complete && !wearing) {
            Spacer(Modifier.height(10.dp))
            PixelButton(tr("НАДЕТЬ СЕТ", "EQUIP SET"), { onEquip(set) }, Modifier.fillMaxWidth(), icon = "shield", height = 48.dp, fontSize = 9.sp)
        }
    }
}

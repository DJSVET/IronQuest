package com.example.ironquest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class InvFilter { ALL, GEAR, LOOT }
private enum class InvSort { RARITY, NAME, COUNT }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    state: PlayerState,
    stats: HeroStats,
    slotFilter: ItemSlot?,
    onSlotFilter: (ItemSlot?) -> Unit,
    onEquip: (ItemDefinition) -> Unit,
    onUnequip: (ItemDefinition) -> Unit,
    onToggleLock: (ItemDefinition) -> Unit,
    onSell: (ItemDefinition, Int) -> Unit,
    onSellDuplicates: () -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    var filter by rememberSaveable { mutableStateOf(InvFilter.ALL) }
    var sort by rememberSaveable { mutableStateOf(InvSort.RARITY) }
    var selected by remember { mutableStateOf<String?>(null) }
    var confirmDupes by remember { mutableStateOf(false) }
    val inventory = state.inventory
    val equippedIds = state.equippedIds.values.toSet()
    val dupesValue = inventory.filter { !it.locked && it.count > 1 }.sumOf { (it.count - 1) * it.item.sellPrice }

    val shown = inventory
        .filter { e ->
            when {
                slotFilter != null -> e.item.slot == slotFilter
                filter == InvFilter.GEAR -> e.item.isEquipment
                filter == InvFilter.LOOT -> !e.item.isEquipment
                else -> true
            }
        }
        .let { list ->
            when (sort) {
                InvSort.RARITY -> list.sortedWith(compareByDescending<InventoryEntry> { it.item.rarity.ordinal }.thenBy { it.item.name(lang) })
                InvSort.NAME -> list.sortedBy { it.item.name(lang) }
                InvSort.COUNT -> list.sortedByDescending { it.count }
            }
        }

    Column(Modifier.fillMaxSize()) {
        PixelTopBar(tr("СУМКА", "BAG"), "backpack") { CoinCounter(state.coins) }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(76.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    // экипировка
                    PixelPanel(Modifier.fillMaxWidth(), padding = PaddingValues(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TitleText(tr("ЭКИПИРОВКА", "EQUIPMENT"), size = 9.sp, color = pal.muted, modifier = Modifier.weight(1f))
                            PixelIcon("bolt", 16.dp)
                            Spacer(Modifier.width(4.dp))
                            PText("${stats.power}", color = pal.gold, bold = true)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            ItemSlot.values().forEach { slot ->
                                val item = state.equipped[slot]
                                val active = slotFilter == slot
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable(role = Role.Tab) { onSlotFilter(if (active) null else slot) }
                                        .pixelFrame(
                                            if (active) lerp(pal.panelLo, pal.accent, 0.3f) else pal.panelLo,
                                            item?.rarity?.color ?: pal.panelHi,
                                            lerp(item?.rarity?.color ?: pal.panel, Color.Black, 0.5f),
                                            if (active) pal.accent else pal.border, 2.dp,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (item != null) ItemIcon(item, 30.dp) else PixelIcon("item:${slot.emptyIcon}:iron", 24.dp, disabled = true, alpha = 0.6f)
                                }
                            }
                        }
                        if (stats.activeSets.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            stats.activeSets.forEach { set ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PixelIcon("sparkle", 16.dp)
                                    Spacer(Modifier.width(6.dp))
                                    PText(tr("Сет «${set.title.ru}» активен", "${set.title.en} set active"), color = pal.gold, size = 14.sp, bold = true)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // фильтры
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (slotFilter != null) {
                            PixelChip(slotFilter.title.text() + " ✕", true, { onSlotFilter(null) }, icon = "item:${slotFilter.emptyIcon}:iron")
                        } else {
                            PixelChip(tr("Всё", "All"), filter == InvFilter.ALL, { filter = InvFilter.ALL })
                            PixelChip(tr("Экипировка", "Gear"), filter == InvFilter.GEAR, { filter = InvFilter.GEAR }, icon = "shield")
                            PixelChip(tr("Коллекция", "Loot"), filter == InvFilter.LOOT, { filter = InvFilter.LOOT }, icon = "item:coll_gem:royal")
                        }
                        PixelChip(
                            when (sort) { InvSort.RARITY -> tr("↓ Редкость", "↓ Rarity"); InvSort.NAME -> tr("↓ Имя", "↓ Name"); InvSort.COUNT -> tr("↓ Кол-во", "↓ Count") },
                            false, { sort = InvSort.values()[(sort.ordinal + 1) % InvSort.values().size] },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PText(tr("Предметов: ${inventory.sumOf { it.count }} · уникальных ${inventory.size}/${ITEM_CATALOG.size}",
                            "Items: ${inventory.sumOf { it.count }} · unique ${inventory.size}/${ITEM_CATALOG.size}"), color = pal.muted, size = 14.sp, modifier = Modifier.weight(1f))
                        if (dupesValue > 0) {
                            PText(tr("Продать дубли", "Sell dupes") + " +$dupesValue", color = pal.gold, size = 14.sp, bold = true,
                                modifier = Modifier.clickable { confirmDupes = true }.padding(6.dp))
                        }
                    }
                }
            }
            if (shown.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState("backpack", tr("ПУСТО", "EMPTY"),
                        if (slotFilter != null) tr("Для этого слота пока нет предметов. Тренируйся или загляни в лавку!", "No items for this slot yet. Train or visit the shop!")
                        else tr("Завершай тренировки — после них выпадает добыча.", "Finish workouts — loot drops after them."))
                }
            }
            items(shown, key = { it.item.id }) { e ->
                ItemCell(e, equipped = e.item.id in equippedIds, modifier = Modifier.animateItem()) { selected = e.item.id }
            }
        }
    }

    selected?.let { id ->
        val entry = inventory.firstOrNull { it.item.id == id }
        if (entry == null) LaunchedEffect(id) { selected = null }
        else ItemDetailDialog(
            entry = entry,
            equipped = entry.item.id in equippedIds,
            state = state,
            onDismiss = { selected = null },
            onEquip = { onEquip(entry.item) },
            onUnequip = { onUnequip(entry.item) },
            onToggleLock = { onToggleLock(entry.item) },
            onSell = { n -> onSell(entry.item, n) },
        )
    }
    if (confirmDupes) {
        ConfirmDialog(
            tr("ПРОДАТЬ ДУБЛИКАТЫ?", "SELL DUPLICATES?"),
            tr("Останется по одной копии каждого предмета. Закреплённые не продаются. Получишь $dupesValue монет.",
                "One copy of each item stays. Locked items are kept. You'll get $dupesValue coins."),
            tr("ПРОДАТЬ", "SELL"), onConfirm = { confirmDupes = false; onSellDuplicates() }, onDismiss = { confirmDupes = false }, icon = "coin",
        )
    }
}

@Composable
private fun ItemCell(entry: InventoryEntry, equipped: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val pal = LocalPalette.current
    val item = entry.item
    val haptic = rememberHaptic()
    Box(
        modifier
            .aspectRatio(1f)
            .clickable(role = Role.Button) { haptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onClick() }
            .pixelFrame(lerp(pal.panelLo, item.rarity.color, 0.12f), item.rarity.color, lerp(item.rarity.color, Color.Black, 0.55f), pal.border, 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        ItemIcon(item, 46.dp)
        if (entry.count > 1) {
            Box(Modifier.align(Alignment.BottomEnd).padding(4.dp)) { PText("×${entry.count}", size = 13.sp, bold = true, color = pal.text) }
        }
        if (equipped) {
            Box(Modifier.align(Alignment.TopStart).padding(4.dp).background(pal.accent).padding(horizontal = 3.dp)) {
                PText("E", size = 11.sp, bold = true, color = pal.onAccent)
            }
        }
        if (entry.locked) Box(Modifier.align(Alignment.TopEnd).padding(3.dp)) { PixelIcon("lock", 14.dp) }
    }
}

@Composable
private fun ItemDetailDialog(
    entry: InventoryEntry,
    equipped: Boolean,
    state: PlayerState,
    onDismiss: () -> Unit,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onToggleLock: () -> Unit,
    onSell: (Int) -> Unit,
) {
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val item = entry.item
    val sellable = entry.count - (if (equipped) 1 else 0)
    PixelDialog(onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(96.dp).pixelFrame(lerp(pal.panelLo, item.rarity.color, 0.15f), item.rarity.color, lerp(item.rarity.color, Color.Black, 0.5f), pal.border, 3.dp),
                contentAlignment = Alignment.Center,
            ) { ItemIcon(item, 72.dp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                PText(item.name(lang), size = 19.sp, bold = true)
                Spacer(Modifier.height(4.dp))
                TitleText(item.rarity.label(lang), size = 8.sp, color = item.rarity.color)
                Spacer(Modifier.height(4.dp))
                PText((item.slot?.title?.get(lang) ?: tr("Коллекция", "Collectible")) + " · ×${entry.count}", color = pal.muted, size = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        PText(item.description(lang), color = pal.muted)
        if (item.stats.isNotEmpty() || !item.perks.isEmpty) {
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxWidth().insetFrame().padding(10.dp)) {
                item.stats.forEach { (s, v) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PixelIcon(s.icon, 18.dp); Spacer(Modifier.width(6.dp))
                        PText(s.title.text(), modifier = Modifier.weight(1f), size = 14.sp)
                        PText("+$v", color = s.color, bold = true)
                    }
                }
                if (item.perks.xp > 0) BonusRow("star", tr("Опыт", "XP"), "+${item.perks.xp}%")
                if (item.perks.coins > 0) BonusRow("coin", tr("Монеты", "Coins"), "+${item.perks.coins}%")
                if (item.perks.luck > 0) BonusRow("clover", tr("Удача", "Luck"), "+${item.perks.luck}%")
            }
        }
        gearSetById(item.setId)?.let { set ->
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxWidth().insetFrame().padding(10.dp)) {
                PText(tr("Сет «${set.title.ru}»", "${set.title.en} set"), color = pal.gold, bold = true)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    set.pieces.forEach { p -> ItemIcon(p, 30.dp, disabled = (state.items[p.id] ?: 0) == 0) }
                }
                Spacer(Modifier.height(4.dp))
                PText(tr("Бонус за весь сет: ", "Full set bonus: ") + setBonusText(set, lang), color = pal.accent2, size = 13.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        if (item.slot != null) {
            PixelButton(
                if (equipped) tr("СНЯТЬ", "UNEQUIP") else tr("НАДЕТЬ", "EQUIP"),
                { if (equipped) onUnequip() else onEquip() },
                Modifier.fillMaxWidth(),
                color = if (equipped) pal.panelHi else pal.accent,
                textColor = if (equipped) pal.text else pal.onAccent,
                icon = if (equipped) "close" else "shield",
            )
            Spacer(Modifier.height(10.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton(if (entry.locked) tr("ОТКРЕПИТЬ", "UNLOCK") else tr("ЗАКРЕПИТЬ", "LOCK"), onToggleLock, Modifier.weight(1f),
                color = pal.panelHi, textColor = pal.text, icon = if (entry.locked) "unlock" else "lock", fontSize = 8.sp, height = 48.dp)
            PixelButton("", { onSell(1) }, Modifier.weight(1f), color = pal.gold, textColor = Color(0xFF2A1A02), enabled = !entry.locked && sellable > 0, height = 48.dp) {
                TitleText(tr("ПРОДАТЬ", "SELL"), size = 8.sp, color = if (!entry.locked && sellable > 0) Color(0xFF2A1A02) else pal.muted, shadow = false)
                Spacer(Modifier.width(6.dp)); PixelIcon("coin", 14.dp); Spacer(Modifier.width(2.dp))
                TitleText("${item.sellPrice}", size = 8.sp, color = if (!entry.locked && sellable > 0) Color(0xFF2A1A02) else pal.muted, shadow = false)
            }
        }
        if (!entry.locked && sellable > 1) {
            Spacer(Modifier.height(8.dp))
            PText(tr("Продать все копии ($sellable) за ${sellable * item.sellPrice}", "Sell all copies ($sellable) for ${sellable * item.sellPrice}"),
                color = pal.gold, bold = true, size = 14.sp, align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { onSell(sellable) }.padding(8.dp))
        }
        if (entry.locked) {
            Spacer(Modifier.height(6.dp))
            PText(tr("Закреплённый предмет нельзя продать.", "Locked items can't be sold."), color = pal.muted, size = 13.sp)
        } else if (equipped && entry.count == 1) {
            Spacer(Modifier.height(6.dp))
            PText(tr("Нельзя продать последнюю надетую копию.", "You can't sell the last equipped copy."), color = pal.muted, size = 13.sp)
        }
    }
}

@Composable
private fun BonusRow(icon: String, title: String, value: String) {
    val pal = LocalPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        PixelIcon(icon, 18.dp); Spacer(Modifier.width(6.dp))
        PText(title, modifier = Modifier.weight(1f), size = 14.sp)
        PText(value, color = pal.accent2, bold = true)
    }
}

fun setBonusText(set: GearSet, lang: AppLanguage): String {
    val parts = mutableListOf<String>()
    set.bonusStats.forEach { (s, v) -> parts += "+$v ${s.short.get(lang)}" }
    if (set.bonusPerks.xp > 0) parts += "+${set.bonusPerks.xp}% XP"
    if (set.bonusPerks.coins > 0) parts += "+${set.bonusPerks.coins}% " + tr(lang, "монет", "coins")
    if (set.bonusPerks.luck > 0) parts += "+${set.bonusPerks.luck}% " + tr(lang, "удачи", "luck")
    return parts.joinToString(", ")
}

package com.example.ironquest

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import kotlin.math.roundToInt
import kotlin.random.Random

private val Context.dataStore by preferencesDataStore(name = "player_data")

/** Внешность героя (бесплатная настройка). */
data class HeroLook(val hair: Int = 0, val skin: Int = 0)

/** Полное состояние игрока, собранное из DataStore. */
data class PlayerState(
    val loaded: Boolean = false,
    val xp: Int = 0,
    val coins: Int = 0,
    val items: Map<String, Int> = emptyMap(),
    val locked: Set<String> = emptySet(),
    val equippedIds: Map<ItemSlot, String> = emptyMap(),
    val claimedAchievements: Set<String> = emptySet(),
    val favoriteAchievements: Set<String> = emptySet(),
    val ownedThemes: Set<String> = setOf(AppTheme.DUNGEON.name),
    val activeTheme: AppTheme = AppTheme.DUNGEON,
    val restDaysUnlocked: Int = 1,
    val restDays: Set<Int> = setOf(java.util.Calendar.SUNDAY),
    val frozenDays: Set<Int> = emptySet(),
    val consumables: Map<Consumable, Int> = emptyMap(),
    val counters: Map<String, Int> = emptyMap(),
    val heroName: String = "",
    val heroLook: HeroLook = HeroLook(),
    val selectedExercises: Set<String> = DEFAULT_SELECTED_EXERCISES,
    val customExercises: List<Exercise> = emptyList(),
    val dailyChestDay: Int = 0,
    val dailyDealDay: Int = 0,
) {
    val inventory: List<InventoryEntry>
        get() = ITEM_CATALOG.mapNotNull { item ->
            val c = items[item.id] ?: 0
            if (c > 0) InventoryEntry(item, c, item.id in locked) else null
        }

    val equipped: Map<ItemSlot, ItemDefinition>
        get() = equippedIds.mapNotNull { (slot, id) -> itemById(id)?.let { slot to it } }.toMap()

    fun count(c: Consumable) = consumables[c] ?: 0
}

/** Награды за тренировку. */
data class WorkoutReward(
    val xp: Int,
    val baseXp: Int,
    val coins: Int,
    val item: ItemDefinition?,
    val levelBefore: Int,
    val levelAfter: Int,
    val usedPotion: Boolean,
    val usedClover: Boolean,
    val usedMagnet: Boolean,
    val perks: Perks,
    val statGains: Map<Stat, Int>,
    val record: WorkoutRecord,
)

object PlayerData {
    private val XP = intPreferencesKey("total_xp")
    private val COINS = intPreferencesKey("player_coins")
    private val CLAIMED = stringSetPreferencesKey("claimed_achievements")
    private val FAVORITES = stringSetPreferencesKey("favorite_achievements")
    private val LOCKED = stringSetPreferencesKey("locked_items")
    private val OWNED_THEMES = stringSetPreferencesKey("owned_themes")
    private val ACTIVE_THEME = stringPreferencesKey("active_theme")
    private val REST_UNLOCKED = intPreferencesKey("rest_days_unlocked")
    private val REST_DAYS = stringSetPreferencesKey("rest_days")
    private val FROZEN_DAYS = stringSetPreferencesKey("frozen_days")
    private val HERO_NAME = stringPreferencesKey("hero_name")
    private val HERO_HAIR = intPreferencesKey("hero_hair")
    private val HERO_SKIN = intPreferencesKey("hero_skin")
    private val SELECTED_EX = stringSetPreferencesKey("selected_exercises")
    private val CUSTOM_EX = stringPreferencesKey("custom_exercises")
    private val DAILY_CHEST = intPreferencesKey("daily_chest_day")
    private val DAILY_DEAL = intPreferencesKey("daily_deal_day")

    private fun itemKey(id: String) = intPreferencesKey("item_$id")
    private fun slotKey(slot: ItemSlot) = stringPreferencesKey("equipped_${slot.key}")
    private fun consumableKey(c: Consumable) = intPreferencesKey("consumable_${c.name.lowercase()}")
    private fun counterKey(name: String) = intPreferencesKey("counter_$name")

    val COUNTERS = listOf("cases_opened", "coins_earned", "items_found", "legendary_found", "mythic_found", "freezes_used")

    fun state(context: Context): Flow<PlayerState> = context.dataStore.data.map { p -> read(p) }

    private fun read(p: Preferences): PlayerState {
        val custom = p[CUSTOM_EX]?.let { json ->
            runCatching {
                val arr = JSONArray(json)
                (0 until arr.length()).mapNotNull { Exercise.fromJson(arr.getJSONObject(it)) }
            }.getOrDefault(emptyList())
        } ?: emptyList()
        val owned = (p[OWNED_THEMES] ?: emptySet()) + AppTheme.DUNGEON.name
        return PlayerState(
            loaded = true,
            xp = p[XP] ?: 0,
            coins = p[COINS] ?: 0,
            items = ITEM_CATALOG.mapNotNull { item -> (p[itemKey(item.id)] ?: 0).takeIf { it > 0 }?.let { item.id to it } }.toMap(),
            locked = p[LOCKED] ?: emptySet(),
            equippedIds = ItemSlot.values().mapNotNull { slot -> p[slotKey(slot)]?.let { slot to it } }.toMap(),
            claimedAchievements = p[CLAIMED] ?: emptySet(),
            favoriteAchievements = p[FAVORITES] ?: emptySet(),
            ownedThemes = owned,
            activeTheme = AppTheme.fromName(p[ACTIVE_THEME]).takeIf { it.name in owned } ?: AppTheme.DUNGEON,
            restDaysUnlocked = (p[REST_UNLOCKED] ?: 1).coerceIn(1, MAX_REST_DAYS),
            restDays = p[REST_DAYS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(java.util.Calendar.SUNDAY),
            frozenDays = p[FROZEN_DAYS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            consumables = Consumable.values().associateWith { p[consumableKey(it)] ?: 0 },
            counters = COUNTERS.associateWith { p[counterKey(it)] ?: 0 },
            heroName = p[HERO_NAME] ?: "",
            heroLook = HeroLook(p[HERO_HAIR] ?: 0, p[HERO_SKIN] ?: 0),
            selectedExercises = p[SELECTED_EX] ?: DEFAULT_SELECTED_EXERCISES,
            customExercises = custom,
            dailyChestDay = p[DAILY_CHEST] ?: 0,
            dailyDealDay = p[DAILY_DEAL] ?: 0,
        )
    }

    private fun MutablePreferences.addCount(key: Preferences.Key<Int>, delta: Int) {
        this[key] = (this[key] ?: 0) + delta
    }

    private fun MutablePreferences.addCoins(amount: Int) {
        addCount(COINS, amount)
        if (amount > 0) addCount(counterKey("coins_earned"), amount)
    }

    private fun MutablePreferences.giveItem(item: ItemDefinition) {
        addCount(itemKey(item.id), 1)
        addCount(counterKey("items_found"), 1)
        if (item.rarity == ItemRarity.LEGENDARY) addCount(counterKey("legendary_found"), 1)
        if (item.rarity == ItemRarity.MYTHIC) addCount(counterKey("mythic_found"), 1)
    }

    /* ---------------------------------------------------------- тренировка */

    /** Сохраняет тренировку, считает и выдаёт все награды атомарно. */
    suspend fun completeWorkout(
        context: Context,
        entries: Map<String, List<Int>>,
        durationSec: Int,
        state: PlayerState,
        perks: Perks,
    ): WorkoutReward {
        val custom = state.customExercises
        val clean = entries.mapValues { it.value.filter { v -> v > 0 } }.filterValues { it.isNotEmpty() }
        val baseXp = clean.entries.sumOf { (id, sets) ->
            (exerciseById(id, custom)?.xpFor(sets.sum()) ?: sets.sum().toFloat()).toDouble()
        }.roundToInt().coerceAtLeast(if (clean.isEmpty()) 0 else 1)
        val repEq = clean.entries.sumOf { (id, sets) -> ((exerciseById(id, custom)?.unit ?: ExerciseUnit.REPS).repEquivalent * sets.sum()).toDouble() }.toInt()
        val statGains = mutableMapOf<Stat, Int>()
        clean.forEach { (id, sets) ->
            exerciseById(id, custom)?.statPoints(sets.sum())?.forEach { (s, v) -> statGains[s] = (statGains[s] ?: 0) + v }
        }

        var reward: WorkoutReward? = null
        context.dataStore.edit { p ->
            val potion = (p[consumableKey(Consumable.XP_POTION)] ?: 0) > 0 && baseXp > 0
            val clover = (p[consumableKey(Consumable.LUCK_CLOVER)] ?: 0) > 0 && repEq > 0
            val magnet = (p[consumableKey(Consumable.COIN_MAGNET)] ?: 0) > 0 && repEq > 0
            if (potion) p.addCount(consumableKey(Consumable.XP_POTION), -1)
            if (clover) p.addCount(consumableKey(Consumable.LUCK_CLOVER), -1)
            if (magnet) p.addCount(consumableKey(Consumable.COIN_MAGNET), -1)

            var xp = (baseXp * (1f + perks.xp / 100f)).roundToInt()
            if (potion) xp *= 2
            var coins = if (repEq <= 0) 0 else (1 + repEq / 30).coerceIn(1, 6)
            coins = (coins * (1f + perks.coins / 100f)).roundToInt()
            if (magnet) coins *= 3

            val levelBefore = getLevelInfo(p[XP] ?: 0).level
            p.addCount(XP, xp)
            p.addCoins(coins)
            val levelAfter = getLevelInfo(p[XP] ?: 0).level
            if (levelAfter > levelBefore) p.addCoins((levelAfter - levelBefore) * 25)

            val item = rollWorkoutDrop(repEq, perks.luck, clover)
            if (item != null) p.giveItem(item)

            val record = WorkoutRecord(System.currentTimeMillis(), xp, clean, durationSec, coins)
            reward = WorkoutReward(xp, baseXp, coins, item, levelBefore, levelAfter, potion, clover, magnet, perks, statGains, record)
        }
        val r = reward!!
        WorkoutHistoryData.addWorkout(context, r.record)
        return r
    }

    /** Шанс и редкость добычи зависят от объёма. Нулевая тренировка ничего не даёт. */
    fun rollWorkoutDrop(repEq: Int, luckPct: Int, clover: Boolean, random: Random = Random): ItemDefinition? {
        if (repEq <= 0) return null
        val chance = when {
            repEq >= 100 -> 90
            repEq >= 70 -> 85
            repEq >= 40 -> 75
            repEq >= 20 -> 65
            repEq >= 10 -> 50
            else -> 35
        } + luckPct
        if (!clover && random.nextInt(100) >= chance) return null
        val shift = (if (clover) 12 else 0) + luckPct / 2
        val roll = (random.nextInt(100) + shift).coerceAtMost(99)
        val rarity = when {
            repEq >= 100 -> when { roll < 30 -> ItemRarity.COMMON; roll < 55 -> ItemRarity.UNCOMMON; roll < 80 -> ItemRarity.RARE; roll < 93 -> ItemRarity.EPIC; roll < 99 -> ItemRarity.LEGENDARY; else -> ItemRarity.MYTHIC }
            repEq >= 50 -> when { roll < 40 -> ItemRarity.COMMON; roll < 65 -> ItemRarity.UNCOMMON; roll < 85 -> ItemRarity.RARE; roll < 95 -> ItemRarity.EPIC; else -> ItemRarity.LEGENDARY }
            repEq >= 20 -> when { roll < 50 -> ItemRarity.COMMON; roll < 75 -> ItemRarity.UNCOMMON; roll < 91 -> ItemRarity.RARE; roll < 98 -> ItemRarity.EPIC; else -> ItemRarity.LEGENDARY }
            else -> when { roll < 60 -> ItemRarity.COMMON; roll < 83 -> ItemRarity.UNCOMMON; roll < 95 -> ItemRarity.RARE; roll < 99 -> ItemRarity.EPIC; else -> ItemRarity.LEGENDARY }
        }
        return randomItemOf(rarity, random)
    }

    /* ---------------------------------------------------------- достижения */

    /** Начисляет награды за открытые достижения один раз. Возвращает новые. */
    suspend fun awardUnlockedAchievements(context: Context, achievements: List<Achievement>): List<Achievement> {
        val fresh = mutableListOf<Achievement>()
        context.dataStore.edit { p ->
            val claimed = (p[CLAIMED] ?: emptySet()).toMutableSet()
            achievements.filter { it.unlocked }.forEach { a ->
                if (claimed.add(a.id)) {
                    fresh.add(a)
                    p.addCount(XP, a.rewardXp)
                    if (a.rewardCoins > 0) p.addCoins(a.rewardCoins)
                }
            }
            p[CLAIMED] = claimed
        }
        return fresh
    }

    suspend fun toggleFavoriteAchievement(context: Context, id: String) {
        context.dataStore.edit { p ->
            val f = (p[FAVORITES] ?: emptySet()).toMutableSet()
            if (!f.remove(id) && f.size < 3) f.add(id)
            p[FAVORITES] = f
        }
    }

    /* ---------------------------------------------------------- инвентарь */

    suspend fun toggleItemLock(context: Context, item: ItemDefinition) {
        context.dataStore.edit { p ->
            val l = (p[LOCKED] ?: emptySet()).toMutableSet()
            if (!l.remove(item.id)) l.add(item.id)
            p[LOCKED] = l
        }
    }

    suspend fun equipItem(context: Context, item: ItemDefinition): Boolean {
        val slot = item.slot ?: return false
        var ok = false
        context.dataStore.edit { p ->
            if ((p[itemKey(item.id)] ?: 0) > 0) {
                p[slotKey(slot)] = item.id
                ok = true
            }
        }
        return ok
    }

    suspend fun unequipItem(context: Context, item: ItemDefinition): Boolean {
        val slot = item.slot ?: return false
        var ok = false
        context.dataStore.edit { p ->
            if (p[slotKey(slot)] == item.id) {
                p.remove(slotKey(slot)); ok = true
            }
        }
        return ok
    }

    /** Надевает все имеющиеся предметы сета. */
    suspend fun equipSet(context: Context, set: GearSet) {
        context.dataStore.edit { p ->
            set.pieces.forEach { item ->
                val slot = item.slot ?: return@forEach
                if ((p[itemKey(item.id)] ?: 0) > 0) p[slotKey(slot)] = item.id
            }
        }
    }

    /** Продаёт копии предмета (count = сколько). Последнюю надетую или закреплённую продать нельзя. */
    suspend fun sellItem(context: Context, item: ItemDefinition, count: Int = 1): Int {
        var earned = 0
        context.dataStore.edit { p ->
            val key = itemKey(item.id)
            val have = p[key] ?: 0
            val equipped = ItemSlot.values().any { p[slotKey(it)] == item.id }
            val locked = (p[LOCKED] ?: emptySet()).contains(item.id)
            if (locked) return@edit
            val keep = if (equipped) 1 else 0
            val sell = count.coerceAtMost(have - keep)
            if (sell > 0) {
                p[key] = have - sell
                earned = sell * item.sellPrice
                p.addCoins(earned)
            }
        }
        return earned
    }

    /** Продаёт все дубликаты (оставляя по одной копии). */
    suspend fun sellDuplicates(context: Context): Int {
        var earned = 0
        context.dataStore.edit { p ->
            val locked = p[LOCKED] ?: emptySet()
            ITEM_CATALOG.forEach { item ->
                if (item.id in locked) return@forEach
                val key = itemKey(item.id)
                val have = p[key] ?: 0
                if (have > 1) {
                    p[key] = 1
                    earned += (have - 1) * item.sellPrice
                }
            }
            if (earned > 0) p.addCoins(earned)
        }
        return earned
    }

    /* ---------------------------------------------------------- магазин */

    /** Покупает сундук и сохраняет предмет вместе со списанием монет. free = ежедневный сундук. */
    suspend fun openCase(context: Context, case: ShopCase, free: Boolean = false): CaseOpenResult {
        var result: CaseOpenResult = CaseOpenResult.NotEnoughCoins
        context.dataStore.edit { p ->
            val coins = p[COINS] ?: 0
            val today = dayKey(System.currentTimeMillis())
            if (free) {
                if ((p[DAILY_CHEST] ?: 0) == today) return@edit
            } else if (coins < case.price) return@edit
            val item = randomItemOf(case.rollRarity())
            if (free) p[DAILY_CHEST] = today else p[COINS] = coins - case.price
            p.giveItem(item)
            p.addCount(counterKey("cases_opened"), 1)
            result = CaseOpenResult.Success(item, case)
        }
        return result
    }

    suspend fun buyConsumable(context: Context, c: Consumable): Boolean {
        var ok = false
        context.dataStore.edit { p ->
            val coins = p[COINS] ?: 0
            val have = p[consumableKey(c)] ?: 0
            if (coins >= c.price && have < c.maxStack) {
                p[COINS] = coins - c.price
                p[consumableKey(c)] = have + 1
                ok = true
            }
        }
        return ok
    }

    suspend fun buyDailyDeal(context: Context, item: ItemDefinition, price: Int): Boolean {
        var ok = false
        context.dataStore.edit { p ->
            val coins = p[COINS] ?: 0
            val today = dayKey(System.currentTimeMillis())
            if (coins >= price && (p[DAILY_DEAL] ?: 0) != today) {
                p[COINS] = coins - price
                p[DAILY_DEAL] = today
                p.giveItem(item)
                ok = true
            }
        }
        return ok
    }

    suspend fun buyRestDay(context: Context): Boolean {
        var ok = false
        context.dataStore.edit { p ->
            val unlocked = (p[REST_UNLOCKED] ?: 1).coerceIn(1, MAX_REST_DAYS)
            val price = REST_DAY_PRICES[unlocked] ?: return@edit
            val coins = p[COINS] ?: 0
            if (coins >= price) {
                p[COINS] = coins - price
                p[REST_UNLOCKED] = unlocked + 1
                ok = true
            }
        }
        return ok
    }

    suspend fun setRestDays(context: Context, days: Set<Int>) {
        context.dataStore.edit { p ->
            val unlocked = (p[REST_UNLOCKED] ?: 1).coerceIn(1, MAX_REST_DAYS)
            p[REST_DAYS] = days.take(unlocked).map { it.toString() }.toSet()
        }
    }

    suspend fun buyTheme(context: Context, theme: AppTheme): Boolean {
        var ok = false
        context.dataStore.edit { p ->
            val owned = (p[OWNED_THEMES] ?: emptySet()).toMutableSet()
            val coins = p[COINS] ?: 0
            if (theme.name !in owned && coins >= theme.price) {
                p[COINS] = coins - theme.price
                owned.add(theme.name)
                p[OWNED_THEMES] = owned
                p[ACTIVE_THEME] = theme.name
                ok = true
            }
        }
        return ok
    }

    suspend fun applyTheme(context: Context, theme: AppTheme) {
        context.dataStore.edit { p ->
            val owned = (p[OWNED_THEMES] ?: emptySet()) + AppTheme.DUNGEON.name
            if (theme.name in owned) p[ACTIVE_THEME] = theme.name
        }
    }

    /**
     * Если с последней тренировки пропущены рабочие дни и хватает заморозок — тратит их,
     * чтобы серия не сгорела. Возвращает количество потраченных заморозок.
     */
    suspend fun applyStreakFreezes(context: Context, history: List<WorkoutRecord>): Int {
        var used = 0
        context.dataStore.edit { p ->
            val rest = p[REST_DAYS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(java.util.Calendar.SUNDAY)
            val frozen = p[FROZEN_DAYS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            val missed = missedDaysSinceLastWorkout(history, rest, frozen)
            val have = p[consumableKey(Consumable.STREAK_FREEZE)] ?: 0
            if (missed.isEmpty() || have < missed.size) return@edit
            if (calculateStreak(history, rest, frozen + missed) <= 0) return@edit
            p[consumableKey(Consumable.STREAK_FREEZE)] = have - missed.size
            p[FROZEN_DAYS] = (frozen + missed).map { it.toString() }.toSet()
            p.addCount(counterKey("freezes_used"), missed.size)
            used = missed.size
        }
        return used
    }

    /* ---------------------------------------------------------- герой и упражнения */

    suspend fun setHeroName(context: Context, name: String) {
        context.dataStore.edit { it[HERO_NAME] = name.take(16) }
    }

    suspend fun setHeroLook(context: Context, look: HeroLook) {
        context.dataStore.edit { it[HERO_HAIR] = look.hair; it[HERO_SKIN] = look.skin }
    }

    suspend fun setSelectedExercises(context: Context, ids: Set<String>) {
        context.dataStore.edit { it[SELECTED_EX] = ids }
    }

    suspend fun saveCustomExercise(context: Context, exercise: Exercise) {
        context.dataStore.edit { p ->
            val list = readCustom(p).filterNot { it.id == exercise.id } + exercise.copy(custom = true)
            p[CUSTOM_EX] = JSONArray().apply { list.forEach { put(it.toJson()) } }.toString()
        }
    }

    /** Удаление = архивирование, чтобы история и характеристики не потеряли данные. */
    suspend fun archiveCustomExercise(context: Context, id: String) {
        context.dataStore.edit { p ->
            val list = readCustom(p).map { if (it.id == id) it.copy(archived = true) else it }
            p[CUSTOM_EX] = JSONArray().apply { list.forEach { put(it.toJson()) } }.toString()
            p[SELECTED_EX] = (p[SELECTED_EX] ?: DEFAULT_SELECTED_EXERCISES) - id
        }
    }

    private fun readCustom(p: Preferences): List<Exercise> = p[CUSTOM_EX]?.let { json ->
        runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { Exercise.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    } ?: emptyList()

    /** Полный сброс игрового прогресса и инвентаря. */
    suspend fun resetProgress(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}

/** Предмет дня в лавке: детерминирован по дате, скидка 40%. */
fun dailyDeal(today: Int = dayKey(System.currentTimeMillis())): Pair<ItemDefinition, Int> {
    val pool = ITEM_CATALOG.filter { it.isEquipment && it.rarity >= ItemRarity.RARE && it.rarity <= ItemRarity.LEGENDARY }
    val item = pool[Random(today).nextInt(pool.size)]
    val price = (item.sellPrice * 3 * 0.6f).roundToInt()
    return item to price
}

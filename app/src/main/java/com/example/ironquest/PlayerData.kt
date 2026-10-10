package com.example.ironquest

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "player_data"
)

data class PlayerStats(
    val xp: Int,
    val armsReps: Int,
    val shouldersReps: Int,
    val backReps: Int,
    val chestReps: Int
)

object PlayerData {

    private val XP_KEY = intPreferencesKey("total_xp")
    private val ARMS_KEY = intPreferencesKey("arms_reps")
    private val SHOULDERS_KEY = intPreferencesKey("shoulders_reps")
    private val BACK_KEY = intPreferencesKey("back_reps")
    private val CHEST_KEY = intPreferencesKey("chest_reps")
    private val CLAIMED_ACHIEVEMENTS_KEY = stringSetPreferencesKey("claimed_achievements")
    private val EQUIPMENT_SLOTS = listOf("head", "body", "hands", "feet")

    fun getStats(context: Context): Flow<PlayerStats> {

        return context.dataStore.data.map { preferences ->

            PlayerStats(
                xp = preferences[XP_KEY] ?: 0,
                armsReps = preferences[ARMS_KEY] ?: 0,
                shouldersReps = preferences[SHOULDERS_KEY] ?: 0,
                backReps = preferences[BACK_KEY] ?: 0,
                chestReps = preferences[CHEST_KEY] ?: 0
            )
        }
    }

    /** Начисляет награды за открытые достижения только один раз. */
    suspend fun awardUnlockedAchievements(
        context: Context,
        achievements: List<Achievement>
    ): Int {
        var earnedXp = 0

        context.dataStore.edit { preferences ->
            val claimed = (preferences[CLAIMED_ACHIEVEMENTS_KEY] ?: emptySet()).toMutableSet()

            achievements.forEach { achievement ->
                if (achievement.unlocked && claimed.add(achievement.id)) {
                    earnedXp += achievement.rewardXp
                }
            }

            if (earnedXp > 0) {
                preferences[XP_KEY] = (preferences[XP_KEY] ?: 0) + earnedXp
            }

            preferences[CLAIMED_ACHIEVEMENTS_KEY] = claimed
        }

        return earnedXp
    }

    /** Current item collection, including quantities for duplicate drops. */
    fun getInventory(context: Context): Flow<List<InventoryEntry>> {
        return context.dataStore.data.map { preferences ->
            ITEM_CATALOG.mapNotNull { item ->
                val count = preferences[intPreferencesKey("item_${item.id}")] ?: 0
                if (count > 0) InventoryEntry(item = item, count = count) else null
            }
        }
    }

    /** Текущая экипировка персонажа: слот -> ID предмета. */
    fun getEquipped(context: Context): Flow<Map<String, String>> = context.dataStore.data.map { preferences ->
        EQUIPMENT_SLOTS.mapNotNull { slot ->
            preferences[stringPreferencesKey("equipped_$slot")]?.let { itemId -> slot to itemId }
        }.toMap()
    }

    /** Надевает найденный предмет. Нельзя надеть вещь, которой нет в инвентаре. */
    suspend fun equipItem(context: Context, item: ItemDefinition): Boolean {
        val slot = item.slot ?: return false
        var equipped = false
        context.dataStore.edit { preferences ->
            val count = preferences[intPreferencesKey("item_${item.id}")] ?: 0
            if (count > 0) {
                preferences[stringPreferencesKey("equipped_$slot")] = item.id
                equipped = true
            }
        }
        return equipped
    }

    /**
     * Дроп зависит от реальной нагрузки. Нулевая тренировка никогда не даёт предмет.
     * С ростом количества повторений увеличиваются и шанс выпадения, и шанс редкости.
     */
    suspend fun rollAndSaveDrop(context: Context, totalReps: Int): ItemDefinition? {
        if (totalReps <= 0) return null

        val dropChance = when {
            totalReps >= 100 -> 90
            totalReps >= 70 -> 85
            totalReps >= 40 -> 75
            totalReps >= 20 -> 65
            totalReps >= 10 -> 50
            else -> 35
        }
        if (kotlin.random.Random.nextInt(100) >= dropChance) return null

        val rarityRoll = kotlin.random.Random.nextInt(100)
        val rarity = when {
            totalReps >= 100 -> when {
                rarityRoll < 30 -> ItemRarity.COMMON
                rarityRoll < 55 -> ItemRarity.UNCOMMON
                rarityRoll < 80 -> ItemRarity.RARE
                rarityRoll < 93 -> ItemRarity.EPIC
                else -> ItemRarity.LEGENDARY
            }
            totalReps >= 50 -> when {
                rarityRoll < 40 -> ItemRarity.COMMON
                rarityRoll < 65 -> ItemRarity.UNCOMMON
                rarityRoll < 85 -> ItemRarity.RARE
                rarityRoll < 95 -> ItemRarity.EPIC
                else -> ItemRarity.LEGENDARY
            }
            totalReps >= 20 -> when {
                rarityRoll < 50 -> ItemRarity.COMMON
                rarityRoll < 75 -> ItemRarity.UNCOMMON
                rarityRoll < 91 -> ItemRarity.RARE
                rarityRoll < 98 -> ItemRarity.EPIC
                else -> ItemRarity.LEGENDARY
            }
            else -> when {
                rarityRoll < 60 -> ItemRarity.COMMON
                rarityRoll < 83 -> ItemRarity.UNCOMMON
                rarityRoll < 95 -> ItemRarity.RARE
                rarityRoll < 99 -> ItemRarity.EPIC
                else -> ItemRarity.LEGENDARY
            }
        }

        val candidates = ITEM_CATALOG.filter { it.rarity == rarity }
        val item = candidates.randomOrNull() ?: ITEM_CATALOG.first()

        context.dataStore.edit { preferences ->
            val key = intPreferencesKey("item_${item.id}")
            preferences[key] = (preferences[key] ?: 0) + 1
        }
        return item
    }

    suspend fun addWorkout(
        context: Context,
        xp: Int,
        armsReps: Int,
        shouldersReps: Int,
        backReps: Int,
        chestReps: Int
    ) {

        context.dataStore.edit { preferences ->

            preferences[XP_KEY] =
                (preferences[XP_KEY] ?: 0) + xp

            preferences[ARMS_KEY] =
                (preferences[ARMS_KEY] ?: 0) + armsReps

            preferences[SHOULDERS_KEY] =
                (preferences[SHOULDERS_KEY] ?: 0) + shouldersReps

            preferences[BACK_KEY] =
                (preferences[BACK_KEY] ?: 0) + backReps

            preferences[CHEST_KEY] =
                (preferences[CHEST_KEY] ?: 0) + chestReps
        }
    }
}
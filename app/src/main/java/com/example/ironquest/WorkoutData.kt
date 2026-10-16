package com.example.ironquest

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.workoutHistoryDataStore by preferencesDataStore(name = "workout_history")

/**
 * Одна тренировка. entries: id упражнения -> список подходов (повторения/секунды/минуты).
 */
data class WorkoutRecord(
    val timestamp: Long,
    val xp: Int,
    val entries: Map<String, List<Int>>,
    val durationSec: Int = 0,
    val coins: Int = 0,
) {
    fun total(exerciseId: String): Int = entries[exerciseId]?.sum() ?: 0

    /** Совместимость со старым кодом/аналитикой. */
    val pullUps: Int get() = total("pullups")
    val dips: Int get() = total("dips")
    val pushUps: Int get() = total("pushups")

    fun repEquivalent(custom: List<Exercise>): Int = entries.entries.sumOf { (id, sets) ->
        val unit = exerciseById(id, custom)?.unit ?: ExerciseUnit.REPS
        (sets.sum() * unit.repEquivalent).toInt()
    }

    val setsCount: Int get() = entries.values.sumOf { it.size }

    fun toJson(): String = JSONObject().apply {
        put("t", timestamp); put("xp", xp); put("d", durationSec); put("c", coins)
        put("e", JSONObject().apply { entries.forEach { (k, v) -> put(k, JSONArray(v)) } })
    }.toString()

    companion object {
        fun fromJson(s: String): WorkoutRecord? = try {
            val o = JSONObject(s)
            val e = o.getJSONObject("e")
            val entries = e.keys().asSequence().associateWith { k ->
                val arr = e.getJSONArray(k)
                (0 until arr.length()).map { arr.getInt(it) }
            }.filterValues { it.isNotEmpty() }
            WorkoutRecord(o.getLong("t"), o.optInt("xp"), entries, o.optInt("d"), o.optInt("c"))
        } catch (_: Exception) {
            null
        }

        /** Старый формат: "timestamp|xp|pullUps|dips|pushUps". */
        fun fromLegacy(s: String): WorkoutRecord? {
            val p = s.split("|")
            if (p.size != 5) return null
            val ts = p[0].toLongOrNull() ?: return null
            val xp = p[1].toIntOrNull() ?: return null
            val values = listOf("pullups" to p[2], "dips" to p[3], "pushups" to p[4])
            val entries = values.mapNotNull { (id, v) -> v.toIntOrNull()?.takeIf { it > 0 }?.let { id to listOf(it) } }.toMap()
            return WorkoutRecord(ts, xp, entries)
        }
    }
}

object WorkoutHistoryData {
    private val LEGACY_KEY = stringSetPreferencesKey("records")
    private val RECORDS_KEY = stringSetPreferencesKey("records_v2")

    fun getHistory(context: Context): Flow<List<WorkoutRecord>> =
        context.workoutHistoryDataStore.data.map { prefs ->
            val legacy = (prefs[LEGACY_KEY] ?: emptySet()).mapNotNull { WorkoutRecord.fromLegacy(it) }
            val modern = (prefs[RECORDS_KEY] ?: emptySet()).mapNotNull { WorkoutRecord.fromJson(it) }
            (legacy + modern).distinctBy { it.timestamp }.sortedByDescending { it.timestamp }
        }

    suspend fun addWorkout(context: Context, record: WorkoutRecord) {
        context.workoutHistoryDataStore.edit { prefs ->
            val set = (prefs[RECORDS_KEY] ?: emptySet()).toMutableSet()
            set.add(record.toJson())
            prefs[RECORDS_KEY] = set
        }
    }

    suspend fun deleteWorkout(context: Context, timestamp: Long) {
        context.workoutHistoryDataStore.edit { prefs ->
            prefs[RECORDS_KEY] = (prefs[RECORDS_KEY] ?: emptySet()).filterNot { WorkoutRecord.fromJson(it)?.timestamp == timestamp }.toSet()
            prefs[LEGACY_KEY] = (prefs[LEGACY_KEY] ?: emptySet()).filterNot { WorkoutRecord.fromLegacy(it)?.timestamp == timestamp }.toSet()
        }
    }

    suspend fun clearHistory(context: Context) {
        context.workoutHistoryDataStore.edit { it.clear() }
    }
}

package com.example.ironquest

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
package com.example.ironquest

import java.util.Calendar
import java.util.Date

 data class LevelInfo(
    val level: Int,
    val currentXp: Int,
    val requiredXp: Int,
    val totalXpForNextLevel: Int
)

fun getLevelInfo(totalXp: Int): LevelInfo {
    // Level 1 -> 2 requires 100 XP; each following level costs 10 XP more.
    var level = 1
    var xpSpent = 0
    var requiredForNext = 100

    while (totalXp >= xpSpent + requiredForNext) {
        xpSpent += requiredForNext
        level++
        requiredForNext = 100 + ((level - 1) * 10)
    }

    return LevelInfo(
        level = level,
        currentXp = totalXp - xpSpent,
        requiredXp = requiredForNext,
        totalXpForNextLevel = xpSpent + requiredForNext
    )
}

/** Returns the local calendar day's start time without using java.time (API 26+ only). */
private fun startOfLocalDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.time = Date(timestamp)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/** Moves a local calendar date by whole days, correctly handling daylight-saving changes. */
private fun shiftLocalDay(dayStart: Long, amount: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dayStart
    calendar.add(Calendar.DAY_OF_YEAR, amount)
    return calendar.timeInMillis
}

fun calculateStreak(history: List<WorkoutRecord>): Int {
    if (history.isEmpty()) return 0

    val workoutDays = history
        .map { startOfLocalDay(it.timestamp) }
        .distinct()
        .sortedDescending()

    if (workoutDays.isEmpty()) return 0

    val today = startOfLocalDay(System.currentTimeMillis())
    val yesterday = shiftLocalDay(today, -1)
    val firstDay = workoutDays.first()

    if (firstDay != today && firstDay != yesterday) return 0

    var streak = 1
    for (i in 1 until workoutDays.size) {
        if (shiftLocalDay(workoutDays[i - 1], -1) == workoutDays[i]) {
            streak++
        } else {
            break
        }
    }

    return streak
}

data class Achievement(
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val id: String = title,
    val rewardXp: Int = 50
)

fun getAchievements(
    history: List<WorkoutRecord>,
    totalXp: Int,
    streak: Int
): List<Achievement> {
    val totalReps = history.sumOf { it.pullUps + it.dips + it.pushUps }

    return listOf(
        Achievement(
            title = "Первый шаг",
            description = "Завершить первую тренировку",
            unlocked = history.isNotEmpty(),
            id = "first_workout",
            rewardXp = 50
        ),
        Achievement(
            title = "Сотня",
            description = "Сделать 100 повторений",
            unlocked = totalReps >= 100,
            id = "hundred_reps",
            rewardXp = 75
        ),
        Achievement(
            title = "Разогрев",
            description = "Достичь серии 3 дня",
            unlocked = streak >= 3,
            id = "streak_3",
            rewardXp = 100
        ),
        Achievement(
            title = "Железная воля",
            description = "Достичь серии 7 дней",
            unlocked = streak >= 7,
            id = "streak_7",
            rewardXp = 250
        ),
        Achievement(
            title = "Уровень 5",
            description = "Достичь 5 уровня",
            unlocked = getLevelInfo(totalXp).level >= 5,
            id = "level_5",
            rewardXp = 200
        ),
        Achievement(
            title = "1000 XP",
            description = "Заработать 1000 XP",
            unlocked = totalXp >= 1000,
            id = "xp_1000",
            rewardXp = 300
        ),
        Achievement(
            title = "Мастер подтягиваний",
            description = "Сделать 100 подтягиваний",
            unlocked = history.sumOf { it.pullUps } >= 100,
            id = "pullups_100",
            rewardXp = 150
        ),
        Achievement(
            title = "Железные руки",
            description = "Сделать 500 повторений",
            unlocked = totalReps >= 500,
            id = "reps_500",
            rewardXp = 200
        )
    )
}

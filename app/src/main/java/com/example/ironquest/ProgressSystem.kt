package com.example.ironquest

import java.time.LocalDate
import java.time.ZoneId

data class LevelInfo(
    val level: Int,
    val currentXp: Int,
    val requiredXp: Int,
    val totalXpForNextLevel: Int
)

fun getLevelInfo(totalXp: Int): LevelInfo {

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

fun calculateStreak(
    history: List<WorkoutRecord>
): Int {

    if (history.isEmpty()) {
        return 0
    }

    val workoutDays = history
        .map {
            java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        .distinct()
        .sortedDescending()

    if (workoutDays.isEmpty()) {
        return 0
    }

    val today = LocalDate.now()

    val firstDay = workoutDays.first()

    if (firstDay != today && firstDay != today.minusDays(1)) {
        return 0
    }

    var streak = 1

    for (i in 1 until workoutDays.size) {

        val previous = workoutDays[i - 1]
        val current = workoutDays[i]

        if (previous.minusDays(1) == current) {
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
    val unlocked: Boolean
)

fun getAchievements(
    history: List<WorkoutRecord>,
    totalXp: Int,
    streak: Int
): List<Achievement> {

    val totalReps = history.sumOf {
        it.pullUps + it.dips + it.pushUps
    }

    return listOf(

        Achievement(
            title = "⚔ Первый шаг",
            description = "Завершить первую тренировку",
            unlocked = history.isNotEmpty()
        ),

        Achievement(
            title = "💪 Сотня",
            description = "Сделать 100 повторений",
            unlocked = totalReps >= 100
        ),

        Achievement(
            title = "🔥 Разогрев",
            description = "Достичь серии 3 дня",
            unlocked = streak >= 3
        ),

        Achievement(
            title = "🔥 Железная воля",
            description = "Достичь серии 7 дней",
            unlocked = streak >= 7
        ),

        Achievement(
            title = "⭐ Уровень 5",
            description = "Достичь 5 уровня",
            unlocked = getLevelInfo(totalXp).level >= 5
        ),

        Achievement(
            title = "🏆 1000 XP",
            description = "Заработать 1000 XP",
            unlocked = totalXp >= 1000
        ),

        Achievement(
            title = "🪽 Мастер подтягиваний",
            description = "Сделать 100 подтягиваний",
            unlocked = history.sumOf { it.pullUps } >= 100
        ),

        Achievement(
            title = "🦾 Железные руки",
            description = "Сделать 500 повторений",
            unlocked = totalReps >= 500
        )
    )
}
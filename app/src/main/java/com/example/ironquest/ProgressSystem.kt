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
    val rewardXp: Int = 50,
    val iconRes: Int = R.drawable.icon_achievements,
    val category: AchievementCategory = AchievementCategory.ACHIEVEMENT
)

enum class AchievementCategory {
    ACHIEVEMENT,
    SET
}

fun getAchievements(
    history: List<WorkoutRecord>,
    totalXp: Int,
    streak: Int,
    inventory: List<InventoryEntry> = emptyList()
): List<Achievement> {
    val totalReps = history.sumOf { it.pullUps + it.dips + it.pushUps }

    return listOf(
        Achievement(
            title = "Первый шаг",
            description = "Завершить первую тренировку",
            unlocked = history.isNotEmpty(),
            id = "first_workout",
            rewardXp = 50,
            iconRes = R.drawable.icon_workout
        ),
        Achievement(
            title = "Сотня",
            description = "Сделать 100 повторений",
            unlocked = totalReps >= 100,
            id = "hundred_reps",
            rewardXp = 75,
            iconRes = R.drawable.icon_pushups
        ),
        Achievement(
            title = "Разогрев",
            description = "Достичь серии 3 дня",
            unlocked = streak >= 3,
            id = "streak_3",
            rewardXp = 100,
            iconRes = R.drawable.icon_history
        ),
        Achievement(
            title = "Железная воля",
            description = "Достичь серии 7 дней",
            unlocked = streak >= 7,
            id = "streak_7",
            rewardXp = 250,
            iconRes = R.drawable.icon_history
        ),
        Achievement(
            title = "Уровень 5",
            description = "Достичь 5 уровня",
            unlocked = getLevelInfo(totalXp).level >= 5,
            id = "level_5",
            rewardXp = 200,
            iconRes = R.drawable.icon_achievements
        ),
        Achievement(
            title = "1000 XP",
            description = "Заработать 1000 XP",
            unlocked = totalXp >= 1000,
            id = "xp_1000",
            rewardXp = 300,
            iconRes = R.drawable.item_crystal
        ),
        Achievement(
            title = "Мастер подтягиваний",
            description = "Сделать 100 подтягиваний",
            unlocked = history.sumOf { it.pullUps } >= 100,
            id = "pullups_100",
            rewardXp = 150,
            iconRes = R.drawable.icon_pullups
        ),
        Achievement(
            title = "Железные руки",
            description = "Сделать 500 повторений",
            unlocked = totalReps >= 500,
            id = "reps_500",
            rewardXp = 200,
            iconRes = R.drawable.icon_arms
        ),
        Achievement(
            title = "Грудной рубеж",
            description = "Сделать 250 отжиманий",
            unlocked = history.sumOf { it.pushUps } >= 250,
            id = "pushups_250",
            rewardXp = 175,
            iconRes = R.drawable.icon_pushups
        ),
        Achievement(
            title = "Король брусьев",
            description = "Сделать 150 повторений на брусьях",
            unlocked = history.sumOf { it.dips } >= 150,
            id = "dips_150",
            rewardXp = 175,
            iconRes = R.drawable.icon_dips
        ),
        Achievement(
            title = "Десятый уровень",
            description = "Достичь 10 уровня",
            unlocked = getLevelInfo(totalXp).level >= 10,
            id = "level_10",
            rewardXp = 450,
            iconRes = R.drawable.icon_achievements
        ),
        Achievement(
            title = "Марафонец",
            description = "Завершить 25 тренировок",
            unlocked = history.size >= 25,
            id = "workouts_25",
            rewardXp = 350,
            iconRes = R.drawable.icon_workout
        ),
        Achievement(
            title = "5000 XP",
            description = "Заработать 5000 XP",
            unlocked = totalXp >= 5000,
            id = "xp_5000",
            rewardXp = 650,
            iconRes = R.drawable.item_legendary_crown
        ),
        Achievement(
            title = "Страж леса",
            description = "Собрать полный сет: броня лесного стража, сапоги странника, железный шлем и стальной наруч",
            unlocked = setOf("forest_armor", "traveler_boots", "iron_helmet", "steel_bracer")
                .all { requiredId -> inventory.any { it.item.id == requiredId && it.count > 0 } },
            id = "set_forest_guardian",
            rewardXp = 300,
            iconRes = R.drawable.item_armor,
            category = AchievementCategory.SET
        ),
        Achievement(
            title = "Ночной чемпион",
            description = "Собрать ночную броню, корону чемпиона, быстрые сапоги и перчатки искры",
            unlocked = setOf("night_armor", "legendary_crown", "swift_boots", "ember_gloves")
                .all { requiredId -> inventory.any { it.item.id == requiredId && it.count > 0 } },
            id = "set_night_champion",
            rewardXp = 500,
            iconRes = R.drawable.item_legendary_crown,
            category = AchievementCategory.SET
        ),
        Achievement(
            title = "Несгибаемый", description = "Завершить 10 тренировок", unlocked = history.size >= 10,
            id = "workouts_10", rewardXp = 150, iconRes = R.drawable.icon_workout
        ),
        Achievement(
            title = "Титан", description = "Сделать 1000 повторений", unlocked = totalReps >= 1000,
            id = "reps_1000", rewardXp = 400, iconRes = R.drawable.icon_arms
        ),
        Achievement(
            title = "Стальной хват", description = "Сделать 300 подтягиваний", unlocked = history.sumOf { it.pullUps } >= 300,
            id = "pullups_300", rewardXp = 350, iconRes = R.drawable.icon_pullups
        ),
        Achievement(
            title = "Владыка брусьев", description = "Сделать 500 повторений на брусьях", unlocked = history.sumOf { it.dips } >= 500,
            id = "dips_500", rewardXp = 400, iconRes = R.drawable.icon_dips
        ),
        Achievement(
            title = "Без выходных", description = "Достичь серии 14 дней", unlocked = streak >= 14,
            id = "streak_14", rewardXp = 500, iconRes = R.drawable.icon_history
        ),
        Achievement(
            title = "Полная коллекция",
            description = "Собрать хотя бы по одному экземпляру каждого предмета",
            unlocked = ITEM_CATALOG.all { item -> inventory.any { it.item.id == item.id && it.count > 0 } },
            id = "all_items_collected",
            rewardXp = 900,
            iconRes = R.drawable.icon_inventory,
            category = AchievementCategory.SET
        )
    )
}

package com.example.ironquest

import java.util.Calendar

/* ================================== УРОВНИ ================================== */

data class LevelInfo(val level: Int, val currentXp: Int, val requiredXp: Int, val totalXpForNextLevel: Int) {
    val progress: Float get() = (currentXp.toFloat() / requiredXp).coerceIn(0f, 1f)
}

/** Уровень 1 -> 2 стоит 100 XP, каждый следующий на 10 XP дороже (как в прошлой версии). */
fun getLevelInfo(totalXp: Int): LevelInfo {
    var level = 1
    var spent = 0
    var required = 100
    while (totalXp >= spent + required) {
        spent += required
        level++
        required = 100 + (level - 1) * 10
    }
    return LevelInfo(level, totalXp - spent, required, spent + required)
}

fun rankTitle(level: Int): LText = when {
    level >= 50 -> LText("Титан", "Titan")
    level >= 40 -> LText("Легенда", "Legend")
    level >= 30 -> LText("Чемпион", "Champion")
    level >= 20 -> LText("Рыцарь", "Knight")
    level >= 15 -> LText("Ветеран", "Veteran")
    level >= 10 -> LText("Воин", "Warrior")
    level >= 5 -> LText("Боец", "Fighter")
    else -> LText("Новобранец", "Recruit")
}

/* ============================ ХАРАКТЕРИСТИКИ ГЕРОЯ ============================ */

data class StatLevel(val points: Int, val level: Int, val current: Int, val required: Int) {
    val progress: Float get() = (current.toFloat() / required).coerceIn(0f, 1f)
}

/** Сумма очков до уровня L: 40(L-1) + 10(L-1)(L-2). */
fun statLevel(points: Int): StatLevel {
    var level = 1
    var spent = 0
    while (true) {
        val need = 40 + 20 * (level - 1)
        if (points < spent + need) return StatLevel(points, level, points - spent, need)
        spent += need
        level++
    }
}

data class HeroStats(
    val points: Map<Stat, Int>,
    val levels: Map<Stat, StatLevel>,
    val gearBonus: Map<Stat, Int>,
    val muscles: Map<Muscle, StatLevel>,
    val perks: Perks,
    val activeSets: List<GearSet>,
    val power: Int,
    val exerciseTotals: Map<String, Int>,
) {
    fun total(stat: Stat): Int = (levels[stat]?.level ?: 1) + (gearBonus[stat] ?: 0)
}

fun computeHeroStats(
    history: List<WorkoutRecord>,
    custom: List<Exercise>,
    equipped: Map<ItemSlot, ItemDefinition>,
    heroLevel: Int,
): HeroStats {
    val points = Stat.values().associateWith { 0 }.toMutableMap()
    val musclePoints = Muscle.values().associateWith { 0f }.toMutableMap()
    val totals = mutableMapOf<String, Int>()
    history.forEach { record ->
        record.entries.forEach { (id, sets) ->
            val amount = sets.sum()
            totals[id] = (totals[id] ?: 0) + amount
            val exercise = exerciseById(id, custom) ?: return@forEach
            exercise.statPoints(amount).forEach { (s, p) -> points[s] = (points[s] ?: 0) + p }
            if (exercise.muscles.isNotEmpty()) {
                val share = amount * exercise.unit.repEquivalent * 2f / exercise.muscles.size
                exercise.muscles.forEach { m -> musclePoints[m] = (musclePoints[m] ?: 0f) + share }
            }
        }
    }
    val bonus = Stat.values().associateWith { 0 }.toMutableMap()
    var perks = Perks()
    equipped.values.forEach { item ->
        item.stats.forEach { (s, v) -> bonus[s] = (bonus[s] ?: 0) + v }
        perks += item.perks
    }
    val equippedIds = equipped.values.map { it.id }.toSet()
    val activeSets = GEAR_SETS.filter { set -> set.pieces.isNotEmpty() && set.pieces.all { it.id in equippedIds } }
    activeSets.forEach { set ->
        set.bonusStats.forEach { (s, v) -> bonus[s] = (bonus[s] ?: 0) + v }
        perks += set.bonusPerks
    }
    val levels = points.mapValues { statLevel(it.value) }
    val power = Stat.values().sumOf { ((levels[it]?.level ?: 1) + (bonus[it] ?: 0)) * 10 } + heroLevel * 5
    return HeroStats(
        points = points, levels = levels, gearBonus = bonus,
        muscles = musclePoints.mapValues { statLevel(it.value.toInt()) },
        perks = perks, activeSets = activeSets, power = power, exerciseTotals = totals,
    )
}

/* ================================ СЕРИЯ И ВЫХОДНЫЕ ================================ */

/** Ключ календарного дня yyyyMMdd в локальном часовом поясе. */
fun dayKey(timestamp: Long): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = timestamp
    return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH)
}

fun calendarOf(dayKey: Int): Calendar = Calendar.getInstance().apply {
    clear()
    set(dayKey / 10000, (dayKey / 100) % 100 - 1, dayKey % 100, 12, 0, 0)
}

fun shiftDay(dayKey: Int, amount: Int): Int {
    val c = calendarOf(dayKey)
    c.add(Calendar.DAY_OF_YEAR, amount)
    return dayKey(c.timeInMillis)
}

/** Calendar.DAY_OF_WEEK (1 = воскресенье ... 7 = суббота). */
fun dayOfWeek(dayKey: Int): Int = calendarOf(dayKey).get(Calendar.DAY_OF_WEEK)

/** Дни недели в порядке «пн..вс». */
val WEEK_ORDER = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

fun weekdayShort(dow: Int, language: AppLanguage): String {
    val ru = mapOf(Calendar.MONDAY to "ПН", Calendar.TUESDAY to "ВТ", Calendar.WEDNESDAY to "СР", Calendar.THURSDAY to "ЧТ",
        Calendar.FRIDAY to "ПТ", Calendar.SATURDAY to "СБ", Calendar.SUNDAY to "ВС")
    val en = mapOf(Calendar.MONDAY to "MO", Calendar.TUESDAY to "TU", Calendar.WEDNESDAY to "WE", Calendar.THURSDAY to "TH",
        Calendar.FRIDAY to "FR", Calendar.SATURDAY to "SA", Calendar.SUNDAY to "SU")
    return (if (language == AppLanguage.RU) ru else en)[dow] ?: "?"
}

fun weekdayFull(dow: Int, language: AppLanguage): String {
    val ru = mapOf(Calendar.MONDAY to "Понедельник", Calendar.TUESDAY to "Вторник", Calendar.WEDNESDAY to "Среда", Calendar.THURSDAY to "Четверг",
        Calendar.FRIDAY to "Пятница", Calendar.SATURDAY to "Суббота", Calendar.SUNDAY to "Воскресенье")
    val en = mapOf(Calendar.MONDAY to "Monday", Calendar.TUESDAY to "Tuesday", Calendar.WEDNESDAY to "Wednesday", Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday", Calendar.SATURDAY to "Saturday", Calendar.SUNDAY to "Sunday")
    return (if (language == AppLanguage.RU) ru else en)[dow] ?: "?"
}

/**
 * Текущая серия: считаются дни с тренировкой. Выходные дни недели и «замороженные» дни
 * не прерывают серию (но и не добавляют к ней). Сегодняшний день, если тренировки ещё не было,
 * тоже не прерывает серию.
 */
fun calculateStreak(
    history: List<WorkoutRecord>,
    restDays: Set<Int> = emptySet(),
    frozenDays: Set<Int> = emptySet(),
    now: Long = System.currentTimeMillis(),
): Int {
    if (history.isEmpty()) return 0
    val days = history.map { dayKey(it.timestamp) }.toSet()
    val earliest = days.minOrNull() ?: return 0
    val today = dayKey(now)
    var cursor = today
    var streak = 0
    var guard = 0
    while (cursor >= earliest && guard < 4000) {
        guard++
        when {
            cursor in days -> streak++
            cursor == today -> Unit
            dayOfWeek(cursor) in restDays || cursor in frozenDays -> Unit
            else -> return streak
        }
        cursor = shiftDay(cursor, -1)
    }
    return streak
}

/** Лучшая серия за всё время (с учётом текущих выходных и заморозок). */
fun bestStreak(
    history: List<WorkoutRecord>,
    restDays: Set<Int> = emptySet(),
    frozenDays: Set<Int> = emptySet(),
    now: Long = System.currentTimeMillis(),
): Int {
    if (history.isEmpty()) return 0
    val days = history.map { dayKey(it.timestamp) }.toSet()
    var cursor = days.minOrNull() ?: return 0
    val today = dayKey(now)
    var best = 0
    var run = 0
    var guard = 0
    while (cursor <= today && guard < 4000) {
        guard++
        when {
            cursor in days -> { run++; best = maxOf(best, run) }
            cursor == today -> Unit
            dayOfWeek(cursor) in restDays || cursor in frozenDays -> Unit
            else -> run = 0
        }
        cursor = shiftDay(cursor, 1)
    }
    return best
}

/**
 * Пропущенные рабочие дни между последней тренировкой и сегодняшним днём (не включая сегодня).
 * Если их можно закрыть заморозками — серия сохранится.
 */
fun missedDaysSinceLastWorkout(
    history: List<WorkoutRecord>,
    restDays: Set<Int>,
    frozenDays: Set<Int>,
    now: Long = System.currentTimeMillis(),
): List<Int> {
    if (history.isEmpty()) return emptyList()
    val days = history.map { dayKey(it.timestamp) }.toSet()
    val last = days.maxOrNull() ?: return emptyList()
    val today = dayKey(now)
    val missed = mutableListOf<Int>()
    var cursor = shiftDay(today, -1)
    var guard = 0
    while (cursor > last && guard < 400) {
        guard++
        if (dayOfWeek(cursor) !in restDays && cursor !in frozenDays) missed.add(cursor)
        cursor = shiftDay(cursor, -1)
    }
    return missed
}

enum class DayState { DONE, REST, FROZEN, MISSED, TODAY, FUTURE }

data class WeekDay(val dayKey: Int, val dow: Int, val state: DayState, val isToday: Boolean)

fun currentWeek(
    history: List<WorkoutRecord>,
    restDays: Set<Int>,
    frozenDays: Set<Int>,
    now: Long = System.currentTimeMillis(),
): List<WeekDay> {
    val days = history.map { dayKey(it.timestamp) }.toSet()
    val today = dayKey(now)
    val c = calendarOf(today)
    val todayDow = c.get(Calendar.DAY_OF_WEEK)
    val offset = WEEK_ORDER.indexOf(todayDow)
    val monday = shiftDay(today, -offset)
    return WEEK_ORDER.mapIndexed { i, dow ->
        val key = shiftDay(monday, i)
        val state = when {
            key in days -> DayState.DONE
            key == today -> if (dow in restDays) DayState.REST else DayState.TODAY
            key > today -> if (dow in restDays) DayState.REST else DayState.FUTURE
            dow in restDays -> DayState.REST
            key in frozenDays -> DayState.FROZEN
            else -> DayState.MISSED
        }
        WeekDay(key, dow, state, key == today)
    }
}

/* ================================= ДОСТИЖЕНИЯ ================================= */

enum class AchievementCategory(val title: LText) {
    WORKOUTS(LText("Тренировки", "Workouts")),
    STREAK(LText("Серии", "Streaks")),
    VOLUME(LText("Объём", "Volume")),
    EXERCISE(LText("Упражнения", "Exercises")),
    LEVEL(LText("Развитие", "Growth")),
    COLLECTION(LText("Коллекция", "Collection")),
    SET(LText("Сеты", "Sets")),
    SPECIAL(LText("Особые", "Special")),
}

data class Achievement(
    val id: String,
    val titleText: LText,
    val descText: LText,
    /** "ui:trophy", "ex:pullup", "item:head_crown:gold". */
    val icon: String,
    val category: AchievementCategory,
    val target: Int,
    val progress: Int,
    val rewardXp: Int,
    val rewardCoins: Int,
) {
    val unlocked: Boolean get() = progress >= target
    val fraction: Float get() = (progress.toFloat() / target).coerceIn(0f, 1f)
    fun title(language: AppLanguage) = titleText.get(language)
    fun description(language: AppLanguage) = descText.get(language)
}

/** Всё, что нужно для подсчёта прогресса достижений. */
data class AchievementInput(
    val history: List<WorkoutRecord>,
    val custom: List<Exercise>,
    val totalXp: Int,
    val bestStreak: Int,
    val ownedItems: Map<String, Int>,
    val counters: Map<String, Int>,
    val stats: HeroStats,
    val equippedCount: Int,
    val ownedThemes: Int,
    val restDaysUnlocked: Int,
    val restDays: Set<Int>,
)

private fun a(
    id: String, ru: String, en: String, dru: String, den: String, icon: String, cat: AchievementCategory,
    target: Int, progress: Int, xp: Int, coins: Int,
) = Achievement(id, LText(ru, en), LText(dru, den), icon, cat, target, progress.coerceAtMost(target), xp, coins)

fun getAchievements(input: AchievementInput): List<Achievement> {
    val h = input.history
    val workouts = h.size
    val reps = h.sumOf { it.repEquivalent(input.custom) }
    val level = getLevelInfo(input.totalXp).level
    val totals = input.stats.exerciseTotals
    fun ex(id: String) = totals[id] ?: 0
    val distinctOwned = input.ownedItems.count { it.value > 0 }
    val maxRarityOwned = ITEM_CATALOG.filter { (input.ownedItems[it.id] ?: 0) > 0 }.maxOfOrNull { it.rarity.ordinal } ?: -1
    val statLevels = input.stats.levels.values.map { it.level }
    val bestWorkout = h.maxOfOrNull { it.repEquivalent(input.custom) } ?: 0
    val maxExercisesInWorkout = h.maxOfOrNull { it.entries.size } ?: 0
    val variety = totals.count { it.value > 0 }
    val hours = h.map { java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(java.util.Calendar.HOUR_OF_DAY) }
    val restWorkout = h.any { dayOfWeek(dayKey(it.timestamp)) in input.restDays }
    val counters = input.counters
    val W = AchievementCategory.WORKOUTS
    val S = AchievementCategory.STREAK
    val V = AchievementCategory.VOLUME
    val E = AchievementCategory.EXERCISE
    val Lv = AchievementCategory.LEVEL
    val C = AchievementCategory.COLLECTION
    val Sp = AchievementCategory.SPECIAL

    val list = mutableListOf(
        a("first_workout", "Первый шаг", "First Step", "Заверши первую тренировку", "Complete your first workout", "ui:sword", W, 1, workouts, 50, 10),
        a("workouts_5", "Разминка окончена", "Warmed Up", "Заверши 5 тренировок", "Complete 5 workouts", "ui:sword", W, 5, workouts, 100, 20),
        a("workouts_10", "Несгибаемый", "Unbending", "Заверши 10 тренировок", "Complete 10 workouts", "ui:sword", W, 10, workouts, 150, 30),
        a("workouts_25", "Марафонец", "Marathoner", "Заверши 25 тренировок", "Complete 25 workouts", "ui:sword", W, 25, workouts, 350, 50),
        a("workouts_50", "Закалённый", "Battle-Hardened", "Заверши 50 тренировок", "Complete 50 workouts", "ui:shield", W, 50, workouts, 600, 100),
        a("workouts_100", "Сотня битв", "Hundred Battles", "Заверши 100 тренировок", "Complete 100 workouts", "ui:crown", W, 100, workouts, 1000, 200),
        a("workouts_250", "Живая легенда", "Living Legend", "Заверши 250 тренировок", "Complete 250 workouts", "ui:crown", W, 250, workouts, 2000, 400),

        a("streak_3", "Разогрев", "Warm-Up", "Достигни серии 3 дня", "Reach a 3-day streak", "ui:flame", S, 3, input.bestStreak, 100, 20),
        a("streak_7", "Железная воля", "Iron Will", "Достигни серии 7 дней", "Reach a 7-day streak", "ui:flame", S, 7, input.bestStreak, 250, 40),
        a("streak_14", "Без отговорок", "No Excuses", "Достигни серии 14 дней", "Reach a 14-day streak", "ui:flame", S, 14, input.bestStreak, 500, 80),
        a("streak_30", "Месяц силы", "Month of Might", "Достигни серии 30 дней", "Reach a 30-day streak", "ui:flame", S, 30, input.bestStreak, 1000, 150),
        a("streak_60", "Пламя не гаснет", "Undying Flame", "Достигни серии 60 дней", "Reach a 60-day streak", "ui:flame", S, 60, input.bestStreak, 1800, 250),
        a("streak_100", "Вечный огонь", "Eternal Fire", "Достигни серии 100 дней", "Reach a 100-day streak", "ui:sparkle", S, 100, input.bestStreak, 3000, 500),

        a("hundred_reps", "Сотня", "One Hundred", "Сделай 100 повторений", "Do 100 reps", "ui:muscle", V, 100, reps, 75, 15),
        a("reps_500", "Железные руки", "Iron Arms", "Сделай 500 повторений", "Do 500 reps", "ui:muscle", V, 500, reps, 200, 30),
        a("reps_1000", "Титан", "Titan", "Сделай 1 000 повторений", "Do 1,000 reps", "ui:muscle", V, 1000, reps, 400, 60),
        a("reps_2500", "Гора мышц", "Mountain of Muscle", "Сделай 2 500 повторений", "Do 2,500 reps", "ui:muscle", V, 2500, reps, 700, 100),
        a("reps_5000", "Неутомимый", "Tireless", "Сделай 5 000 повторений", "Do 5,000 reps", "ui:bolt", V, 5000, reps, 1200, 180),
        a("reps_10000", "Десять тысяч", "Ten Thousand", "Сделай 10 000 повторений", "Do 10,000 reps", "ui:bolt", V, 10000, reps, 2500, 400),
        a("big_workout_100", "Мощный подход", "Power Session", "Сделай 100+ повторений за одну тренировку", "Do 100+ reps in one workout", "ui:bolt", V, 100, bestWorkout, 200, 40),
        a("big_workout_300", "Босс-рейд", "Boss Raid", "Сделай 300+ повторений за одну тренировку", "Do 300+ reps in one workout", "ui:skull", V, 300, bestWorkout, 500, 80),

        a("pullups_100", "Мастер подтягиваний", "Pull-Up Master", "Сделай 100 подтягиваний", "Do 100 pull-ups", "ex:pullup", E, 100, ex("pullups"), 150, 25),
        a("pullups_300", "Стальной хват", "Steel Grip", "Сделай 300 подтягиваний", "Do 300 pull-ups", "ex:pullup", E, 300, ex("pullups"), 350, 50),
        a("pullups_1000", "Повелитель турника", "Bar Overlord", "Сделай 1 000 подтягиваний", "Do 1,000 pull-ups", "ex:pullup", E, 1000, ex("pullups"), 1000, 150),
        a("pushups_250", "Грудной рубеж", "Chest Frontier", "Сделай 250 отжиманий", "Do 250 push-ups", "ex:pushup", E, 250, ex("pushups"), 175, 25),
        a("pushups_1000", "Тысяча отжиманий", "Thousand Push-Ups", "Сделай 1 000 отжиманий", "Do 1,000 push-ups", "ex:pushup", E, 1000, ex("pushups"), 500, 80),
        a("pushups_5000", "Отжимающийся бог", "Push-Up Deity", "Сделай 5 000 отжиманий", "Do 5,000 push-ups", "ex:pushup", E, 5000, ex("pushups"), 1500, 250),
        a("dips_150", "Король брусьев", "Dip King", "Сделай 150 повторений на брусьях", "Do 150 dips", "ex:dips", E, 150, ex("dips"), 175, 25),
        a("dips_500", "Владыка брусьев", "Dip Overlord", "Сделай 500 повторений на брусьях", "Do 500 dips", "ex:dips", E, 500, ex("dips"), 400, 60),
        a("squats_500", "Крепкие ноги", "Strong Legs", "Сделай 500 приседаний", "Do 500 squats", "ex:squat", E, 500, ex("squats"), 300, 40),
        a("squats_2000", "Ноги-колонны", "Pillar Legs", "Сделай 2 000 приседаний", "Do 2,000 squats", "ex:squat", E, 2000, ex("squats"), 800, 120),
        a("plank_600", "Каменный пресс", "Stone Core", "Простой в планке 10 минут суммарно", "Hold a plank for 10 minutes total", "ex:plank", E, 600, ex("plank"), 250, 40),
        a("plank_3600", "Скала", "The Rock", "Простой в планке 1 час суммарно", "Hold a plank for 1 hour total", "ex:plank", E, 3600, ex("plank"), 900, 150),
        a("burpees_100", "Любитель бёрпи", "Burpee Lover", "Сделай 100 бёрпи", "Do 100 burpees", "ex:burpee", E, 100, ex("burpees"), 300, 50),
        a("running_60", "Бегущий", "Runner", "Пробеги 60 минут суммарно", "Run 60 minutes total", "ex:run", E, 60, ex("running"), 300, 50),
        a("variety_5", "Универсал", "All-Rounder", "Выполни 5 разных упражнений", "Do 5 different exercises", "ui:book", E, 5, variety, 200, 40),
        a("combo_4", "Комбо", "Combo", "Сделай 4+ упражнения за одну тренировку", "Do 4+ exercises in one workout", "ui:sparkle", E, 4, maxExercisesInWorkout, 150, 30),
        a("custom_exercise", "Изобретатель", "Inventor", "Создай своё упражнение", "Create a custom exercise", "ui:pencil", E, 1, input.custom.size, 100, 20),

        a("level_5", "Уровень 5", "Level 5", "Достигни 5 уровня", "Reach level 5", "ui:star", Lv, 5, level, 200, 30),
        a("level_10", "Десятый уровень", "Level 10", "Достигни 10 уровня", "Reach level 10", "ui:star", Lv, 10, level, 450, 60),
        a("level_20", "Рыцарь", "Knight", "Достигни 20 уровня", "Reach level 20", "ui:star", Lv, 20, level, 900, 120),
        a("level_30", "Чемпион", "Champion", "Достигни 30 уровня", "Reach level 30", "ui:crown", Lv, 30, level, 1500, 200),
        a("level_50", "Титан", "Titan", "Достигни 50 уровня", "Reach level 50", "ui:crown", Lv, 50, level, 3000, 500),
        a("xp_1000", "1000 XP", "1000 XP", "Заработай 1 000 опыта", "Earn 1,000 XP", "ui:star", Lv, 1000, input.totalXp, 300, 40),
        a("xp_5000", "5000 XP", "5000 XP", "Заработай 5 000 опыта", "Earn 5,000 XP", "ui:star", Lv, 5000, input.totalXp, 650, 80),
        a("xp_20000", "20000 XP", "20000 XP", "Заработай 20 000 опыта", "Earn 20,000 XP", "ui:sparkle", Lv, 20000, input.totalXp, 1500, 200),
        a("stat_10", "Специалист", "Specialist", "Подними любую характеристику до 10", "Raise any attribute to 10", "ui:fist", Lv, 10, statLevels.maxOrNull() ?: 1, 300, 50),
        a("stat_25", "Мастер", "Master", "Подними любую характеристику до 25", "Raise any attribute to 25", "ui:fist", Lv, 25, statLevels.maxOrNull() ?: 1, 800, 120),
        a("all_stats_10", "Гармония", "Harmony", "Подними все характеристики до 10", "Raise all attributes to 10", "ui:shield", Lv, 10, statLevels.minOrNull() ?: 1, 1000, 150),

        a("items_5", "Собиратель", "Gatherer", "Собери 5 разных предметов", "Collect 5 different items", "ui:backpack", C, 5, distinctOwned, 100, 20),
        a("items_15", "Коллекционер", "Collector", "Собери 15 разных предметов", "Collect 15 different items", "ui:backpack", C, 15, distinctOwned, 300, 50),
        a("items_40", "Хранитель сокровищ", "Treasure Keeper", "Собери 40 разных предметов", "Collect 40 different items", "ui:chest_open", C, 40, distinctOwned, 800, 120),
        a("all_items_collected", "Полная коллекция", "Complete Collection", "Собери все предметы игры", "Collect every item in the game", "ui:crown", C, ITEM_CATALOG.size, distinctOwned, 3000, 500),
        a("legendary_found", "Легендарная находка", "Legendary Find", "Получи легендарный предмет", "Obtain a legendary item", "item:coll_gem:gold", C, 1, if (maxRarityOwned >= ItemRarity.LEGENDARY.ordinal || (counters["legendary_found"] ?: 0) > 0) 1 else 0, 400, 60),
        a("mythic_found", "Мифическая находка", "Mythic Find", "Получи мифический предмет", "Obtain a mythic item", "item:coll_orb:void", C, 1, if (maxRarityOwned >= ItemRarity.MYTHIC.ordinal || (counters["mythic_found"] ?: 0) > 0) 1 else 0, 1000, 150),
        a("cases_1", "Первый сундук", "First Chest", "Открой сундук в лавке", "Open a chest in the shop", "ui:chest", C, 1, counters["cases_opened"] ?: 0, 50, 10),
        a("cases_10", "Охотник за сундуками", "Chest Hunter", "Открой 10 сундуков", "Open 10 chests", "ui:chest", C, 10, counters["cases_opened"] ?: 0, 300, 50),
        a("cases_50", "Расхититель", "Raider", "Открой 50 сундуков", "Open 50 chests", "ui:chest_open", C, 50, counters["cases_opened"] ?: 0, 1000, 150),
        a("coins_1000", "Богач", "Rich", "Заработай 1 000 монет за всё время", "Earn 1,000 coins in total", "ui:coin", C, 1000, counters["coins_earned"] ?: 0, 300, 0),
        a("full_equip", "При полном параде", "Fully Geared", "Заполни все 7 слотов экипировки", "Fill all 7 equipment slots", "ui:shield", C, ItemSlot.values().size, input.equippedCount, 300, 50),

        a("theme_1", "Дизайнер", "Designer", "Купи новую тему оформления", "Buy a new app theme", "ui:palette", Sp, 1, (input.ownedThemes - 1).coerceAtLeast(0), 150, 0),
        a("rest_day_2", "Право на отдых", "Right to Rest", "Открой второй выходной", "Unlock a second rest day", "ui:moon", Sp, 2, input.restDaysUnlocked, 100, 0),
        a("freeze_used", "Ледяная броня", "Ice Armor", "Заморозка спасла твою серию", "A freeze saved your streak", "ui:snowflake", Sp, 1, counters["freezes_used"] ?: 0, 150, 20),
        a("early_bird", "Ранняя пташка", "Early Bird", "Тренировка до 8:00 утра", "Train before 8:00 AM", "ui:sparkle", Sp, 1, if (hours.any { it < 8 }) 1 else 0, 150, 20),
        a("night_owl", "Ночная сова", "Night Owl", "Тренировка после 22:00", "Train after 10:00 PM", "ui:moon", Sp, 1, if (hours.any { it >= 22 }) 1 else 0, 150, 20),
        a("rest_warrior", "Воин выходного дня", "Weekend Warrior", "Потренируйся в свой выходной", "Train on your rest day", "ui:calendar", Sp, 1, if (restWorkout) 1 else 0, 150, 20),
    )

    GEAR_SETS.forEach { set ->
        val pieces = set.pieces
        val owned = pieces.count { (input.ownedItems[it.id] ?: 0) > 0 }
        val maxRarity = pieces.maxOfOrNull { it.rarity.ordinal } ?: 0
        val (xp, coins) = when (maxRarity) {
            0 -> 150 to 30; 1 -> 250 to 40; 2 -> 300 to 50; 3 -> 500 to 80; 4 -> 1000 to 150; else -> 2000 to 400
        }
        val icon = pieces.firstOrNull { it.slot == ItemSlot.HEAD } ?: pieces.firstOrNull()
        list.add(
            Achievement(
                id = "set_${set.id}",
                titleText = set.title,
                descText = LText("Собери все предметы сета (${pieces.size})", "Collect every piece of the set (${pieces.size})"),
                icon = "item:${icon?.shape ?: "body_tunic"}:${icon?.material ?: "iron"}",
                category = AchievementCategory.SET,
                target = pieces.size,
                progress = owned,
                rewardXp = xp,
                rewardCoins = coins,
            )
        )
    }
    return list
}

package com.example.ironquest

import org.json.JSONArray
import org.json.JSONObject

enum class ExerciseUnit(val short: LText, val step: Int) {
    REPS(LText("повт.", "reps"), 1),
    SECONDS(LText("сек", "sec"), 5),
    MINUTES(LText("мин", "min"), 1);

    /** Сколько «условных повторений» даёт одна единица: для дропа, монет и статистики. */
    val repEquivalent: Float
        get() = when (this) {
            REPS -> 1f
            SECONDS -> 0.2f
            MINUTES -> 8f
        }
}

enum class Difficulty(val title: LText, val factor: Float) {
    EASY(LText("Лёгкое", "Easy"), 0.6f),
    NORMAL(LText("Среднее", "Normal"), 1f),
    HARD(LText("Тяжёлое", "Hard"), 1.5f);
}

data class Exercise(
    val id: String,
    val title: LText,
    val icon: String,
    val unit: ExerciseUnit,
    /** Веса влияния на характеристики (сумма обычно 3–4). */
    val stats: Map<Stat, Int>,
    val muscles: Set<Muscle>,
    /** Опыт за одну единицу (повторение / секунду / минуту). */
    val xpPerUnit: Float,
    val custom: Boolean = false,
    val archived: Boolean = false,
    val difficulty: Difficulty = Difficulty.NORMAL,
) {
    fun name(language: AppLanguage) = title.get(language)

    fun xpFor(amount: Int): Float = amount * xpPerUnit

    /** Очки характеристик за выполненный объём. */
    fun statPoints(amount: Int): Map<Stat, Int> {
        val eq = amount * unit.repEquivalent
        return stats.mapValues { (_, w) -> (eq * w).toInt() }
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("ru", title.ru); put("en", title.en); put("icon", icon); put("unit", unit.name)
        put("xp", xpPerUnit.toDouble()); put("arch", archived); put("diff", difficulty.name)
        put("stats", JSONObject().apply { stats.forEach { (s, w) -> put(s.name, w) } })
        put("muscles", JSONArray().apply { muscles.forEach { put(it.name) } })
    }

    companion object {
        fun fromJson(o: JSONObject): Exercise? = try {
            val st = o.getJSONObject("stats")
            val stats = Stat.values().mapNotNull { s -> if (st.has(s.name)) s to st.getInt(s.name) else null }.toMap()
            val mArr = o.optJSONArray("muscles") ?: JSONArray()
            val muscles = (0 until mArr.length()).mapNotNull { i -> runCatching { Muscle.valueOf(mArr.getString(i)) }.getOrNull() }.toSet()
            Exercise(
                id = o.getString("id"),
                title = LText(o.getString("ru"), o.optString("en", o.getString("ru"))),
                icon = o.optString("icon", "dumbbell"),
                unit = runCatching { ExerciseUnit.valueOf(o.getString("unit")) }.getOrDefault(ExerciseUnit.REPS),
                stats = stats.ifEmpty { mapOf(Stat.STR to 2) },
                muscles = muscles,
                xpPerUnit = o.optDouble("xp", 1.0).toFloat(),
                custom = true,
                archived = o.optBoolean("arch", false),
                difficulty = runCatching { Difficulty.valueOf(o.optString("diff", "NORMAL")) }.getOrDefault(Difficulty.NORMAL),
            )
        } catch (_: Exception) {
            null
        }

        /** Базовый опыт за единицу для своих упражнений. */
        fun baseXp(unit: ExerciseUnit, difficulty: Difficulty): Float = when (unit) {
            ExerciseUnit.REPS -> 1f
            ExerciseUnit.SECONDS -> 0.2f
            ExerciseUnit.MINUTES -> 5f
        } * difficulty.factor

        fun customStats(primary: Stat, secondary: Stat?, difficulty: Difficulty): Map<Stat, Int> {
            val base = if (difficulty == Difficulty.HARD) 3 else 2
            return if (secondary == null || secondary == primary) mapOf(primary to base + 1)
            else mapOf(primary to base, secondary to 1 + if (difficulty == Difficulty.HARD) 1 else 0)
        }
    }
}

private fun ex(
    id: String, ru: String, en: String, icon: String, unit: ExerciseUnit, xp: Float,
    stats: Map<Stat, Int>, vararg muscles: Muscle,
) = Exercise(id, LText(ru, en), icon, unit, stats, muscles.toSet(), xp)

/** Встроенные упражнения. ID первых трёх совпадают со старой историей тренировок. */
val BUILTIN_EXERCISES: List<Exercise> = listOf(
    ex("pullups", "Подтягивания", "Pull-ups", "pullup", ExerciseUnit.REPS, 1.5f,
        mapOf(Stat.STR to 3, Stat.END to 1), Muscle.BACK, Muscle.ARMS),
    ex("dips", "Брусья", "Dips", "dips", ExerciseUnit.REPS, 1.3f,
        mapOf(Stat.STR to 3, Stat.END to 1), Muscle.CHEST, Muscle.ARMS, Muscle.SHOULDERS),
    ex("pushups", "Отжимания", "Push-ups", "pushup", ExerciseUnit.REPS, 1f,
        mapOf(Stat.STR to 2, Stat.END to 2), Muscle.CHEST, Muscle.ARMS, Muscle.SHOULDERS),
    ex("squats", "Приседания", "Squats", "squat", ExerciseUnit.REPS, 0.8f,
        mapOf(Stat.STR to 1, Stat.END to 2, Stat.AGI to 1), Muscle.LEGS),
    ex("lunges", "Выпады", "Lunges", "lunge", ExerciseUnit.REPS, 0.9f,
        mapOf(Stat.AGI to 2, Stat.STR to 1, Stat.END to 1), Muscle.LEGS),
    ex("plank", "Планка", "Plank", "plank", ExerciseUnit.SECONDS, 0.25f,
        mapOf(Stat.VIT to 3, Stat.END to 1), Muscle.CORE, Muscle.SHOULDERS),
    ex("crunches", "Скручивания", "Crunches", "crunch", ExerciseUnit.REPS, 0.6f,
        mapOf(Stat.VIT to 3), Muscle.CORE),
    ex("leg_raises", "Подъёмы ног", "Leg Raises", "legraise", ExerciseUnit.REPS, 0.9f,
        mapOf(Stat.VIT to 3, Stat.STR to 1), Muscle.CORE),
    ex("burpees", "Бёрпи", "Burpees", "burpee", ExerciseUnit.REPS, 1.6f,
        mapOf(Stat.AGI to 2, Stat.END to 2, Stat.STR to 1), Muscle.LEGS, Muscle.CHEST, Muscle.CORE),
    ex("jumping_jacks", "Прыжки «звезда»", "Jumping Jacks", "jack", ExerciseUnit.REPS, 0.4f,
        mapOf(Stat.AGI to 2, Stat.END to 1), Muscle.LEGS, Muscle.SHOULDERS),
    ex("jump_rope", "Скакалка", "Jump Rope", "rope", ExerciseUnit.REPS, 0.15f,
        mapOf(Stat.AGI to 2, Stat.END to 2), Muscle.LEGS),
    ex("running", "Бег", "Running", "run", ExerciseUnit.MINUTES, 4f,
        mapOf(Stat.END to 3, Stat.AGI to 1), Muscle.LEGS),
    ex("curls", "Сгибания на бицепс", "Biceps Curls", "curl", ExerciseUnit.REPS, 0.7f,
        mapOf(Stat.STR to 3), Muscle.ARMS),
    ex("kettlebell", "Махи гирей", "Kettlebell Swings", "kettlebell", ExerciseUnit.REPS, 1f,
        mapOf(Stat.STR to 2, Stat.END to 1, Stat.AGI to 1), Muscle.LEGS, Muscle.BACK, Muscle.CORE),
    ex("glute_bridge", "Ягодичный мост", "Glute Bridge", "bridge", ExerciseUnit.REPS, 0.6f,
        mapOf(Stat.VIT to 2, Stat.STR to 1), Muscle.LEGS, Muscle.CORE),
)

/** Упражнения по умолчанию в тренировке (как в прошлой версии приложения). */
val DEFAULT_SELECTED_EXERCISES = setOf("pullups", "dips", "pushups")

val EXERCISE_ICON_CHOICES = listOf(
    "dumbbell", "kettlebell", "pullup", "pushup", "dips", "squat", "lunge", "plank", "crunch",
    "legraise", "burpee", "jack", "rope", "run", "curl", "bridge", "star", "bolt", "heart",
)

fun allExercises(custom: List<Exercise>): List<Exercise> = BUILTIN_EXERCISES + custom

fun exerciseById(id: String, custom: List<Exercise>): Exercise? =
    BUILTIN_EXERCISES.firstOrNull { it.id == id } ?: custom.firstOrNull { it.id == id }

/** Форматирование объёма с учётом единиц. */
fun formatAmount(amount: Int, unit: ExerciseUnit, language: AppLanguage): String = when (unit) {
    ExerciseUnit.REPS -> "$amount"
    ExerciseUnit.SECONDS -> if (amount >= 60) "%d:%02d".format(amount / 60, amount % 60) else "${amount}${tr(language, "с", "s")}"
    ExerciseUnit.MINUTES -> "$amount ${tr(language, "мин", "min")}"
}

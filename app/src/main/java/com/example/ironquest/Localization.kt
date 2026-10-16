package com.example.ironquest

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    RU("ru", "Russian", "Русский"),
    EN("en", "English", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage = values().firstOrNull { it.code == code } ?: RU
    }
}

object AppPreferences {
    private const val PREFS = "ironquest_app_settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_REST_TIMER = "rest_timer_seconds"
    private const val KEY_HAPTICS = "haptics_enabled"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getLanguage(context: Context): AppLanguage =
        AppLanguage.fromCode(prefs(context).getString(KEY_LANGUAGE, AppLanguage.RU.code))

    fun setLanguage(context: Context, language: AppLanguage) {
        prefs(context).edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    /** 0 = таймер отдыха выключен. */
    fun restTimerSeconds(context: Context): Int = prefs(context).getInt(KEY_REST_TIMER, 60)
    fun setRestTimerSeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_REST_TIMER, seconds).apply()
    }

    fun hapticsEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_HAPTICS, true)
    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }
}

/** Двуязычная строка для каталогов (предметы, упражнения, достижения). */
data class LText(val ru: String, val en: String) {
    fun get(language: AppLanguage): String = if (language == AppLanguage.RU) ru else en
}

val LocalLanguage = staticCompositionLocalOf { AppLanguage.RU }

/** Короткий способ локализовать строку прямо в UI. */
@Composable
fun tr(ru: String, en: String): String = if (LocalLanguage.current == AppLanguage.RU) ru else en

fun tr(language: AppLanguage, ru: String, en: String): String = if (language == AppLanguage.RU) ru else en

@Composable
fun LText.text(): String = get(LocalLanguage.current)

/** Склонение русских существительных: 1 день, 2 дня, 5 дней. */
fun plural(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> one
        mod10 in 2..4 && mod100 !in 12..14 -> few
        else -> many
    }
}

@Composable
fun daysLabel(n: Int): String = tr("$n ${plural(n, "день", "дня", "дней")}", if (n == 1) "1 day" else "$n days")

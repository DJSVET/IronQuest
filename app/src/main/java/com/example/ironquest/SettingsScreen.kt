package com.example.ironquest

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    state: PlayerState,
    haptics: Boolean,
    onBack: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onSaveRestDays: (Set<Int>) -> Unit,
    onResetProgress: () -> Unit,
) {
    val context = LocalContext.current
    val pal = LocalPalette.current
    val lang = LocalLanguage.current
    val scope = rememberCoroutineScope()
    val prefs = remember { ReminderSettings.preferences(context) }

    var showReset by remember { mutableStateOf(false) }
    var showRest by remember { mutableStateOf(false) }
    var showHero by remember { mutableStateOf(false) }
    var workoutReminder by remember { mutableStateOf(ReminderSettings.workoutEnabled(context)) }
    var streakReminder by remember { mutableStateOf(ReminderSettings.streakEnabled(context)) }
    var hour by remember { mutableStateOf(ReminderSettings.hour(context)) }
    var minute by remember { mutableStateOf(ReminderSettings.minute(context)) }
    var permissionMessage by remember { mutableStateOf("") }
    var updateMessage by remember(lang) { mutableStateOf(tr(lang, "Нажми, чтобы проверить последнюю версию на GitHub", "Tap to check the latest version on GitHub")) }
    var updateRelease by remember { mutableStateOf<ReleaseUpdate?>(null) }
    var checking by remember { mutableStateOf(false) }
    var updateNotifications by remember { mutableStateOf(prefs.getBoolean("update_notifications_enabled", true)) }
    val versionName = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—" }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionMessage = if (granted) tr(lang, "Уведомления разрешены", "Notifications allowed")
        else tr(lang, "Разреши уведомления в настройках Android, чтобы получать напоминания", "Allow notifications in Android settings to get reminders")
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun saveReminders() {
        prefs.edit()
            .putBoolean(ReminderSettings.KEY_WORKOUT_ENABLED, workoutReminder)
            .putBoolean(ReminderSettings.KEY_STREAK_ENABLED, streakReminder)
            .putInt(ReminderSettings.KEY_HOUR, hour)
            .putInt(ReminderSettings.KEY_MINUTE, minute)
            .apply()
        ReminderScheduler.update(context)
    }

    fun checkUpdates() {
        if (checking) return
        checking = true
        updateMessage = tr(lang, "Проверяем GitHub Releases...", "Checking GitHub Releases...")
        scope.launch {
            try {
                val release = ReleaseUpdateChecker.check(versionName)
                updateRelease = release
                updateMessage = if (release == null) tr(lang, "Установлена последняя версия", "You have the latest version")
                else tr(lang, "Доступно обновление: v${release.version}", "Update available: v${release.version}")
            } catch (_: Exception) {
                updateMessage = tr(lang, "Не удалось проверить. Проверь интернет.", "Couldn't check. Check your connection.")
            } finally {
                checking = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        PixelTopBar(tr("НАСТРОЙКИ", "SETTINGS"), "gear", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            SectionHeader(tr("ГЕРОЙ", "HERO"), "castle")
            PixelPanel(Modifier.fillMaxWidth()) {
                SettingsLink("pencil", tr("Имя и внешность", "Name and look"), state.heroName.ifBlank { tr("Герой", "Hero") }) { showHero = true }
                Divider()
                SettingsLink("moon", tr("Выходные дни", "Rest days"),
                    if (state.restDays.isEmpty()) tr("не выбраны", "none") else WEEK_ORDER.filter { it in state.restDays }.joinToString(", ") { weekdayShort(it, lang) } +
                        " · ${state.restDays.size}/${state.restDaysUnlocked}") { showRest = true }
            }

            SectionHeader(tr("ЯЗЫК", "LANGUAGE"), "globe")
            PixelPanel(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { option ->
                        PixelChip(option.nativeName, lang == option, { onLanguageChange(option) }, Modifier.weight(1f))
                    }
                }
            }

            SectionHeader(tr("УВЕДОМЛЕНИЯ", "NOTIFICATIONS"), "bell")
            PixelPanel(Modifier.fillMaxWidth()) {
                SwitchRow(tr("Напоминать о тренировке", "Workout reminder"), tr("Каждый день в выбранное время", "Every day at the chosen time"), workoutReminder) {
                    workoutReminder = it; if (it) requestPermission(); saveReminders()
                }
                Divider()
                SwitchRow(tr("Беречь серию", "Protect streak"), tr("Напомнит, если сегодня ещё не было тренировки (не в выходной)", "Reminds you if you haven't trained today (not on rest days)"), streakReminder) {
                    streakReminder = it; if (it) requestPermission(); saveReminders()
                }
                Divider()
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable {
                        TimePickerDialog(context, { _, h, m -> hour = h; minute = m; saveReminders() }, hour, minute, true).show()
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PixelIcon("hourglass", 22.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        PText(tr("Время напоминания", "Reminder time"), bold = true)
                        PText(tr("Нажми, чтобы изменить", "Tap to change"), color = pal.muted, size = 13.sp)
                    }
                    TitleText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute), size = 13.sp, color = pal.accent)
                }
                if (permissionMessage.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    PText(permissionMessage, color = pal.muted, size = 13.sp)
                }
            }

            SectionHeader(tr("ОЩУЩЕНИЯ", "FEEL"), "sparkle")
            PixelPanel(Modifier.fillMaxWidth()) {
                SwitchRow(tr("Вибрация", "Haptics"), tr("Отклик кнопок, щелчки рулетки, конец отдыха", "Button feedback, reel ticks, rest timer end"), haptics, onHapticsChange)
            }

            SectionHeader(tr("ОБНОВЛЕНИЯ", "UPDATES"), "arrow_up")
            PixelPanel(Modifier.fillMaxWidth()) {
                SwitchRow(tr("Сообщать о новых версиях", "Notify about new versions"), tr("Проверять GitHub Releases раз в сутки", "Check GitHub Releases once a day"), updateNotifications) { enabled ->
                    updateNotifications = enabled
                    prefs.edit().putBoolean("update_notifications_enabled", enabled).apply()
                    if (enabled) { requestPermission(); UpdateCheckScheduler.schedule(context.applicationContext) }
                }
                Divider()
                PText(updateMessage, color = pal.muted, size = 14.sp)
                Spacer(Modifier.height(10.dp))
                PixelButton(if (checking) tr("ПРОВЕРКА...", "CHECKING...") else tr("ПРОВЕРИТЬ", "CHECK"), { checkUpdates() }, Modifier.fillMaxWidth(),
                    enabled = !checking, height = 48.dp, fontSize = 9.sp, icon = "arrow_up")
            }

            SectionHeader(tr("О ПРИЛОЖЕНИИ", "ABOUT"), "info")
            PixelPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PText(tr("Версия", "Version"), bold = true, modifier = Modifier.weight(1f))
                    PText(versionName, color = pal.accent, bold = true)
                }
                Divider()
                SettingsLink("globe", tr("GitHub проекта", "Project GitHub"), "DJSVET/IronQuest") {
                    runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/DJSVET/IronQuest"))) }
                }
                Divider()
                PText(tr("Шрифты: Press Start 2P и Tiny5 (SIL Open Font License).", "Fonts: Press Start 2P and Tiny5 (SIL Open Font License)."), color = pal.muted, size = 12.sp)
            }

            SectionHeader(tr("ОПАСНАЯ ЗОНА", "DANGER ZONE"), "skull")
            PixelButton(tr("СБРОСИТЬ ВСЁ", "RESET ALL"), { showReset = true }, Modifier.fillMaxWidth(), color = pal.danger, textColor = Color.White, icon = "trash", fontSize = 10.sp)
            Spacer(Modifier.height(12.dp))
            PText(tr("Настройки хранятся на устройстве. Android может немного сдвигать время уведомлений.",
                "Settings are stored on this device. Android may slightly delay notifications."), color = pal.muted, size = 13.sp)
        }
    }

    updateRelease?.let { release ->
        ConfirmDialog(
            tr("ДОСТУПНО ОБНОВЛЕНИЕ", "UPDATE AVAILABLE"),
            tr("Версия v${release.version} опубликована на GitHub.", "Version v${release.version} is published on GitHub."),
            if (release.apkUrl != null) tr("СКАЧАТЬ", "DOWNLOAD") else tr("ОТКРЫТЬ", "OPEN"),
            onConfirm = {
                runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(release.apkUrl ?: release.url))) }
                updateRelease = null
            },
            onDismiss = { updateRelease = null }, icon = "arrow_up", cancel = tr("ПОЗЖЕ", "LATER"),
        )
    }
    if (showReset) {
        ConfirmDialog(
            tr("СБРОСИТЬ ПРОГРЕСС?", "RESET PROGRESS?"),
            tr("Будут удалены уровень, опыт, история, достижения, монеты, предметы, темы и свои упражнения. Отменить нельзя.",
                "Level, XP, history, achievements, coins, items, themes and custom exercises will be deleted. This can't be undone."),
            tr("СБРОСИТЬ", "RESET"), onConfirm = { showReset = false; onResetProgress() }, onDismiss = { showReset = false }, icon = "skull", danger = true,
        )
    }
    if (showRest) RestDaysDialog(state, onDismiss = { showRest = false }, onSave = { onSaveRestDays(it); showRest = false })
    if (showHero) HeroEditDialog(state, onDismiss = { showHero = false }, onSave = { n, l ->
        scope.launch { PlayerData.setHeroName(context, n); PlayerData.setHeroLook(context, l) }
        showHero = false
    })
}

@Composable
private fun Divider() {
    val pal = LocalPalette.current
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp).height(2.dp).background(pal.panelLo))
}

@Composable
private fun SettingsLink(icon: String, title: String, value: String, onClick: () -> Unit) {
    val pal = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PixelIcon(icon, 22.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            PText(title, bold = true)
            PText(value, color = pal.muted, size = 13.sp, maxLines = 1)
        }
        PText("›", color = pal.accent, size = 22.sp, bold = true)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val pal = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Switch) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            PText(title, bold = true)
            PText(subtitle, color = pal.muted, size = 13.sp)
        }
        PixelSwitch(checked)
    }
}

/** Пиксельный переключатель. */
@Composable
fun PixelSwitch(checked: Boolean) {
    val pal = LocalPalette.current
    val x by animateDpAsState(if (checked) 24.dp else 0.dp, label = "sw")
    Box(Modifier.width(52.dp).height(28.dp).insetFrame(if (checked) lerp(pal.inset, pal.accent, 0.45f) else pal.inset)) {
        Box(
            Modifier
                .offset(x = x)
                .size(28.dp)
                .pixelFrame(if (checked) pal.accent else pal.muted, Color.White.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.3f), pal.border, 2.dp)
        )
    }
}

package com.example.ironquest

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    language: AppLanguage = AppLanguage.RU,
    onBack: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onResetProgress: () -> Unit
) {
    val context = LocalContext.current
    val s = strings(language)
    val prefs = remember { ReminderSettings.preferences(context) }
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondary = Color(0xFF8194AD)

    var showResetDialog by remember { mutableStateOf(false) }
    var workoutReminder by remember { mutableStateOf(ReminderSettings.workoutEnabled(context)) }
    var streakReminder by remember { mutableStateOf(ReminderSettings.streakEnabled(context)) }
    var hour by remember { mutableStateOf(ReminderSettings.hour(context)) }
    var minute by remember { mutableStateOf(ReminderSettings.minute(context)) }
    var permissionMessage by remember { mutableStateOf("") }
    var updateMessage by remember(language) { mutableStateOf(s.updatePrompt) }
    var updateRelease by remember { mutableStateOf<ReleaseUpdate?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateNotifications by remember { mutableStateOf(prefs.getBoolean("update_notifications_enabled", true)) }
    val scope = rememberCoroutineScope()

    fun checkForUpdates() {
        if (checkingUpdate) return
        checkingUpdate = true
        updateMessage = s.updateChecking
        scope.launch {
            try {
                val currentVersion = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName ?: "v0.4"
                val release = ReleaseUpdateChecker.check(currentVersion)
                updateRelease = release
                updateMessage = if (release == null) {
                    s.updateLatest
                } else {
                    s.updateAvailable(release.version)
                }
            } catch (_: Exception) {
                updateMessage = s.updateFailed
            } finally {
                checkingUpdate = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionMessage = if (granted) s.notificationGranted else s.notificationDenied
    }

    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun saveAndReschedule() {
        prefs.edit()
            .putBoolean(ReminderSettings.KEY_WORKOUT_ENABLED, workoutReminder)
            .putBoolean(ReminderSettings.KEY_STREAK_ENABLED, streakReminder)
            .putInt(ReminderSettings.KEY_HOUR, hour)
            .putInt(ReminderSettings.KEY_MINUTE, minute)
            .apply()
        ReminderScheduler.update(context)
    }

    if (updateRelease != null) {
        AlertDialog(
            onDismissRequest = { updateRelease = null },
            title = { Text(s.updateDialogTitle) },
            text = { Text(s.updateDialogText(updateRelease!!.version)) },
            confirmButton = {
                TextButton(onClick = {
                    val release = updateRelease!!
                    val url = release.apkUrl ?: release.url
                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                    updateRelease = null
                }) { Text(if (updateRelease!!.apkUrl != null) s.downloadApk else s.openRelease) }
            },
            dismissButton = { TextButton(onClick = { updateRelease = null }) { Text(s.later) } },
            containerColor = panel,
            titleContentColor = text,
            textContentColor = secondary
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(s.resetTitle) },
            text = { Text(s.resetText) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onResetProgress()
                }) { Text(s.resetConfirm, color = Color(0xFFFF5964)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(s.cancel) }
            },
            containerColor = panel,
            titleContentColor = text,
            textContentColor = secondary
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(background).verticalScroll(rememberScrollState()).padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF223148))
            ) {
                Text("←", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Text(s.settings, color = text, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.padding(start = 36.dp))
        }

        Text(s.language, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
            Text(s.chooseLanguage, color = secondary, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.values().forEach { option ->
                    val selected = language == option
                    Text(
                        text = option.nativeName,
                        color = if (selected) Color(0xFF080F1B) else secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selected) accent else Color(0xFF223148), RoundedCornerShape(8.dp))
                            .clickable { onLanguageChange(option) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(s.notifications, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
            SettingsSwitchRow(
                title = s.remindWorkout,
                subtitle = s.remindWorkoutSub,
                checked = workoutReminder,
                onCheckedChange = {
                    workoutReminder = it
                    if (it) requestNotificationPermissionIfNeeded()
                    saveAndReschedule()
                },
                accent = accent,
                text = text,
                secondary = secondary
            )
            Divider(color = Color(0xFF29384D), modifier = Modifier.padding(vertical = 8.dp))
            SettingsSwitchRow(
                title = s.protectStreak,
                subtitle = s.protectStreakSub,
                checked = streakReminder,
                onCheckedChange = {
                    streakReminder = it
                    if (it) requestNotificationPermissionIfNeeded()
                    saveAndReschedule()
                },
                accent = accent,
                text = text,
                secondary = secondary
            )
            Divider(color = Color(0xFF29384D), modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    TimePickerDialog(
                        context,
                        { _, selectedHour, selectedMinute ->
                            hour = selectedHour
                            minute = selectedMinute
                            saveAndReschedule()
                        },
                        hour,
                        minute,
                        true
                    ).show()
                }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.reminderTime, color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(s.reminderTimeSub, color = secondary, fontSize = 11.sp)
                }
                Text(String.format(Locale.getDefault(), "%02d:%02d", hour, minute), color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }

        if (permissionMessage.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(permissionMessage, color = secondary, fontSize = 11.sp)
        }

        Spacer(Modifier.height(22.dp))
        Text(s.updates, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
            SettingsSwitchRow(
                title = s.updateNotifications,
                subtitle = s.updateNotificationsSub,
                checked = updateNotifications,
                onCheckedChange = { enabled ->
                    updateNotifications = enabled
                    prefs.edit().putBoolean("update_notifications_enabled", enabled).apply()
                    if (enabled) {
                        requestNotificationPermissionIfNeeded()
                        UpdateCheckScheduler.schedule(context.applicationContext)
                    }
                },
                accent = accent,
                text = text,
                secondary = secondary
            )
            Divider(color = Color(0xFF29384D), modifier = Modifier.padding(vertical = 8.dp))
            Text(updateMessage, color = secondary, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { checkForUpdates() },
                enabled = !checkingUpdate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accent, disabledContainerColor = Color(0xFF64705A))
            ) {
                Text(if (checkingUpdate) s.checking else s.checkUpdates, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(s.aboutApp, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(s.appVersion, color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Divider(color = Color(0xFF29384D), modifier = Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/DJSVET/IronQuest"))
                    context.startActivity(intent)
                }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.projectGithub, color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${s.open} ↗", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(s.dangerZone, color = Color(0xFFFF5964), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F2634))
        ) {
            Text(s.resetAllData, color = Color.White, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            s.settingsNote,
            color = secondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color,
    text: Color,
    secondary: Color
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = secondary, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

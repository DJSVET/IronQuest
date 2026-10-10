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
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { ReminderSettings.preferences(context) }
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondary = Color(0xFF8194AD)

    var workoutReminder by remember { mutableStateOf(ReminderSettings.workoutEnabled(context)) }
    var streakReminder by remember { mutableStateOf(ReminderSettings.streakEnabled(context)) }
    var hour by remember { mutableStateOf(ReminderSettings.hour(context)) }
    var minute by remember { mutableStateOf(ReminderSettings.minute(context)) }
    var permissionMessage by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionMessage = if (granted) "Уведомления разрешены" else "Разреши уведомления в настройках Android, чтобы получать напоминания"
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
            Text("НАСТРОЙКИ", color = text, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.padding(start = 36.dp))
        }

        Text("УВЕДОМЛЕНИЯ", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
            SettingsSwitchRow(
                title = "Напоминать о тренировке",
                subtitle = "Ежедневное напоминание в выбранное время",
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
                title = "Беречь тренировочную серию",
                subtitle = "Если сегодня ещё не было тренировки, напомнит о серии",
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
                    Text("Время напоминания", color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Каждый день, если тренировка ещё не выполнена", color = secondary, fontSize = 11.sp)
                }
                Text(String.format(Locale.getDefault(), "%02d:%02d", hour, minute), color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }

        if (permissionMessage.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(permissionMessage, color = secondary, fontSize = 11.sp)
        }

        Spacer(Modifier.height(22.dp))
        Text("О ПРИЛОЖЕНИИ", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Версия приложения", color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("v0.2", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Divider(color = Color(0xFF29384D), modifier = Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/DJSVET/IronQuest"))
                    context.startActivity(intent)
                }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("GitHub проекта", color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Открыть ↗", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Настройки сохраняются на устройстве. Android может ограничивать точное время доставки уведомлений.",
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

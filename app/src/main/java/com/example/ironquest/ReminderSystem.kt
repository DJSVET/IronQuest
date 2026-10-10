package com.example.ironquest

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderSettings {
    private const val PREFS = "ironquest_reminders"
    const val KEY_WORKOUT_ENABLED = "workout_enabled"
    const val KEY_STREAK_ENABLED = "streak_enabled"
    const val KEY_HOUR = "reminder_hour"
    const val KEY_MINUTE = "reminder_minute"

    fun preferences(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun workoutEnabled(context: Context) =
        preferences(context).getBoolean(KEY_WORKOUT_ENABLED, false)

    fun streakEnabled(context: Context) =
        preferences(context).getBoolean(KEY_STREAK_ENABLED, false)

    fun hour(context: Context) = preferences(context).getInt(KEY_HOUR, 19)
    fun minute(context: Context) = preferences(context).getInt(KEY_MINUTE, 0)
}

object ReminderScheduler {
    private const val REQUEST_CODE = 8201

    fun update(context: Context) {
        val prefs = ReminderSettings.preferences(context)
        val enabled = prefs.getBoolean(ReminderSettings.KEY_WORKOUT_ENABLED, false) ||
            prefs.getBoolean(ReminderSettings.KEY_STREAK_ENABLED, false)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context)

        alarmManager.cancel(pendingIntent)
        if (!enabled) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, prefs.getInt(ReminderSettings.KEY_HOUR, 19))
            set(Calendar.MINUTE, prefs.getInt(ReminderSettings.KEY_MINUTE, 0))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    internal fun scheduleNext(context: Context) = update(context)

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WorkoutReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class WorkoutReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val history = WorkoutHistoryData.getHistory(context).first()
                val latestWorkout = history.firstOrNull()
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val workedOutToday = latestWorkout != null && latestWorkout.timestamp >= today

                if (!workedOutToday) {
                    val workoutReminder = ReminderSettings.workoutEnabled(context)
                    val streakReminder = ReminderSettings.streakEnabled(context)
                    val streak = calculateStreak(history)

                    val title: String
                    val message: String
                    when {
                        streakReminder && streak > 0 -> {
                            title = "Не теряй серию!"
                            message = "Твоя серия — $streak дн. Сделай тренировку сегодня, чтобы продолжить прогресс."
                        }
                        workoutReminder -> {
                            title = "Время тренировки"
                            message = "Пора сделать ещё один шаг к своей цели в IronQuest."
                        }
                        else -> return@launch
                    }

                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val contentIntent = PendingIntent.getActivity(
                        context,
                        8202,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val notification = NotificationCompat.Builder(context, "ironquest_workouts")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                    try {
                        NotificationManagerCompat.from(context).notify(8203, notification)
                    } catch (_: SecurityException) {
                        // Android 13+ may block notifications until permission is granted.
                    }
                }
            } finally {
                ReminderScheduler.scheduleNext(context)
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ironquest_workouts",
                "Напоминания IronQuest",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о тренировках и тренировочной серии"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.update(context)
        }
    }
}

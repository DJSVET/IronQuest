package com.example.ironquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

object UpdateCheckScheduler {
    private const val PERIODIC_WORK = "ironquest_release_update_periodic"
    private const val IMMEDIATE_WORK = "ironquest_release_update_now"

    fun schedule(context: Context) {
        val appContext = context.applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<ReleaseUpdateWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )

        val immediate = OneTimeWorkRequestBuilder<ReleaseUpdateWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            IMMEDIATE_WORK,
            ExistingWorkPolicy.KEEP,
            immediate
        )
    }
}

class ReleaseUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val notificationPrefs = applicationContext.getSharedPreferences("ironquest_reminders", Context.MODE_PRIVATE)
        if (!notificationPrefs.getBoolean("update_notifications_enabled", true)) return Result.success()
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= 33) {
                applicationContext.packageManager.getPackageInfo(
                    applicationContext.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            }
            val installedVersion = packageInfo.versionName ?: "0.0"
            val release = ReleaseUpdateChecker.check(installedVersion) ?: return Result.success()
            val prefs = applicationContext.getSharedPreferences("ironquest_update_notifications", Context.MODE_PRIVATE)
            if (prefs.getString("last_notified_version", null) == release.version) return Result.success()

            val s = strings(AppPreferences.getLanguage(applicationContext))
            createChannel()
            val openRelease = Intent(Intent.ACTION_VIEW, Uri.parse(release.url))
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                8301,
                openRelease,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val message = if (release.apkUrl != null) {
                s.updateNotificationApkMessage(release.version)
            } else {
                s.updateNotificationReleaseMessage(release.version)
            }
            val notification = NotificationCompat.Builder(applicationContext, "ironquest_updates")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(s.updateNotificationTitle)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                NotificationManagerCompat.from(applicationContext).notify(8302, notification)
                prefs.edit().putString("last_notified_version", release.version).apply()
            } catch (_: SecurityException) {
                // Пользователь мог запретить уведомления в Android.
            }
            Result.success()
        } catch (_: java.io.IOException) {
            Result.retry()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val s = strings(AppPreferences.getLanguage(applicationContext))
            val channel = NotificationChannel(
                "ironquest_updates",
                s.updateChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = s.updateChannelDescription }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

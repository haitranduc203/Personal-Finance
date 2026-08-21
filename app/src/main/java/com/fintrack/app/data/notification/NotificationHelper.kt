package com.fintrack.app.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fintrack.app.MainActivity

/**
 * Contract for dispatching notifications, enabling clean test injection.
 */
fun interface NotificationSender {
    fun showDailyReminderNotification(context: Context): Boolean
}

object NotificationHelper : NotificationSender {

    private const val TAG = "FinTrackNotification"
    const val CHANNEL_ID = "fintrack_daily_reminder"
    private const val NOTIFICATION_ID = 1001

    /**
     * Creates Notification Channel for Android 8.0+ (API 26+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(com.fintrack.app.R.string.notif_channel_name)
            val descriptionText = context.getString(com.fintrack.app.R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Builds and delivers the daily expense reminder notification.
     */
    override fun showDailyReminderNotification(context: Context): Boolean {
        // Ensure channel is created
        createNotificationChannel(context)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission is not granted")
            return false
        }

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled for this application")
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.fintrack.app.R.drawable.ic_notification)
            .setContentTitle(context.getString(com.fintrack.app.R.string.notif_title))
            .setContentText(context.getString(com.fintrack.app.R.string.notif_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(com.fintrack.app.R.string.notif_big_text))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            val areEnabled = manager.areNotificationsEnabled()
            Log.d(TAG, "Sending notification. Notifications enabled: $areEnabled")
            manager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification sent with ID: $NOTIFICATION_ID")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while posting notification", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification", e)
            false
        }
    }
}

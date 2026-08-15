package com.fintrack.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fintrack.app.MainActivity

object NotificationHelper {

    private const val TAG = "FinTrackNotification"
    const val CHANNEL_ID = "fintrack_daily_reminder"
    private const val NOTIFICATION_ID = 1001

    /**
     * Creates Notification Channel for Android 8.0+ (API 26+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nhắc nhở ghi chép chi tiêu"
            val descriptionText = "Thông báo nhắc người dùng ghi chép chi tiêu và thu nhập hàng ngày"
            val importance = NotificationManager.IMPORTANCE_HIGH
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
    fun showDailyReminderNotification(context: Context) {
        // Ensure channel is created
        createNotificationChannel(context)

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
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Đừng quên ghi chép chi tiêu hôm nay! 📝")
            .setContentText("Dành 1 phút ghi lại các khoản thu chi để kiểm soát tài chính chính xác nhé.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Dành 1 phút ghi lại các khoản thu chi để kiểm soát tài chính và theo dõi ngân sách chính xác nhé.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            val areEnabled = manager.areNotificationsEnabled()
            Log.d(TAG, "Sending notification. Notifications enabled: $areEnabled")
            manager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification sent with ID: $NOTIFICATION_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification", e)
        }
    }
}

package com.fintrack.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fintrack.app.FinTrackApplication
import com.fintrack.app.data.notification.NotificationHelper
import kotlinx.coroutines.flow.first

/**
 * Background CoroutineWorker executing the daily expense reminder.
 */
class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = context.applicationContext as? FinTrackApplication
            val preferences = app?.preferencesRepository?.userPreferencesFlow?.first()

            val isReminderEnabled = preferences?.isDailyReminderEnabled ?: true
            if (isReminderEnabled) {
                NotificationHelper.showDailyReminderNotification(context)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

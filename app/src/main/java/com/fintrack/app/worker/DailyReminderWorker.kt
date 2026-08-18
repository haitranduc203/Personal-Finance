package com.fintrack.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fintrack.app.FinTrackApplication
import com.fintrack.app.data.notification.NotificationHelper
import kotlinx.coroutines.flow.first

import android.util.Log
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

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
            val preferences = withTimeoutOrNull(5.seconds) {
                app?.preferencesRepository?.userPreferencesFlow?.first()
            }

            val isReminderEnabled = preferences?.isDailyReminderEnabled ?: true
            if (isReminderEnabled) {
                NotificationHelper.showDailyReminderNotification(context)
            }
            Result.success()
        } catch (e: IOException) {
            Log.w("DailyReminderWorker", "IO error during reminder check, will retry", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "Permanent failure in DailyReminderWorker", e)
            Result.failure()
        }
    }
}

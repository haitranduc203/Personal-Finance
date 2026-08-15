package com.fintrack.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val PERIODIC_WORK_NAME = "fintrack_daily_reminder_periodic_work"
    const val IMMEDIATE_TEST_WORK_NAME = "fintrack_immediate_test_reminder_work"

    /**
     * Calculates the initial delay in milliseconds from now until the target hour & minute.
     */
    fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = LocalDateTime.now()
        var scheduledTime = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
        if (now.isAfter(scheduledTime) || now.isEqual(scheduledTime)) {
            scheduledTime = scheduledTime.plusDays(1)
        }
        return Duration.between(now, scheduledTime).toMillis()
    }

    /**
     * Schedules or updates the 24-hour periodic reminder worker.
     */
    fun scheduleReminder(context: Context, hour: Int, minute: Int) {
        val initialDelayMillis = calculateInitialDelay(hour, minute)

        val reminderWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    /**
     * Cancels the scheduled reminder work.
     */
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    /**
     * Triggers an immediate one-time reminder for live verification.
     */
    fun triggerImmediateReminder(context: Context) {
        val immediateWork = OneTimeWorkRequestBuilder<DailyReminderWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_TEST_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            immediateWork
        )
    }
}

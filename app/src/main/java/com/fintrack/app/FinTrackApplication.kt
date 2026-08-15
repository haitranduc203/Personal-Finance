package com.fintrack.app

import android.app.Application
import com.fintrack.app.data.local.AppDatabase
import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.notification.NotificationHelper
import com.fintrack.app.data.repository.CategoryRepository
import com.fintrack.app.data.repository.CategoryRepositoryImpl
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.PreferencesRepositoryImpl
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.data.repository.TransactionRepositoryImpl
import com.fintrack.app.worker.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Custom Application class maintaining application-level singleton dependencies.
 */
class FinTrackApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 1. Initialize Notification Channel
        NotificationHelper.createNotificationChannel(this)

        // 2. Schedule Daily Reminder according to saved preferences & seed sample data
        applicationScope.launch {
            try {
                // Seed Categories
                categoryRepository.seedDefaultCategoriesIfEmpty()

                // Seed Demo Transactions for portfolio showcase
                if (database.transactionDao().count() <= 1) {
                    val now = System.currentTimeMillis()
                    val oneDay = 24 * 60 * 60 * 1000L
                    val demoList = listOf(
                        TransactionEntity(amount = 25000000L, type = TransactionType.INCOME, categoryId = 9L, note = "Lương tháng này", transactionDate = now - 1 * oneDay),
                        TransactionEntity(amount = 5000000L, type = TransactionType.INCOME, categoryId = 10L, note = "Thưởng hoàn thành dự án", transactionDate = now - 3 * oneDay),
                        TransactionEntity(amount = 3200000L, type = TransactionType.INCOME, categoryId = 11L, note = "Lợi nhuận đầu tư chứng khoán", transactionDate = now - 5 * oneDay),
                        TransactionEntity(amount = 6500000L, type = TransactionType.EXPENSE, categoryId = 4L, note = "Tiền thuê căn hộ & phí quản lý", transactionDate = now - 2 * oneDay),
                        TransactionEntity(amount = 1850000L, type = TransactionType.EXPENSE, categoryId = 2L, note = "Mua sắm đồ gia dụng", transactionDate = now - 2 * oneDay),
                        TransactionEntity(amount = 950000L, type = TransactionType.EXPENSE, categoryId = 1L, note = "Ăn tối cùng gia đình", transactionDate = now - 3 * oneDay),
                        TransactionEntity(amount = 450000L, type = TransactionType.EXPENSE, categoryId = 3L, note = "Đổ xăng xe & rửa xe", transactionDate = now - 4 * oneDay),
                        TransactionEntity(amount = 1200000L, type = TransactionType.EXPENSE, categoryId = 8L, note = "Tiền điện nước & Internet", transactionDate = now - 5 * oneDay),
                        TransactionEntity(amount = 600000L, type = TransactionType.EXPENSE, categoryId = 7L, note = "Khóa học nâng cao Android", transactionDate = now - 6 * oneDay),
                        TransactionEntity(amount = 350000L, type = TransactionType.EXPENSE, categoryId = 5L, note = "Vé xem phim cuối tuần", transactionDate = now - 7 * oneDay)
                    )
                    demoList.forEach { transactionRepository.addTransaction(it) }
                }

                val prefs = preferencesRepository.userPreferencesFlow.first()
                if (prefs.isDailyReminderEnabled) {
                    ReminderScheduler.scheduleReminder(
                        context = this@FinTrackApplication,
                        hour = prefs.reminderHour,
                        minute = prefs.reminderMinute
                    )
                }
            } catch (e: Exception) {
                // Ignore initialization failures
            }
        }
    }
}

package com.fintrack.app

import android.app.Application
import com.fintrack.app.data.local.AppDatabase
import com.fintrack.app.data.repository.CategoryRepository
import com.fintrack.app.data.repository.CategoryRepositoryImpl
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.PreferencesRepositoryImpl
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.data.repository.TransactionRepositoryImpl

/**
 * Custom Application class maintaining application-level singleton dependencies.
 */
class FinTrackApplication : Application() {
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
}

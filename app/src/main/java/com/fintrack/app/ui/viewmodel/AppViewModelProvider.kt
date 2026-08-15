package com.fintrack.app.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fintrack.app.FinTrackApplication
import com.fintrack.app.ui.screens.add_edit.AddEditTransactionViewModel
import com.fintrack.app.ui.screens.detail.TransactionDetailViewModel
import com.fintrack.app.ui.screens.home.HomeViewModel
import com.fintrack.app.ui.screens.settings.SettingsViewModel
import com.fintrack.app.ui.screens.statistics.StatisticsViewModel
import com.fintrack.app.ui.screens.transactions.TransactionsViewModel

/**
 * Factory for creating ViewModels with dependencies injected from FinTrackApplication.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AddEditTransactionViewModel(
                transactionRepository = finTrackApplication().transactionRepository,
                categoryRepository = finTrackApplication().categoryRepository
            )
        }
        initializer {
            TransactionDetailViewModel(
                transactionRepository = finTrackApplication().transactionRepository
            )
        }
        initializer {
            HomeViewModel(
                transactionRepository = finTrackApplication().transactionRepository
            )
        }
        initializer {
            TransactionsViewModel(
                transactionRepository = finTrackApplication().transactionRepository
            )
        }
        initializer {
            StatisticsViewModel(
                transactionRepository = finTrackApplication().transactionRepository
            )
        }
        initializer {
            SettingsViewModel(
                application = finTrackApplication(),
                preferencesRepository = finTrackApplication().preferencesRepository,
                transactionRepository = finTrackApplication().transactionRepository,
                categoryRepository = finTrackApplication().categoryRepository
            )
        }
    }
}

/**
 * Extension function to retrieve [FinTrackApplication] instance from [CreationExtras].
 */
fun CreationExtras.finTrackApplication(): FinTrackApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinTrackApplication)

package com.fintrack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.fintrack.app.ui.screens.add_edit.AddEditTransactionScreen
import com.fintrack.app.ui.screens.detail.TransactionDetailScreen
import com.fintrack.app.ui.screens.home.HomeScreen
import com.fintrack.app.ui.screens.settings.SettingsScreen
import com.fintrack.app.ui.screens.statistics.StatisticsScreen
import com.fintrack.app.ui.screens.transactions.TransactionsScreen

/**
 * Top-level Navigation Host managing type-safe destinations in FinTrack.
 */
@Composable
fun FinTrackNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Home Destination
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions) {
                        launchSingleTop = true
                    }
                },
                onNavigateToTransactionDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail(transactionId))
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddEditTransaction())
                }
            )
        }

        // 2. Transactions Destination
        composable<Screen.Transactions> {
            TransactionsScreen(
                onNavigateToDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail(transactionId))
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddEditTransaction())
                }
            )
        }

        // 3. Statistics Destination
        composable<Screen.Statistics> {
            StatisticsScreen()
        }

        // 4. Settings Destination
        composable<Screen.Settings> {
            SettingsScreen()
        }

        // 5. Add / Edit Transaction Destination
        composable<Screen.AddEditTransaction> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddEditTransaction>()
            AddEditTransactionScreen(
                transactionId = route.transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 6. Transaction Detail Destination
        composable<Screen.TransactionDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.TransactionDetail>()
            TransactionDetailScreen(
                transactionId = route.transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { transactionId ->
                    navController.navigate(Screen.AddEditTransaction(transactionId))
                }
            )
        }
    }
}

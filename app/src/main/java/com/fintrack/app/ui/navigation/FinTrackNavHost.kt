package com.fintrack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.fintrack.app.FinTrackApplication
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.ui.screens.add_edit.AddEditTransactionScreen
import com.fintrack.app.ui.screens.detail.TransactionDetailScreen
import com.fintrack.app.ui.screens.home.HomeScreen
import com.fintrack.app.ui.screens.onboarding.OnboardingScreen
import com.fintrack.app.ui.screens.settings.SettingsScreen
import com.fintrack.app.ui.screens.splash.SplashScreen
import com.fintrack.app.ui.screens.statistics.StatisticsScreen
import com.fintrack.app.ui.screens.transactions.TransactionsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Top-level Navigation Host managing type-safe destinations in FinTrack.
 */
@Composable
fun FinTrackNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.Splash,
    preferencesRepository: PreferencesRepository = (LocalContext.current.applicationContext as FinTrackApplication).preferencesRepository
) {
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 0. Splash Destination
        composable<Screen.Splash> {
            SplashScreen(
                onSplashFinished = {
                    coroutineScope.launch {
                        val prefs = preferencesRepository.userPreferencesFlow.first()
                        val destination = if (prefs.isOnboardingCompleted) Screen.Home else Screen.Onboarding
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        // 0.1. Onboarding Destination
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Onboarding) {
                            inclusive = true
                        }
                    }
                }
            )
        }

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
            SettingsScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Home) {
                            inclusive = false
                        }
                    }
                }
            )
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

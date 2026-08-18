package com.fintrack.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destination routes for FinTrack application.
 */
sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Transactions : Screen

    @Serializable
    data object Statistics : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class AddEditTransaction(val transactionId: Long? = null) : Screen

    @Serializable
    data class TransactionDetail(val transactionId: Long) : Screen
}

/**
 * Data representation of a primary bottom navigation bar item.
 */
data class BottomNavItem(
    val route: Screen,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

/**
 * List of primary destinations rendered in the M3 BottomNavigationBar.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home,
        label = "Trang chủ",
        icon = Icons.Default.Home,
        contentDescription = "Trang chủ"
    ),
    BottomNavItem(
        route = Screen.Transactions,
        label = "Giao dịch",
        icon = Icons.AutoMirrored.Filled.List,
        contentDescription = "Lịch sử giao dịch"
    ),
    BottomNavItem(
        route = Screen.Statistics,
        label = "Thống kê",
        icon = Icons.Default.PieChart,
        contentDescription = "Thống kê và báo cáo"
    ),
    BottomNavItem(
        route = Screen.Settings,
        label = "Cài đặt",
        icon = Icons.Default.Settings,
        contentDescription = "Cài đặt ứng dụng"
    )
)

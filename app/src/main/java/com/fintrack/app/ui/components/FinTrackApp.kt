package com.fintrack.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fintrack.app.ui.navigation.BottomNavItem
import com.fintrack.app.ui.navigation.FinTrackNavHost
import com.fintrack.app.ui.navigation.Screen
import com.fintrack.app.ui.navigation.bottomNavItems
import com.fintrack.app.ui.theme.FinTrackTheme

/**
 * Stateful root entry composable for FinTrack Application.
 */
@Composable
fun FinTrackApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if current screen is one of the 4 main bottom tab destinations
    val isBottomBarVisible = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    FinTrackAppContent(
        isBottomBarVisible = isBottomBarVisible,
        bottomNavItems = bottomNavItems,
        isTabSelected = { item ->
            currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
        },
        onTabSelected = { item ->
            navController.navigate(item.route) {
                // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        },
        onAddTransactionClick = {
            navController.navigate(Screen.AddEditTransaction())
        }
    ) { innerPadding ->
        FinTrackNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Stateless root UI container for FinTrack Application.
 */
@Composable
fun FinTrackAppContent(
    isBottomBarVisible: Boolean,
    bottomNavItems: List<BottomNavItem>,
    isTabSelected: (BottomNavItem) -> Boolean,
    onTabSelected: (BottomNavItem) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = isTabSelected(item)
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = { onTabSelected(item) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isBottomBarVisible) {
                FloatingActionButton(
                    onClick = onAddTransactionClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch mới")
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

// ----------------------------------------------------
// Compose Previews
// ----------------------------------------------------

@PreviewLightDark
@Composable
private fun FinTrackAppContentPreview() {
    FinTrackTheme {
        FinTrackAppContent(
            isBottomBarVisible = true,
            bottomNavItems = bottomNavItems,
            isTabSelected = { it.route == Screen.Home },
            onTabSelected = {},
            onAddTransactionClick = {}
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Nội dung màn hình chính",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

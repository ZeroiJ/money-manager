package com.example.moneymanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moneymanager.ui.screens.add.AddTransactionScreen
import com.example.moneymanager.ui.screens.budgets.BudgetsScreen
import com.example.moneymanager.ui.screens.home.HomeScreen
import com.example.moneymanager.ui.screens.reports.ReportsScreen
import com.example.moneymanager.ui.screens.settings.SettingsScreen
import com.example.moneymanager.ui.screens.transactions.TransactionListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Transactions : Screen("transactions", "Transactions", Icons.AutoMirrored.Filled.List)
    data object AddTransaction : Screen("add_transaction?id={id}", "Add", Icons.Filled.AddCircle) {
        fun createRoute(transactionId: Long? = null): String {
            return if (transactionId != null) "add_transaction?id=$transactionId" else "add_transaction"
        }
    }
    data object Budgets : Screen("budgets", "Budgets", Icons.Filled.AccountBalanceWallet)
    data object Reports : Screen("reports", "Reports", Icons.Filled.PieChart)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Home)
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.AddTransaction,
        Screen.Budgets,
        Screen.Reports
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isBottomBarVisible = bottomNavItems.any { item ->
        val baseRoute = item.route.substringBefore("?")
        val currentRoute = currentDestination?.route?.substringBefore("?")
        baseRoute == currentRoute
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    bottomNavItems.forEach { screen ->
                        val baseRoute = screen.route.substringBefore("?")
                        val currentRoute = currentDestination?.route?.substringBefore("?")
                        val isSelected = currentRoute == baseRoute

                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                val targetRoute = if (screen == Screen.AddTransaction) Screen.AddTransaction.createRoute(null) else screen.route
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddTransaction.createRoute(null)) },
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToEditTransaction = { id -> navController.navigate(Screen.AddTransaction.createRoute(id)) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    onNavigateToEditTransaction = { id -> navController.navigate(Screen.AddTransaction.createRoute(id)) }
                )
            }
            composable(
                route = "add_transaction?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("id")
                val transactionId = idStr?.toLongOrNull()
                AddTransactionScreen(
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Budgets.route) {
                BudgetsScreen()
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

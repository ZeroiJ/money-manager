package com.example.moneymanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.screens.add.AddTransactionScreen
import com.example.moneymanager.ui.screens.budgets.BudgetsScreen
import com.example.moneymanager.ui.screens.home.HomeScreen
import com.example.moneymanager.ui.screens.reports.ReportsScreen
import com.example.moneymanager.ui.screens.settings.SettingsScreen
import com.example.moneymanager.ui.screens.transactions.TransactionListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "HOME", Icons.Filled.Home)
    data object Transactions : Screen("transactions", "FEED", Icons.AutoMirrored.Filled.List)
    data object AddTransaction : Screen("add_transaction?id={id}", "ADD", Icons.Filled.Add) {
        fun createRoute(transactionId: Long? = null): String {
            return if (transactionId != null) "add_transaction?id=$transactionId" else "add_transaction"
        }
    }
    data object Budgets : Screen("budgets", "BUDGETS", Icons.Filled.AccountBalanceWallet)
    data object Reports : Screen("reports", "REPORTS", Icons.Filled.PieChart)
    data object Settings : Screen("settings", "SETTINGS", Icons.Filled.Home)
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.5.dp, color = MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavItems.forEach { screen ->
                            val baseRoute = screen.route.substringBefore("?")
                            val currentRoute = currentDestination?.route?.substringBefore("?")
                            val isSelected = currentRoute == baseRoute

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeoYellow else Color.Transparent)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.dp, NeoBlack, RoundedCornerShape(6.dp))
                                        } else Modifier
                                    )
                                    .clickable {
                                        val targetRoute = if (screen == Screen.AddTransaction) Screen.AddTransaction.createRoute(null) else screen.route
                                        navController.navigate(targetRoute) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                        ),
                                        color = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
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
                    onNavigateToEditTransaction = { txId ->
                        navController.navigate(Screen.AddTransaction.createRoute(txId))
                    }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    onNavigateToEditTransaction = { txId ->
                        navController.navigate(Screen.AddTransaction.createRoute(txId))
                    }
                )
            }

            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getLong("id") ?: -1L
                val validTxId = if (txId > 0) txId else null
                AddTransactionScreen(
                    transactionId = validTxId,
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

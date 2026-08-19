package com.example.moneymanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.screens.add.AddTransactionScreen
import com.example.moneymanager.ui.screens.budgets.BudgetsScreen
import com.example.moneymanager.ui.screens.home.HomeScreen
import com.example.moneymanager.ui.screens.reports.ReportsScreen
import com.example.moneymanager.ui.screens.settings.SettingsScreen
import com.example.moneymanager.ui.screens.transactions.TransactionListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "HOME", Icons.Default.Home)
    object Transactions : Screen("transactions", "FEED", Icons.AutoMirrored.Filled.List)
    object Add : Screen("add_transaction", "ADD", Icons.Default.Add)
    object Budgets : Screen("budgets", "BUDGETS", Icons.Default.AccountBalanceWallet)
    object Reports : Screen("reports", "REPORTS", Icons.Default.PieChart)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
    object EditTransaction : Screen("edit_transaction/{transactionId}", "EDIT", Icons.Default.Edit) {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.Add,
    Screen.Budgets,
    Screen.Reports
)

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    AppNavGraph(navController = navController)
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val shouldShowBottomBar = androidx.compose.runtime.remember(currentRoute) {
                bottomNavItems.any { it.route == currentRoute }
            }
            if (shouldShowBottomBar) {
                ChromaBottomNavigationBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        if (screen == Screen.Add) {
                            navController.navigate(Screen.Add.route)
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
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
                    onNavigateToAdd = { navController.navigate(Screen.Add.route) },
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToEditTransaction = { txId ->
                        navController.navigate(Screen.EditTransaction.createRoute(txId))
                    }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    onNavigateToEditTransaction = { txId ->
                        navController.navigate(Screen.EditTransaction.createRoute(txId))
                    }
                )
            }
            composable(Screen.Add.route) {
                AddTransactionScreen(
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
            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                AddTransactionScreen(
                    transactionId = txId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ChromaBottomNavigationBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChromaStone50)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItemShape = androidx.compose.runtime.remember { RoundedCornerShape(4.dp) }
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route

                Box(
                    modifier = Modifier
                        .clip(navItemShape)
                        .background(if (isSelected) ChromaStone200 else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) ChromaBlack else Color.Transparent,
                            shape = navItemShape
                        )
                        .clickable { onItemClick(screen) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) ChromaOrange else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 9.sp
                            ),
                            color = if (isSelected) ChromaBlack else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

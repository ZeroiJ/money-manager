package com.example.moneymanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ios.AmbientBackground
import com.example.moneymanager.ui.ios.IosTabBar
import com.example.moneymanager.ui.ios.IosTabItem
import com.example.moneymanager.ui.ios.readAnimatorScale
import com.example.moneymanager.ui.ios.resolveAmbientMode
import com.example.moneymanager.ui.screens.add.AddTransactionScreen
import com.example.moneymanager.ui.screens.budgets.BudgetsScreen
import com.example.moneymanager.ui.screens.home.HomeScreen
import com.example.moneymanager.ui.screens.reports.ReportsScreen
import com.example.moneymanager.ui.screens.settings.SettingsScreen
import com.example.moneymanager.ui.screens.transactions.TransactionListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "HOME", Icons.Default.Home)
    object Transactions : Screen("transactions", "FEED", Icons.AutoMirrored.Filled.List)
    object Budgets : Screen("budgets", "BUDGETS", Icons.Default.AccountBalanceWallet)
    object Reports : Screen("reports", "REPORTS", Icons.Default.PieChart)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
    object EditTransaction : Screen("edit_transaction/{transactionId}", "EDIT", Icons.Default.Edit) {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Budgets,
    Screen.Reports
)

@Composable
fun MainNavigation(
    presetCategory: String? = null,
    presetPaymentMode: String? = null
) {
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        presetCategory = presetCategory,
        presetPaymentMode = presetPaymentMode
    )
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    presetCategory: String? = null,
    presetPaymentMode: String? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val ambientEnabled by hiltViewModel<com.example.moneymanager.ui.screens.settings.SettingsViewModel>().ambientEnabled.collectAsState()
    val animatorScale = androidx.compose.runtime.remember { readAnimatorScale(context) }
    val ambientMode = resolveAmbientMode(animatorScale, ambientEnabled)

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground(mode = ambientMode, dark = dark)
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                val shouldShowBottomBar = androidx.compose.runtime.remember(currentRoute) {
                    bottomNavItems.any { it.route == currentRoute }
                }
                if (shouldShowBottomBar) {
                    val tabItems = androidx.compose.runtime.remember {
                        bottomNavItems.map {
                            IosTabItem(
                                title = it.title.lowercase().replaceFirstChar(Char::titlecase),
                                icon = it.icon
                            )
                        }
                    }
                    val selectedIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }
                        .coerceAtLeast(0)
                    IosTabBar(
                        selected = selectedIndex,
                        items = tabItems,
                        onSelect = { index ->
                            val screen = bottomNavItems[index]
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    presetCategory = presetCategory,
                    presetPaymentMode = presetPaymentMode,
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
}

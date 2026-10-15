package com.example.moneymanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.moneymanager.util.ErrorBus
import com.example.moneymanager.ui.ios.AmbientBackground
import com.example.moneymanager.ui.ios.IosCommitRail
import com.example.moneymanager.ui.ios.LocalHideAmounts
import com.example.moneymanager.ui.ios.readAnimatorScale
import com.example.moneymanager.ui.ios.resolveAmbientMode
import com.example.moneymanager.ui.screens.add.AddTransactionScreen
import com.example.moneymanager.ui.screens.home.HomeScreen
import com.example.moneymanager.ui.screens.reports.ReportsScreen
import com.example.moneymanager.ui.screens.settings.SettingsScreen
import com.example.moneymanager.ui.screens.transactions.TransactionListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "HOME", Icons.Default.Home)
    object Transactions : Screen("transactions", "FEED", Icons.AutoMirrored.Filled.List)
    object Expense : Screen("expense", "EXPENSE", Icons.Default.Add)
    object Reports : Screen("reports", "REPORTS", Icons.Default.PieChart)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
    object EditTransaction : Screen("edit_transaction/{transactionId}", "EDIT", Icons.Default.Edit) {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
}

// Persistent 3-tab layout — 01 HOME / 02 EXPENSE / 03 REPORTS (budget tab removed).
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Expense,
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
    // Light-mode only: ambient mesh always uses the Sage gradient (v1.17.0).
    val dark = false
    val settingsVm: com.example.moneymanager.ui.screens.settings.SettingsViewModel = hiltViewModel()
    val ambientEnabled by settingsVm.ambientEnabled.collectAsState()
    val hideAmounts by settingsVm.hideAmounts.collectAsState()
    val animatorScale = androidx.compose.runtime.remember { readAnimatorScale(context) }
    val ambientMode = resolveAmbientMode(animatorScale, ambientEnabled)

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground(mode = ambientMode, dark = dark)
        // Default insets: content gets top + horizontal safe-drawing padding.
        // The bottom inset is owned by IosCommitRail (not double-padded here).
        val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
        // One global listener: every ErrorBus report becomes a snackbar with a
        // Details action into Settings → Diagnostics. Local-only, no telemetry.
        androidx.compose.runtime.LaunchedEffect(Unit) {
            ErrorBus.events.collect { err ->
                val action = snackbarHostState.showSnackbar(
                    message = err.summary(),
                    actionLabel = "Details",
                    duration = SnackbarDuration.Long
                )
                if (action == SnackbarResult.ActionPerformed) {
                    navController.navigate(Screen.Settings.route)
                }
            }
        }
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                val shouldShowBottomBar = androidx.compose.runtime.remember(currentRoute) {
                    bottomNavItems.any { it.route == currentRoute }
                }
                if (shouldShowBottomBar) {
                    // Commit rail (v1.19.0): ochre key jumps to Expense,
                    // flip switch toggles Home/Reports. Same tab-switch
                    // semantics as the old bar (state save/restore).
                    val goTab = { screen: Screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    IosCommitRail(
                        selectedHome = currentRoute == Screen.Home.route,
                        selectedReports = currentRoute == Screen.Reports.route,
                        onSelectHome = { goTab(Screen.Home) },
                        onSelectExpense = { goTab(Screen.Expense) },
                        onSelectReports = { goTab(Screen.Reports) }
                    )
                }
            }
        ) { innerPadding ->androidx.compose.runtime.CompositionLocalProvider(
            LocalHideAmounts provides hideAmounts
        ) {
            NavHost(
            navController = navController,
            // Widget/SMS preset routes straight into tab 03 (fast capture).
            startDestination = if (presetCategory != null) Screen.Expense.route else Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    hideAmounts = hideAmounts,
                    onToggleHideAmounts = { settingsVm.setHideAmounts(!hideAmounts) },
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
            composable(Screen.Expense.route) {
                // Tab 02: fast capture lives in the tab bar itself.
                AddTransactionScreen(
                    presetCategory = presetCategory,
                    presetPaymentMode = presetPaymentMode,
                    showBack = false,
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
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
}

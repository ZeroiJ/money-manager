package com.example.moneymanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.Geist
import com.example.moneymanager.ui.ios.ConfettiOverlay
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosGroupedList
import com.example.moneymanager.ui.ios.IosHeroWord
import com.example.moneymanager.ui.ios.IosRow
import com.example.moneymanager.ui.ios.LoadErrorCard
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.firstEntryTransition
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.ui.screens.add.AddSpendSheet
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    presetCategory: String? = null,
    presetPaymentMode: String? = null,
    hideAmounts: Boolean = false,
    onToggleHideAmounts: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditTransaction: (Long) -> Unit = {}
) {
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val todaySpend by viewModel.todaySpend.collectAsState()
    val monthSpend by viewModel.monthSpend.collectAsState()
    var showAddSheet by remember { mutableStateOf(presetCategory != null) }
    val scheme = currentScheme()
    val monthWord = remember {
        FormatUtils.formatMonth(FormatUtils.getCurrentMonthKey()).substringBefore(" ")
    }

    // First-entry celebration: empty (after load) -> non-empty, once ever.
    var showConfetti by remember { mutableStateOf(false) }
    var celebratedFirstEntry by rememberSaveable { mutableStateOf(false) }
    var wasEmpty by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(recentTxs.isEmpty()) {
        val empty = recentTxs.isEmpty()
        if (firstEntryTransition(wasEmpty, empty, celebratedFirstEntry)) {
            celebratedFirstEntry = true
            showConfetti = true
        }
        wasEmpty = empty
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                shape = CircleShape,
                containerColor = scheme.accent,
                contentColor = Color.White,
                modifier = Modifier.pressShrink()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Month summary card ───────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(scheme.card)
                        .padding(24.dp)
                ) {
                    Column {
                        IosHeroWord(
                            text = monthWord,
                            color = scheme.label,
                            fontSize = 40.sp,
                            lineHeight = 44.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        IosAmount(
                            text = FormatUtils.formatCurrency(monthSpend),
                            color = scheme.label,
                            fontSize = 34.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Today · ",
                                fontFamily = Geist,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = scheme.secondaryLabel
                            )
                            IosAmount(
                                text = FormatUtils.formatCurrency(todaySpend),
                                color = scheme.secondaryLabel,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // ── Recent activity ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent",
                        fontFamily = Geist,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = scheme.label,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onNavigateToTransactions,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "View all",
                            fontFamily = Geist,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = scheme.accent
                        )
                    }
                    IconButton(
                        onClick = onToggleHideAmounts,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (hideAmounts) "Show amounts" else "Hide amounts",
                            tint = scheme.secondaryLabel
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = scheme.secondaryLabel
                        )
                    }
                }
            }

            if (loadError != null) {
                item {
                    LoadErrorCard(
                        message = loadError ?: "Couldn't load transactions",
                        onRetry = { viewModel.retryLoad() }
                    )
                }
            } else if (recentTxs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(scheme.card)
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IosHeroWord(
                                text = "A clean slate",
                                color = scheme.label,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Log an expense in under 5 seconds.",
                                fontFamily = Geist,
                                fontSize = 15.sp,
                                color = scheme.secondaryLabel
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add expense",
                                fontFamily = Geist,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = scheme.accent,
                                modifier = Modifier.clickable { showAddSheet = true }
                            )
                        }
                    }
                }
            } else {
                item {
                    IosGroupedList(header = null) {
                        recentTxs.take(10).forEach { tx ->
                            HomeFeedRow(
                                transaction = tx,
                                category = categoriesMap[tx.categoryId],
                                onClick = { onNavigateToEditTransaction(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    ConfettiOverlay(
        show = showConfetti,
        title = "First entry!",
        subtitle = "Your money story starts here.",
        onDone = { showConfetti = false }
    )

    if (showAddSheet) {
        AddSpendSheet(
            presetCategory = presetCategory,
            presetPaymentMode = presetPaymentMode,
            onDismiss = { showAddSheet = false }
        )
    }
    }
}

/**
 * Grouped feed row: category icon, note + category · mode, amount + date.
 * Tap → edit transaction.
 */
@Composable
private fun HomeFeedRow(
    transaction: Transaction,
    category: Category?,
    onClick: () -> Unit
) {
    val scheme = currentScheme()
    val isExpense = transaction.type == TransactionType.EXPENSE
    IosRow(
        title = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Expense"),
        subtitle = "${category?.name ?: "Misc"} · ${transaction.paymentMode.name}",
        icon = FormatUtils.getCategoryIcon(category?.icon ?: "category"),
        iconTint = scheme.label,
        scheme = scheme,
        onClick = onClick,
        showChevron = false
    ) {
        Column(horizontalAlignment = Alignment.End) {
            IosAmount(
                text = (if (isExpense) "−" else "+") + FormatUtils.formatCurrency(transaction.amount),
                color = if (isExpense) scheme.destructive else scheme.success,
                fontSize = 15.sp
            )
            Text(
                text = FormatUtils.formatDate(transaction.date),
                fontFamily = Geist,
                fontSize = 11.sp,
                color = scheme.secondaryLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
    }
}

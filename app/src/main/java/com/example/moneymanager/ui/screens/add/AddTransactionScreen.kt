package com.example.moneymanager.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.*
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val categories by viewModel.categories.collectAsState()
    val householdMembers by viewModel.householdMembers.collectAsState()
    val editingId by viewModel.editingTransactionId.collectAsState()
    val amount by viewModel.amountInput.collectAsState()
    val txType by viewModel.transactionType.collectAsState()
    val scope by viewModel.transactionScope.collectAsState()
    val mode by viewModel.paymentMode.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val note by viewModel.noteInput.collectAsState()
    val paidBy by viewModel.selectedPaidBy.collectAsState()

    val isEditMode = editingId != null && editingId!! > 0

    LaunchedEffect(transactionId) {
        if (transactionId != null && transactionId > 0) {
            viewModel.loadTransaction(transactionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "EDIT EXPENSE" else "QUICK ENTRY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 8.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Type Selector: Expense vs Income
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(TransactionType.EXPENSE to "EXPENSE", TransactionType.INCOME to "INCOME").forEach { (type, label) ->
                    val isSelected = txType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neoShadow(offset = if (isSelected) 3.dp else 1.dp, cornerRadius = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) {
                                    if (type == TransactionType.EXPENSE) NeoRed else NeoGreen
                                } else MaterialTheme.colorScheme.surface
                            )
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable { viewModel.transactionType.value = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            ),
                            color = if (isSelected) NeoWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Amount Display Card with Arithmetic Sub-expression
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface,
                shadowOffset = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AMOUNT (INR)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₹ ${if (amount.isEmpty()) "0" else amount}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = if (txType == TransactionType.EXPENSE) NeoRed else NeoGreen,
                        maxLines = 1
                    )

                    // Quick Increment Chips
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("+50", "+100", "+500", "+2000").forEach { incStr ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeoGray100)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.onNumpadClick(incStr) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = incStr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = NeoBlack
                                )
                            }
                        }
                    }
                }
            }

            // Scope Selector: Personal vs Household
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 3.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SCOPE (REQUIRED)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(TransactionScope.PERSONAL, "PERSONAL", NeoBlue),
                            Triple(TransactionScope.HOUSEHOLD, "HOUSEHOLD", NeoOrange)
                        ).forEach { (scopeItem, label, color) ->
                            val isSelected = scope == scopeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) color else MaterialTheme.colorScheme.surface)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.transactionScope.value = scopeItem }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = if (isSelected) NeoWhite else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Household Paid By Selector
                    if (scope == TransactionScope.HOUSEHOLD && householdMembers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "PAID BY:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            householdMembers.forEach { member ->
                                val isPaidBy = paidBy == member.name
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPaidBy) NeoYellow else NeoGray100)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                        .clickable { viewModel.selectedPaidBy.value = member.name }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = NeoBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Mode Selector: UPI, Cash, Card
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 3.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PAYMENT MODE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMode.values().forEach { paymentModeItem ->
                            val isSelected = mode == paymentModeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.paymentMode.value = paymentModeItem }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = paymentModeItem.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Category Picker Grid
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 3.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            val isSelected = selectedCatId == cat.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.5.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.selectedCategoryId.value = cat.id }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = FormatUtils.getCategoryIcon(cat.icon),
                                        contentDescription = cat.name,
                                        tint = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                        ),
                                        color = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.noteInput.value = it },
                label = { Text("Note / Tag (optional)", fontWeight = FontWeight.Bold) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Calculator Keypad
            NeoCalculatorKeypad(
                onKeyClick = { key -> viewModel.onNumpadClick(key) }
            )

            // Save Action Button
            NeoButton(
                text = if (isEditMode) "UPDATE TRANSACTION" else "SAVE TRANSACTION",
                onClick = {
                    viewModel.saveTransaction {
                        onNavigateBack()
                    }
                },
                backgroundColor = NeoYellow,
                textColor = NeoBlack,
                borderColor = NeoBlack,
                shadowOffset = 5.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NeoCalculatorKeypad(onKeyClick: (String) -> Unit) {
    val rows = listOf(
        listOf("7", "8", "9", "DEL"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", ".", "=")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isAction = key in listOf("+", "-", "=", "DEL", "C")
                    val isEquals = key == "="
                    val isClear = key == "C" || key == "DEL"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .neoShadow(offset = 2.dp, cornerRadius = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isEquals -> NeoGreen
                                    isClear -> NeoRed.copy(alpha = 0.2f)
                                    isAction -> NeoYellow
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable { onKeyClick(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = when {
                                isEquals -> NeoWhite
                                isClear -> NeoRed
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

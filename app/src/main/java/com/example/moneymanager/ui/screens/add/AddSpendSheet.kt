package com.example.moneymanager.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
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
import com.example.moneymanager.theme.Geist
import com.example.moneymanager.theme.PaperMono
import com.example.moneymanager.ui.ios.GlassSheet
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosSegmented
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.Haptics
import kotlinx.coroutines.launch

/**
 * Single-page quick-add sheet: amount → scope → pay mode → category → note → commit.
 * Everything visible in one scroll; no stepper. ViewModel untouched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpendSheet(
    presetCategory: String? = null,
    presetPaymentMode: String? = null,
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val householdMembers by viewModel.householdMembers.collectAsState()
    val amount by viewModel.amountInput.collectAsState()
    val txType by viewModel.transactionType.collectAsState()
    val scope by viewModel.transactionScope.collectAsState()
    val mode by viewModel.paymentMode.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val note by viewModel.noteInput.collectAsState()
    val paidBy by viewModel.selectedPaidBy.collectAsState()
    val scheme = currentScheme()

    LaunchedEffect(presetCategory, presetPaymentMode) {
        if (presetCategory != null) {
            viewModel.applyPreset(presetCategory, presetPaymentMode)
        }
    }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val sheetScope = rememberCoroutineScope()
    val sheetView = LocalView.current
    val canCommit = (viewModel.evaluatedAmount() ?: 0.0) > 0 && selectedCatId != null

    GlassSheet(onDismiss = onDismiss, sheetState = sheetState, scheme = scheme) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New expense",
                    fontFamily = Geist,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = scheme.label,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = scheme.secondaryLabel
                    )
                }
            }

            IosSegmented(
                options = listOf("Expense", "Income"),
                selected = if (txType == TransactionType.EXPENSE) 0 else 1,
                onSelect = {
                    viewModel.transactionType.value =
                        if (it == 0) TransactionType.EXPENSE else TransactionType.INCOME
                },
                scheme = scheme
            )

            AmountBlock(
                amount = amount,
                txType = txType,
                onKeyClick = { viewModel.onNumpadClick(it) }
            )

            ScopeBlock(
                scope = scope,
                householdMembers = householdMembers,
                paidBy = paidBy,
                onScopeChange = { viewModel.transactionScope.value = it },
                onPaidByChange = { viewModel.selectedPaidBy.value = it }
            )

            PayModeBlock(
                mode = mode,
                onModeChange = { viewModel.paymentMode.value = it }
            )

            CategoryBlock(
                categories = categories,
                selectedCatId = selectedCatId,
                onSelect = { viewModel.selectedCategoryId.value = it }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.noteInput.value = it },
                label = { Text("Note (optional)", fontFamily = Geist) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = scheme.accent,
                    unfocusedBorderColor = scheme.separator
                )
            )

            Button(
                onClick = {
                    Haptics.saveConfirmed(sheetView)
                    sheetScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        viewModel.saveTransaction { onDismiss() }
                    }
                },
                enabled = canCommit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .pressShrink(enabled = canCommit),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.accent,
                    disabledContainerColor = scheme.secondaryFill,
                    contentColor = Color.White,
                    disabledContentColor = scheme.secondaryLabel
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = "Log expense",
                    fontFamily = Geist,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
private fun AmountBlock(
    amount: String,
    txType: TransactionType,
    onKeyClick: (String) -> Unit
) {
    val scheme = currentScheme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.secondaryFill.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IosAmount(
            text = "₹ ${if (amount.isEmpty()) "0" else amount}",
            color = if (txType == TransactionType.EXPENSE) scheme.destructive else scheme.success,
            fontSize = 34.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipView = LocalView.current
            listOf("+50", "+100", "+500", "+2000").forEach { incStr ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(scheme.card)
                        .clickable {
                            Haptics.keyPress(chipView)
                            onKeyClick(incStr)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = incStr,
                        fontFamily = PaperMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = scheme.label
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        IosKeypad(onKeyClick = onKeyClick)
    }
}

@Composable
private fun ScopeBlock(
    scope: TransactionScope,
    householdMembers: List<HouseholdMember>,
    paidBy: String?,
    onScopeChange: (TransactionScope) -> Unit,
    onPaidByChange: (String) -> Unit
) {
    val scheme = currentScheme()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IosSegmented(
            options = listOf("Personal", "Household"),
            selected = if (scope == TransactionScope.PERSONAL) 0 else 1,
            onSelect = {
                onScopeChange(
                    if (it == 0) TransactionScope.PERSONAL else TransactionScope.HOUSEHOLD
                )
            },
            scheme = scheme
        )
        if (scope == TransactionScope.HOUSEHOLD && householdMembers.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                householdMembers.forEach { member ->
                    val isPaidBy = paidBy == member.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPaidBy) scheme.accent.copy(alpha = 0.15f)
                                else scheme.secondaryFill
                            )
                            .clickable { onPaidByChange(member.name) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name,
                            fontFamily = Geist,
                            fontWeight = if (isPaidBy) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (isPaidBy) scheme.accent else scheme.label
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PayModeBlock(
    mode: PaymentMode,
    onModeChange: (PaymentMode) -> Unit
) {
    val scheme = currentScheme()
    val modes = PaymentMode.values()
    IosSegmented(
        options = modes.map { it.name.lowercase().replaceFirstChar(Char::titlecase) },
        selected = modes.indexOf(mode).coerceAtLeast(0),
        onSelect = { onModeChange(modes[it]) },
        scheme = scheme
    )
}

@Composable
private fun CategoryBlock(
    categories: List<Category>,
    selectedCatId: Long?,
    onSelect: (Long) -> Unit
) {
    val scheme = currentScheme()
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.height(210.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCatId == cat.id
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) scheme.accent.copy(alpha = 0.15f)
                        else scheme.secondaryFill
                    )
                    .clickable { onSelect(cat.id) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = FormatUtils.getCategoryIcon(cat.icon),
                        contentDescription = cat.name,
                        tint = if (isSelected) scheme.accent else scheme.label,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cat.name,
                        fontFamily = Geist,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) scheme.accent else scheme.label,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

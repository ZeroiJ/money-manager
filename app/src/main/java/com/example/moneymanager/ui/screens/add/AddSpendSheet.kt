package com.example.moneymanager.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiDivider
import com.example.moneymanager.ui.ascii.AsciiSectionHeader
import com.example.moneymanager.ui.ascii.AsciiSelectChip
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.Haptics
import kotlinx.coroutines.launch

private enum class QuickAddStep(val label: String) {
    AMOUNT("1. AMOUNT"),
    SCOPE("2. SCOPE"),
    PAYMENT("3. PAYMENT"),
    CATEGORY("4. CATEGORY"),
    NOTE("5. NOTE")
}

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

    var step by remember { mutableStateOf(QuickAddStep.AMOUNT) }

    LaunchedEffect(presetCategory, presetPaymentMode) {
        if (presetCategory != null) {
            viewModel.applyPreset(presetCategory, presetPaymentMode)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetScope = rememberCoroutineScope()
    val sheetView = LocalView.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Chroma.color.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsciiSectionHeader(text = "quick_entry.sh", showRule = false, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STEP ${step.ordinal + 1}/5",
                    style = Chroma.type.labelSmall.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = Chroma.color.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Chroma.color.surface)
                        .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Chroma.color.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            AsciiDivider(color = Ascii.hairline)

            when (step) {
                QuickAddStep.AMOUNT -> {
                    AmountStep(
                        amount = amount,
                        txType = txType,
                        onTypeChange = { viewModel.transactionType.value = it },
                        onKeyClick = { viewModel.onNumpadClick(it) }
                    )
                }
                QuickAddStep.SCOPE -> {
                    ScopeStep(
                        scope = scope,
                        householdMembers = householdMembers,
                        paidBy = paidBy,
                        onScopeChange = { viewModel.transactionScope.value = it },
                        onPaidByChange = { viewModel.selectedPaidBy.value = it }
                    )
                }
                QuickAddStep.PAYMENT -> {
                    PaymentStep(
                        mode = mode,
                        onModeChange = { viewModel.paymentMode.value = it }
                    )
                }
                QuickAddStep.CATEGORY -> {
                    CategoryStep(
                        categories = categories,
                        selectedCatId = selectedCatId,
                        onSelect = { viewModel.selectedCategoryId.value = it }
                    )
                }
                QuickAddStep.NOTE -> {
                    NoteStep(note = note, onNoteChange = { viewModel.noteInput.value = it })
                }
            }

            StepNavRow(
                step = step,
                canGoNext = when (step) {
                    QuickAddStep.AMOUNT -> (viewModel.evaluatedAmount() ?: 0.0) > 0
                    QuickAddStep.SCOPE -> true
                    QuickAddStep.PAYMENT -> true
                    QuickAddStep.CATEGORY -> selectedCatId != null
                    QuickAddStep.NOTE -> (viewModel.evaluatedAmount() ?: 0.0) > 0 && selectedCatId != null
                },
                onBack = {
                    Haptics.keyPress(sheetView)
                    step = QuickAddStep.entries[step.ordinal - 1]
                },
                onNext = {
                    Haptics.keyPress(sheetView)
                    step = QuickAddStep.entries[step.ordinal + 1]
                },
                onCommit = {
                    Haptics.saveConfirmed(sheetView)
                    sheetScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        viewModel.saveTransaction { onDismiss() }
                    }
                }
            )
        }
    }
}

@Composable
private fun AmountStep(
    amount: String,
    txType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    onKeyClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsciiSelectChip(
                label = "EXPENSE",
                selected = txType == TransactionType.EXPENSE,
                onClick = { onTypeChange(TransactionType.EXPENSE) },
                modifier = Modifier.weight(1f),
                selectedColor = ChromaRed
            )
            AsciiSelectChip(
                label = "INCOME",
                selected = txType == TransactionType.INCOME,
                onClick = { onTypeChange(TransactionType.INCOME) },
                modifier = Modifier.weight(1f),
                selectedColor = ChromaGreen
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Chroma.color.surface)
                .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "amount.inr // keypad",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Chroma.color.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹ ${if (amount.isEmpty()) "0" else amount}",
                style = Chroma.type.displaySmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Black
                ),
                color = if (txType == TransactionType.EXPENSE) ChromaRed else ChromaGreen,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chipView = LocalView.current
            listOf("+50", "+100", "+500", "+2000").forEach { incStr ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ChromaStone100)
                        .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                        .clickable {
                            Haptics.keyPress(chipView)
                            onKeyClick(incStr)
                        }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = incStr,
                        style = Chroma.type.labelSmall.copy(
                            fontFamily = PlexMono,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ChromaBlack
                    )
                }
            }
        }

        ChromaCalculatorKeypad(onKeyClick = onKeyClick)
    }
}

@Composable
private fun ScopeStep(
    scope: TransactionScope,
    householdMembers: List<HouseholdMember>,
    paidBy: String?,
    onScopeChange: (TransactionScope) -> Unit,
    onPaidByChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "scope.tag // required",
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Chroma.color.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsciiSelectChip(
                label = "PERSONAL",
                selected = scope == TransactionScope.PERSONAL,
                onClick = { onScopeChange(TransactionScope.PERSONAL) },
                modifier = Modifier.weight(1f),
                selectedColor = ChromaBlue
            )
            AsciiSelectChip(
                label = "HOUSEHOLD",
                selected = scope == TransactionScope.HOUSEHOLD,
                onClick = { onScopeChange(TransactionScope.HOUSEHOLD) },
                modifier = Modifier.weight(1f),
                selectedColor = ChromaOrange
            )
        }

        if (scope == TransactionScope.HOUSEHOLD && householdMembers.isNotEmpty()) {
            Text(
                text = "PAID_BY:",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                householdMembers.forEach { member ->
                    val isPaidBy = paidBy == member.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPaidBy) ChromaYellow else ChromaStone100)
                            .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                            .clickable { onPaidByChange(member.name) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name,
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChromaBlack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStep(
    mode: PaymentMode,
    onModeChange: (PaymentMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "payment_mode.select",
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Chroma.color.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMode.values().forEach { paymentModeItem ->
                AsciiSelectChip(
                    label = paymentModeItem.name,
                    selected = mode == paymentModeItem,
                    onClick = { onModeChange(paymentModeItem) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CategoryStep(
    categories: List<com.example.moneymanager.data.model.Category>,
    selectedCatId: Long?,
    onSelect: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "category.picker",
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Chroma.color.onSurfaceVariant
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(190.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                val isSelected = selectedCatId == cat.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) ChromaStone200 else Chroma.color.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ChromaBlack else ChromaStone300,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable { onSelect(cat.id) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FormatUtils.getCategoryIcon(cat.icon),
                            contentDescription = cat.name,
                            tint = ChromaBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.name,
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = ChromaBlack,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteStep(note: String, onNoteChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "note.tag // optional",
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Chroma.color.onSurfaceVariant
        )
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("Note / Tag", fontFamily = PlexMono) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(2.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChromaBlack,
                unfocusedBorderColor = ChromaStone300
            )
        )
    }
}

@Composable
private fun StepNavRow(
    step: QuickAddStep,
    canGoNext: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onCommit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (step.ordinal > 0) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Chroma.color.surface)
                    .border(1.5.dp, ChromaBlack, RoundedCornerShape(2.dp))
                    .clickable(onClick = onBack)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "← BACK",
                    style = Chroma.type.labelMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = ChromaBlack
                )
            }
        }
        if (step == QuickAddStep.NOTE) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (canGoNext) ChromaOrange else ChromaStone200)
                    .border(1.5.dp, ChromaBlack, RoundedCornerShape(2.dp))
                    .clickable(enabled = canGoNext, onClick = onCommit)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ COMMIT TRANSACTION ]",
                    style = Chroma.type.labelMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (canGoNext) ChromaWhite else Chroma.color.onSurfaceVariant
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (canGoNext) ChromaBlack else ChromaStone200)
                    .border(1.5.dp, ChromaBlack, RoundedCornerShape(2.dp))
                    .clickable(enabled = canGoNext, onClick = onNext)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NEXT →",
                    style = Chroma.type.labelMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (canGoNext) ChromaWhite else Chroma.color.onSurfaceVariant
                )
            }
        }
    }
}
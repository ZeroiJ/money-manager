package com.example.moneymanager.ui.screens.add

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.theme.PaperMono
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.ui.kinetic.Hairline
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.Haptics
import com.example.moneymanager.util.ReceiptStorage

/**
 * Paper ledger expense form (v1.20.0, option C): the whole tab is one paper
 * surface — amount as unboxed hero type, every picker a hairline square
 * with selection-by-inversion only, one full-bleed ink commit strip.
 * No cards-in-cards. Same fields and logic as before.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    presetCategory: String? = null,
    presetPaymentMode: String? = null,
    showBack: Boolean = true,
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
    val receiptUri by viewModel.receiptUri.collectAsState()
    val scheme = currentScheme()

    val context = LocalContext.current
    val launchPhotoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ReceiptStorage.saveReceipt(context, uri)
            if (savedPath != null) {
                viewModel.setReceipt(savedPath)
            }
        }
    }

    val isEditMode = editingId != null && editingId!! > 0

    LaunchedEffect(transactionId) {
        if (transactionId != null && transactionId > 0) {
            viewModel.loadTransaction(transactionId)
        }
    }

    LaunchedEffect(presetCategory, presetPaymentMode) {
        if (presetCategory != null) {
            viewModel.applyPreset(categoryName = presetCategory, paymentModeName = presetPaymentMode)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.paper)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Back chevron only in edit mode; the tab itself carries no title.
        if (showBack) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.label
                    )
                }
            }
        }

        LedgerSegmented(
            options = listOf("Expense", "Income"),
            selected = if (txType == TransactionType.EXPENSE) 0 else 1,
            onSelect = {
                viewModel.transactionType.value =
                    if (it == 0) TransactionType.EXPENSE else TransactionType.INCOME
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Amount hero: unboxed giant figure + hairline quick chips.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MonoLabel(
                text = "Entry amount",
                color = scheme.secondaryLabel
            )
            Spacer(modifier = Modifier.height(6.dp))
            IosAmount(
                text = "₹ ${if (amount.isEmpty()) "0" else amount}_",
                color = scheme.label,
                fontSize = 56.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("+50", "+100", "+500", "+2000").forEach { incStr ->
                    val chipView = LocalView.current
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .border(1.dp, scheme.label)
                            .clickable {
                                Haptics.keyPress(chipView)
                                viewModel.onNumpadClick(incStr)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
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
        }

        Hairline()

        // Scope section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonoLabel(text = "Scope", color = scheme.secondaryLabel)
            val modes = listOf(TransactionScope.PERSONAL, TransactionScope.HOUSEHOLD)
            LedgerSegmented(
                options = listOf("Personal", "Household"),
                selected = modes.indexOf(scope).coerceAtLeast(0),
                onSelect = { viewModel.transactionScope.value = modes[it] }
            )
            if (scope == TransactionScope.HOUSEHOLD && householdMembers.isNotEmpty()) {
                MonoLabel(text = "Paid by", color = scheme.secondaryLabel)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    householdMembers.forEach { member ->
                        val isPaidBy = paidBy == member.name
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .then(
                                    if (isPaidBy) Modifier.background(scheme.label)
                                    else Modifier.border(1.dp, scheme.label)
                                )
                                .clickable { viewModel.selectedPaidBy.value = member.name }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name,
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isPaidBy) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isPaidBy) scheme.paper else scheme.label
                            )
                        }
                    }
                }
            }
        }

        Hairline()

        // Payment mode section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonoLabel(text = "Payment", color = scheme.secondaryLabel)
            val payModes = PaymentMode.values()
            LedgerSegmented(
                options = payModes.map { it.name.lowercase().replaceFirstChar(Char::titlecase) },
                selected = payModes.indexOf(mode).coerceAtLeast(0),
                onSelect = { viewModel.paymentMode.value = payModes[it] }
            )
        }

        Hairline()

        // Category picker — static 4-column block, hairline squares,
        // selection by inversion only.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonoLabel(text = "Category", color = scheme.secondaryLabel)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.chunked(4).forEach { rowCats ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowCats.forEach { cat ->
                            val isSelected = selectedCatId == cat.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 76.dp)
                                    .then(
                                        if (isSelected) Modifier.background(scheme.label)
                                        else Modifier.border(1.dp, scheme.label)
                                    )
                                    .clickable { viewModel.selectedCategoryId.value = cat.id }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = FormatUtils.getCategoryIcon(cat.icon),
                                        contentDescription = cat.name,
                                        tint = if (isSelected) scheme.paper else scheme.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.name,
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 11.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) scheme.paper else scheme.label,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        // Pad the final row so cells stay equal-width.
                        repeat(4 - rowCats.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }

        Hairline()

        // Note field — hairline underline, no box.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonoLabel(text = "Note", color = scheme.secondaryLabel)
            BasicTextField(
                value = note,
                onValueChange = { viewModel.noteInput.value = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = SpaceGrotesk,
                    fontSize = 17.sp,
                    color = scheme.label
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (note.isEmpty()) {
                                Text(
                                    text = "Note (optional)",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 17.sp,
                                    color = scheme.secondaryLabel
                                )
                            }
                            inner()
                        }
                        Hairline()
                    }
                }
            )
        }

        Hairline()

        // Date section
        var showDatePicker by remember { mutableStateOf(false) }
        val selectedDate by viewModel.selectedDate.collectAsState()
        val dateLabel = remember(selectedDate) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, EEE", java.util.Locale.US)
            sdf.format(java.util.Date(selectedDate))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonoLabel(text = "Date", color = scheme.secondaryLabel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateLabel,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = scheme.label
                )
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "Pick date",
                    tint = scheme.label,
                    modifier = Modifier.size(20.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Today" to { viewModel.setDateToToday() },
                    "Yesterday" to { viewModel.setDateToYesterday() },
                    "3 days ago" to {
                        val cal = java.util.Calendar.getInstance().apply {
                            add(java.util.Calendar.DAY_OF_YEAR, -3)
                        }
                        viewModel.selectedDate.value = cal.timeInMillis
                    },
                    "1 week ago" to {
                        val cal = java.util.Calendar.getInstance().apply {
                            add(java.util.Calendar.DAY_OF_YEAR, -7)
                        }
                        viewModel.selectedDate.value = cal.timeInMillis
                    }
                ).forEach { (label, action) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                            .border(1.dp, scheme.label)
                            .clickable { action() }
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = scheme.label,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.selectedDate.value = it
                        }
                        showDatePicker = false
                    }) {
                        Text(
                            "Set date",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.label
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", fontFamily = SpaceGrotesk, color = scheme.secondaryLabel)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Hairline()

        // Receipt section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonoLabel(text = "Receipt", color = scheme.secondaryLabel)
            val receiptBitmap = remember(receiptUri) {
                receiptUri?.let { path ->
                    ReceiptStorage.getReceiptFile(path)?.let { file ->
                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(file.absolutePath, bounds)
                        var sampleSize = 1
                        while (bounds.outWidth / (sampleSize * 2) >= 800 ||
                            bounds.outHeight / (sampleSize * 2) >= 800
                        ) {
                            sampleSize *= 2
                        }
                        BitmapFactory.decodeFile(
                            file.absolutePath,
                            BitmapFactory.Options().apply { inSampleSize = sampleSize }
                        )
                    }
                }
            }

            if (receiptBitmap != null) {
                Image(
                    bitmap = receiptBitmap.asImageBitmap(),
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RectangleShape)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .background(scheme.label)
                            .clickable {
                                launchPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Attach",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = scheme.paper
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .border(1.dp, scheme.label)
                            .clickable { viewModel.removeReceipt() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Remove",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = scheme.destructive
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .background(scheme.label)
                        .clickable {
                            launchPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Attach receipt",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = scheme.paper
                    )
                }
            }
        }

        Hairline()

        IosKeypad(
            onKeyClick = { key -> viewModel.onNumpadClick(key) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Commit strip — full-bleed ink, paper text.
        val saveButtonView = LocalView.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.label)
                .clickable {
                    Haptics.saveConfirmed(saveButtonView)
                    viewModel.saveTransaction {
                        onNavigateBack()
                    }
                }
                .pressShrink()
                .padding(vertical = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEditMode) "SAVE CHANGES" else "CONFIRM & LOG EXPENSE",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp,
                color = scheme.paper
            )
        }
    }
}

/**
 * Hairline segmented control for the paper ledger: bordered row, selection
 * by inversion only (ink fill + paper text). 48dp cells.
 */
@Composable
private fun LedgerSegmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scheme: com.example.moneymanager.theme.IosScheme = currentScheme()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, scheme.label)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .then(
                        if (isSelected) Modifier.background(scheme.label)
                        else Modifier
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontFamily = SpaceGrotesk,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 15.sp,
                    color = if (isSelected) scheme.paper else scheme.label,
                    maxLines = 1
                )
            }
            if (index < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .defaultMinSize(minHeight = 48.dp)
                        .background(scheme.label)
                )
            }
        }
    }
}

@Composable
fun IosKeypad(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = currentScheme()
    val keypadView = LocalView.current
    val rows = listOf(
        listOf("7", "8", "9", "DEL"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", ".", "=")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MonoLabel(text = "Keypad", color = scheme.secondaryLabel)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isEquals = key == "="

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .then(
                                if (isEquals) Modifier.background(scheme.label)
                                else Modifier.border(1.dp, scheme.label)
                            )
                            .clickable {
                                Haptics.keyPress(keypadView)
                                onKeyClick(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontFamily = PaperMono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = if (isEquals) scheme.paper else scheme.label
                        )
                    }
                }
            }
        }
    }
}

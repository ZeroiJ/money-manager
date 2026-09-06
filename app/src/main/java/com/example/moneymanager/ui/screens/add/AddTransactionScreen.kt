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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiDivider
import com.example.moneymanager.ui.ascii.AsciiSelectChip
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.Haptics
import com.example.moneymanager.util.ReceiptStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    presetCategory: String? = null,
    presetPaymentMode: String? = null,
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
            .padding(top = 12.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Inline header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Chroma.color.surface)
                    .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Chroma.color.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "transaction // edit",
                style = Chroma.type.titleMedium.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Type Selector: Expense vs Income — flat segmented chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsciiSelectChip(
                label = "EXPENSE",
                selected = txType == TransactionType.EXPENSE,
                onClick = { viewModel.transactionType.value = TransactionType.EXPENSE },
                modifier = Modifier.weight(1f),
                selectedColor = ChromaRed
            )
            AsciiSelectChip(
                label = "INCOME",
                selected = txType == TransactionType.INCOME,
                onClick = { viewModel.transactionType.value = TransactionType.INCOME },
                modifier = Modifier.weight(1f),
                selectedColor = ChromaGreen
            )
        }

        // Amount Display — flat hairline box
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

            // Quick Increment Chips
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("+50", "+100", "+500", "+2000").forEach { incStr ->
                    val chipView = LocalView.current
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(ChromaStone100)
                            .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                            .clickable {
                                Haptics.keyPress(chipView)
                                viewModel.onNumpadClick(incStr)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
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
        }

        AsciiDivider(color = Ascii.hairline)

        // Scope Selector — flat chip group
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Chroma.color.surface)
                .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                .padding(12.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsciiSelectChip(
                    label = "PERSONAL",
                    selected = scope == TransactionScope.PERSONAL,
                    onClick = { viewModel.transactionScope.value = TransactionScope.PERSONAL },
                    modifier = Modifier.weight(1f),
                    selectedColor = ChromaBlue
                )
                AsciiSelectChip(
                    label = "HOUSEHOLD",
                    selected = scope == TransactionScope.HOUSEHOLD,
                    onClick = { viewModel.transactionScope.value = TransactionScope.HOUSEHOLD },
                    modifier = Modifier.weight(1f),
                    selectedColor = ChromaOrange
                )
            }

            // Household Paid By Selector
            if (scope == TransactionScope.HOUSEHOLD && householdMembers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "PAID_BY:",
                    style = Chroma.type.labelSmall.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
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
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isPaidBy) ChromaYellow else ChromaStone100)
                                .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                .clickable { viewModel.selectedPaidBy.value = member.name }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
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

        // Payment Mode Selector — flat chip group
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Chroma.color.surface)
                .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                .padding(12.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMode.values().forEach { paymentModeItem ->
                    AsciiSelectChip(
                        label = paymentModeItem.name,
                        selected = mode == paymentModeItem,
                        onClick = { viewModel.paymentMode.value = paymentModeItem },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Picker Grid — flat frame
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Chroma.color.surface)
                .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                .padding(12.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
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
                            .clickable { viewModel.selectedCategoryId.value = cat.id }
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

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = { viewModel.noteInput.value = it },
            label = { Text("Note / Tag (optional)", fontFamily = PlexMono) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(2.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChromaBlack,
                unfocusedBorderColor = ChromaStone300
            )
        )

        // Date Picker
        var showDatePicker by remember { mutableStateOf(false) }
        val selectedDate by viewModel.selectedDate.collectAsState()
        val dateLabel = remember(selectedDate) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, EEE", java.util.Locale.US)
            sdf.format(java.util.Date(selectedDate))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Chroma.color.surface)
                .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅",
                    style = Chroma.type.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DATE:",
                    style = Chroma.type.labelSmall.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateLabel.uppercase(),
                    style = Chroma.type.bodyMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "Pick date",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Quick date chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "TODAY" to { viewModel.setDateToToday() },
                "YESTERDAY" to { viewModel.setDateToYesterday() },
                "3 DAYS AGO" to {
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -3) }
                    viewModel.selectedDate.value = cal.timeInMillis
                },
                "1 WEEK AGO" to {
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -7) }
                    viewModel.selectedDate.value = cal.timeInMillis
                }
            ).forEach { (label, action) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ChromaStone100)
                        .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                        .clickable { action() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = Chroma.type.labelSmall.copy(
                            fontFamily = PlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = ChromaBlack
                    )
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
                    ChromaButton(
                        text = "SET DATE",
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                viewModel.selectedDate.value = it
                            }
                            showDatePicker = false
                        },
                        backgroundColor = ChromaBlack,
                        textColor = ChromaWhite,
                        shadowOffset = 1.dp
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Receipt Attachment — flat frame
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
                text = "receipt.jpg",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Chroma.color.onSurfaceVariant
            )
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
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.5.dp, ChromaBlack, RoundedCornerShape(2.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChromaButton(
                        text = "📎 ATTACH RECEIPT",
                        onClick = {
                            launchPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        backgroundColor = ChromaBlack,
                        textColor = ChromaWhite,
                        borderColor = ChromaBlack,
                        shadowOffset = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                    ChromaButton(
                        text = "REMOVE",
                        onClick = { viewModel.removeReceipt() },
                        backgroundColor = ChromaRed,
                        textColor = ChromaWhite,
                        borderColor = ChromaBlack,
                        shadowOffset = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                ChromaButton(
                    text = "📎 ATTACH RECEIPT",
                    onClick = {
                        launchPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    backgroundColor = ChromaBlack,
                    textColor = ChromaWhite,
                    borderColor = ChromaBlack,
                    shadowOffset = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Calculator Keypad
        ChromaCalculatorKeypad(
            onKeyClick = { key -> viewModel.onNumpadClick(key) }
        )

        // Save Action Button
        val saveButtonView = LocalView.current
        ChromaButton(
            text = if (isEditMode) "[ COMMIT UPDATE ]" else "[ COMMIT TRANSACTION ]",
            onClick = {
                Haptics.saveConfirmed(saveButtonView)
                viewModel.saveTransaction {
                    onNavigateBack()
                }
            },
            backgroundColor = ChromaOrange,
            textColor = ChromaWhite,
            borderColor = ChromaBlack,
            shadowOffset = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ChromaCalculatorKeypad(onKeyClick: (String) -> Unit) {
    val keypadView = LocalView.current
    val rows = listOf(
        listOf("7", "8", "9", "DEL"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", ".", "=")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    val isAction = key in listOf("+", "-", "=", "DEL", "C")
                    val isEquals = key == "="
                    val isClear = key == "C" || key == "DEL"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .chromaShadow(offset = 1.dp, cornerRadius = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isEquals -> ChromaGreen
                                    isClear -> ChromaStone200
                                    isAction -> ChromaStone100
                                    else -> Chroma.color.surface
                                }
                            )
                            .border(1.5.dp, Chroma.color.outline, RoundedCornerShape(4.dp))
                            .clickable {
                                Haptics.keyPress(keypadView)
                                onKeyClick(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            style = Chroma.type.titleMedium.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = when {
                                isEquals -> ChromaWhite
                                isClear -> ChromaRed
                                else -> Chroma.color.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}
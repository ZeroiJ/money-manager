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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
                    .clip(RoundedCornerShape(4.dp))
                    .background(Chroma.color.surface)
                    .border(1.5.dp, Chroma.color.outline, RoundedCornerShape(4.dp))
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
                text = if (isEditMode) "transaction // edit" else "quick_entry.sh",
                style = Chroma.type.titleMedium.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
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
                            .chromaShadow(offset = if (isSelected) 2.dp else 1.dp, cornerRadius = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) {
                                    if (type == TransactionType.EXPENSE) ChromaRed else ChromaGreen
                                } else Chroma.color.surface
                            )
                            .border(1.5.dp, Chroma.color.outline, RoundedCornerShape(4.dp))
                            .clickable { viewModel.transactionType.value = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[ $label ]",
                            style = Chroma.type.labelMedium.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) ChromaWhite else Chroma.color.onSurface
                        )
                    }
                }
            }

            // Amount Display Card
            ChromaCard(
                modifier = Modifier.fillMaxWidth(),
                windowTitle = "amount.inr // keypad",
                shadowOffset = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ChromaStone100)
                                    .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                    .clickable { viewModel.onNumpadClick(incStr) }
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
            }

            // Scope Selector: Personal vs Household
            ChromaCard(
                modifier = Modifier.fillMaxWidth(),
                windowTitle = "scope.tag // required",
                shadowOffset = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(TransactionScope.PERSONAL, "PERSONAL", ChromaBlue),
                            Triple(TransactionScope.HOUSEHOLD, "HOUSEHOLD", ChromaOrange)
                        ).forEach { (scopeItem, label, color) ->
                            val isSelected = scope == scopeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) color else Chroma.color.surface)
                                    .border(1.5.dp, Chroma.color.outline, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.transactionScope.value = scopeItem }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "[ $label ]",
                                    style = Chroma.type.labelMedium.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) ChromaWhite else Chroma.color.onSurface
                                )
                            }
                        }
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
            }

            // Payment Mode Selector: UPI, Cash, Card
            ChromaCard(
                modifier = Modifier.fillMaxWidth(),
                windowTitle = "payment_mode.select",
                shadowOffset = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMode.values().forEach { paymentModeItem ->
                            val isSelected = mode == paymentModeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) ChromaBlack else Chroma.color.surface)
                                    .border(1.5.dp, Chroma.color.outline, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.paymentMode.value = paymentModeItem }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = paymentModeItem.name,
                                    style = Chroma.type.labelMedium.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) ChromaWhite else Chroma.color.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Category Picker Grid
            ChromaCard(
                modifier = Modifier.fillMaxWidth(),
                windowTitle = "category.picker",
                shadowOffset = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) ChromaStone200 else Chroma.color.surface)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ChromaBlack else ChromaStone300,
                                        shape = RoundedCornerShape(4.dp)
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
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.noteInput.value = it },
                label = { Text("Note / Tag (optional)", fontFamily = PlexMono) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
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
                    .clip(RoundedCornerShape(4.dp))
                    .background(Chroma.color.surface)
                    .border(1.5.dp, Chroma.color.outline, RoundedCornerShape(4.dp))
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
                            shadowOffset = 2.dp
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

            // Receipt Attachment
            ChromaCard(
                modifier = Modifier.fillMaxWidth(),
                windowTitle = "receipt.jpg",
                shadowOffset = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.5.dp, ChromaBlack, RoundedCornerShape(4.dp))
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
                                shadowOffset = 2.dp,
                                modifier = Modifier.weight(1f)
                            )
                            ChromaButton(
                                text = "REMOVE",
                                onClick = { viewModel.removeReceipt() },
                                backgroundColor = ChromaRed,
                                textColor = ChromaWhite,
                                borderColor = ChromaBlack,
                                shadowOffset = 2.dp,
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
                            shadowOffset = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Calculator Keypad
            ChromaCalculatorKeypad(
                onKeyClick = { key -> viewModel.onNumpadClick(key) }
            )

            // Save Action Button
            ChromaButton(
                text = if (isEditMode) "[ COMMIT UPDATE ]" else "[ COMMIT TRANSACTION ]",
                onClick = {
                    viewModel.saveTransaction {
                        onNavigateBack()
                    }
                },
                backgroundColor = ChromaOrange,
                textColor = ChromaWhite,
                borderColor = ChromaBlack,
                shadowOffset = 3.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

@Composable
fun ChromaCalculatorKeypad(onKeyClick: (String) -> Unit) {
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
                            .chromaShadow(offset = 1.5.dp, cornerRadius = 4.dp)
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
                            .clickable { onKeyClick(key) },
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

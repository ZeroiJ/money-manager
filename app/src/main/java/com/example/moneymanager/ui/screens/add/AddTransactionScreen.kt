package com.example.moneymanager.ui.screens.add

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import com.example.moneymanager.ui.ios.IosSegmented
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.ui.kinetic.KineticButton
import com.example.moneymanager.ui.kinetic.KineticInk
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.Haptics
import com.example.moneymanager.util.ReceiptStorage

/**
 * Full transaction form, used solely for editing a tapped row.
 * Same fields and logic as before; iOS grouped-card chrome only.
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
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.accent
                    )
                }
            }
            Text(
                text = if (isEditMode) "Edit transaction" else "New transaction",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = scheme.label
            )
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

        // Amount hero (PRD §4.3): ochre tile, large active figure.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent)
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MonoLabel(
                text = "Entry amount",
                color = KineticInk.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            IosAmount(
                text = "₹ ${if (amount.isEmpty()) "0" else amount}_",
                color = KineticInk,
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("+50", "+100", "+500", "+2000").forEach { incStr ->
                    val chipView = LocalView.current
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .background(KineticInk)
                            .clickable {
                                Haptics.keyPress(chipView)
                                viewModel.onNumpadClick(incStr)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = incStr,
                            fontFamily = PaperMono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = scheme.accent
                        )
                    }
                }
            }
        }

        // Scope card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val modes = listOf(TransactionScope.PERSONAL, TransactionScope.HOUSEHOLD)
            IosSegmented(
                options = listOf("Personal", "Household"),
                selected = modes.indexOf(scope).coerceAtLeast(0),
                onSelect = { viewModel.transactionScope.value = modes[it] },
                scheme = scheme
            )
            if (scope == TransactionScope.HOUSEHOLD && householdMembers.isNotEmpty()) {
                Text(
                    text = "Paid by",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = scheme.secondaryLabel
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    householdMembers.forEach { member ->
                        val isPaidBy = paidBy == member.name
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPaidBy) scheme.accent.copy(alpha = 0.15f)
                                    else scheme.secondaryFill
                                )
                                .clickable { viewModel.selectedPaidBy.value = member.name }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name,
                                fontFamily = SpaceGrotesk,
                                fontWeight = if (isPaidBy) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isPaidBy) scheme.accent else scheme.label
                            )
                        }
                    }
                }
            }
        }

        // Payment mode card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.card)
                .padding(16.dp)
        ) {
            val payModes = PaymentMode.values()
            IosSegmented(
                options = payModes.map { it.name.lowercase().replaceFirstChar(Char::titlecase) },
                selected = payModes.indexOf(mode).coerceAtLeast(0),
                onSelect = { viewModel.paymentMode.value = payModes[it] },
                scheme = scheme
            )
        }

        // Category grid card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.card)
                .padding(16.dp)
        ) {
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
                            .clickable { viewModel.selectedCategoryId.value = cat.id }
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
                                fontFamily = SpaceGrotesk,
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

        OutlinedTextField(
            value = note,
            onValueChange = { viewModel.noteInput.value = it },
            label = { Text("Note (optional)", fontFamily = SpaceGrotesk) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = scheme.accent,
                unfocusedBorderColor = scheme.separator
            )
        )

        // Date card
        var showDatePicker by remember { mutableStateOf(false) }
        val selectedDate by viewModel.selectedDate.collectAsState()
        val dateLabel = remember(selectedDate) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, EEE", java.util.Locale.US)
            sdf.format(java.util.Date(selectedDate))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.card)
                .clickable { showDatePicker = true }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = scheme.secondaryLabel
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateLabel,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = scheme.label
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Pick date",
                        tint = scheme.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(scheme.secondaryFill)
                            .clickable { action() }
                            .padding(vertical = 8.dp),
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
                            color = scheme.accent
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

        // Receipt card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Receipt",
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = scheme.secondaryLabel
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
                        .clip(RoundedCornerShape(12.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            launchPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                    ) {
                        Text("Attach", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { viewModel.removeReceipt() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.destructive)
                    ) {
                        Text("Remove", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = {
                        launchPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                ) {
                    Text("Attach receipt", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        IosKeypad(
            onKeyClick = { key -> viewModel.onNumpadClick(key) }
        )

        val saveButtonView = LocalView.current
        KineticButton(
            label = if (isEditMode) "SAVE CHANGES" else "CONFIRM & LOG EXPENSE",
            onClick = {
                Haptics.saveConfirmed(saveButtonView)
                viewModel.saveTransaction {
                    onNavigateBack()
                }
            },
            modifier = Modifier.pressShrink(),
            scheme = scheme
        )

        MonoLabel(
            text = "Secure enclave active // local ledger only",
            color = scheme.secondaryLabel,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun IosKeypad(onKeyClick: (String) -> Unit) {
    val scheme = currentScheme()
    val keypadView = LocalView.current
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isEquals -> scheme.accent
                                    else -> scheme.secondaryFill
                                }
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
                            color = when {
                                isEquals -> Color.White
                                isClear -> scheme.destructive
                                isAction -> scheme.accent
                                else -> scheme.label
                            }
                        )
                    }
                }
            }
        }
    }
}

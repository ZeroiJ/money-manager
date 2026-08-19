package com.example.moneymanager.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.*
import com.example.moneymanager.theme.*
import com.example.moneymanager.util.FormatUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val categories by viewModel.categories.collectAsState()
    val recurringRules by viewModel.recurringRules.collectAsState()
    val householdMembers by viewModel.householdMembers.collectAsState()
    val useIndianGrouping by viewModel.useIndianGrouping.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showBackupExportDialog by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline Privacy Banner Card
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoYellow,
                    borderColor = NeoBlack,
                    shadowOffset = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeoBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = NeoYellow, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% OFFLINE & PRIVATE",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = NeoBlack
                            )
                            Text(
                                text = "All financial data remains on this device. No servers, zero telemetry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoBlack
                            )
                        }
                    }
                }
            }

            // Display Preferences
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INDIAN NUMBER GROUPING",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = if (useIndianGrouping) "Lakh/Crore format (e.g. ₹1,50,000)" else "Standard format (e.g. ₹150,000)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useIndianGrouping,
                            onCheckedChange = { viewModel.setUseIndianGrouping(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeoBlack,
                                checkedTrackColor = NeoYellow,
                                uncheckedThumbColor = NeoGray700,
                                uncheckedTrackColor = NeoGray200
                            )
                        )
                    }
                }
            }

            // Categories Management
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 3.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXPENSE CATEGORIES",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            TextButton(onClick = { showAddCategoryDialog = true }) {
                                Text("+ ADD CUSTOM", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        categories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(category.color.toInt()))
                                        .border(1.dp, NeoBlack, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = FormatUtils.getCategoryIcon(category.icon),
                                        contentDescription = null,
                                        tint = NeoBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                if (!category.isDefault) {
                                    IconButton(
                                        onClick = { viewModel.deleteCategory(category) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = NeoRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recurring Subscriptions Management
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 3.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECURRING SUBSCRIPTIONS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            TextButton(onClick = { showAddRecurringDialog = true }) {
                                Text("+ ADD NEW", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (recurringRules.isEmpty()) {
                            Text(
                                text = "No active recurring bills. Use + ADD NEW to configure Rent, WiFi, Netflix, SIP, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            recurringRules.forEach { rule ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rule.note.ifBlank { "Subscription" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black)
                                        )
                                        Text(
                                            text = "${FormatUtils.formatCurrency(rule.amount)} • ${rule.frequency.name} • Next: ${FormatUtils.formatDate(rule.nextDueDate)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteRecurringRule(rule) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = NeoRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = 1.dp, color = NeoGray200)
                            }
                        }
                    }
                }
            }

            // Household Members
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 3.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HOUSEHOLD MEMBERS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            TextButton(onClick = { showAddMemberDialog = true }) {
                                Text("+ ADD MEMBER", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        householdMembers.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NeoOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                if (householdMembers.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.deleteHouseholdMember(member) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = NeoRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Backup & Data Export
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 4.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "DATA BACKUP & EXPORT",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )

                        NeoButton(
                            text = "EXPORT TO CSV (EXCEL / SHEETS)",
                            onClick = {
                                scope.launch {
                                    val csv = viewModel.exportCsvBackup()
                                    showBackupExportDialog = csv
                                }
                            },
                            backgroundColor = NeoWhite,
                            textColor = NeoBlack,
                            borderColor = NeoBlack,
                            shadowOffset = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        NeoButton(
                            text = "EXPORT FULL BACKUP (JSON)",
                            onClick = {
                                scope.launch {
                                    val json = viewModel.exportJsonBackup()
                                    showBackupExportDialog = json
                                }
                            },
                            backgroundColor = NeoYellow,
                            textColor = NeoBlack,
                            borderColor = NeoBlack,
                            shadowOffset = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        NeoButton(
                            text = "RESTORE / IMPORT JSON BACKUP",
                            onClick = { showImportDialog = true },
                            backgroundColor = NeoGray100,
                            textColor = NeoBlack,
                            borderColor = NeoBlack,
                            shadowOffset = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, icon, color ->
                viewModel.addCategory(name, icon, color)
                showAddCategoryDialog = false
            }
        )
    }

    // Add Recurring Dialog
    if (showAddRecurringDialog) {
        AddRecurringDialog(
            categories = categories,
            onDismiss = { showAddRecurringDialog = false },
            onSave = { amount, type, catId, note, mode, scopeType, freq, dueDate ->
                viewModel.addRecurringRule(amount, type, catId, note, mode, scopeType, freq, dueDate)
                showAddRecurringDialog = false
            }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        var memberName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            shape = RoundedCornerShape(6.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("ADD HOUSEHOLD MEMBER", fontWeight = FontWeight.Black) },
            text = {
                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("Member Name (e.g. Partner, Roommate)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
            },
            confirmButton = {
                NeoButton(
                    text = "ADD",
                    onClick = {
                        viewModel.addHouseholdMember(memberName)
                        showAddMemberDialog = false
                    },
                    backgroundColor = NeoYellow,
                    textColor = NeoBlack,
                    borderColor = NeoBlack,
                    shadowOffset = 2.dp,
                    enabled = memberName.isNotBlank()
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Backup View & Copy Dialog
    if (showBackupExportDialog != null) {
        val content = showBackupExportDialog!!
        AlertDialog(
            onDismissRequest = { showBackupExportDialog = null },
            shape = RoundedCornerShape(6.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("BACKUP GENERATED", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Your offline data backup has been generated. You can copy it to your clipboard:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content.take(300) + if (content.length > 300) "..." else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                NeoButton(
                    text = "COPY TO CLIPBOARD",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MoneyManagerBackup", content))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupExportDialog = null
                    },
                    backgroundColor = NeoYellow,
                    textColor = NeoBlack,
                    borderColor = NeoBlack,
                    shadowOffset = 2.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showBackupExportDialog = null }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var importText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            shape = RoundedCornerShape(6.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("RESTORE JSON BACKUP", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Paste your exported JSON backup text below to restore:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text("Paste JSON Here") },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                NeoButton(
                    text = "RESTORE",
                    onClick = {
                        scope.launch {
                            val success = viewModel.importJsonBackup(importText)
                            if (success) {
                                Toast.makeText(context, "Data successfully restored!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid JSON format. Please verify.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    backgroundColor = NeoYellow,
                    textColor = NeoBlack,
                    borderColor = NeoBlack,
                    shadowOffset = 2.dp,
                    enabled = importText.isNotBlank()
                )
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(FormatUtils.AVAILABLE_ICONS.first()) }
    var selectedColor by remember { mutableStateOf(FormatUtils.PRESET_COLORS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(6.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("NEW EXPENSE CATEGORY", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Color", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FormatUtils.PRESET_COLORS) { colorLong ->
                        val color = Color(colorLong)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                                .clickable { selectedColor = colorLong }
                                .border(
                                    width = if (selectedColor == colorLong) 2.5.dp else 1.dp,
                                    color = NeoBlack,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Icon", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(FormatUtils.AVAILABLE_ICONS) { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, NeoBlack, RoundedCornerShape(4.dp))
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FormatUtils.getCategoryIcon(iconName),
                                contentDescription = null,
                                tint = NeoBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            NeoButton(
                text = "CREATE",
                onClick = { onSave(name, selectedIcon, selectedColor) },
                backgroundColor = NeoYellow,
                textColor = NeoBlack,
                borderColor = NeoBlack,
                shadowOffset = 2.dp,
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
fun AddRecurringDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Double, TransactionType, Long, String, PaymentMode, TransactionScope, Frequency, Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 0L) }
    var frequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var paymentMode by remember { mutableStateOf(PaymentMode.UPI) }
    var scope by remember { mutableStateOf(TransactionScope.PERSONAL) }

    val subscriptionTemplates = listOf(
        "House Rent" to 15000.0,
        "WiFi / Broadband" to 999.0,
        "Maid / Cook" to 4000.0,
        "Milk / Groceries" to 2000.0,
        "Netflix / OTT" to 499.0,
        "Mobile Recharge" to 349.0,
        "SIP / Mutual Fund" to 5000.0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(6.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("ADD RECURRING RULE", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Quick Templates:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subscriptionTemplates.forEach { (tmplName, defaultAmt) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeoGray100)
                                .border(1.5.dp, NeoBlack, RoundedCornerShape(4.dp))
                                .clickable {
                                    note = tmplName
                                    amountText = defaultAmt.toLong().toString()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(tmplName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = NeoBlack)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Description (e.g., Rent, WiFi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Frequency", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Frequency.entries.forEach { freq ->
                        val isSelected = frequency == freq
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, NeoBlack, RoundedCornerShape(4.dp))
                                .clickable { frequency = freq }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(freq.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = NeoBlack)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Scope", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TransactionScope.entries.forEach { s ->
                        val isSelected = scope == s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) (if (s == TransactionScope.PERSONAL) NeoBlue else NeoOrange) else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, NeoBlack, RoundedCornerShape(4.dp))
                                .clickable { scope = s }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(s.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = if (isSelected) NeoWhite else NeoBlack)
                        }
                    }
                }
            }
        },
        confirmButton = {
            NeoButton(
                text = "ADD RULE",
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt, TransactionType.EXPENSE, selectedCatId, note, paymentMode, scope, frequency, System.currentTimeMillis())
                    }
                },
                backgroundColor = NeoYellow,
                textColor = NeoBlack,
                borderColor = NeoBlack,
                shadowOffset = 2.dp,
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0 && note.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

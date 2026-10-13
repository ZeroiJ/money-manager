package com.example.moneymanager.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.db.AppDatabase
import com.example.moneymanager.data.model.PendingImport
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosGroupedList
import com.example.moneymanager.ui.ios.IosRow
import com.example.moneymanager.ui.ios.IosSwitchRow
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.util.ErrorBus
import com.example.moneymanager.util.ErrorKind
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.XlsxImporter
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scheme = currentScheme()
    val useIndianGrouping by viewModel.useIndianGrouping.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val ambientEnabled by viewModel.ambientEnabled.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val householdMembers by viewModel.householdMembers.collectAsState()
    val xlsxPreview by viewModel.xlsxPreview.collectAsState()
    val xlsxBusy by viewModel.xlsxBusy.collectAsState()
    val xlsxError by viewModel.xlsxError.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonStr = viewModel.exportJsonBackup()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(jsonStr.toByteArray())
                    }
                    Toast.makeText(context, "JSON Export Complete", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    ErrorBus.report(ErrorKind.EXPORT_FAILED, "Settings → Export", "Couldn't write the JSON backup", e)
                }
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val csvStr = viewModel.exportCsvBackup()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(csvStr.toByteArray())
                    }
                    Toast.makeText(context, "CSV Export Complete", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    ErrorBus.report(ErrorKind.EXPORT_FAILED, "Settings → Export", "Couldn't write the CSV backup", e)
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val stringBuilder = StringBuilder()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line: String? = reader.readLine()
                            while (line != null) {
                                stringBuilder.append(line)
                                line = reader.readLine()
                            }
                        }
                    }
                    val success = viewModel.importJsonBackup(stringBuilder.toString())
                    if (success) {
                        Toast.makeText(context, "Backup Restored Successfully", Toast.LENGTH_LONG).show()
                    } else {
                        ErrorBus.report(ErrorKind.IMPORT_FAILED, "Settings → Import", "Invalid backup file", null)
                    }
                } catch (e: Exception) {
                    ErrorBus.report(ErrorKind.IMPORT_FAILED, "Settings → Import", "Couldn't read the backup file", e)
                }
            }
        }
    }

    val importXlsxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.parseXlsx(context, uri)
        }
    }

    val pendingImports by viewModel.pendingImports.collectAsState()
    var listenerEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        listenerEnabled = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }
    val notificationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        listenerEnabled = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val n = viewModel.scanSmsInbox(context)
                Toast.makeText(
                    context,
                    if (n > 0) "$n new payments found" else "No new payments in inbox",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(context, "SMS permission needed for auto-import", Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = scheme.accent
                )
            }
            Text(
                text = "Settings",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = scheme.label
            )
        }

        LazyColumn(            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Security
            item {
                IosGroupedList(header = "Security", scheme = scheme) {
                    IosRow(
                        title = "On-device only",
                        subtitle = "No tracking, no analytics. Records live in local SQLite.",
                        icon = null,
                        iconTint = scheme.success,
                        scheme = scheme,
                        showChevron = false
                    ) {}
                    HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                    IosSwitchRow(
                        title = "App lock",
                        subtitle = if (biometricEnabled) "On — biometric required"
                        else "Off — local device lock only",
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric() },
                        scheme = scheme
                    )
                }
            }

            // Display
            item {
                IosGroupedList(header = "Display", scheme = scheme) {
                    IosSwitchRow(
                        title = "Indian number grouping",
                        subtitle = if (useIndianGrouping) "e.g. ₹1,50,000"
                        else "e.g. ₹150,000.00",
                        checked = useIndianGrouping,
                        onCheckedChange = { viewModel.setUseIndianGrouping(it) },
                        scheme = scheme
                    )
                    HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                    IosSwitchRow(
                        title = "Paper texture",
                        subtitle = "Subtle grain behind every screen",
                        checked = ambientEnabled,
                        onCheckedChange = { viewModel.setAmbientEnabled(it) },
                        scheme = scheme
                    )
                }
            }

            // Household
            item {
                IosGroupedList(header = "Household · ${householdMembers.size}", scheme = scheme) {
                    if (householdMembers.isEmpty()) {
                        Text(
                            text = "No members yet.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 15.sp,
                            color = scheme.secondaryLabel,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        householdMembers.forEachIndexed { index, member ->
                            IosRow(
                                title = member.name,
                                subtitle = null,
                                icon = null,
                                iconTint = scheme.accent,
                                scheme = scheme,
                                showChevron = false
                            ) {
                                IconButton(
                                    onClick = { viewModel.deleteHouseholdMember(member) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete ${member.name}",
                                        tint = scheme.destructive
                                    )
                                }
                            }
                            if (index < householdMembers.size - 1) {
                                HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsActionButton(
                    text = "Add member",
                    primary = false,
                    scheme = scheme,
                    onClick = {
                        newMemberName = ""
                        showAddMemberDialog = true
                    }
                )
            }

            // Categories
            item {
                IosGroupedList(header = "Categories · ${categories.size}", scheme = scheme) {
                    if (categories.isEmpty()) {
                        Text(
                            text = "No categories yet.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 15.sp,
                            color = scheme.secondaryLabel,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        categories.forEachIndexed { index, cat ->
                            IosRow(
                                title = cat.name,
                                subtitle = null,
                                icon = FormatUtils.getCategoryIcon(cat.icon),
                                iconTint = scheme.label,
                                scheme = scheme,
                                showChevron = false
                            ) {
                                if (cat.isDefault) {
                                    Text(
                                        text = "System",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 13.sp,
                                        color = scheme.secondaryLabel
                                    )
                                } else {
                                    IconButton(
                                        onClick = { viewModel.deleteCategory(cat) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete ${cat.name}",
                                            tint = scheme.destructive
                                        )
                                    }
                                }
                            }
                            if (index < categories.size - 1) {
                                HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsActionButton(
                    text = "Add category",
                    primary = false,
                    scheme = scheme,
                    onClick = {
                        newCategoryName = ""
                        showAddCategoryDialog = true
                    }
                )
            }

            // Backup
            item {
                IosGroupedList(header = "Backup", scheme = scheme) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsActionButton(
                            text = "Export backup (JSON)",
                            primary = true,
                            scheme = scheme,
                            onClick = {
                                exportJsonLauncher.launch(
                                    "money_manager_backup_${System.currentTimeMillis()}.json"
                                )
                            }
                        )
                        SettingsActionButton(
                            text = "Export spreadsheet (CSV)",
                            primary = false,
                            scheme = scheme,
                            onClick = {
                                exportCsvLauncher.launch(
                                    "money_manager_export_${System.currentTimeMillis()}.csv"
                                )
                            }
                        )
                        SettingsActionButton(
                            text = "Restore backup (JSON)",
                            primary = false,
                            scheme = scheme,
                            onClick = { importJsonLauncher.launch(arrayOf("application/json")) }
                        )
                        SettingsActionButton(
                            text = "Import Excel (.xlsx)",
                            primary = false,
                            scheme = scheme,
                            onClick = {
                                importXlsxLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel"
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // SMS auto-import
            item {
                IosGroupedList(
                    header = "SMS auto-import · ${pendingImports.size}",
                    scheme = scheme
                ) {
                    IosRow(
                        title = "UPI SMS capture",
                        subtitle = "Bank SMS + GPay → review queue, on-device only.",
                        icon = null,
                        iconTint = scheme.accent,
                        scheme = scheme,
                        showChevron = false
                    ) {}
                    HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                    IosRow(
                        title = "Notification listener",
                        subtitle = if (listenerEnabled) "On" else "Off",
                        icon = null,
                        iconTint = scheme.accent,
                        scheme = scheme,
                        showChevron = false
                    ) {
                        Text(
                            text = if (listenerEnabled) "On" else "Off",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (listenerEnabled) scheme.success else scheme.secondaryLabel
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsActionButton(
                            text = if (listenerEnabled) "Notification access granted"
                            else "Enable notification access",
                            primary = !listenerEnabled,
                            scheme = scheme,
                            onClick = {
                                notificationSettingsLauncher.launch(
                                    android.content.Intent(
                                        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                                    )
                                )
                            }
                        )
                        SettingsActionButton(
                            text = "Scan SMS inbox",
                            primary = true,
                            scheme = scheme,
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_SMS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    scope.launch {
                                        val n = viewModel.scanSmsInbox(context)
                                        Toast.makeText(
                                            context,
                                            if (n > 0) "$n new payments found"
                                            else "No new payments in inbox",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                                }
                            }
                        )
                    }
                    if (pendingImports.isNotEmpty()) {
                        HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                        Text(
                            text = "Review queue — approve to log, × to discard",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = scheme.secondaryLabel
                        )
                        pendingImports.forEach { item ->
                            PendingImportRow(
                                item = item,
                                scheme = scheme,
                                onApprove = { viewModel.approvePending(item) },
                                onDiscard = { viewModel.discardPending(item) }
                            )
                        }
                    }
                }
            }

            // Diagnostics — local-only health + error history. Nothing here
            // ever leaves the device; there is no telemetry in this app.
            item {
                val recentErrors by ErrorBus.recent.collectAsState()
                var counts by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
                LaunchedEffect(Unit) {
                    counts = try {
                        viewModel.dbCounts()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
                val appVersion = remember {
                    try {
                        @Suppress("DEPRECATION")
                        context.packageManager
                            .getPackageInfo(context.packageName, 0)
                            ?.versionName ?: "?"
                    } catch (_: Exception) {
                        "?"
                    }
                }
                val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                IosGroupedList(header = "Diagnostics · on this phone only", scheme = scheme) {
                    IosRow(
                        title = "App",
                        subtitle = "v$appVersion · Room database v${AppDatabase.DB_VERSION} (SQLite)",
                        icon = null,
                        iconTint = scheme.accent,
                        scheme = scheme,
                        showChevron = false
                    ) {}
                    if (counts.isNotEmpty()) {
                        HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                        IosRow(
                            title = "Stored records",
                            subtitle = counts.joinToString(" · ") { (name, n) -> "$name $n" },
                            icon = null,
                            iconTint = scheme.accent,
                            scheme = scheme,
                            showChevron = false
                        ) {}
                    }
                    HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
                    Text(
                        text = if (recentErrors.isEmpty()) "No errors recorded — everything is healthy."
                        else "Recent errors (${recentErrors.size})",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = scheme.secondaryLabel,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    recentErrors.asReversed().forEach { err ->
                        val time = remember(err.at) {
                            java.text.SimpleDateFormat("d MMM HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(err.at))
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "${err.kind.label} · ${err.where} · $time",
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = scheme.label
                            )
                            Text(
                                text = err.message + (err.cause?.let { " ($it)" } ?: ""),
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = scheme.secondaryLabel
                            )
                        }
                    }
                    if (recentErrors.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SettingsActionButton(
                                    text = "Copy report",
                                    primary = false,
                                    scheme = scheme,
                                    onClick = {
                                        val report = buildString {
                                            appendLine("Money Manager v$appVersion · Room v${AppDatabase.DB_VERSION}")
                                            appendLine(counts.joinToString(", ") { (name, n) -> "$name: $n" })
                                            recentErrors.forEach { err ->
                                                appendLine("[${err.kind.label}] ${err.where}: ${err.message} ${err.cause ?: ""}")
                                            }
                                        }
                                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(report))
                                        Toast.makeText(context, "Report copied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SettingsActionButton(
                                    text = "Clear log",
                                    primary = false,
                                    scheme = scheme,
                                    onClick = { ErrorBus.clear() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = scheme.card,
            title = {
                Text(
                    text = "New category",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = scheme.label
                )
            },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category name", fontFamily = SpaceGrotesk) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = scheme.accent,
                        unfocusedBorderColor = scheme.separator
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName.trim(), "category", 0xFF2563EB)
                        }
                        showAddCategoryDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                ) {
                    Text("Save", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", fontFamily = SpaceGrotesk, color = scheme.accent)
                }
            }
        )
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = scheme.card,
            title = {
                Text(
                    text = "New member",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = scheme.label
                )
            },
            text = {
                OutlinedTextField(
                    value = newMemberName,
                    onValueChange = { newMemberName = it },
                    label = { Text("Member name", fontFamily = SpaceGrotesk) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = scheme.accent,
                        unfocusedBorderColor = scheme.separator
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            viewModel.addHouseholdMember(newMemberName.trim())
                        }
                        showAddMemberDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                ) {
                    Text("Save", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel", fontFamily = SpaceGrotesk, color = scheme.accent)
                }
            }
        )
    }

    if (xlsxBusy || xlsxPreview != null || xlsxError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissXlsxPreview() },
            shape = RoundedCornerShape(16.dp),
            containerColor = scheme.card,
            title = {
                Text(
                    text = "Excel import",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = scheme.label
                )
            },
            text = {
                when {
                    xlsxBusy -> Text(
                        text = "Parsing workbook…",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = scheme.secondaryLabel
                    )
                    xlsxError != null -> Text(
                        text = xlsxError ?: "Unknown error",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = scheme.destructive
                    )
                    else -> {
                        val preview = xlsxPreview
                        if (preview != null) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${preview.rows.size} rows ready · ${preview.skippedRows} skipped / ${preview.totalRows} total",
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = scheme.secondaryLabel
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (preview.rows.isEmpty()) {
                                    Text(
                                        text = "No valid rows found in sheet.",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 15.sp,
                                        color = scheme.secondaryLabel
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(320.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        itemsIndexed(preview.rows) { _, row ->
                                            XlsxPreviewRow(row, scheme)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val preview = xlsxPreview
                if (preview != null && preview.rows.isNotEmpty()) {
                    Button(
                        onClick = {
                            scope.launch {
                                val inserted = viewModel.commitImport()
                                Toast.makeText(
                                    context,
                                    "Imported $inserted rows",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                    ) {
                        Text("Import", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissXlsxPreview() }) {
                    Text("Close", fontFamily = SpaceGrotesk, color = scheme.accent)
                }
            }
        )
    }
}

@Composable
private fun SettingsActionButton(
    text: String,
    primary: Boolean,
    scheme: IosScheme,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .pressShrink(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) scheme.accent else scheme.secondaryFill,
            contentColor = if (primary) Color.White else scheme.label
        ),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun PendingImportRow(
    item: PendingImport,
    scheme: IosScheme,
    onApprove: () -> Unit,
    onDiscard: () -> Unit
) {
    val dateLabel =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(item.date))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.secondaryFill)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            IosAmount(
                text = FormatUtils.formatCurrency(item.amount) +
                    if (item.type == TransactionType.INCOME) " + " else "",
                color = if (item.type == TransactionType.INCOME) scheme.success else scheme.label,
                fontSize = 15.sp
            )
            Text(
                text = "${item.merchant} · $dateLabel · ${item.sender}",
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = scheme.secondaryLabel,
                maxLines = 1
            )
        }
        Row {
            IconButton(onClick = onApprove, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Approve",
                    tint = scheme.success
                )
            }
            IconButton(onClick = onDiscard, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Discard",
                    tint = scheme.destructive
                )
            }
        }
    }
}

@Composable
private fun XlsxPreviewRow(row: XlsxImporter.ParsedTransaction, scheme: IosScheme) {
    val dateLabel =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(row.date))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.secondaryFill)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            IosAmount(
                text = FormatUtils.formatCurrency(row.amount) +
                    if (row.type == TransactionType.INCOME) " + " else "",
                color = if (row.type == TransactionType.INCOME) scheme.success else scheme.label,
                fontSize = 15.sp
            )
            Text(
                text = row.note.ifBlank { "(no note)" },
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = scheme.secondaryLabel,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (row.categoryHint.isBlank()) "—" else row.categoryHint,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = scheme.label
            )
            Text(
                text = "${dateLabel} · ${row.scope.name.take(4)} · ${row.paymentMode.name}",
                fontFamily = SpaceGrotesk,
                fontSize = 11.sp,
                color = scheme.secondaryLabel
            )
        }
    }
}

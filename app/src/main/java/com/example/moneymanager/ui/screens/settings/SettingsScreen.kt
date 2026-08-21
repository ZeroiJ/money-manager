package com.example.moneymanager.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.theme.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val useIndianGrouping by viewModel.useIndianGrouping.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val householdMembers by viewModel.householdMembers.collectAsState()
    val recurringRules by viewModel.recurringRules.collectAsState()

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
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(
                        context,
                        if (success) "Backup Restored Successfully" else "Invalid JSON format",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Import error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importXlsxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val result = viewModel.importXlsx(context, uri)
                    Toast.makeText(
                        context,
                        "Imported ${result.inserted} of ${result.total} rows",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Throwable) {
                    Toast.makeText(context, "Import error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "system // config",
                        style = Chroma.type.titleMedium.copy(
                            fontFamily = PlexMono,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 8.dp)
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Chroma.color.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline Security Banner
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "security.status // local_storage",
                    statusIndicator = "[ 100% PRIVATE ]",
                    shadowOffset = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AIR-GAPPED OFFLINE ROOM DATABASE",
                            style = Chroma.type.titleSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero tracking, zero analytics, zero telemetry. All records reside purely on your device in local SQLite.",
                            style = Chroma.type.bodySmall,
                            color = Chroma.color.onSurfaceVariant
                        )
                    }
                }
            }

            // Biometric Lock
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "security.cfg",
                    statusIndicator = if (biometricEnabled) "[ LOCKED ]" else "[ UNLOCKED ]",
                    shadowOffset = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BIOMETRIC_LOCK",
                                    style = Chroma.type.bodyMedium.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Local device lock only — no account, no cloud",
                                    style = Chroma.type.labelSmall.copy(fontSize = 10.sp),
                                    color = Chroma.color.onSurfaceVariant
                                )
                            }
                            ChromaBadge(
                                text = if (biometricEnabled) "[ LOCKED ]" else "[ UNLOCKED ]",
                                backgroundColor = if (biometricEnabled) ChromaGreen else ChromaStone100,
                                textColor = if (biometricEnabled) ChromaWhite else ChromaBlack,
                                borderColor = if (biometricEnabled) ChromaGreen else ChromaBlack
                            )
                        }

                        ChromaButton(
                            text = if (biometricEnabled) "DISABLE BIOMETRIC" else "ENABLE BIOMETRIC",
                            onClick = { viewModel.toggleBiometric() },
                            backgroundColor = if (biometricEnabled) ChromaRed else ChromaBlack,
                            textColor = ChromaWhite,
                            shadowOffset = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Display Preferences
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "display.format",
                    shadowOffset = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "INDIAN_NUMBER_GROUPING",
                                    style = Chroma.type.bodyMedium.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (useIndianGrouping) "e.g. ₹1,50,000 (Lakhs/Crores)" else "e.g. ₹150,000.00 (Standard)",
                                    style = Chroma.type.labelSmall.copy(fontSize = 10.sp),
                                    color = Chroma.color.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useIndianGrouping,
                                onCheckedChange = { viewModel.setUseIndianGrouping(it) }
                            )
                        }
                    }
                }
            }

            // Household Members Manager
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "household_members.list",
                    statusIndicator = "${householdMembers.size} MEMBERS",
                    shadowOffset = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        householdMembers.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ChromaStone100)
                                    .border(0.5.dp, ChromaStone300, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = member.name,
                                    style = Chroma.type.bodyMedium.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                IconButton(
                                    onClick = { viewModel.deleteHouseholdMember(member) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ChromaRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        ChromaButton(
                            text = "+ ADD HOUSEHOLD MEMBER",
                            onClick = {
                                newMemberName = ""
                                showAddMemberDialog = true
                            },
                            backgroundColor = ChromaBlack,
                            textColor = ChromaWhite,
                            shadowOffset = 1.dp
                        )
                    }
                }
            }

            // Categories Manager
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "categories.config",
                    statusIndicator = "${categories.size} ACTIVE",
                    shadowOffset = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ChromaStone100)
                                    .border(0.5.dp, ChromaStone300, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(ChromaBlack)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.name,
                                        style = Chroma.type.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                if (!cat.isDefault) {
                                    IconButton(
                                        onClick = { viewModel.deleteCategory(cat) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ChromaRed, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Text(
                                        text = "[ SYSTEM ]",
                                        style = Chroma.type.labelSmall.copy(
                                            fontFamily = PlexMono,
                                            fontSize = 9.sp
                                        ),
                                        color = ChromaStone500
                                    )
                                }
                            }
                        }

                        ChromaButton(
                            text = "+ CREATE CUSTOM CATEGORY",
                            onClick = {
                                newCategoryName = ""
                                showAddCategoryDialog = true
                            },
                            backgroundColor = ChromaBlack,
                            textColor = ChromaWhite,
                            shadowOffset = 1.dp
                        )
                    }
                }
            }

            // Backup & Export
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "backup_restore.sh",
                    shadowOffset = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChromaButton(
                            text = "EXPORT FULL BACKUP (JSON)",
                            onClick = { exportJsonLauncher.launch("money_manager_backup_${System.currentTimeMillis()}.json") },
                            backgroundColor = ChromaBlack,
                            textColor = ChromaWhite,
                            shadowOffset = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChromaButton(
                            text = "EXPORT SPREADSHEET (CSV)",
                            onClick = { exportCsvLauncher.launch("money_manager_export_${System.currentTimeMillis()}.csv") },
                            backgroundColor = ChromaStone200,
                            textColor = ChromaBlack,
                            borderColor = ChromaBlack,
                            shadowOffset = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChromaButton(
                            text = "RESTORE / IMPORT (JSON)",
                            onClick = { importJsonLauncher.launch(arrayOf("application/json")) },
                            backgroundColor = ChromaStone100,
                            textColor = ChromaBlack,
                            borderColor = ChromaStone400,
                            shadowOffset = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChromaButton(
                            text = "IMPORT EXCEL (.XLSX)",
                            onClick = { importXlsxLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")) },
                            backgroundColor = ChromaStone200,
                            textColor = ChromaBlack,
                            borderColor = ChromaBlack,
                            shadowOffset = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            shape = RoundedCornerShape(4.dp),
            containerColor = Chroma.color.surface,
            title = {
                Text(
                    text = "CATEGORY // NEW",
                    style = Chroma.type.titleMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name", fontFamily = PlexMono) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
            },
            confirmButton = {
                ChromaButton(
                    text = "SAVE",
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName.trim(), "category", 0xFF2563EB)
                        }
                        showAddCategoryDialog = false
                    },
                    backgroundColor = ChromaOrange,
                    textColor = ChromaWhite,
                    shadowOffset = 2.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = Chroma.color.onSurface)
                }
            }
        )
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            shape = RoundedCornerShape(4.dp),
            containerColor = Chroma.color.surface,
            title = {
                Text(
                    text = "HOUSEHOLD_MEMBER // NEW",
                    style = Chroma.type.titleMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = newMemberName,
                    onValueChange = { newMemberName = it },
                    label = { Text("Member Name", fontFamily = PlexMono) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                )
            },
            confirmButton = {
                ChromaButton(
                    text = "SAVE",
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            viewModel.addHouseholdMember(newMemberName.trim())
                        }
                        showAddMemberDialog = false
                    },
                    backgroundColor = ChromaOrange,
                    textColor = ChromaWhite,
                    shadowOffset = 2.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = Chroma.color.onSurface)
                }
            }
        )
    }
}

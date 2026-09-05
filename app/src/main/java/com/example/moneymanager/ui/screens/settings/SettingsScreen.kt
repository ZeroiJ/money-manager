package com.example.moneymanager.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiDivider
import com.example.moneymanager.ui.ascii.AsciiSectionHeader
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

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

    Column(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
        // Inline header with flat back box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Chroma.color.surface)
                    .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Chroma.color.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "system // config",
                style = Chroma.type.titleMedium.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SECURITY section
            item { AsciiSectionHeader(text = "SECURITY") }

            item {
                FlatSection {
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

                    AsciiDivider(modifier = Modifier.padding(vertical = 10.dp))

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
                        if (biometricEnabled) {
                            ChromaBadge(
                                text = "[ LOCKED ]",
                                backgroundColor = ChromaGreen,
                                textColor = ChromaWhite,
                                borderColor = ChromaGreen
                            )
                        } else {
                            ChromaBadge(
                                text = "[ UNLOCKED ]",
                                backgroundColor = ChromaStone100,
                                textColor = ChromaBlack,
                                borderColor = ChromaBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
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

            // DISPLAY section
            item { AsciiSectionHeader(text = "DISPLAY") }

            item {
                FlatSection {
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

            // HOUSEHOLD MEMBERS section
            item { AsciiSectionHeader(text = "HOUSEHOLD_MEMBERS [ ${householdMembers.size} ]") }

            item {
                FlatSection {
                    if (householdMembers.isEmpty()) {
                        Text(
                            text = "No members yet.",
                            style = Chroma.type.bodySmall,
                            color = Chroma.color.onSurfaceVariant
                        )
                    } else {
                        householdMembers.forEach { member ->
                            FlatRow(
                                label = member.name,
                                trailing = {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                            .clickable { viewModel.deleteHouseholdMember(member) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ChromaRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            )
                            AsciiDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    ChromaButton(
                        text = "+ ADD HOUSEHOLD MEMBER",
                        onClick = {
                            newMemberName = ""
                            showAddMemberDialog = true
                        },
                        backgroundColor = ChromaBlack,
                        textColor = ChromaWhite,
                        shadowOffset = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // CATEGORIES section
            item { AsciiSectionHeader(text = "CATEGORIES [ ${categories.size} ]") }

            item {
                FlatSection {
                    if (categories.isEmpty()) {
                        Text(
                            text = "No categories yet.",
                            style = Chroma.type.bodySmall,
                            color = Chroma.color.onSurfaceVariant
                        )
                    } else {
                        categories.forEach { cat ->
                            FlatRow(
                                label = cat.name,
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(ChromaBlack)
                                    )
                                },
                                trailing = {
                                    if (cat.isDefault) {
                                        Text(
                                            text = "[ SYSTEM ]",
                                            style = Chroma.type.labelSmall.copy(
                                                fontFamily = PlexMono,
                                                fontSize = 9.sp
                                            ),
                                            color = ChromaStone500
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                                .clickable { viewModel.deleteCategory(cat) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = ChromaRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            )
                            AsciiDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    ChromaButton(
                        text = "+ CREATE CUSTOM CATEGORY",
                        onClick = {
                            newCategoryName = ""
                            showAddCategoryDialog = true
                        },
                        backgroundColor = ChromaBlack,
                        textColor = ChromaWhite,
                        shadowOffset = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // BACKUP & RESTORE section
            item { AsciiSectionHeader(text = "BACKUP_RESTORE") }

            item {
                FlatSection {
                    ChromaButton(
                        text = "EXPORT FULL BACKUP (JSON)",
                        onClick = { exportJsonLauncher.launch("money_manager_backup_${System.currentTimeMillis()}.json") },
                        backgroundColor = ChromaBlack,
                        textColor = ChromaWhite,
                        shadowOffset = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ChromaButton(
                        text = "EXPORT SPREADSHEET (CSV)",
                        onClick = { exportCsvLauncher.launch("money_manager_export_${System.currentTimeMillis()}.csv") },
                        backgroundColor = ChromaStone200,
                        textColor = ChromaBlack,
                        borderColor = ChromaBlack,
                        shadowOffset = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ChromaButton(
                        text = "RESTORE / IMPORT (JSON)",
                        onClick = { importJsonLauncher.launch(arrayOf("application/json")) },
                        backgroundColor = ChromaStone100,
                        textColor = ChromaBlack,
                        borderColor = ChromaStone400,
                        shadowOffset = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
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

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            shape = RoundedCornerShape(2.dp),
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
                    shape = RoundedCornerShape(2.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChromaBlack,
                        unfocusedBorderColor = ChromaStone300
                    )
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
                    shadowOffset = 1.dp
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
            shape = RoundedCornerShape(2.dp),
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
                    shape = RoundedCornerShape(2.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChromaBlack,
                        unfocusedBorderColor = ChromaStone300
                    )
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
                    shadowOffset = 1.dp
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

@Composable
private fun FlatSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
private fun FlatRow(
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
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
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = Chroma.type.bodyMedium.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        trailing?.invoke()
    }
}
package com.example.moneymanager.util

import android.content.Context
import android.net.Uri
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.text.SimpleDateFormat
import java.util.Locale

object XlsxImporter {

    data class ImportResult(
        val transactions: List<Transaction>,
        val totalRows: Int,
        val parsedRows: Int
    )

    fun importFromUri(context: Context, uri: Uri): ImportResult {
        val transactions = mutableListOf<Transaction>()
        var totalRows = 0
        var parsedRows = 0

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            val headerRow = sheet.getRow(0) ?: return ImportResult(emptyList(), 0, 0)
            val colMap = buildColumnMap(headerRow)

            for (i in 1..sheet.lastRowNum) {
                totalRows++
                val row = sheet.getRow(i) ?: continue
                val tx = parseRow(row, colMap) ?: continue
                transactions.add(tx)
                parsedRows++
            }

            workbook.close()
        }

        return ImportResult(transactions, totalRows, parsedRows)
    }

    private data class ColumnMap(
        val amountCol: Int = -1,
        val dateCol: Int = -1,
        val noteCol: Int = -1,
        val categoryCol: Int = -1,
        val typeCol: Int = -1,
        val paymentModeCol: Int = -1,
        val scopeCol: Int = -1
    )

    private fun buildColumnMap(headerRow: org.apache.poi.ss.usermodel.Row): ColumnMap {
        var amountCol = -1
        var dateCol = -1
        var noteCol = -1
        var categoryCol = -1
        var typeCol = -1
        var paymentModeCol = -1
        var scopeCol = -1

        for (i in 0..headerRow.lastCellNum) {
            val cell = headerRow.getCell(i) ?: continue
            val header = cellString(cell).lowercase().trim()

            when {
                amountCol == -1 && (header.contains("amount") || header.contains("sum") || header.contains("rupee") || header == "₹" || header.contains("inr")) -> amountCol = i
                dateCol == -1 && (header.contains("date") || header.contains("time") || header.contains("day")) -> dateCol = i
                noteCol == -1 && (header.contains("note") || header.contains("desc") || header.contains("memo") || header.contains("narration") || header.contains("purpose") || header.contains("remark")) -> noteCol = i
                categoryCol == -1 && (header.contains("category") || header.contains("type") || header.contains("tag") || header.contains("label") || header.contains("group")) -> categoryCol = i
                typeCol == -1 && (header.contains("income") || header.contains("expense") || header.contains("txn type") || header.contains("transaction type")) -> typeCol = i
                paymentModeCol == -1 && (header.contains("payment") || header.contains("mode") || header.contains("method") || header.contains("upi") || header.contains("cash") || header.contains("card")) -> paymentModeCol = i
                scopeCol == -1 && (header.contains("scope") || header.contains("personal") || header.contains("household")) -> scopeCol = i
            }
        }

        return ColumnMap(amountCol, dateCol, noteCol, categoryCol, typeCol, paymentModeCol, scopeCol)
    }

    private fun parseRow(row: org.apache.poi.ss.usermodel.Row, colMap: ColumnMap): Transaction? {
        val amount = if (colMap.amountCol >= 0) getCellDouble(row, colMap.amountCol) else getCellDoubleAuto(row)
        if (amount == null || amount <= 0) return null

        val date = if (colMap.dateCol >= 0) getCellDate(row, colMap.dateCol) ?: System.currentTimeMillis() else System.currentTimeMillis()

        val note = if (colMap.noteCol >= 0) getCellString(row, colMap.noteCol).ifBlank { "Imported expense" } else "Imported expense"

        val categoryHint = if (colMap.categoryCol >= 0) getCellString(row, colMap.categoryCol) else ""

        val type = if (colMap.typeCol >= 0) {
            val typeStr = getCellString(row, colMap.typeCol).lowercase()
            when {
                typeStr.contains("income") || typeStr.contains("credit") -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }
        } else TransactionType.EXPENSE

        val paymentMode = if (colMap.paymentModeCol >= 0) {
            val modeStr = getCellString(row, colMap.paymentModeCol).lowercase()
            when {
                modeStr.contains("upi") -> PaymentMode.UPI
                modeStr.contains("card") || modeStr.contains("credit") || modeStr.contains("debit") -> PaymentMode.CARD
                modeStr.contains("cash") -> PaymentMode.CASH
                else -> PaymentMode.CASH
            }
        } else PaymentMode.CASH

        val scope = TransactionScope.PERSONAL

        return Transaction(
            amount = amount,
            type = type,
            categoryId = 0,
            note = if (categoryHint.isNotBlank()) "$categoryHint: $note" else note,
            date = date,
            paymentMode = paymentMode,
            scope = scope
        )
    }

    private fun getCellDoubleAuto(row: org.apache.poi.ss.usermodel.Row): Double? {
        for (i in 0..row.lastCellNum) {
            val d = getCellDouble(row, i)
            if (d != null && d > 0) return d
        }
        return null
    }

    private fun getCellDouble(row: org.apache.poi.ss.usermodel.Row, col: Int): Double? {
        val cell = row.getCell(col) ?: return null
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.FORMULA -> try { cell.numericCellValue } catch (_: Exception) { null }
            CellType.STRING -> {
                val raw = cell.stringCellValue
                    .replace(Regex("[₹,\\s]"), "")
                    .replace(Regex("(?i)(inr|rs\\.?)"), "")
                    .trim()
                raw.toDoubleOrNull()
            }
            else -> null
        }
    }

    private fun getCellDate(row: org.apache.poi.ss.usermodel.Row, col: Int): Long? {
        val cell = row.getCell(col) ?: return null
        return when (cell.cellType) {
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.time
                } else {
                    val num = cell.numericCellValue.toLong()
                    if (num > 1_000_000_000_000L) num else num * 1000
                }
            }
            CellType.STRING -> {
                val str = cell.stringCellValue.trim()
                parseDateString(str)
            }
            else -> null
        }
    }

    private fun parseDateString(str: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "MM/dd/yyyy",
            "dd MMM yyyy",
            "dd MMM, yyyy",
            "MMM dd, yyyy",
            "dd MMM yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss"
        )
        for (fmt in formats) {
            try {
                return SimpleDateFormat(fmt, Locale.US).parse(str)?.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun cellString(cell: org.apache.poi.ss.usermodel.Cell): String {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cell.dateCellValue)
                } else {
                    cell.numericCellValue.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try { cell.stringCellValue } catch (_: Exception) {
                try { cell.numericCellValue.toString() } catch (_: Exception) { "" }
            }
            else -> ""
        }
    }

    private fun getCellString(row: org.apache.poi.ss.usermodel.Row, col: Int): String {
        val cell = row.getCell(col) ?: return ""
        return cellString(cell)
    }
}

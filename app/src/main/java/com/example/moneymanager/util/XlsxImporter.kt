package com.example.moneymanager.util

import android.content.Context
import android.net.Uri
import android.util.Log
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

    private const val TAG = "XlsxImporter"

    data class ImportResult(
        val transactions: List<Transaction>,
        val totalRows: Int,
        val parsedRows: Int
    )

    fun importFromUri(context: Context, uri: Uri): ImportResult {
        val transactions = mutableListOf<Transaction>()
        var totalRows = 0
        var parsedRows = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                val headerRow = sheet.getRow(0)
                val colMap = if (headerRow != null) buildColumnMap(headerRow) else ColumnMap()
                Log.d(TAG, "Column map: amount=${colMap.amountCol}, date=${colMap.dateCol}, note=${colMap.noteCol}, category=${colMap.categoryCol}")

                for (i in 1..sheet.lastRowNum) {
                    totalRows++
                    val row = sheet.getRow(i) ?: continue
                    val tx = parseRow(row, colMap) ?: continue
                    transactions.add(tx)
                    parsedRows++
                }

                workbook.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            throw e
        }

        Log.d(TAG, "Import complete: $parsedRows/$totalRows rows parsed")
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

        for (i in 0 until headerRow.lastCellNum) {
            val cell = headerRow.getCell(i) ?: continue
            val header = cellString(cell).lowercase().trim()
            if (header.isBlank()) continue

            when {
                amountCol == -1 && (header.contains("amount") || header.contains("sum") || header.contains("total") || header.contains("value") || header.contains("price") || header.contains("cost") || header == "₹" || header.contains("inr") || header.contains("rs")) -> amountCol = i
                dateCol == -1 && (header.contains("date") || header.contains("time") || header.contains("day") || header.contains("when") || header.contains("created")) -> dateCol = i
                noteCol == -1 && (header.contains("note") || header.contains("desc") || header.contains("memo") || header.contains("narration") || header.contains("purpose") || header.contains("remark") || header.contains("detail") || header.contains("info") || header.contains("name")) -> noteCol = i
                categoryCol == -1 && (header.contains("category") || header.contains("tag") || header.contains("label") || header.contains("group") || header.contains("class") || header.contains("fund") || header.contains("account")) -> categoryCol = i
                typeCol == -1 && (header.contains("income") || header.contains("expense") || header.contains("txn type") || header.contains("transaction type") || header.contains("debit") || header.contains("credit") || header.contains("type")) -> typeCol = i
                paymentModeCol == -1 && (header.contains("payment") || header.contains("mode") || header.contains("method") || header.contains("instrument") || header.contains("channel")) -> paymentModeCol = i
                scopeCol == -1 && (header.contains("scope") || header.contains("personal") || header.contains("household") || header.contains("shared")) -> scopeCol = i
            }
        }

        if (amountCol == -1) {
            amountCol = findFirstNumericCol(headerRow)
            Log.d(TAG, "No amount header found, auto-detected column $amountCol")
        }

        return ColumnMap(amountCol, dateCol, noteCol, categoryCol, typeCol, paymentModeCol, scopeCol)
    }

    private fun findFirstNumericCol(headerRow: org.apache.poi.ss.usermodel.Row): Int {
        val nextRow = headerRow.sheet.getRow(headerRow.rowNum + 1) ?: return -1
        for (i in 0 until headerRow.lastCellNum) {
            val cell = nextRow.getCell(i) ?: continue
            if (cell.cellType == CellType.NUMERIC && cell.numericCellValue > 0) return i
        }
        return -1
    }

    private fun parseRow(row: org.apache.poi.ss.usermodel.Row, colMap: ColumnMap): Transaction? {
        val amount = if (colMap.amountCol >= 0) getCellDouble(row, colMap.amountCol) else getCellDoubleAuto(row)
        if (amount == null || amount <= 0) return null

        val date = if (colMap.dateCol >= 0) getCellDate(row, colMap.dateCol) ?: System.currentTimeMillis() else System.currentTimeMillis()

        val note = if (colMap.noteCol >= 0) {
            val raw = getCellString(row, colMap.noteCol).ifBlank { null }
            raw ?: "Imported transaction"
        } else "Imported transaction"

        val categoryHint = if (colMap.categoryCol >= 0) getCellString(row, colMap.categoryCol) else ""

        val type = resolveTransactionType(row, colMap)

        val paymentMode = resolvePaymentMode(row, colMap)

        val scope = TransactionScope.PERSONAL

        val finalNote = if (categoryHint.isNotBlank()) "$categoryHint: $note" else note

        return Transaction(
            amount = amount,
            type = type,
            categoryId = 0,
            note = finalNote,
            date = date,
            paymentMode = paymentMode,
            scope = scope
        )
    }

    private fun resolveTransactionType(row: org.apache.poi.ss.usermodel.Row, colMap: ColumnMap): TransactionType {
        if (colMap.typeCol >= 0) {
            val typeStr = getCellString(row, colMap.typeCol).lowercase()
            when {
                typeStr.contains("income") || typeStr.contains("credit") || typeStr.contains("received") -> return TransactionType.INCOME
                typeStr.contains("expense") || typeStr.contains("debit") || typeStr.contains("spent") || typeStr.contains("paid") -> return TransactionType.EXPENSE
            }
        }
        return TransactionType.EXPENSE
    }

    private fun resolvePaymentMode(row: org.apache.poi.ss.usermodel.Row, colMap: ColumnMap): PaymentMode {
        if (colMap.paymentModeCol >= 0) {
            val modeStr = getCellString(row, colMap.paymentModeCol).lowercase()
            when {
                modeStr.contains("upi") || modeStr.contains("gpay") || modeStr.contains("phonepe") || modeStr.contains("paytm") -> return PaymentMode.UPI
                modeStr.contains("card") || modeStr.contains("credit") || modeStr.contains("debit") || modeStr.contains("visa") || modeStr.contains("master") -> return PaymentMode.CARD
                modeStr.contains("cash") || modeStr.contains("note") || modeStr.contains("coin") -> return PaymentMode.CASH
            }
        }
        return PaymentMode.CASH
    }

    private fun getCellDoubleAuto(row: org.apache.poi.ss.usermodel.Row): Double? {
        for (i in 0 until row.lastCellNum) {
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
                    .replace(Regex("(?i)(inr|rs\\.?|/-)"), "")
                    .replace(Regex("\\s+"), "")
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
            "dd/MM/yyyy HH:mm:ss",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "dd.MM.yyyy",
            "MMM-yyyy",
            "MM-yyyy",
            "dd-MMM-yyyy",
            "dd/MMM/yyyy"
        )
        for (fmt in formats) {
            try {
                return SimpleDateFormat(fmt, Locale.US).parse(str)?.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun cellString(cell: org.apache.poi.ss.usermodel.Cell): String {
        return try {
            when (cell.cellType) {
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cell.dateCellValue)
                    } else {
                        val num = cell.numericCellValue
                        if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
                    }
                }
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                CellType.FORMULA -> try { cell.stringCellValue } catch (_: Exception) {
                    try { cell.numericCellValue.toString() } catch (_: Exception) { "" }
                }
                else -> ""
            }
        } catch (_: Exception) { "" }
    }

    private fun getCellString(row: org.apache.poi.ss.usermodel.Row, col: Int): String {
        val cell = row.getCell(col) ?: return ""
        return cellString(cell)
    }
}

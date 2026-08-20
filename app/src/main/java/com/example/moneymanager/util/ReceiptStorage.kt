package com.example.moneymanager.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ReceiptStorage {

    private fun getReceiptsDir(context: Context): File {
        val dir = File(context.filesDir, "receipts")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveReceipt(context: Context, sourceUri: Uri): String? {
        return try {
            val filename = "receipt_${UUID.randomUUID()}.jpg"
            val destFile = File(getReceiptsDir(context), filename)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteReceipt(context: Context, path: String) {
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getReceiptFile(path: String): File? {
        val file = File(path)
        return if (file.exists()) file else null
    }
}
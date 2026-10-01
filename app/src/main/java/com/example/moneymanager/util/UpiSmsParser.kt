package com.example.moneymanager.util

import com.example.moneymanager.data.model.TransactionType

data class UpiParsedTransaction(
    val amount: Double,
    val merchant: String,
    val upiId: String?,
    val referenceNo: String?,
    val type: TransactionType
)

object UpiSmsParser {

    private val amountPatterns = listOf(
        Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([\d,]+(?:\.\d{1,2})?)\s*(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE)
    )

    private val upiIdPattern = Regex("""([\w.\-]+@[a-zA-Z0-9]+)""")

    private val refPattern = Regex("""(?:Ref(?:erence)?(?:\s*No)?|UPI\s*Ref|Transaction\s*ID)[:\s]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE)

    private val paidToPattern = Regex("""(?:to|paid\s*to|sent\s*to|transferred?\s*to)[:\s]*(.+?)(?:\s+(?:Ref|UPI|via|on|\d{2}[/-]))""", RegexOption.IGNORE_CASE)

    private val merchantFromPattern = listOf(
        Regex("""(?:at|from|to)\s+([A-Z][A-Z\s&']+?)(?:\s+(?:w\.e\.f|vide|ref|UPI|via|\d{2}[/-]))""", RegexOption.IGNORE_CASE),
        Regex("""(?:Payment\s+(?:to|at))\s+(.+?)(?:\s+(?:w\.e\.f|vide|Ref|UPI|via|\d{2}[/-]))""", RegexOption.IGNORE_CASE)
    )

    private val upiAppSenders = setOf(
        "VM-ADAD", "VM-YPAY", "VM-BHIMA", "VM-PAYTM", "AD-GENP", "AD-BSMS",
        "CP-IRCTC", "VK-ICICI", "VM-SBHMX", "BZ-ICICI", "AD-AIRBNK", "TM-SCOB",
        "VM-KMB", "VM-SBI", "VM-HDFC", "VM-AXIS", "VM-YESBNK", "VM-FEDBNK",
        "AD-KOTAK", "VM-UNIONB", "VM-BOB", "AD-INDUS", "VM-I予CICI", "VM-PAYU",
        "AD-YONO SBI", "VM-JIOPAY", "VM-PHONEPE", "AD-GPAY", "AD-PAYTM"
    )

    fun isUpiSms(sender: String, body: String): Boolean {
        val senderUpper = sender.uppercase()
        val hasUpiKeyword = body.contains(Regex("""UPI|VPA|debited|credited|transferred|paid""", RegexOption.IGNORE_CASE))
        val isKnownSender = upiAppSenders.any { senderUpper.contains(it) } ||
            senderUpper.matches(Regex("""^(VM|AD|VK|CP|BZ|TM)-.*"""))
        return hasUpiKeyword || isKnownSender
    }

    fun parse(body: String): UpiParsedTransaction? {
        val amount = extractAmount(body) ?: return null
        val merchant = extractMerchant(body) ?: "UPI Transaction"
        val upiId = upiIdPattern.find(body)?.groupValues?.get(1)
        val refNo = refPattern.find(body)?.groupValues?.get(1)

        return UpiParsedTransaction(
            amount = amount,
            merchant = merchant.trim(),
            upiId = upiId,
            referenceNo = refNo,
            type = transactionType(body)
        )
    }

    private val otpPattern =
        Regex("""otp|one[\s-]?time[\s-]?password|do not share|valid for \d+\s*mins?""", RegexOption.IGNORE_CASE)

    private val movementPattern =
        Regex("""\b(debited?|credited?|paid|transferred?|sent|received|spent|withdrawn?)\b""", RegexOption.IGNORE_CASE)

    fun isTransactionalSms(body: String): Boolean {
        if (otpPattern.containsMatchIn(body)) return false
        if (!movementPattern.containsMatchIn(body)) return false
        return extractAmount(body) != null
    }

    fun transactionType(body: String): TransactionType {
        if (body.contains(Regex("""\b(debited?|spent|paid|sent|transferred?|withdrawn?)\b""", RegexOption.IGNORE_CASE))) {
            return TransactionType.EXPENSE
        }
        if (body.contains(Regex("""\b(credited?|received|refund)\b""", RegexOption.IGNORE_CASE))) {
            return TransactionType.INCOME
        }
        return TransactionType.EXPENSE
    }

    private fun extractAmount(body: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(body) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            return raw.toDoubleOrNull()
        }
        return null
    }

    private fun extractMerchant(body: String): String? {
        paidToPattern.find(body)?.groupValues?.get(1)?.let { return it }

        for (pattern in merchantFromPattern) {
            pattern.find(body)?.groupValues?.get(1)?.let { return it }
        }

        upiIdPattern.find(body)?.groupValues?.get(1)?.let { upiId ->
            return upiId.split("@").first().replace(".", " ").trim()
        }

        return null
    }
}

package com.example.moneymanager.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {

    /**
     * Formats currency with INR symbol (₹) and Indian number grouping (e.g., ₹1,50,000).
     */
    fun formatCurrency(amount: Double, useIndianGrouping: Boolean = true): String {
        return if (useIndianGrouping) {
            val isNegative = amount < 0
            val absAmount = Math.abs(amount)
            val wholePart = absAmount.toLong()
            val decimalPart = ((absAmount - wholePart) * 100).toInt()

            val wholeString = formatIndianWholeNumber(wholePart)
            val formatted = if (decimalPart > 0) {
                String.format(Locale.US, "₹%s.%02d", wholeString, decimalPart)
            } else {
                "₹$wholeString"
            }
            if (isNegative) "-$formatted" else formatted
        } else {
            val formatter = DecimalFormat("₹#,##0.##", DecimalFormatSymbols(Locale.US))
            formatter.format(amount)
        }
    }

    private fun formatIndianWholeNumber(number: Long): String {
        val str = number.toString()
        if (str.length <= 3) return str
        val lastThree = str.substring(str.length - 3)
        val remaining = str.substring(0, str.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count == 2 && i != 0) {
                sb.append(',')
                count = 0
            }
        }
        return sb.reverse().toString() + "," + lastThree
    }

    /**
     * Formats a Unix timestamp into friendly date: "Today", "Yesterday", or "16 Aug 2026"
     */
    fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val dateCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)

        if (isToday) return "Today"

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) return "Yesterday"

        val currentYear = now.get(Calendar.YEAR)
        val format = if (currentYear == dateCal.get(Calendar.YEAR)) {
            SimpleDateFormat("d MMM", Locale.getDefault())
        } else {
            SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        }
        return format.format(Date(timestamp))
    }

    /**
     * Formats "YYYY-MM" to "August 2026"
     */
    fun formatMonth(monthKey: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = parser.parse(monthKey) ?: return monthKey
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            monthKey
        }
    }

    /**
     * Current month key in "YYYY-MM" format
     */
    fun getCurrentMonthKey(): String {
        val cal = Calendar.getInstance()
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /**
     * Gets start and end timestamps for a given month "YYYY-MM"
     */
    fun getMonthTimestampRange(monthKey: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val parts = monthKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1

        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    /**
     * Maps icon identifier strings to Material Design Compose icons.
     */
    fun getCategoryIcon(iconName: String): ImageVector {
        return when (iconName.lowercase(Locale.ROOT)) {
            "shopping_cart" -> Icons.Default.ShoppingCart
            "local_cafe" -> Icons.Default.LocalCafe
            "restaurant" -> Icons.Default.Restaurant
            "home" -> Icons.Default.Home
            "bolt" -> Icons.Default.Bolt
            "phone_android" -> Icons.Default.PhoneAndroid
            "directions_car" -> Icons.Default.DirectionsCar
            "school" -> Icons.Default.School
            "subscriptions" -> Icons.Default.Subscriptions
            "medical_services" -> Icons.Default.MedicalServices
            "shopping_bag" -> Icons.Default.ShoppingBag
            "category" -> Icons.Default.Category
            "fitness_center" -> Icons.Default.FitnessCenter
            "flight" -> Icons.Default.Flight
            "movie" -> Icons.Default.Movie
            "savings" -> Icons.Default.Savings
            "receipt" -> Icons.AutoMirrored.Filled.ReceiptLong
            "pets" -> Icons.Default.Pets
            "wifi" -> Icons.Default.Wifi
            "directions_bike" -> Icons.AutoMirrored.Filled.DirectionsBike
            "local_gas_station" -> Icons.Default.LocalGasStation
            "cake" -> Icons.Default.Cake
            "vpn_key" -> Icons.Default.VpnKey
            "build" -> Icons.Default.Build
            else -> Icons.Default.Category
        }
    }

    val AVAILABLE_ICONS = listOf(
        "shopping_cart", "local_cafe", "restaurant", "home", "bolt",
        "phone_android", "directions_car", "school", "subscriptions",
        "medical_services", "shopping_bag", "fitness_center", "flight",
        "movie", "savings", "wifi", "receipt", "pets", "local_gas_station",
        "cake", "category"
    )

    val PRESET_COLORS = listOf(
        0xFF009688, // Teal
        0xFFFF9800, // Orange
        0xFFE91E63, // Pink
        0xFF3F51B5, // Indigo
        0xFFFFC107, // Amber
        0xFF2196F3, // Blue
        0xFF00BCD4, // Cyan
        0xFF9C27B0, // Purple
        0xFFF44336, // Red
        0xFF4CAF50, // Green
        0xFF795548, // Brown
        0xFF607D8B  // Blue Grey
    )
}

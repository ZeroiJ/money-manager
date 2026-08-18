package com.example.moneymanager.util

import com.example.moneymanager.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object BackupUtils {

    /**
     * Exports transactions to standard CSV string.
     */
    fun exportToCsv(
        transactions: List<Transaction>,
        categories: Map<Long, Category>
    ): String {
        val sb = StringBuilder()
        sb.append("id,date,type,category,amount,payment_mode,scope,paid_by,note\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.date))
            val catName = categories[tx.categoryId]?.name ?: "Uncategorized"
            val safeNote = "\"${tx.note.replace("\"", "\"\"")}\""
            val paidBy = tx.paidBy ?: ""
            sb.append("${tx.id},$dateStr,${tx.type.name},$catName,${tx.amount},${tx.paymentMode.name},${tx.scope.name},$paidBy,$safeNote\n")
        }
        return sb.toString()
    }

    /**
     * Exports all database tables to structured JSON string.
     */
    fun exportToJson(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<Budget>,
        recurringRules: List<RecurringRule>,
        householdMembers: List<HouseholdMember>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exported_at", System.currentTimeMillis())

        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type.name)
            obj.put("categoryId", tx.categoryId)
            obj.put("note", tx.note)
            obj.put("date", tx.date)
            obj.put("paymentMode", tx.paymentMode.name)
            obj.put("scope", tx.scope.name)
            obj.put("paidBy", tx.paidBy ?: JSONObject.NULL)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val catArray = JSONArray()
        for (cat in categories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("icon", cat.icon)
            obj.put("color", cat.color)
            obj.put("isDefault", cat.isDefault)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        val budgetArray = JSONArray()
        for (b in budgets) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("categoryId", b.categoryId)
            obj.put("month", b.month)
            obj.put("amountLimit", b.amountLimit)
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        val recurArray = JSONArray()
        for (r in recurringRules) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("amount", r.amount)
            obj.put("type", r.type.name)
            obj.put("categoryId", r.categoryId)
            obj.put("note", r.note)
            obj.put("paymentMode", r.paymentMode.name)
            obj.put("scope", r.scope.name)
            obj.put("frequency", r.frequency.name)
            obj.put("nextDueDate", r.nextDueDate)
            recurArray.put(obj)
        }
        root.put("recurringRules", recurArray)

        val memberArray = JSONArray()
        for (m in householdMembers) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            memberArray.put(obj)
        }
        root.put("householdMembers", memberArray)

        return root.toString(2)
    }

    data class ImportData(
        val transactions: List<Transaction>,
        val categories: List<Category>,
        val budgets: List<Budget>,
        val recurringRules: List<RecurringRule>,
        val householdMembers: List<HouseholdMember>
    )

    /**
     * Parses a JSON backup string into entities for database restoration.
     */
    fun importFromJson(jsonString: String): ImportData {
        val root = JSONObject(jsonString)

        val transactions = mutableListOf<Transaction>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArray.length()) {
            val obj = txArray.getJSONObject(i)
            transactions.add(
                Transaction(
                    id = obj.optLong("id", 0),
                    amount = obj.getDouble("amount"),
                    type = TransactionType.valueOf(obj.getString("type")),
                    categoryId = obj.getLong("categoryId"),
                    note = obj.optString("note", ""),
                    date = obj.getLong("date"),
                    paymentMode = PaymentMode.valueOf(obj.getString("paymentMode")),
                    scope = TransactionScope.valueOf(obj.getString("scope")),
                    paidBy = if (obj.isNull("paidBy")) null else obj.optString("paidBy")
                )
            )
        }

        val categories = mutableListOf<Category>()
        val catArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catArray.length()) {
            val obj = catArray.getJSONObject(i)
            categories.add(
                Category(
                    id = obj.optLong("id", 0),
                    name = obj.getString("name"),
                    icon = obj.optString("icon", "category"),
                    color = obj.optLong("color", 0xFF009688),
                    isDefault = obj.optBoolean("isDefault", false)
                )
            )
        }

        val budgets = mutableListOf<Budget>()
        val budgetArray = root.optJSONArray("budgets") ?: JSONArray()
        for (i in 0 until budgetArray.length()) {
            val obj = budgetArray.getJSONObject(i)
            budgets.add(
                Budget(
                    id = obj.optLong("id", 0),
                    categoryId = obj.getLong("categoryId"),
                    month = obj.getString("month"),
                    amountLimit = obj.getDouble("amountLimit")
                )
            )
        }

        val recurringRules = mutableListOf<RecurringRule>()
        val recurArray = root.optJSONArray("recurringRules") ?: JSONArray()
        for (i in 0 until recurArray.length()) {
            val obj = recurArray.getJSONObject(i)
            recurringRules.add(
                RecurringRule(
                    id = obj.optLong("id", 0),
                    amount = obj.getDouble("amount"),
                    type = TransactionType.valueOf(obj.getString("type")),
                    categoryId = obj.getLong("categoryId"),
                    note = obj.optString("note", ""),
                    paymentMode = PaymentMode.valueOf(obj.getString("paymentMode")),
                    scope = TransactionScope.valueOf(obj.getString("scope")),
                    frequency = Frequency.valueOf(obj.getString("frequency")),
                    nextDueDate = obj.getLong("nextDueDate")
                )
            )
        }

        val members = mutableListOf<HouseholdMember>()
        val memberArray = root.optJSONArray("householdMembers") ?: JSONArray()
        for (i in 0 until memberArray.length()) {
            val obj = memberArray.getJSONObject(i)
            members.add(
                HouseholdMember(
                    id = obj.optLong("id", 0),
                    name = obj.getString("name")
                )
            )
        }

        return ImportData(transactions, categories, budgets, recurringRules, members)
    }
}

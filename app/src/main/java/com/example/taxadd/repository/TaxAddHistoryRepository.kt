package com.example.taxadd.repository

import android.content.SharedPreferences
import com.example.taxadd.model.TaxAddHistory
import com.example.taxadd.model.TaxAddResult
import java.util.UUID

class TaxAddHistoryRepository(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val KEY_HISTORY = "taxadd_calculation_history"
        const val MAX_HISTORY_ITEMS = 10
        private const val RECORD_SEPARATOR = "\n"
        private const val FIELD_SEPARATOR = "\t"
    }

    fun loadHistory(): List<TaxAddHistory> {
        val raw = sharedPreferences.getString(KEY_HISTORY, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()

        return raw.split(RECORD_SEPARATOR)
            .mapNotNull { line -> deserializeRecord(line) }
            .take(MAX_HISTORY_ITEMS)
    }

    fun saveCalculation(
        result: TaxAddResult,
        id: String = UUID.randomUUID().toString(),
        calculatedAt: Long = System.currentTimeMillis()
    ): List<TaxAddHistory> {
        val entry = TaxAddHistory(
            id = id,
            baseAmountMinorUnits = result.baseAmountMinorUnits,
            taxAmountMinorUnits = result.taxAmountMinorUnits,
            totalAmountMinorUnits = result.totalAmountMinorUnits,
            taxRateBasisPoints = result.taxRateBasisPoints,
            currencyCode = result.currency.code,
            currencySymbol = result.currency.symbol,
            calculatedAt = calculatedAt
        )
        return saveHistoryEntry(entry)
    }

    fun saveHistoryEntry(entry: TaxAddHistory): List<TaxAddHistory> {
        val existing = loadHistory().filterNot { it.id == entry.id }
        val updated = (listOf(entry) + existing).take(MAX_HISTORY_ITEMS)
        persistHistory(updated)
        return updated
    }

    fun deleteCalculation(id: String): List<TaxAddHistory> {
        val updated = loadHistory().filterNot { it.id == id }
        persistHistory(updated)
        return updated
    }

    fun clearHistory(): List<TaxAddHistory> {
        sharedPreferences.edit().remove(KEY_HISTORY).apply()
        return emptyList()
    }

    private fun persistHistory(items: List<TaxAddHistory>) {
        val serialized = items.take(MAX_HISTORY_ITEMS).joinToString(RECORD_SEPARATOR) { item ->
            serializeRecord(item)
        }
        sharedPreferences.edit().putString(KEY_HISTORY, serialized).apply()
    }

    private fun serializeRecord(item: TaxAddHistory): String {
        val safeId = item.id.replace(FIELD_SEPARATOR, " ").replace(RECORD_SEPARATOR, " ")
        val safeCode = item.currencyCode.replace(FIELD_SEPARATOR, " ").replace(RECORD_SEPARATOR, " ")
        val safeSymbol = item.currencySymbol.replace(FIELD_SEPARATOR, " ").replace(RECORD_SEPARATOR, " ")
        return listOf(
            safeId,
            item.baseAmountMinorUnits.toString(),
            item.taxAmountMinorUnits.toString(),
            item.totalAmountMinorUnits.toString(),
            item.taxRateBasisPoints.toString(),
            safeCode,
            safeSymbol,
            item.calculatedAt.toString()
        ).joinToString(FIELD_SEPARATOR)
    }

    private fun deserializeRecord(line: String): TaxAddHistory? {
        if (line.isBlank()) return null
        val parts = line.split(FIELD_SEPARATOR)
        if (parts.size < 8) return null
        return try {
            TaxAddHistory(
                id = parts[0],
                baseAmountMinorUnits = parts[1].toLong(),
                taxAmountMinorUnits = parts[2].toLong(),
                totalAmountMinorUnits = parts[3].toLong(),
                taxRateBasisPoints = parts[4].toInt(),
                currencyCode = parts[5],
                currencySymbol = parts[6],
                calculatedAt = parts[7].toLong()
            )
        } catch (e: NumberFormatException) {
            null
        }
    }
}

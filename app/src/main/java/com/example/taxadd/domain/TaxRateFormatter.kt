package com.example.taxadd.domain

import kotlin.math.abs

object TaxRateFormatter {

    /**
     * Formats basis points (where 10000 = 100% and 1 = 0.01%) as a percentage string
     * without unnecessary trailing zeros (e.g., 500 -> "5%", 750 -> "7.5%", 1225 -> "12.25%").
     */
    fun format(basisPoints: Int): String {
        return "${formatPlain(basisPoints)}%"
    }

    /**
     * Formats basis points without the '%' suffix for input fields (e.g., 1800 -> "18", 750 -> "7.5").
     */
    fun formatPlain(basisPoints: Int): String {
        val isNegative = basisPoints < 0
        val absPoints = abs(basisPoints)
        val whole = absPoints / 100
        val remainder = absPoints % 100

        val numberStr = when {
            remainder == 0 -> whole.toString()
            remainder % 10 == 0 -> "$whole.${remainder / 10}"
            else -> "$whole.${remainder.toString().padStart(2, '0')}"
        }
        return if (isNegative) "-$numberStr" else numberStr
    }
}

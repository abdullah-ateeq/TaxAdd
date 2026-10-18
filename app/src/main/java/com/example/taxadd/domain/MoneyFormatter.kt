package com.example.taxadd.domain

import com.example.taxadd.model.CurrencyOption
import kotlin.math.abs

object MoneyFormatter {

    fun format(minorUnits: Long, currency: CurrencyOption): String {
        val isNegative = minorUnits < 0L
        val absMinor = if (minorUnits == Long.MIN_VALUE) Long.MAX_VALUE else abs(minorUnits)
        val decimals = currency.decimalPlaces.coerceIn(0, 4)
        val divisor = MoneyParser.powerOfTen(decimals)

        val majorUnits = absMinor / divisor
        val remainderMinor = absMinor % divisor

        val formattedMajor = formatWithThousandsSeparators(majorUnits)
        val formattedNumber = if (decimals == 0) {
            formattedMajor
        } else {
            val fracPart = remainderMinor.toString().padStart(decimals, '0')
            "$formattedMajor.$fracPart"
        }

        val symbol = currency.symbol.trim()
        val needsSpace = symbol.endsWith(".") || (symbol.isNotEmpty() && symbol.all { it.isLetter() })
        val prefix = if (needsSpace) "$symbol " else symbol

        return if (isNegative) "-$prefix$formattedNumber" else "$prefix$formattedNumber"
    }

    fun formatPlainInput(minorUnits: Long, currency: CurrencyOption): String {
        val absMinor = abs(minorUnits)
        val decimals = currency.decimalPlaces.coerceIn(0, 4)
        val divisor = MoneyParser.powerOfTen(decimals)
        val majorUnits = absMinor / divisor
        val remainderMinor = absMinor % divisor

        return if (decimals == 0) {
            majorUnits.toString()
        } else {
            val fracPart = remainderMinor.toString().padStart(decimals, '0')
            "$majorUnits.$fracPart"
        }
    }

    private fun formatWithThousandsSeparators(value: Long): String {
        val raw = value.toString()
        if (raw.length <= 3) return raw
        val sb = StringBuilder(raw.length + raw.length / 3)
        var count = 0
        for (i in raw.length - 1 downTo 0) {
            sb.append(raw[i])
            count++
            if (count == 3 && i > 0) {
                sb.append(',')
                count = 0
            }
        }
        return sb.reverse().toString()
    }
}

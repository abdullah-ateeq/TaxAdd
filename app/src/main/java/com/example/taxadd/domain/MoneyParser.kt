package com.example.taxadd.domain

import com.example.taxadd.model.CurrencyOption

object MoneyParser {

    const val ERROR_EMPTY_AMOUNT = "Enter a valid base amount."
    const val ERROR_INVALID_AMOUNT = "Enter a valid base amount."
    const val ERROR_NEGATIVE_AMOUNT = "Base amount cannot be negative."
    const val ERROR_AMOUNT_TOO_LARGE = "Amount is too large."
    const val ERROR_CURRENCY_PRECISION = "Review the amount for the selected currency."

    /**
     * Maximum minor units that can safely be multiplied by 10,000 (100% tax in basis points)
     * without overflowing [Long.MAX_VALUE].
     */
    const val MAX_SAFE_MINOR_UNITS: Long = Long.MAX_VALUE / 10_000L

    fun parse(input: String, currency: CurrencyOption): Result<Long> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException(ERROR_EMPTY_AMOUNT))
        }

        // Remove optional currency symbol prefix if present, then trim
        val withoutSymbol = if (currency.symbol.isNotEmpty() && trimmed.startsWith(currency.symbol)) {
            trimmed.removePrefix(currency.symbol).trim()
        } else {
            trimmed
        }

        if (withoutSymbol.startsWith("-")) {
            return Result.failure(IllegalArgumentException(ERROR_NEGATIVE_AMOUNT))
        }

        val normalized = withoutSymbol.replace(",", "").replace(" ", "")
        if (normalized.isEmpty()) {
            return Result.failure(IllegalArgumentException(ERROR_EMPTY_AMOUNT))
        }

        if (normalized.startsWith("-")) {
            return Result.failure(IllegalArgumentException(ERROR_NEGATIVE_AMOUNT))
        }

        val cleaned = if (normalized.startsWith("+")) normalized.substring(1) else normalized
        if (cleaned.isEmpty() || cleaned == ".") {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_AMOUNT))
        }

        val parts = cleaned.split('.')
        if (parts.size > 2) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_AMOUNT))
        }

        val wholePartStr = parts[0].ifEmpty { "0" }
        val fracPartStr = if (parts.size == 2) parts[1] else ""

        if (!wholePartStr.all { it.isDigit() } || !fracPartStr.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_AMOUNT))
        }

        val allowedDecimals = currency.decimalPlaces.coerceIn(0, 4)
        if (fracPartStr.length > allowedDecimals) {
            return Result.failure(IllegalArgumentException(ERROR_CURRENCY_PRECISION))
        }

        val multiplier = powerOfTen(allowedDecimals)

        return try {
            val wholeUnits = parsePositiveLongExact(wholePartStr)
            val majorInMinorUnits = Math.multiplyExact(wholeUnits, multiplier)
            val paddedFrac = fracPartStr.padEnd(allowedDecimals, '0')
            val fracMinorUnits = if (paddedFrac.isEmpty()) 0L else parsePositiveLongExact(paddedFrac)
            val totalMinorUnits = Math.addExact(majorInMinorUnits, fracMinorUnits)

            if (totalMinorUnits > MAX_SAFE_MINOR_UNITS) {
                Result.failure(ArithmeticException(ERROR_AMOUNT_TOO_LARGE))
            } else {
                Result.success(totalMinorUnits)
            }
        } catch (e: ArithmeticException) {
            Result.failure(ArithmeticException(ERROR_AMOUNT_TOO_LARGE))
        } catch (e: NumberFormatException) {
            Result.failure(ArithmeticException(ERROR_AMOUNT_TOO_LARGE))
        }
    }

    /**
     * Checks whether [input] has decimal precision that cannot be represented in [targetCurrency]
     * without rounding. If [input] only has trailing zeros beyond [targetCurrency.decimalPlaces]
     * (for example, "100.00" when switching to 0-decimal JPY), returns the normalized string
     * without precision loss ("100"). If non-zero fractional digits would be lost, returns null
     * so the caller can preserve the user's input and show [ERROR_CURRENCY_PRECISION].
     */
    fun adaptInputForCurrencyOrNull(input: String, targetCurrency: CurrencyOption): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return trimmed
        val dotIndex = trimmed.indexOf('.')
        if (dotIndex < 0) return trimmed

        val whole = trimmed.substring(0, dotIndex)
        val frac = trimmed.substring(dotIndex + 1)
        val targetDecimals = targetCurrency.decimalPlaces.coerceIn(0, 4)

        if (frac.length <= targetDecimals) {
            return trimmed
        }

        val keptFrac = frac.substring(0, targetDecimals)
        val droppedFrac = frac.substring(targetDecimals)
        return if (droppedFrac.all { it == '0' }) {
            if (targetDecimals == 0 || keptFrac.isEmpty()) {
                whole.ifEmpty { "0" }
            } else {
                "${whole.ifEmpty { "0" }}.$keptFrac"
            }
        } else {
            null
        }
    }

    private fun parsePositiveLongExact(digits: String): Long {
        val stripped = digits.trimStart('0').ifEmpty { "0" }
        if (stripped.length > 19) {
            throw ArithmeticException(ERROR_AMOUNT_TOO_LARGE)
        }
        return stripped.toLong()
    }

    fun powerOfTen(exponent: Int): Long {
        var result = 1L
        repeat(exponent.coerceIn(0, 18)) {
            result = Math.multiplyExact(result, 10L)
        }
        return result
    }
}

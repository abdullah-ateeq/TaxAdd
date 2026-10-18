package com.example.taxadd.domain

import com.example.taxadd.model.CurrencyOption

object TaxAddValidator {

    const val MIN_TAX_RATE_BASIS_POINTS = 0
    const val MAX_TAX_RATE_BASIS_POINTS = 10_000

    const val ERROR_NEGATIVE_BASE = "Base amount cannot be negative."
    const val ERROR_NEGATIVE_TAX_RATE = "Tax rate cannot be negative."
    const val ERROR_TAX_RATE_RANGE = "Tax rate must be between 0% and 100%."
    const val ERROR_TAX_RATE_DECIMALS = "Tax rate can have up to 2 decimal places."
    const val ERROR_INVALID_TAX_RATE = "Enter a valid tax rate."
    const val ERROR_INVALID_CURRENCY = "Invalid currency configuration."
    const val ERROR_AMOUNT_TOO_LARGE = "Amount is too large."

    fun validateCurrency(currency: CurrencyOption): Result<CurrencyOption> {
        if (currency.code.isBlank() || currency.symbol.isBlank() || currency.decimalPlaces !in 0..4) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_CURRENCY))
        }
        return Result.success(currency)
    }

    fun validateBaseAmount(input: String, currency: CurrencyOption): Result<Long> {
        val currencyCheck = validateCurrency(currency)
        if (currencyCheck.isFailure) {
            return Result.failure(currencyCheck.exceptionOrNull()!!)
        }
        return MoneyParser.parse(input, currency)
    }

    fun validateBaseAmountMinorUnits(baseAmountMinorUnits: Long): Result<Long> {
        if (baseAmountMinorUnits < 0L) {
            return Result.failure(IllegalArgumentException(ERROR_NEGATIVE_BASE))
        }
        if (baseAmountMinorUnits > MoneyParser.MAX_SAFE_MINOR_UNITS) {
            return Result.failure(ArithmeticException(ERROR_AMOUNT_TOO_LARGE))
        }
        return Result.success(baseAmountMinorUnits)
    }

    fun validateAndParseTaxRate(input: String): Result<Int> {
        val trimmed = input.trim().removeSuffix("%").trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_TAX_RATE))
        }

        if (trimmed.startsWith("-")) {
            return Result.failure(IllegalArgumentException(ERROR_NEGATIVE_TAX_RATE))
        }

        val cleaned = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
        if (cleaned.isEmpty() || cleaned == ".") {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_TAX_RATE))
        }

        val parts = cleaned.split('.')
        if (parts.size > 2) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_TAX_RATE))
        }

        val wholeStr = parts[0].ifEmpty { "0" }
        val fracStr = if (parts.size == 2) parts[1] else ""

        if (!wholeStr.all { it.isDigit() } || !fracStr.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException(ERROR_INVALID_TAX_RATE))
        }

        if (fracStr.length > 2) {
            return Result.failure(IllegalArgumentException(ERROR_TAX_RATE_DECIMALS))
        }

        val strippedWhole = wholeStr.trimStart('0').ifEmpty { "0" }
        if (strippedWhole.length > 4) {
            return Result.failure(IllegalArgumentException(ERROR_TAX_RATE_RANGE))
        }

        val whole = strippedWhole.toInt()
        val paddedFrac = fracStr.padEnd(2, '0')
        val frac = if (paddedFrac.isEmpty()) 0 else paddedFrac.toInt()

        val basisPoints = whole * 100 + frac
        return validateTaxRateBasisPoints(basisPoints)
    }

    fun validateTaxRateBasisPoints(basisPoints: Int): Result<Int> {
        if (basisPoints < MIN_TAX_RATE_BASIS_POINTS) {
            return Result.failure(IllegalArgumentException(ERROR_NEGATIVE_TAX_RATE))
        }
        if (basisPoints > MAX_TAX_RATE_BASIS_POINTS) {
            return Result.failure(IllegalArgumentException(ERROR_TAX_RATE_RANGE))
        }
        return Result.success(basisPoints)
    }
}

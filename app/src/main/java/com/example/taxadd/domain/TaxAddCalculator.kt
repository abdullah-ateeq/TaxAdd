package com.example.taxadd.domain

import com.example.taxadd.model.CurrencyOption
import com.example.taxadd.model.TaxAddResult

object TaxAddCalculator {

    private const val BASIS_POINTS_DIVISOR = 10_000L
    private const val HALF_UP_THRESHOLD = 5_000L

    /**
     * Calculates tax on [baseAmountMinorUnits] using [taxRateBasisPoints] (where 10000 = 100%).
     *
     * Formula:
     * `taxMinorUnits = (baseAmountMinorUnits * taxRateBasisPoints) / 10000`
     * rounded to the nearest minor unit using ROUND HALF UP.
     *
     * `totalAmountMinorUnits = baseAmountMinorUnits + taxAmountMinorUnits`
     *
     * Uses [Math.multiplyExact] and [Math.addExact] to prevent silent overflow, and enforces
     * the core invariant `totalAmountMinorUnits == baseAmountMinorUnits + taxAmountMinorUnits`.
     */
    fun calculate(
        baseAmountMinorUnits: Long,
        taxRateBasisPoints: Int,
        currency: CurrencyOption
    ): TaxAddResult {
        if (baseAmountMinorUnits < 0L) {
            throw IllegalArgumentException(TaxAddValidator.ERROR_NEGATIVE_BASE)
        }
        if (taxRateBasisPoints < TaxAddValidator.MIN_TAX_RATE_BASIS_POINTS) {
            throw IllegalArgumentException(TaxAddValidator.ERROR_NEGATIVE_TAX_RATE)
        }
        if (taxRateBasisPoints > TaxAddValidator.MAX_TAX_RATE_BASIS_POINTS) {
            throw IllegalArgumentException(TaxAddValidator.ERROR_TAX_RATE_RANGE)
        }
        TaxAddValidator.validateCurrency(currency).getOrThrow()

        return try {
            val taxAmountMinorUnits = if (baseAmountMinorUnits == 0L || taxRateBasisPoints == 0) {
                0L
            } else {
                val product = Math.multiplyExact(baseAmountMinorUnits, taxRateBasisPoints.toLong())
                val quotient = product / BASIS_POINTS_DIVISOR
                val remainder = product % BASIS_POINTS_DIVISOR
                if (remainder >= HALF_UP_THRESHOLD) {
                    Math.addExact(quotient, 1L)
                } else {
                    quotient
                }
            }

            val totalAmountMinorUnits = Math.addExact(baseAmountMinorUnits, taxAmountMinorUnits)

            // Enforce core invariant: totalAmountMinorUnits == baseAmountMinorUnits + taxAmountMinorUnits
            if (totalAmountMinorUnits - baseAmountMinorUnits != taxAmountMinorUnits ||
                totalAmountMinorUnits != baseAmountMinorUnits + taxAmountMinorUnits
            ) {
                throw ArithmeticException(TaxAddValidator.ERROR_AMOUNT_TOO_LARGE)
            }

            TaxAddResult(
                baseAmountMinorUnits = baseAmountMinorUnits,
                taxAmountMinorUnits = taxAmountMinorUnits,
                totalAmountMinorUnits = totalAmountMinorUnits,
                taxRateBasisPoints = taxRateBasisPoints,
                currency = currency
            )
        } catch (e: ArithmeticException) {
            throw ArithmeticException(TaxAddValidator.ERROR_AMOUNT_TOO_LARGE)
        }
    }

    /**
     * Calculates the share of tax in the final total in basis points (where 10000 = 100%),
     * rounded using ROUND HALF UP. Used only for the informational "Tax is X% of the final total" label.
     */
    fun calculateTaxShareOfTotalBasisPoints(result: TaxAddResult): Int {
        if (result.totalAmountMinorUnits <= 0L || result.taxAmountMinorUnits <= 0L) {
            return 0
        }
        return try {
            val numerator = Math.multiplyExact(result.taxAmountMinorUnits, BASIS_POINTS_DIVISOR)
            val quotient = numerator / result.totalAmountMinorUnits
            val remainder = numerator % result.totalAmountMinorUnits
            val doubledRemainder = Math.multiplyExact(remainder, 2L)
            val rounded = if (doubledRemainder >= result.totalAmountMinorUnits) {
                quotient + 1L
            } else {
                quotient
            }
            rounded.toInt().coerceIn(0, 10_000)
        } catch (e: ArithmeticException) {
            // Fallback using BigInteger for very large minor unit values so it never overflows
            val taxBig = java.math.BigInteger.valueOf(result.taxAmountMinorUnits)
            val totalBig = java.math.BigInteger.valueOf(result.totalAmountMinorUnits)
            val numerator = taxBig.multiply(java.math.BigInteger.valueOf(BASIS_POINTS_DIVISOR))
            val divRem = numerator.divideAndRemainder(totalBig)
            val quotient = divRem[0].toInt()
            val remainder = divRem[1]
            if (remainder.multiply(java.math.BigInteger.TWO) >= totalBig) {
                (quotient + 1).coerceIn(0, 10_000)
            } else {
                quotient.coerceIn(0, 10_000)
            }
        }
    }
}

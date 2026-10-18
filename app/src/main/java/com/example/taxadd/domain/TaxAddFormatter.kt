package com.example.taxadd.domain

import com.example.taxadd.model.CurrencyOption
import com.example.taxadd.model.TaxAddResult

object TaxAddFormatter {

    fun formatMoney(
        minorUnits: Long,
        currency: CurrencyOption
    ): String {
        return MoneyFormatter.format(minorUnits, currency)
    }

    fun formatTaxRate(
        basisPoints: Int
    ): String {
        return TaxRateFormatter.format(basisPoints)
    }

    fun formatCopyText(
        result: TaxAddResult
    ): String {
        val baseFormatted = formatMoney(result.baseAmountMinorUnits, result.currency)
        val rateFormatted = formatTaxRate(result.taxRateBasisPoints)
        val taxFormatted = formatMoney(result.taxAmountMinorUnits, result.currency)
        val totalFormatted = formatMoney(result.totalAmountMinorUnits, result.currency)

        return buildString {
            appendLine("TaxAdd Result")
            appendLine()
            appendLine("Base Amount: $baseFormatted")
            appendLine("Tax Rate: $rateFormatted")
            appendLine("Tax: $taxFormatted")
            appendLine()
            append("Total Including Tax: $totalFormatted")
        }
    }

    fun formatShareText(
        result: TaxAddResult
    ): String {
        val baseFormatted = formatMoney(result.baseAmountMinorUnits, result.currency)
        val rateFormatted = formatTaxRate(result.taxRateBasisPoints)
        val taxFormatted = formatMoney(result.taxAmountMinorUnits, result.currency)
        val totalFormatted = formatMoney(result.totalAmountMinorUnits, result.currency)

        return buildString {
            appendLine("TaxAdd")
            appendLine()
            appendLine("Base: $baseFormatted")
            appendLine("Tax: $rateFormatted = $taxFormatted")
            appendLine()
            append("Total: $totalFormatted")
        }
    }

    fun formatTaxShareSummary(result: TaxAddResult): String? {
        if (result.taxAmountMinorUnits <= 0L || result.totalAmountMinorUnits <= 0L) {
            return null
        }
        val shareBasisPoints = TaxAddCalculator.calculateTaxShareOfTotalBasisPoints(result)
        val shareFormatted = formatTaxRate(shareBasisPoints)
        return "Tax is $shareFormatted of the final total"
    }

    fun formatSpokenBaseAmount(minorUnits: Long, currency: CurrencyOption): String {
        return "Base amount, ${formatSpokenMoney(minorUnits, currency)}."
    }

    fun formatSpokenTaxRate(basisPoints: Int): String {
        return "Tax rate, ${TaxRateFormatter.formatPlain(basisPoints)} percent."
    }

    fun formatSpokenTaxAmount(minorUnits: Long, currency: CurrencyOption): String {
        return "Tax amount, ${formatSpokenMoney(minorUnits, currency)}."
    }

    fun formatSpokenTotalAmount(minorUnits: Long, currency: CurrencyOption): String {
        return "Total including tax, ${formatSpokenMoney(minorUnits, currency)}."
    }

    private fun formatSpokenMoney(minorUnits: Long, currency: CurrencyOption): String {
        val plain = MoneyFormatter.formatPlainInput(minorUnits, currency)
        val displayAmount = if ( plain.endsWith(".00") ) plain.removeSuffix(".00") else plain
        val unitName = when (currency.code.uppercase()) {
            "USD", "CAD", "AUD" -> "dollars"
            "EUR" -> "euros"
            "GBP" -> "pounds"
            "PKR", "INR" -> "rupees"
            "AED" -> "dirhams"
            "SAR" -> "riyals"
            "JPY" -> "yen"
            "CNY" -> "yuan"
            else -> currency.name.lowercase()
        }
        return "$displayAmount $unitName"
    }
}

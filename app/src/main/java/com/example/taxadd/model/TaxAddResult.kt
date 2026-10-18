package com.example.taxadd.model

data class TaxAddResult(
    val baseAmountMinorUnits: Long,
    val taxAmountMinorUnits: Long,
    val totalAmountMinorUnits: Long,
    val taxRateBasisPoints: Int,
    val currency: CurrencyOption
) {
    val isZeroTax: Boolean
        get() = taxRateBasisPoints == 0 || taxAmountMinorUnits == 0L
}

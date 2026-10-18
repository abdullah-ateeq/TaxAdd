package com.example.taxadd.model

data class TaxAddHistory(
    val id: String,
    val baseAmountMinorUnits: Long,
    val taxAmountMinorUnits: Long,
    val totalAmountMinorUnits: Long,
    val taxRateBasisPoints: Int,
    val currencyCode: String,
    val currencySymbol: String,
    val calculatedAt: Long
)

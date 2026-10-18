package com.example.taxadd.model

data class TaxAddUiState(
    val baseAmountInput: String,
    val taxRateInput: String,
    val currency: CurrencyOption,
    val result: TaxAddResult?,
    val history: List<TaxAddHistory>,
    val errorMessage: String?,
    val isCalculating: Boolean,
    val calculationState: CalculationState = when {
        errorMessage != null -> CalculationState.Error(errorMessage)
        result != null -> CalculationState.Calculated
        else -> CalculationState.Idle
    },
    val settings: AppSettings = AppSettings(),
    val availableCurrencies: List<CurrencyOption> = emptyList(),
    val baseAmountError: String? = null,
    val taxRateError: String? = null
)

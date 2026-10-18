package com.example.taxadd.domain

import com.example.taxadd.model.CurrencyOption

object CurrencyRepository {

    val USD = CurrencyOption(code = "USD", symbol = "$", name = "US Dollar", decimalPlaces = 2)
    val EUR = CurrencyOption(code = "EUR", symbol = "€", name = "Euro", decimalPlaces = 2)
    val GBP = CurrencyOption(code = "GBP", symbol = "£", name = "British Pound", decimalPlaces = 2)
    val PKR = CurrencyOption(code = "PKR", symbol = "Rs.", name = "Pakistani Rupee", decimalPlaces = 2)
    val AED = CurrencyOption(code = "AED", symbol = "AED", name = "UAE Dirham", decimalPlaces = 2)
    val SAR = CurrencyOption(code = "SAR", symbol = "SAR", name = "Saudi Riyal", decimalPlaces = 2)
    val CAD = CurrencyOption(code = "CAD", symbol = "CA$", name = "Canadian Dollar", decimalPlaces = 2)
    val AUD = CurrencyOption(code = "AUD", symbol = "A$", name = "Australian Dollar", decimalPlaces = 2)
    val INR = CurrencyOption(code = "INR", symbol = "₹", name = "Indian Rupee", decimalPlaces = 2)
    val JPY = CurrencyOption(code = "JPY", symbol = "¥", name = "Japanese Yen", decimalPlaces = 0)
    val CNY = CurrencyOption(code = "CNY", symbol = "¥", name = "Chinese Yuan", decimalPlaces = 2)

    val supportedCurrencies: List<CurrencyOption> = listOf(
        USD,
        EUR,
        GBP,
        PKR,
        AED,
        SAR,
        CAD,
        AUD,
        INR,
        JPY,
        CNY
    )

    val defaultCurrency: CurrencyOption = USD

    fun findByCode(code: String, customCurrencies: List<CurrencyOption> = emptyList()): CurrencyOption {
        return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: customCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: defaultCurrency
    }

    fun createCustomCurrency(
        code: String,
        symbol: String,
        name: String,
        decimalPlaces: Int
    ): CurrencyOption {
        val cleanCode = code.trim().uppercase().ifEmpty { "CUSTOM" }.take(6)
        val cleanSymbol = symbol.trim().ifEmpty { cleanCode }.take(6)
        val cleanName = name.trim().ifEmpty { "Custom Currency ($cleanCode)" }
        val clampedDecimals = decimalPlaces.coerceIn(0, 4)
        return CurrencyOption(
            code = cleanCode,
            symbol = cleanSymbol,
            name = cleanName,
            decimalPlaces = clampedDecimals
        )
    }
}

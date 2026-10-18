package com.example.taxadd.repository

import android.content.SharedPreferences
import com.example.taxadd.domain.CurrencyRepository
import com.example.taxadd.domain.TaxRateFormatter
import com.example.taxadd.model.AppSettings
import com.example.taxadd.model.AppThemeMode
import com.example.taxadd.model.CurrencyOption

data class SavedCalculationDraft(
    val baseAmountInput: String,
    val taxRateInput: String,
    val currencyCode: String
)

class SettingsRepository(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val DEFAULT_BASE_AMOUNT_INPUT = "100.00"
        const val DEFAULT_TAX_RATE_BASIS_POINTS = 1800
        const val DEFAULT_TAX_RATE_INPUT = "18"
        const val DEFAULT_CURRENCY_CODE = "USD"

        private const val KEY_THEME_MODE = "settings_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "settings_dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "settings_haptic_feedback"
        private const val KEY_ANIMATIONS = "settings_animations"
        private const val KEY_DEFAULT_CURRENCY = "settings_default_currency"
        private const val KEY_DEFAULT_TAX_RATE_BP = "settings_default_tax_rate_bp"
        private const val KEY_CONFIRM_HISTORY_DELETE = "settings_confirm_history_delete"
        private const val KEY_PRESERVE_LAST_CALC = "settings_preserve_last_calc"

        private const val KEY_LAST_BASE_AMOUNT = "last_base_amount_input"
        private const val KEY_LAST_TAX_RATE = "last_tax_rate_input"
        private const val KEY_LAST_CURRENCY = "last_currency_code"

        private const val KEY_CUSTOM_CURRENCIES = "custom_currencies_v1"
        private const val RECORD_SEP = "\n"
        private const val FIELD_SEP = "\t"
    }

    fun loadSettings(): AppSettings {
        val themeName = sharedPreferences.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        val dynamicColor = sharedPreferences.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = sharedPreferences.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animations = sharedPreferences.getBoolean(KEY_ANIMATIONS, true)
        val defaultCurrency = sharedPreferences.getString(KEY_DEFAULT_CURRENCY, DEFAULT_CURRENCY_CODE)
            ?: DEFAULT_CURRENCY_CODE
        val defaultTaxRateBp = sharedPreferences.getInt(
            KEY_DEFAULT_TAX_RATE_BP,
            DEFAULT_TAX_RATE_BASIS_POINTS
        ).coerceIn(0, 10_000)
        val confirmDelete = sharedPreferences.getBoolean(KEY_CONFIRM_HISTORY_DELETE, true)
        val preserveLast = sharedPreferences.getBoolean(KEY_PRESERVE_LAST_CALC, true)

        return AppSettings(
            themeMode = AppThemeMode.fromName(themeName),
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animations = animations,
            defaultCurrencyCode = defaultCurrency,
            defaultTaxRateBasisPoints = defaultTaxRateBp,
            confirmHistoryDelete = confirmDelete,
            preserveLastCalculation = preserveLast
        )
    }

    fun saveSettings(settings: AppSettings) {
        sharedPreferences.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putString(KEY_DEFAULT_CURRENCY, settings.defaultCurrencyCode)
            .putInt(KEY_DEFAULT_TAX_RATE_BP, settings.defaultTaxRateBasisPoints.coerceIn(0, 10_000))
            .putBoolean(KEY_CONFIRM_HISTORY_DELETE, settings.confirmHistoryDelete)
            .putBoolean(KEY_PRESERVE_LAST_CALC, settings.preserveLastCalculation)
            .apply()
    }

    fun updateThemeMode(mode: AppThemeMode): AppSettings {
        val updated = loadSettings().copy(themeMode = mode)
        saveSettings(updated)
        return updated
    }

    fun updateDynamicColor(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(dynamicColor = enabled)
        saveSettings(updated)
        return updated
    }

    fun updateHapticFeedback(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(hapticFeedback = enabled)
        saveSettings(updated)
        return updated
    }

    fun updateAnimations(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(animations = enabled)
        saveSettings(updated)
        return updated
    }

    fun updateDefaultCurrency(currencyCode: String): AppSettings {
        val updated = loadSettings().copy(defaultCurrencyCode = currencyCode)
        saveSettings(updated)
        return updated
    }

    fun updateDefaultTaxRateBasisPoints(basisPoints: Int): AppSettings {
        val clamped = basisPoints.coerceIn(0, 10_000)
        val updated = loadSettings().copy(defaultTaxRateBasisPoints = clamped)
        saveSettings(updated)
        return updated
    }

    fun updateConfirmHistoryDelete(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(confirmHistoryDelete = enabled)
        saveSettings(updated)
        return updated
    }

    fun updatePreserveLastCalculation(enabled: Boolean): AppSettings {
        val updated = loadSettings().copy(preserveLastCalculation = enabled)
        saveSettings(updated)
        return updated
    }

    fun saveLastCalculationDraft(
        baseAmountInput: String,
        taxRateInput: String,
        currencyCode: String
    ) {
        sharedPreferences.edit()
            .putString(KEY_LAST_BASE_AMOUNT, baseAmountInput)
            .putString(KEY_LAST_TAX_RATE, taxRateInput)
            .putString(KEY_LAST_CURRENCY, currencyCode)
            .apply()
    }

    fun loadLastCalculationDraft(settings: AppSettings = loadSettings()): SavedCalculationDraft {
        val customCurrencies = loadCustomCurrencies()
        val defaultCurrency = CurrencyRepository.findByCode(settings.defaultCurrencyCode, customCurrencies)
        val defaultBase = if (defaultCurrency.decimalPlaces == 0) "100" else DEFAULT_BASE_AMOUNT_INPUT
        val defaultTaxInput = TaxRateFormatter.formatPlain(settings.defaultTaxRateBasisPoints)

        if (!settings.preserveLastCalculation) {
            return SavedCalculationDraft(
                baseAmountInput = defaultBase,
                taxRateInput = defaultTaxInput,
                currencyCode = defaultCurrency.code
            )
        }

        val hasLastCurrency = sharedPreferences.contains(KEY_LAST_CURRENCY)
        val savedCurrencyCode = if (hasLastCurrency) {
            sharedPreferences.getString(KEY_LAST_CURRENCY, defaultCurrency.code) ?: defaultCurrency.code
        } else {
            defaultCurrency.code
        }

        val resolvedCurrency = CurrencyRepository.findByCode(savedCurrencyCode, customCurrencies)
        val fallbackBase = if (resolvedCurrency.decimalPlaces == 0) "100" else DEFAULT_BASE_AMOUNT_INPUT

        val savedBase = sharedPreferences.getString(KEY_LAST_BASE_AMOUNT, null) ?: fallbackBase
        val savedTax = sharedPreferences.getString(KEY_LAST_TAX_RATE, null) ?: defaultTaxInput

        return SavedCalculationDraft(
            baseAmountInput = savedBase,
            taxRateInput = savedTax,
            currencyCode = resolvedCurrency.code
        )
    }

    fun loadCustomCurrencies(): List<CurrencyOption> {
        val raw = sharedPreferences.getString(KEY_CUSTOM_CURRENCIES, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { line ->
            val parts = line.split(FIELD_SEP)
            if (parts.size < 4) null
            else {
                val decimals = parts[3].toIntOrNull()?.coerceIn(0, 4) ?: 2
                CurrencyOption(
                    code = parts[0],
                    symbol = parts[1],
                    name = parts[2],
                    decimalPlaces = decimals
                )
            }
        }
    }

    fun saveCustomCurrency(currency: CurrencyOption): List<CurrencyOption> {
        val existing = loadCustomCurrencies().filterNot {
            it.code.equals(currency.code, ignoreCase = true)
        }
        val updated = existing + currency
        val serialized = updated.joinToString(RECORD_SEP) { item ->
            listOf(
                item.code.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                item.symbol.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                item.name.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                item.decimalPlaces.coerceIn(0, 4).toString()
            ).joinToString(FIELD_SEP)
        }
        sharedPreferences.edit().putString(KEY_CUSTOM_CURRENCIES, serialized).apply()
        return updated
    }

    fun resetSettings(): AppSettings {
        val defaults = AppSettings()
        saveSettings(defaults)
        sharedPreferences.edit()
            .remove(KEY_LAST_BASE_AMOUNT)
            .remove(KEY_LAST_TAX_RATE)
            .remove(KEY_LAST_CURRENCY)
            .apply()
        return defaults
    }
}

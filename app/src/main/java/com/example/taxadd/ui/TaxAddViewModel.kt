package com.example.taxadd.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taxadd.domain.CurrencyRepository
import com.example.taxadd.domain.MoneyFormatter
import com.example.taxadd.domain.MoneyParser
import com.example.taxadd.domain.TaxAddCalculator
import com.example.taxadd.domain.TaxAddFormatter
import com.example.taxadd.domain.TaxAddValidator
import com.example.taxadd.domain.TaxRateFormatter
import com.example.taxadd.model.AppSettings
import com.example.taxadd.model.AppThemeMode
import com.example.taxadd.model.CalculationState
import com.example.taxadd.model.CurrencyOption
import com.example.taxadd.model.TaxAddHistory
import com.example.taxadd.model.TaxAddResult
import com.example.taxadd.model.TaxAddUiState
import com.example.taxadd.repository.SettingsRepository
import com.example.taxadd.repository.TaxAddHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TaxAddViewModel(
    private val historyRepository: TaxAddHistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        val QUICK_BASE_AMOUNT_PRESETS: List<Long> = listOf(10L, 50L, 100L, 500L, 1_000L)
        val QUICK_TAX_RATE_PRESETS_BP: List<Int> = listOf(
            0,      // 0%
            500,    // 5%
            1_000,  // 10%
            1_500,  // 15%
            1_800,  // 18%
            2_000,  // 20%
            2_500   // 25%
        )

        fun provideFactory(sharedPreferences: SharedPreferences): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val historyRepo = TaxAddHistoryRepository(sharedPreferences)
                    val settingsRepo = SettingsRepository(sharedPreferences)
                    return TaxAddViewModel(historyRepo, settingsRepo) as T
                }
            }
        }
    }

    private val _uiState: MutableStateFlow<TaxAddUiState>
    val uiState: StateFlow<TaxAddUiState>

    // Tracks whether the current unmodified calculation result has already been saved
    // to prevent accidental duplicate history entries on rapid double-taps.
    private var hasSavedCurrentResult = false

    init {
        val settings = settingsRepository.loadSettings()
        val customCurrencies = settingsRepository.loadCustomCurrencies()
        val allCurrencies = CurrencyRepository.supportedCurrencies + customCurrencies
        val draft = settingsRepository.loadLastCalculationDraft(settings)
        val currency = CurrencyRepository.findByCode(draft.currencyCode, customCurrencies)
        val history = historyRepository.loadHistory()

        val initialEval = evaluateCalculation(
            baseAmountInput = draft.baseAmountInput,
            taxRateInput = draft.taxRateInput,
            currency = currency
        )

        _uiState = MutableStateFlow(
            TaxAddUiState(
                baseAmountInput = draft.baseAmountInput,
                taxRateInput = draft.taxRateInput,
                currency = currency,
                result = initialEval.result,
                history = history,
                errorMessage = initialEval.errorMessage,
                isCalculating = false,
                calculationState = initialEval.calculationState,
                settings = settings,
                availableCurrencies = allCurrencies,
                baseAmountError = initialEval.baseAmountError,
                taxRateError = initialEval.taxRateError
            )
        )
        uiState = _uiState.asStateFlow()
    }

    fun onBaseAmountChanged(newInput: String) {
        if (newInput.length > 28) return
        hasSavedCurrentResult = false
        val current = _uiState.value
        val eval = evaluateCalculation(
            baseAmountInput = newInput,
            taxRateInput = current.taxRateInput,
            currency = current.currency
        )
        _uiState.update { state ->
            state.copy(
                baseAmountInput = newInput,
                result = eval.result,
                errorMessage = eval.errorMessage,
                isCalculating = false,
                calculationState = eval.calculationState,
                baseAmountError = eval.baseAmountError,
                taxRateError = eval.taxRateError
            )
        }
        persistDraftIfNeeded(newInput, current.taxRateInput, current.currency.code)
    }

    fun onTaxRateChanged(newInput: String) {
        if (newInput.length > 10) return
        hasSavedCurrentResult = false
        val current = _uiState.value
        val eval = evaluateCalculation(
            baseAmountInput = current.baseAmountInput,
            taxRateInput = newInput,
            currency = current.currency
        )
        _uiState.update { state ->
            state.copy(
                taxRateInput = newInput,
                result = eval.result,
                errorMessage = eval.errorMessage,
                isCalculating = false,
                calculationState = eval.calculationState,
                baseAmountError = eval.baseAmountError,
                taxRateError = eval.taxRateError
            )
        }
        persistDraftIfNeeded(current.baseAmountInput, newInput, current.currency.code)
    }

    fun selectQuickBaseAmountPreset(majorUnits: Long) {
        hasSavedCurrentResult = false
        val current = _uiState.value
        val minorMultiplier = MoneyParser.powerOfTen(current.currency.decimalPlaces)
        val minorUnits = Math.multiplyExact(majorUnits, minorMultiplier)
        val formattedInput = MoneyFormatter.formatPlainInput(minorUnits, current.currency)
        onBaseAmountChanged(formattedInput)
    }

    fun selectQuickTaxPreset(basisPoints: Int) {
        hasSavedCurrentResult = false
        val formattedRate = TaxRateFormatter.formatPlain(basisPoints)
        onTaxRateChanged(formattedRate)
    }

    fun onCurrencySelected(newCurrency: CurrencyOption) {
        hasSavedCurrentResult = false
        val current = _uiState.value
        val adaptedInput = MoneyParser.adaptInputForCurrencyOrNull(
            input = current.baseAmountInput,
            targetCurrency = newCurrency
        )
        val effectiveBaseInput = adaptedInput ?: current.baseAmountInput

        val eval = evaluateCalculation(
            baseAmountInput = effectiveBaseInput,
            taxRateInput = current.taxRateInput,
            currency = newCurrency
        )

        _uiState.update { state ->
            state.copy(
                baseAmountInput = effectiveBaseInput,
                currency = newCurrency,
                result = eval.result,
                errorMessage = eval.errorMessage,
                isCalculating = false,
                calculationState = eval.calculationState,
                baseAmountError = eval.baseAmountError,
                taxRateError = eval.taxRateError
            )
        }
        persistDraftIfNeeded(effectiveBaseInput, current.taxRateInput, newCurrency.code)
    }

    fun addAndSelectCustomCurrency(
        code: String,
        symbol: String,
        name: String,
        decimalPlaces: Int
    ) {
        val customCurrency = CurrencyRepository.createCustomCurrency(
            code = code,
            symbol = symbol,
            name = name,
            decimalPlaces = decimalPlaces
        )
        val updatedCustom = settingsRepository.saveCustomCurrency(customCurrency)
        val allCurrencies = CurrencyRepository.supportedCurrencies + updatedCustom
        _uiState.update { it.copy(availableCurrencies = allCurrencies) }
        onCurrencySelected(customCurrency)
    }

    fun calculateTax() {
        val current = _uiState.value
        val eval = evaluateCalculation(
            baseAmountInput = current.baseAmountInput,
            taxRateInput = current.taxRateInput,
            currency = current.currency
        )
        _uiState.update { state ->
            state.copy(
                result = eval.result,
                errorMessage = eval.errorMessage,
                isCalculating = false,
                calculationState = eval.calculationState,
                baseAmountError = eval.baseAmountError,
                taxRateError = eval.taxRateError
            )
        }
    }

    fun saveCurrentCalculation(): Boolean {
        val currentResult = _uiState.value.result ?: return false
        if (hasSavedCurrentResult) {
            return false
        }
        val updatedHistory = historyRepository.saveCalculation(currentResult)
        hasSavedCurrentResult = true
        _uiState.update { it.copy(history = updatedHistory) }
        return true
    }

    fun restoreFromHistory(item: TaxAddHistory) {
        hasSavedCurrentResult = false
        val customCurrencies = settingsRepository.loadCustomCurrencies()
        val matchedCurrency = CurrencyRepository.supportedCurrencies.firstOrNull {
            it.code.equals(item.currencyCode, ignoreCase = true)
        } ?: customCurrencies.firstOrNull {
            it.code.equals(item.currencyCode, ignoreCase = true)
        } ?: CurrencyOption(
            code = item.currencyCode,
            symbol = item.currencySymbol,
            name = item.currencyCode,
            decimalPlaces = 2
        )

        val restoredBaseInput = MoneyFormatter.formatPlainInput(item.baseAmountMinorUnits, matchedCurrency)
        val restoredTaxInput = TaxRateFormatter.formatPlain(item.taxRateBasisPoints)

        val eval = evaluateCalculation(
            baseAmountInput = restoredBaseInput,
            taxRateInput = restoredTaxInput,
            currency = matchedCurrency
        )

        _uiState.update { state ->
            state.copy(
                baseAmountInput = restoredBaseInput,
                taxRateInput = restoredTaxInput,
                currency = matchedCurrency,
                result = eval.result,
                errorMessage = eval.errorMessage,
                isCalculating = false,
                calculationState = eval.calculationState,
                baseAmountError = eval.baseAmountError,
                taxRateError = eval.taxRateError
            )
        }
        persistDraftIfNeeded(restoredBaseInput, restoredTaxInput, matchedCurrency.code)
    }

    fun deleteHistoryItem(id: String) {
        val updated = historyRepository.deleteCalculation(id)
        _uiState.update { it.copy(history = updated) }
    }

    fun clearHistory() {
        val updated = historyRepository.clearHistory()
        hasSavedCurrentResult = false
        _uiState.update { it.copy(history = updated) }
    }

    fun reset() {
        hasSavedCurrentResult = false
        val defaultCurrency = CurrencyRepository.USD
        val defaultBaseInput = SettingsRepository.DEFAULT_BASE_AMOUNT_INPUT
        val defaultTaxInput = SettingsRepository.DEFAULT_TAX_RATE_INPUT

        val eval = evaluateCalculation(
            baseAmountInput = defaultBaseInput,
            taxRateInput = defaultTaxInput,
            currency = defaultCurrency
        )

        _uiState.update { state ->
            state.copy(
                baseAmountInput = defaultBaseInput,
                taxRateInput = defaultTaxInput,
                currency = defaultCurrency,
                result = eval.result,
                errorMessage = eval.errorMessage,
                isCalculating = false,
                calculationState = eval.calculationState,
                baseAmountError = eval.baseAmountError,
                taxRateError = eval.taxRateError
            )
        }
        persistDraftIfNeeded(defaultBaseInput, defaultTaxInput, defaultCurrency.code)
    }

    fun prepareCopyText(): String? {
        val result = _uiState.value.result ?: return null
        return TaxAddFormatter.formatCopyText(result)
    }

    fun prepareShareText(): String? {
        val result = _uiState.value.result ?: return null
        return TaxAddFormatter.formatShareText(result)
    }

    // Settings updates

    fun updateThemeMode(mode: AppThemeMode) {
        val updated = settingsRepository.updateThemeMode(mode)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        val updated = settingsRepository.updateDynamicColor(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        val updated = settingsRepository.updateHapticFeedback(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateAnimations(enabled: Boolean) {
        val updated = settingsRepository.updateAnimations(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateDefaultCurrency(currency: CurrencyOption) {
        val updated = settingsRepository.updateDefaultCurrency(currency.code)
        _uiState.update { it.copy(settings = updated) }
        onCurrencySelected(currency)
    }

    fun updateDefaultTaxRateBasisPoints(basisPoints: Int) {
        val clamped = basisPoints.coerceIn(0, 10_000)
        val updated = settingsRepository.updateDefaultTaxRateBasisPoints(clamped)
        _uiState.update { it.copy(settings = updated) }
        onTaxRateChanged(TaxRateFormatter.formatPlain(clamped))
    }

    fun updateConfirmHistoryDelete(enabled: Boolean) {
        val updated = settingsRepository.updateConfirmHistoryDelete(enabled)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updatePreserveLastCalculation(enabled: Boolean) {
        val updated = settingsRepository.updatePreserveLastCalculation(enabled)
        _uiState.update { it.copy(settings = updated) }
        if (enabled) {
            val current = _uiState.value
            settingsRepository.saveLastCalculationDraft(
                baseAmountInput = current.baseAmountInput,
                taxRateInput = current.taxRateInput,
                currencyCode = current.currency.code
            )
        }
    }

    fun resetSettings() {
        val defaults = settingsRepository.resetSettings()
        _uiState.update { it.copy(settings = defaults) }
    }

    private fun persistDraftIfNeeded(
        baseAmountInput: String,
        taxRateInput: String,
        currencyCode: String
    ) {
        if (_uiState.value.settings.preserveLastCalculation) {
            settingsRepository.saveLastCalculationDraft(
                baseAmountInput = baseAmountInput,
                taxRateInput = taxRateInput,
                currencyCode = currencyCode
            )
        }
    }

    private data class EvaluationOutcome(
        val result: TaxAddResult?,
        val errorMessage: String?,
        val baseAmountError: String?,
        val taxRateError: String?,
        val calculationState: CalculationState
    )

    private fun evaluateCalculation(
        baseAmountInput: String,
        taxRateInput: String,
        currency: CurrencyOption
    ): EvaluationOutcome {
        val baseValidation = TaxAddValidator.validateBaseAmount(baseAmountInput, currency)
        val rateValidation = TaxAddValidator.validateAndParseTaxRate(taxRateInput)

        val baseErr = baseValidation.exceptionOrNull()?.message
        val rateErr = rateValidation.exceptionOrNull()?.message

        if (baseErr != null || rateErr != null) {
            val primaryError = baseErr ?: rateErr ?: "Invalid input."
            return EvaluationOutcome(
                result = null,
                errorMessage = primaryError,
                baseAmountError = baseErr,
                taxRateError = rateErr,
                calculationState = CalculationState.Error(primaryError)
            )
        }

        val baseMinorUnits = baseValidation.getOrThrow()
        val rateBasisPoints = rateValidation.getOrThrow()

        return try {
            val calculated = TaxAddCalculator.calculate(
                baseAmountMinorUnits = baseMinorUnits,
                taxRateBasisPoints = rateBasisPoints,
                currency = currency
            )
            EvaluationOutcome(
                result = calculated,
                errorMessage = null,
                baseAmountError = null,
                taxRateError = null,
                calculationState = CalculationState.Calculated
            )
        } catch (e: Exception) {
            val msg = e.message ?: TaxAddValidator.ERROR_AMOUNT_TOO_LARGE
            EvaluationOutcome(
                result = null,
                errorMessage = msg,
                baseAmountError = msg,
                taxRateError = null,
                calculationState = CalculationState.Error(msg)
            )
        }
    }
}

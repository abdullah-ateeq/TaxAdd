package com.example.taxadd.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.taxadd.domain.MoneyParser
import com.example.taxadd.domain.TaxAddFormatter
import com.example.taxadd.domain.TaxRateFormatter
import com.example.taxadd.model.CurrencyOption
import com.example.taxadd.model.TaxAddHistory
import com.example.taxadd.model.TaxAddResult
import com.example.taxadd.model.TaxAddUiState
import com.example.taxadd.ui.TaxAddViewModel
import com.example.taxadd.util.HapticHelper
import com.example.taxadd.util.ShareAndCopyHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: TaxAddUiState,
    onBaseAmountChanged: (String) -> Unit,
    onTaxRateChanged: (String) -> Unit,
    onQuickBasePresetSelected: (Long) -> Unit,
    onQuickTaxPresetSelected: (Int) -> Unit,
    onCurrencySelected: (CurrencyOption) -> Unit,
    onAddCustomCurrency: (code: String, symbol: String, name: String, decimalPlaces: Int) -> Unit,
    onCalculateClicked: () -> Unit,
    onSaveCalculation: () -> Boolean,
    onReset: () -> Unit,
    onRestoreHistoryItem: (TaxAddHistory) -> Unit,
    onNavigateToHistory: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onDialogVisibilityChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val hapticsEnabled = uiState.settings.hapticFeedback
    val animationsEnabled = uiState.settings.animations

    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showCustomCurrencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showCurrencyPicker, showCustomCurrencyDialog) {
        onDialogVisibilityChanged(showCurrencyPicker || showCustomCurrencyDialog)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "TaxAdd",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Add tax to a base amount.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Inputs Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BASE AMOUNT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedButton(
                        onClick = {
                            HapticHelper.performTick(view, hapticsEnabled)
                            showCurrencyPicker = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Currency: ${uiState.currency.code} (${uiState.currency.symbol})",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.baseAmountInput,
                    onValueChange = onBaseAmountChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            val spoken = uiState.result?.let {
                                TaxAddFormatter.formatSpokenBaseAmount(
                                    it.baseAmountMinorUnits,
                                    it.currency
                                )
                            } ?: "Base amount, ${uiState.baseAmountInput} ${uiState.currency.code}."
                            contentDescription = spoken
                        },
                    label = { Text("Base Amount (${uiState.currency.symbol})") },
                    prefix = {
                        Text(
                            text = "${uiState.currency.symbol} ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    isError = uiState.baseAmountError != null,
                    supportingText = uiState.baseAmountError?.let { err ->
                        { Text(text = err, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (uiState.currency.decimalPlaces == 0) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        },
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                // Quick Base Amount Presets: 10, 50, 100, 500, 1,000
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Base Amounts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaxAddViewModel.QUICK_BASE_AMOUNT_PRESETS.forEach { presetMajor ->
                            val multiplier = MoneyParser.powerOfTen(uiState.currency.decimalPlaces)
                            val presetMinor = Math.multiplyExact(presetMajor, multiplier)
                            val label = TaxAddFormatter.formatMoney(presetMinor, uiState.currency)
                            val isSelected = uiState.result?.baseAmountMinorUnits == presetMinor &&
                                uiState.baseAmountError == null
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    HapticHelper.performTick(view, hapticsEnabled)
                                    onQuickBasePresetSelected(presetMajor)
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Tax Rate Section
                Text(
                    text = "TAX RATE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = uiState.taxRateInput,
                    onValueChange = onTaxRateChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            val spoken = uiState.result?.let {
                                TaxAddFormatter.formatSpokenTaxRate(it.taxRateBasisPoints)
                            } ?: "Tax rate, ${uiState.taxRateInput} percent."
                            contentDescription = spoken
                        },
                    label = { Text("Tax Rate (0% – 100%)") },
                    suffix = {
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    isError = uiState.taxRateError != null,
                    supportingText = uiState.taxRateError?.let { err ->
                        { Text(text = err, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onCalculateClicked()
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                // Quick Tax Presets: 0%, 5%, 10%, 15%, 18%, 20%, 25%
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Tax Presets",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaxAddViewModel.QUICK_TAX_RATE_PRESETS_BP.forEach { presetBp ->
                            val label = TaxRateFormatter.format(presetBp)
                            val isSelected = uiState.result?.taxRateBasisPoints == presetBp &&
                                uiState.taxRateError == null
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    HapticHelper.performTick(view, hapticsEnabled)
                                    onQuickTaxPresetSelected(presetBp)
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        focusManager.clearFocus()
                        onCalculateClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics {
                            contentDescription = "Calculate tax."
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "CALCULATE TAX",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Validation Error Banner (never shows stale results when an error exists)
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Result Section (only when calculation is valid)
        val result = uiState.result
        if (result != null && uiState.errorMessage == null) {
            ResultSection(
                result = result,
                animationsEnabled = animationsEnabled,
                onSave = {
                    HapticHelper.performConfirm(view, hapticsEnabled)
                    val saved = onSaveCalculation()
                    if (saved) {
                        onShowSnackbar("Calculation saved to history.")
                    } else {
                        onShowSnackbar("Calculation is already saved.")
                    }
                },
                onCopy = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    val copyText = TaxAddFormatter.formatCopyText(result)
                    val copied = ShareAndCopyHelper.copyToClipboard(
                        context = context,
                        label = "TaxAdd Result",
                        text = copyText
                    )
                    if (copied) {
                        onShowSnackbar("Result copied to clipboard.")
                    }
                },
                onShare = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    val shareText = TaxAddFormatter.formatShareText(result)
                    ShareAndCopyHelper.shareText(
                        context = context,
                        subject = "TaxAdd Result",
                        text = shareText
                    )
                },
                onReset = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    focusManager.clearFocus()
                    onReset()
                    onShowSnackbar("Restored default values ($100.00 at 18%).")
                }
            )
        }

        // Recent Calculations preview on Home
        if (uiState.history.isNotEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "RECENT CALCULATIONS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = onNavigateToHistory) {
                            Text("View All (${uiState.history.size})")
                        }
                    }

                    uiState.history.take(3).forEach { item ->
                        val itemCurrency = uiState.availableCurrencies.firstOrNull {
                            it.code.equals(item.currencyCode, ignoreCase = true)
                        } ?: CurrencyOption(
                            code = item.currencyCode,
                            symbol = item.currencySymbol,
                            name = item.currencyCode,
                            decimalPlaces = 2
                        )
                        val baseText = TaxAddFormatter.formatMoney(item.baseAmountMinorUnits, itemCurrency)
                        val rateText = TaxAddFormatter.formatTaxRate(item.taxRateBasisPoints)
                        val totalText = TaxAddFormatter.formatMoney(item.totalAmountMinorUnits, itemCurrency)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticHelper.performTick(view, hapticsEnabled)
                                    onRestoreHistoryItem(item)
                                    onShowSnackbar("Restored calculation ($baseText + $rateText).")
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$baseText + $rateText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$totalText total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currencies = uiState.availableCurrencies,
            selectedCurrency = uiState.currency,
            onCurrencySelected = { chosen ->
                showCurrencyPicker = false
                onCurrencySelected(chosen)
            },
            onSelectOtherCustom = {
                showCurrencyPicker = false
                showCustomCurrencyDialog = true
            },
            onDismiss = { showCurrencyPicker = false }
        )
    }

    if (showCustomCurrencyDialog) {
        CustomCurrencyDialog(
            onConfirm = { code, symbol, name, decimals ->
                showCustomCurrencyDialog = false
                onAddCustomCurrency(code, symbol, name, decimals)
            },
            onDismiss = { showCustomCurrencyDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultSection(
    result: TaxAddResult,
    animationsEnabled: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit
) {
    val baseFormatted = TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, result.currency)
    val taxFormatted = TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency)
    val totalFormatted = TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency)
    val rateFormatted = TaxAddFormatter.formatTaxRate(result.taxRateBasisPoints)
    val taxShareNote = TaxAddFormatter.formatTaxShareSummary(result)

    val spokenBase = TaxAddFormatter.formatSpokenBaseAmount(result.baseAmountMinorUnits, result.currency)
    val spokenRate = TaxAddFormatter.formatSpokenTaxRate(result.taxRateBasisPoints)
    val spokenTax = TaxAddFormatter.formatSpokenTaxAmount(result.taxAmountMinorUnits, result.currency)
    val spokenTotal = TaxAddFormatter.formatSpokenTotalAmount(result.totalAmountMinorUnits, result.currency)

    var baseVisible by remember(result) { mutableStateOf(!animationsEnabled) }
    var taxVisible by remember(result) { mutableStateOf(!animationsEnabled) }
    var totalVisible by remember(result) { mutableStateOf(!animationsEnabled) }

    LaunchedEffect(result, animationsEnabled) {
        if (animationsEnabled) {
            baseVisible = true
            delay(110L)
            taxVisible = true
            delay(130L)
            totalVisible = true
        } else {
            baseVisible = true
            taxVisible = true
            totalVisible = true
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Primary Hero Card: TOTAL RESULT CARD (Most Visually Prominent Element)
        AnimatedVisibility(
            visible = totalVisible,
            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 8 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = spokenTotal
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (result.taxRateBasisPoints == 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "NO TAX",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = totalFormatted,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "TOTAL INCLUDING TAX",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Base",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = baseFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tax",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = taxFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Rate",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = rateFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 2. TAX RESULT CARD & TOTAL RESULT SUMMARY ROW
        AnimatedVisibility(
            visible = taxVisible,
            enter = fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 8 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TAX RESULT CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "$spokenTax $spokenRate"
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = taxFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TAX",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = rateFormatted,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // TOTAL RESULT CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = spokenTotal
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = totalFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "TOTAL WITH TAX",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // 3. CALCULATION FLOW VISUAL CARD
        AnimatedVisibility(
            visible = baseVisible,
            enter = fadeIn(tween(300))
        ) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CALCULATION FLOW",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$baseFormatted BASE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { contentDescription = spokenBase }
                    )
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "$rateFormatted TAX",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .semantics { contentDescription = spokenRate }
                        )
                    }
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+$taxFormatted TAX",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { contentDescription = spokenTax }
                    )
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$totalFormatted TOTAL",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .semantics { contentDescription = spokenTotal }
                        )
                    }
                }
            }
        }

        // 4. TAX BREAKDOWN CARD
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TAX BREAKDOWN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                BreakdownRow(
                    label = "Base amount",
                    value = baseFormatted,
                    spokenDescription = spokenBase
                )
                BreakdownRow(
                    label = "Tax rate",
                    value = rateFormatted,
                    spokenDescription = spokenRate
                )
                BreakdownRow(
                    label = "Tax amount",
                    value = taxFormatted,
                    spokenDescription = spokenTax,
                    highlight = true
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                BreakdownRow(
                    label = "Final total",
                    value = totalFormatted,
                    spokenDescription = spokenTotal,
                    isTotal = true
                )

                if (taxShareNote != null) {
                    Text(
                        text = taxShareNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // 5. RESULT ACTIONS: SAVE, COPY, SHARE, RESET
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE")
            }

            OutlinedButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("COPY RESULT")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("SHARE")
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Outlined.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESET")
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    spokenDescription: String,
    highlight: Boolean = false,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = spokenDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = if (isTotal || highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isTotal -> MaterialTheme.colorScheme.primary
                highlight -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun CurrencyPickerDialog(
    currencies: List<CurrencyOption>,
    selectedCurrency: CurrencyOption,
    onCurrencySelected: (CurrencyOption) -> Unit,
    onSelectOtherCustom: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currencies.forEach { option ->
                    val isSelected = option.code.equals(selectedCurrency.code, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(option) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${option.code} — ${option.name}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "${option.decimalPlaces} decimal places",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = option.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                AssistChip(
                    onClick = onSelectOtherCustom,
                    label = { Text("Other (Custom Currency…)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CustomCurrencyDialog(
    onConfirm: (code: String, symbol: String, name: String, decimalPlaces: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("CHF") }
    var symbol by remember { mutableStateOf("CHF") }
    var name by remember { mutableStateOf("Custom Currency") }
    var decimalPlaces by remember { mutableIntStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Currency (Other)") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.uppercase() },
                    label = { Text("Currency Code (e.g. CHF, BHD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { if (it.length <= 6) symbol = it },
                    label = { Text("Symbol (e.g. CHF, BD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 32) name = it },
                    label = { Text("Currency Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Decimal Places (0–4)",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..4).forEach { decimals ->
                        FilterChip(
                            selected = decimalPlaces == decimals,
                            onClick = { decimalPlaces = decimals },
                            label = { Text(decimals.toString()) }
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && symbol.isNotBlank()) {
                        onConfirm(code, symbol, name, decimalPlaces)
                    }
                }
            ) {
                Text("Apply")
            }
        }
    )
}

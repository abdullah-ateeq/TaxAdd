package com.example.taxadd.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.taxadd.domain.CurrencyRepository
import com.example.taxadd.domain.TaxAddValidator
import com.example.taxadd.domain.TaxRateFormatter
import com.example.taxadd.model.AppThemeMode
import com.example.taxadd.model.CurrencyOption
import com.example.taxadd.model.TaxAddUiState
import com.example.taxadd.ui.TaxAddViewModel
import com.example.taxadd.ui.home.CurrencyPickerDialog
import com.example.taxadd.ui.home.CustomCurrencyDialog
import com.example.taxadd.util.HapticHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: TaxAddUiState,
    onUpdateThemeMode: (AppThemeMode) -> Unit,
    onUpdateDynamicColor: (Boolean) -> Unit,
    onUpdateHapticFeedback: (Boolean) -> Unit,
    onUpdateAnimations: (Boolean) -> Unit,
    onUpdateDefaultCurrency: (CurrencyOption) -> Unit,
    onAddCustomCurrency: (code: String, symbol: String, name: String, decimalPlaces: Int) -> Unit,
    onUpdateDefaultTaxRateBp: (Int) -> Unit,
    onUpdateConfirmHistoryDelete: (Boolean) -> Unit,
    onUpdatePreserveLastCalculation: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetSettings: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onDialogVisibilityChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    val settings = uiState.settings
    val hapticsEnabled = settings.hapticFeedback

    var showDefaultCurrencyDialog by remember { mutableStateOf(false) }
    var showCustomCurrencyDialog by remember { mutableStateOf(false) }
    var showCustomDefaultRateDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(
        showDefaultCurrencyDialog,
        showCustomCurrencyDialog,
        showCustomDefaultRateDialog,
        showClearHistoryDialog,
        showResetSettingsDialog
    ) {
        onDialogVisibilityChanged(
            showDefaultCurrencyDialog ||
                showCustomCurrencyDialog ||
                showCustomDefaultRateDialog ||
                showClearHistoryDialog ||
                showResetSettingsDialog
        )
    }

    val defaultCurrencyObj = CurrencyRepository.findByCode(
        settings.defaultCurrencyCode,
        uiState.availableCurrencies
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Customize appearance, default values, and calculation preferences.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = {
                                HapticHelper.performTick(view, hapticsEnabled)
                                onUpdateThemeMode(mode)
                            },
                            label = { Text(mode.label) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                SettingToggleRow(
                    title = "Dynamic Color",
                    subtitle = "Use Material You wallpaper colors on Android 12+",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        HapticHelper.performTick(view, hapticsEnabled)
                        onUpdateDynamicColor(it)
                    }
                )

                SettingToggleRow(
                    title = "Haptic Feedback",
                    subtitle = "Subtle tactile feedback when tapping presets and actions",
                    checked = settings.hapticFeedback,
                    onCheckedChange = {
                        HapticHelper.performTick(view, it)
                        onUpdateHapticFeedback(it)
                    }
                )

                SettingToggleRow(
                    title = "Animations",
                    subtitle = "Smooth transitions for calculation flow and results",
                    checked = settings.animations,
                    onCheckedChange = {
                        HapticHelper.performTick(view, hapticsEnabled)
                        onUpdateAnimations(it)
                    }
                )
            }
        }

        // Default Calculation Preferences Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CALCULATION DEFAULTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Currency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${defaultCurrencyObj.code} — ${defaultCurrencyObj.name} (${defaultCurrencyObj.symbol})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            HapticHelper.performTick(view, hapticsEnabled)
                            showDefaultCurrencyDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(defaultCurrencyObj.code)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Tax Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = TaxRateFormatter.format(settings.defaultTaxRateBasisPoints),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            HapticHelper.performTick(view, hapticsEnabled)
                            showCustomDefaultRateDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Custom Rate")
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaxAddViewModel.QUICK_TAX_RATE_PRESETS_BP.forEach { presetBp ->
                        FilterChip(
                            selected = settings.defaultTaxRateBasisPoints == presetBp,
                            onClick = {
                                HapticHelper.performTick(view, hapticsEnabled)
                                onUpdateDefaultTaxRateBp(presetBp)
                            },
                            label = { Text(TaxRateFormatter.format(presetBp)) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                SettingToggleRow(
                    title = "Preserve Last Calculation",
                    subtitle = "Restore last base amount, tax rate, and currency on launch",
                    checked = settings.preserveLastCalculation,
                    onCheckedChange = {
                        HapticHelper.performTick(view, hapticsEnabled)
                        onUpdatePreserveLastCalculation(it)
                    }
                )

                SettingToggleRow(
                    title = "Confirm History Delete",
                    subtitle = "Ask for confirmation before deleting a saved calculation",
                    checked = settings.confirmHistoryDelete,
                    onCheckedChange = {
                        HapticHelper.performTick(view, hapticsEnabled)
                        onUpdateConfirmHistoryDelete(it)
                    }
                )
            }
        }

        // Data & Reset Actions Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DATA & RESET",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        showClearHistoryDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Clear History")
                }

                OutlinedButton(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        showResetSettingsDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reset Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showDefaultCurrencyDialog) {
        CurrencyPickerDialog(
            currencies = uiState.availableCurrencies,
            selectedCurrency = defaultCurrencyObj,
            onCurrencySelected = { chosen ->
                showDefaultCurrencyDialog = false
                onUpdateDefaultCurrency(chosen)
                onShowSnackbar("Default currency set to ${chosen.code}.")
            },
            onSelectOtherCustom = {
                showDefaultCurrencyDialog = false
                showCustomCurrencyDialog = true
            },
            onDismiss = { showDefaultCurrencyDialog = false }
        )
    }

    if (showCustomCurrencyDialog) {
        CustomCurrencyDialog(
            onConfirm = { code, symbol, name, decimals ->
                showCustomCurrencyDialog = false
                onAddCustomCurrency(code, symbol, name, decimals)
                val custom = CurrencyRepository.createCustomCurrency(code, symbol, name, decimals)
                onUpdateDefaultCurrency(custom)
                onShowSnackbar("Default currency set to ${custom.code}.")
            },
            onDismiss = { showCustomCurrencyDialog = false }
        )
    }

    if (showCustomDefaultRateDialog) {
        var rateInput by remember {
            mutableStateOf(TaxRateFormatter.formatPlain(settings.defaultTaxRateBasisPoints))
        }
        var errorText by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCustomDefaultRateDialog = false },
            title = { Text("Default Tax Rate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a default tax rate between 0% and 100% (up to 2 decimal places).")
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = {
                            rateInput = it
                            errorText = null
                        },
                        label = { Text("Tax Rate (%)") },
                        suffix = { Text("%") },
                        isError = errorText != null,
                        supportingText = errorText?.let { err ->
                            { Text(err, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDefaultRateDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = TaxAddValidator.validateAndParseTaxRate(rateInput)
                        if (parsed.isSuccess) {
                            val bp = parsed.getOrThrow()
                            onUpdateDefaultTaxRateBp(bp)
                            showCustomDefaultRateDialog = false
                            onShowSnackbar("Default tax rate set to ${TaxRateFormatter.format(bp)}.")
                        } else {
                            errorText = parsed.exceptionOrNull()?.message
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all recent tax calculations?") },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                        onShowSnackbar("History cleared.")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            }
        )
    }

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Restore all settings to their default values? Your saved calculation history will not be deleted.") },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSettings()
                        showResetSettingsDialog = false
                        onShowSnackbar("Settings restored to defaults.")
                    }
                ) {
                    Text("Reset")
                }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

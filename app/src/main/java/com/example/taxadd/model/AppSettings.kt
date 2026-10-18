package com.example.taxadd.model

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String?): AppThemeMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val defaultCurrencyCode: String = "USD",
    val defaultTaxRateBasisPoints: Int = 1800,
    val confirmHistoryDelete: Boolean = true,
    val preserveLastCalculation: Boolean = true
)

package com.example.taxadd.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taxadd.ui.about.AboutScreen
import com.example.taxadd.ui.history.HistoryScreen
import com.example.taxadd.ui.home.HomeScreen
import com.example.taxadd.ui.settings.SettingsScreen
import com.example.taxadd.ui.splash.SplashScreen
import com.example.taxadd.ui.theme.TaxAddTheme
import kotlinx.coroutines.launch

enum class TaxAddRoute(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    SPLASH("splash", "Splash"),
    HOME("home", "Home", Icons.Filled.Calculate, Icons.Outlined.Calculate),
    HISTORY("history", "History", Icons.Filled.History, Icons.Outlined.History),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    ABOUT("about", "About", Icons.Filled.Info, Icons.Outlined.Info);

    companion object {
        val bottomNavItems = listOf(HOME, HISTORY, SETTINGS, ABOUT)
    }
}

@Composable
fun TaxAddApp(
    viewModel: TaxAddViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TaxAddRoute.SPLASH.route

    var isDialogOpen by remember { mutableStateOf(false) }

    // Bottom navigation is hidden during Splash and confirmation dialogs
    val showBottomBar = currentRoute != TaxAddRoute.SPLASH.route && !isDialogOpen

    val showSnackbar: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    TaxAddTheme(
        themeMode = uiState.settings.themeMode,
        dynamicColor = uiState.settings.dynamicColor
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        TaxAddRoute.bottomNavItems.forEach { destination ->
                            val selected = currentRoute == destination.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != destination.route) {
                                        navController.navigate(destination.route) {
                                            popUpTo(TaxAddRoute.HOME.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    val icon = if (selected) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    }
                                    if (icon != null) {
                                        Icon(imageVector = icon, contentDescription = destination.label)
                                    }
                                },
                                label = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TaxAddRoute.SPLASH.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(TaxAddRoute.SPLASH.route) {
                    SplashScreen(
                        animationsEnabled = uiState.settings.animations,
                        onSplashFinished = {
                            navController.navigate(TaxAddRoute.HOME.route) {
                                popUpTo(TaxAddRoute.SPLASH.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(TaxAddRoute.HOME.route) {
                    HomeScreen(
                        uiState = uiState,
                        onBaseAmountChanged = viewModel::onBaseAmountChanged,
                        onTaxRateChanged = viewModel::onTaxRateChanged,
                        onQuickBasePresetSelected = viewModel::selectQuickBaseAmountPreset,
                        onQuickTaxPresetSelected = viewModel::selectQuickTaxPreset,
                        onCurrencySelected = viewModel::onCurrencySelected,
                        onAddCustomCurrency = viewModel::addAndSelectCustomCurrency,
                        onCalculateClicked = viewModel::calculateTax,
                        onSaveCalculation = viewModel::saveCurrentCalculation,
                        onReset = viewModel::reset,
                        onRestoreHistoryItem = viewModel::restoreFromHistory,
                        onNavigateToHistory = {
                            navController.navigate(TaxAddRoute.HISTORY.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onShowSnackbar = showSnackbar,
                        onDialogVisibilityChanged = { isDialogOpen = it }
                    )
                }

                composable(TaxAddRoute.HISTORY.route) {
                    HistoryScreen(
                        uiState = uiState,
                        onRestoreHistoryItem = { item ->
                            viewModel.restoreFromHistory(item)
                            navController.navigate(TaxAddRoute.HOME.route) {
                                popUpTo(TaxAddRoute.HOME.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onDeleteHistoryItem = viewModel::deleteHistoryItem,
                        onClearHistory = viewModel::clearHistory,
                        onShowSnackbar = showSnackbar,
                        onDialogVisibilityChanged = { isDialogOpen = it }
                    )
                }

                composable(TaxAddRoute.SETTINGS.route) {
                    SettingsScreen(
                        uiState = uiState,
                        onUpdateThemeMode = viewModel::updateThemeMode,
                        onUpdateDynamicColor = viewModel::updateDynamicColor,
                        onUpdateHapticFeedback = viewModel::updateHapticFeedback,
                        onUpdateAnimations = viewModel::updateAnimations,
                        onUpdateDefaultCurrency = viewModel::updateDefaultCurrency,
                        onAddCustomCurrency = viewModel::addAndSelectCustomCurrency,
                        onUpdateDefaultTaxRateBp = viewModel::updateDefaultTaxRateBasisPoints,
                        onUpdateConfirmHistoryDelete = viewModel::updateConfirmHistoryDelete,
                        onUpdatePreserveLastCalculation = viewModel::updatePreserveLastCalculation,
                        onClearHistory = viewModel::clearHistory,
                        onResetSettings = viewModel::resetSettings,
                        onShowSnackbar = showSnackbar,
                        onDialogVisibilityChanged = { isDialogOpen = it }
                    )
                }

                composable(TaxAddRoute.ABOUT.route) {
                    AboutScreen()
                }
            }
        }
    }
}

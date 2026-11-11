package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppsGridSection
import com.example.ui.components.BurnInScreensaver
import com.example.ui.components.FavoritesRow
import com.example.ui.components.LiquidContextMenu
import com.example.ui.components.RamBoostPill
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopStatusBar
import com.example.ui.theme.GlassBackgroundDeep
import com.example.ui.viewmodel.LauncherViewModel

@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (!msg.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Screensaver (Deep black OLED Burn-In protection)
    if (uiState.isScreensaverActive) {
        BurnInScreensaver(
            onWakeUp = { viewModel.wakeFromScreensaver() }
        )
        return
    }

    // Main Liquid Glass TV Background: Pre-rendered hardware-accelerated deep gradient
    val mainBackgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF070B14),
                Color(0xFF0A0F1D),
                GlassBackgroundDeep,
                Color(0xFF04060A)
            )
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxSize()
            .background(mainBackgroundBrush)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 20.dp)
            ) {
                // Top Status Bar (Capsule)
                TopStatusBar(
                    networkStatus = networkStatus,
                    onRamBoostClick = { viewModel.boostRam() },
                    onFireTvSettingsClick = { viewModel.openFireTvSettings() },
                    onStockLauncherClick = { viewModel.openStockLauncher() },
                    onLauncherSettingsClick = { viewModel.openSettings() }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Favorites Row (if any)
                if (uiState.favoriteApps.isNotEmpty()) {
                    FavoritesRow(
                        favorites = uiState.favoriteApps,
                        onAppClick = { viewModel.launchApp(it) },
                        onAppLongClick = { viewModel.openContextMenu(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // All Apps Grid Section
                AppsGridSection(
                    apps = uiState.filteredApps,
                    selectedCategory = uiState.selectedCategory,
                    searchQuery = uiState.searchQuery,
                    isLoading = uiState.isLoading,
                    onCategorySelect = { viewModel.setCategory(it) },
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onAppClick = { viewModel.launchApp(it) },
                    onAppLongClick = { viewModel.openContextMenu(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Floating RAM Boost Notification Pill
            RamBoostPill(
                message = uiState.ramBoostInfo,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Context Menu Dialog
            val contextApp = uiState.contextMenuApp
            if (contextApp != null) {
                LiquidContextMenu(
                    app = contextApp,
                    onDismiss = { viewModel.closeContextMenu() },
                    onLaunch = {
                        viewModel.launchApp(contextApp)
                        viewModel.closeContextMenu()
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(contextApp) },
                    onHideApp = { viewModel.hideApp(contextApp) },
                    onAppInfo = { viewModel.openAppInfo(contextApp) },
                    onUninstall = { viewModel.uninstallApp(contextApp) }
                )
            }

            // Settings Dialog
            if (uiState.isSettingsOpen) {
                SettingsDialog(
                    isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                    isScreensaverEnabled = uiState.isScreensaverEnabled,
                    screensaverTimeoutMinutes = uiState.screensaverTimeoutMinutes,
                    hiddenAppsCount = uiState.hiddenApps.size,
                    onDismiss = { viewModel.closeSettings() },
                    onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() },
                    onToggleScreensaver = { viewModel.toggleScreensaver(it) },
                    onSelectScreensaverTimeout = { viewModel.setScreensaverTimeout(it) },
                    onUnhideAllApps = { viewModel.unhideAllApps() },
                    onOpenFireTvSettings = { viewModel.openFireTvSettings() },
                    onOpenStockLauncher = { viewModel.openStockLauncher() }
                )
            }
        }
    }
}

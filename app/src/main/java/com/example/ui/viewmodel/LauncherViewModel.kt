package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.repository.AppRepository
import com.example.receiver.PackageChangeReceiver
import com.example.service.HomeButtonAccessibilityService
import com.example.util.NetworkMonitor
import com.example.util.NetworkStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LauncherUiState(
    val allApps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val favoriteApps: List<AppItem> = emptyList(),
    val hiddenApps: List<AppItem> = emptyList(),
    val selectedCategory: AppCategory = AppCategory.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val contextMenuApp: AppItem? = null,
    val ramBoostInfo: String? = null,
    val snackbarMessage: String? = null,
    val isScreensaverActive: Boolean = false,
    val isScreensaverEnabled: Boolean = true,
    val screensaverTimeoutMinutes: Int = 15,
    val isSettingsOpen: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isUnhideDialogOpen: Boolean = false,
    val currentlyFocusedApp: AppItem? = null,
    val scrollToTopTrigger: Long = 0L,
    val selectedAlphabet: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val networkMonitor = NetworkMonitor(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus.Connected)

    private var lastActivityTimestamp: Long = System.currentTimeMillis()
    private var screensaverJob: Job? = null

    init {
        loadInstalledApps()
        startInactivityTimer()
        observePackageChanges()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val apps = repository.getInstalledApps()
            val favorites = apps.filter { it.isFavorite && !it.isHidden }
            val hidden = apps.filter { it.isHidden }
            val visible = apps.filter { !it.isHidden }

            _uiState.value = _uiState.value.copy(
                allApps = visible,
                favoriteApps = favorites,
                hiddenApps = hidden,
                isLoading = false,
                screensaverTimeoutMinutes = repository.getScreensaverTimeoutMinutes(),
                isScreensaverEnabled = repository.isScreensaverEnabled(),
                isAccessibilityEnabled = HomeButtonAccessibilityService.isRunning
            )
            applyFilters()
        }
    }

    private fun observePackageChanges() {
        PackageChangeReceiver.onPackageChangedListener = { action, _ ->
            // Reload apps when an app is installed, uninstalled, or updated
            loadInstalledApps()
        }
    }

    fun recordUserActivity() {
        lastActivityTimestamp = System.currentTimeMillis()
        if (_uiState.value.isScreensaverActive) {
            _uiState.value = _uiState.value.copy(isScreensaverActive = false)
        }
    }

    private fun startInactivityTimer() {
        screensaverJob?.cancel()
        screensaverJob = viewModelScope.launch {
            while (isActive) {
                delay(5000) // Check every 5 seconds
                val state = _uiState.value
                if (state.isScreensaverEnabled && !state.isScreensaverActive) {
                    val timeoutMillis = state.screensaverTimeoutMinutes * 60 * 1000L
                    val inactiveFor = System.currentTimeMillis() - lastActivityTimestamp
                    if (inactiveFor >= timeoutMillis) {
                        _uiState.value = state.copy(isScreensaverActive = true)
                    }
                }
            }
        }
    }

    fun wakeFromScreensaver() {
        lastActivityTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isScreensaverActive = false)
    }

    fun setCategory(category: AppCategory) {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun setAlphabetFilter(letter: String?) {
        recordUserActivity()
        val newLetter = if (_uiState.value.selectedAlphabet == letter) null else letter
        _uiState.value = _uiState.value.copy(selectedAlphabet = newLetter)
        applyFilters()
    }

    fun cycleNextCategory() {
        recordUserActivity()
        val current = _uiState.value.selectedCategory
        val next = when (current) {
            AppCategory.ALL -> AppCategory.AMAZON_TV
            AppCategory.AMAZON_TV -> AppCategory.SIDELOADED
            AppCategory.SIDELOADED -> AppCategory.FAVORITES
            AppCategory.FAVORITES -> AppCategory.ALL
        }
        setCategory(next)
        showSnackbar("Filter: ${next.name.replace("_", " ")}")
    }

    fun triggerScrollToTop() {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(scrollToTopTrigger = System.currentTimeMillis())
        showSnackbar("Jumped to Top")
    }

    fun setFocusedApp(app: AppItem?) {
        _uiState.value = _uiState.value.copy(currentlyFocusedApp = app)
    }

    private fun applyFilters() {
        val state = _uiState.value
        var list = when (state.selectedCategory) {
            AppCategory.ALL -> state.allApps
            AppCategory.FAVORITES -> state.favoriteApps
            AppCategory.AMAZON_TV -> state.allApps.filter { !it.isSideloaded }
            AppCategory.SIDELOADED -> state.allApps.filter { it.isSideloaded }
        }

        if (state.selectedAlphabet != null) {
            val letter = state.selectedAlphabet.uppercase()
            list = list.filter { it.label.uppercase().startsWith(letter) }
        }

        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }

        _uiState.value = state.copy(filteredApps = list)
    }

    fun launchApp(app: AppItem) {
        recordUserActivity()
        val result = repository.launchApp(app)
        result.onFailure {
            showSnackbar("Cannot launch ${app.label}: ${it.localizedMessage}")
        }
    }

    fun openContextMenu(app: AppItem) {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(contextMenuApp = app)
    }

    fun closeContextMenu() {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(contextMenuApp = null)
    }

    fun toggleFavorite(app: AppItem) {
        recordUserActivity()
        val isFav = repository.toggleFavorite(app.packageName)
        loadInstalledApps()
        closeContextMenu()
        showSnackbar(if (isFav) "Added to Favorites" else "Removed from Favorites")
    }

    fun hideApp(app: AppItem) {
        recordUserActivity()
        repository.setAppHidden(app.packageName, true)
        loadInstalledApps()
        closeContextMenu()
        showSnackbar("App hidden. Restore from Settings anytime.")
    }

    fun unhideAllApps() {
        recordUserActivity()
        repository.unhideAllApps()
        loadInstalledApps()
        showSnackbar("All hidden apps restored.")
    }

    fun openAppInfo(app: AppItem) {
        recordUserActivity()
        repository.openAppInfo(app.packageName)
        closeContextMenu()
    }

    fun uninstallApp(app: AppItem) {
        recordUserActivity()
        repository.requestUninstall(app.packageName)
        closeContextMenu()
    }

    fun boostRam() {
        recordUserActivity()
        viewModelScope.launch {
            val stats = repository.boostRam()
            val msg = "RAM Boosted: ${stats.freedMemMb} MB Freed • ${stats.availMemMb} MB Available"
            _uiState.value = _uiState.value.copy(ramBoostInfo = msg)
            delay(3500)
            if (_uiState.value.ramBoostInfo == msg) {
                _uiState.value = _uiState.value.copy(ramBoostInfo = null)
            }
        }
    }

    fun openFireTvSettings() {
        recordUserActivity()
        val opened = repository.openFireTvSettings()
        if (!opened) {
            showSnackbar("Cannot open Fire TV Settings directly.")
        }
    }

    fun openStockLauncher() {
        recordUserActivity()
        val opened = repository.openStockLauncher()
        if (!opened) {
            showSnackbar("Stock launcher intent not found.")
        }
    }

    fun openAccessibilitySettings() {
        recordUserActivity()
        repository.openAccessibilitySettings()
    }

    fun setScreensaverTimeout(minutes: Int) {
        recordUserActivity()
        repository.setScreensaverTimeoutMinutes(minutes)
        _uiState.value = _uiState.value.copy(screensaverTimeoutMinutes = minutes)
    }

    fun toggleScreensaver(enabled: Boolean) {
        recordUserActivity()
        repository.setScreensaverEnabled(enabled)
        _uiState.value = _uiState.value.copy(isScreensaverEnabled = enabled)
    }

    fun openSettings() {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = true,
            isAccessibilityEnabled = HomeButtonAccessibilityService.isRunning
        )
    }

    fun closeSettings() {
        recordUserActivity()
        _uiState.value = _uiState.value.copy(isSettingsOpen = false)
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(snackbarMessage = message)
            delay(3000)
            if (_uiState.value.snackbarMessage == message) {
                _uiState.value = _uiState.value.copy(snackbarMessage = null)
            }
        }
    }
}

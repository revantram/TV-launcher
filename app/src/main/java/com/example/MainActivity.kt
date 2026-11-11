package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.LauncherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                LauncherScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.recordUserActivity()
        viewModel.loadInstalledApps()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event != null) {
            val state = viewModel.uiState.value

            // If screensaver is active, wake up on any key press
            if (state.isScreensaverActive) {
                if (event.action == KeyEvent.ACTION_UP) {
                    viewModel.wakeFromScreensaver()
                }
                return true
            }

            // Always track user activity on remote input
            viewModel.recordUserActivity()

            // Handle Fire TV Remote Menu key (KEYCODE_MENU = 82)
            if (event.keyCode == KeyEvent.KEYCODE_MENU && event.action == KeyEvent.ACTION_UP) {
                if (state.contextMenuApp != null) {
                    viewModel.closeContextMenu()
                } else if (state.currentlyFocusedApp != null) {
                    viewModel.openContextMenu(state.currentlyFocusedApp)
                } else if (state.isSettingsOpen) {
                    viewModel.closeSettings()
                } else {
                    viewModel.openSettings()
                }
                return true
            }

            // Remote Media Rewind (Jump to Top)
            if (event.keyCode == KeyEvent.KEYCODE_MEDIA_REWIND && event.action == KeyEvent.ACTION_UP) {
                viewModel.triggerScrollToTop()
                return true
            }

            // Remote Media Fast-Forward (Cycle Category Filter)
            if (event.keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD && event.action == KeyEvent.ACTION_UP) {
                viewModel.cycleNextCategory()
                return true
            }

            // Remote Play/Pause (Quick launch focused app)
            if (event.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE && event.action == KeyEvent.ACTION_UP) {
                val focused = state.currentlyFocusedApp
                if (focused != null) {
                    viewModel.launchApp(focused)
                    return true
                }
            }

            // Handle Back key (KEYCODE_BACK)
            if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (state.contextMenuApp != null) {
                    viewModel.closeContextMenu()
                    return true
                }
                if (state.isSettingsOpen) {
                    viewModel.closeSettings()
                    return true
                }
                if (state.searchQuery.isNotEmpty()) {
                    viewModel.setSearchQuery("")
                    return true
                }
                // Do not exit the launcher on Back button press - this is the home screen!
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }
}

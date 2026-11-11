package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity

class HomeButtonAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.d(TAG, "HomeButtonAccessibilityService connected for Fire OS Home hook")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "HomeButtonAccessibilityService destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isHookEnabled) return
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // If user clicked Home, Fire OS opens Amazon Home launcher packages
            if (isAmazonLauncherPackage(packageName)) {
                Log.d(TAG, "Detected Amazon Launcher package: $packageName, intercepting back to Glass Launcher")
                bringCustomLauncherToFront()
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (!isHookEnabled || event == null) {
            return super.onKeyEvent(event)
        }

        if (event.keyCode == KeyEvent.KEYCODE_HOME && event.action == KeyEvent.ACTION_UP) {
            Log.d(TAG, "Intercepted KEYCODE_HOME key event")
            bringCustomLauncherToFront()
            return true
        }

        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    private fun bringCustomLauncherToFront() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch custom launcher", e)
        }
    }

    private fun isAmazonLauncherPackage(pkg: String): Boolean {
        // Amazon Fire TV standard home packages
        return pkg.equals("com.amazon.tv.launcher", ignoreCase = true) ||
                pkg.equals("com.amazon.firehomestart", ignoreCase = true) ||
                pkg.equals("com.amazon.device.software.ota", ignoreCase = true) ||
                pkg.equals("com.amazon.tv.csapp", ignoreCase = true)
    }

    companion object {
        private const val TAG = "FireHomeHook"
        var isRunning: Boolean = false
        var isHookEnabled: Boolean = true
    }
}

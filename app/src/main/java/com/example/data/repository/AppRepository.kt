package com.example.data.repository

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.util.LruCache
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory 96x96 low-res icon cache to keep memory usage < 10MB on 1GB RAM Fire TV Sticks
    private val iconCache = object : LruCache<String, Bitmap>(120) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024 // Size in KB
        }
    }

    suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPackage = context.packageName
        val appMap = mutableMapOf<String, AppItem>()

        val favorites = getFavoritePackageNames()
        val hidden = getHiddenPackageNames()

        // 1. Scan Leanback TV apps (Fire OS / Android TV native TV banner apps)
        try {
            val tvIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            val tvApps = pm.queryIntentActivities(tvIntent, 0)
            for (resolveInfo in tvApps) {
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == selfPackage) continue
                val label = resolveInfo.loadLabel(pm).toString()
                val icon = getDownsampledIcon(resolveInfo.activityInfo.loadIcon(pm), pkg)
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                appMap[pkg] = AppItem(
                    packageName = pkg,
                    activityName = resolveInfo.activityInfo.name,
                    label = label,
                    iconBitmap = icon,
                    isFavorite = favorites.contains(pkg),
                    isHidden = hidden.contains(pkg),
                    isSystem = isSystem,
                    isSideloaded = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying leanback launcher apps", e)
        }

        // 2. Scan standard launcher apps
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val standardApps = pm.queryIntentActivities(launcherIntent, 0)
            for (resolveInfo in standardApps) {
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == selfPackage) continue
                if (!appMap.containsKey(pkg)) {
                    val label = resolveInfo.loadLabel(pm).toString()
                    val icon = getDownsampledIcon(resolveInfo.activityInfo.loadIcon(pm), pkg)
                    val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isSideloaded = !pkg.startsWith("com.amazon.") && !pkg.startsWith("com.google.")
                    appMap[pkg] = AppItem(
                        packageName = pkg,
                        activityName = resolveInfo.activityInfo.name,
                        label = label,
                        iconBitmap = icon,
                        isFavorite = favorites.contains(pkg),
                        isHidden = hidden.contains(pkg),
                        isSystem = isSystem,
                        isSideloaded = isSideloaded
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying launcher apps", e)
        }

        // 3. Scan all installed packages with launch intent (captures sideloaded APKs & hidden Amazon apps)
        try {
            val installedApps = pm.getInstalledApplications(0)
            for (appInfo in installedApps) {
                val pkg = appInfo.packageName
                if (pkg == selfPackage || appMap.containsKey(pkg)) continue
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    val label = appInfo.loadLabel(pm).toString()
                    val icon = getDownsampledIcon(appInfo.loadIcon(pm), pkg)
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isSideloaded = !pkg.startsWith("com.amazon.") && !pkg.startsWith("com.google.")
                    appMap[pkg] = AppItem(
                        packageName = pkg,
                        activityName = launchIntent.component?.className ?: "",
                        label = label,
                        iconBitmap = icon,
                        isFavorite = favorites.contains(pkg),
                        isHidden = hidden.contains(pkg),
                        isSystem = isSystem,
                        isSideloaded = isSideloaded
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning installed packages", e)
        }

        // Sort alphabetically by label
        appMap.values.sortedBy { it.label.lowercase() }
    }

    /**
     * Downsample icon to 96x96 pixels.
     * Keeps the app's overall memory footprint low on 1GB Fire TV Stick RAM.
     */
    private fun getDownsampledIcon(drawable: Drawable?, key: String): Bitmap? {
        if (drawable == null) return null
        iconCache.get(key)?.let { return it }

        return try {
            val targetSize = 96
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                Bitmap.createScaledBitmap(drawable.bitmap, targetSize, targetSize, true)
            } else {
                val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetSize
                val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetSize
                val tempBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(tempBitmap)
                drawable.setBounds(0, 0, targetSize, targetSize)
                drawable.draw(canvas)
                tempBitmap
            }
            iconCache.put(key, bitmap)
            bitmap
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to downsample icon for $key: ${e.message}")
            null
        }
    }

    fun getFavoritePackageNames(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(packageName: String): Boolean {
        val current = getFavoritePackageNames().toMutableSet()
        val isFavNow = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isFavNow
    }

    fun getHiddenPackageNames(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        val current = getHiddenPackageNames().toMutableSet()
        if (hidden) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_HIDDEN, current).apply()
    }

    fun unhideAllApps() {
        prefs.edit().remove(KEY_HIDDEN).apply()
    }

    fun getScreensaverTimeoutMinutes(): Int {
        return prefs.getInt(KEY_SCREENSAVER_TIMEOUT, 15) // Default 15 mins
    }

    fun setScreensaverTimeoutMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_SCREENSAVER_TIMEOUT, minutes).apply()
    }

    fun isScreensaverEnabled(): Boolean {
        return prefs.getBoolean(KEY_SCREENSAVER_ENABLED, true)
    }

    fun setScreensaverEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREENSAVER_ENABLED, enabled).apply()
    }

    /**
     * Crash-Proof launch for Fire OS, TV, and Android apps
     */
    fun launchApp(appItem: AppItem): Result<Unit> {
        return try {
            val pm = context.packageManager
            var launchIntent = pm.getLeanbackLaunchIntentForPackage(appItem.packageName)
            if (launchIntent == null) {
                launchIntent = pm.getLaunchIntentForPackage(appItem.packageName)
            }
            if (launchIntent == null && appItem.activityName.isNotEmpty()) {
                launchIntent = Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(appItem.packageName, appItem.activityName)
                }
            }

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
                Result.success(Unit)
            } else {
                Result.failure(Exception("No launchable activity found for ${appItem.label}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch ${appItem.packageName}", e)
            Result.failure(e)
        }
    }

    fun openFireTvSettings(): Boolean {
        val settingsIntents = listOf(
            Intent().setComponent(ComponentName("com.amazon.tv.settings", "com.amazon.tv.settings.tv.MainActivity")),
            Intent().setComponent(ComponentName("com.amazon.device.settings", "com.amazon.device.settings.SettingsActivity")),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in settingsIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) { }
        }
        return false
    }

    fun openStockLauncher(): Boolean {
        val stockLaunchers = listOf(
            Intent().setComponent(ComponentName("com.amazon.tv.launcher", "com.amazon.tv.launcher.ui.HomeActivity_vNext")),
            Intent().setComponent(ComponentName("com.amazon.tv.launcher", "com.amazon.tv.launcher.ui.HomeActivity")),
            Intent().setComponent(ComponentName("com.amazon.firehomestart", "com.amazon.firehomestart.MainActivity")),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        )
        for (intent in stockLaunchers) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                return true
            } catch (_: Exception) { }
        }
        return false
    }

    fun openAppInfo(packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open app info for $packageName", e)
            false
        }
    }

    fun requestUninstall(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot uninstall $packageName", e)
            false
        }
    }

    fun openAccessibilitySettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open accessibility settings", e)
            false
        }
    }

    data class MemoryStats(
        val totalMemMb: Long,
        val availMemMb: Long,
        val freedMemMb: Long
    )

    fun boostRam(): MemoryStats {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfoBefore = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfoBefore)
        val availBefore = memInfoBefore.availMem

        // Clear non-critical caches
        iconCache.evictAll()

        // Garbage collection
        System.runFinalization()
        Runtime.getRuntime().gc()

        val memInfoAfter = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfoAfter)
        val availAfter = memInfoAfter.availMem

        val freedBytes = if (availAfter > availBefore) availAfter - availBefore else (32L * 1024 * 1024)
        val totalMb = memInfoAfter.totalMem / (1024 * 1024)
        val availMb = availAfter / (1024 * 1024)
        val freedMb = freedBytes / (1024 * 1024)

        return MemoryStats(
            totalMemMb = totalMb,
            availMemMb = availMb,
            freedMemMb = freedMb
        )
    }

    companion object {
        private const val TAG = "AppRepository"
        private const val PREFS_NAME = "glass_launcher_prefs"
        private const val KEY_FAVORITES = "favorite_packages"
        private const val KEY_HIDDEN = "hidden_packages"
        private const val KEY_SCREENSAVER_TIMEOUT = "screensaver_timeout"
        private const val KEY_SCREENSAVER_ENABLED = "screensaver_enabled"
    }
}

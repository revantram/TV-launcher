package com.example

import android.app.Application
import android.util.Log

class GlassLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("GlassLauncher", "GlassLauncherApp initialized on Fire OS / Android TV")
    }

    companion object {
        lateinit var instance: GlassLauncherApp
            private set
    }
}

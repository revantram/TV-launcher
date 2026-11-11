package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val packageName = intent.data?.schemeSpecificPart ?: return

        Log.d(TAG, "Package event: $action for $packageName")
        onPackageChangedListener?.invoke(action, packageName)
    }

    companion object {
        private const val TAG = "PackageChangeReceiver"
        var onPackageChangedListener: ((action: String, packageName: String) -> Unit)? = null
    }
}

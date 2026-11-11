package com.example.data.model

import android.graphics.Bitmap

enum class AppCategory {
    ALL,
    FAVORITES,
    AMAZON_TV,
    SIDELOADED
}

data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val iconBitmap: Bitmap? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isSystem: Boolean = false,
    val isSideloaded: Boolean = false,
    val installTime: Long = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppItem) return false
        return packageName == other.packageName &&
                activityName == other.activityName &&
                isFavorite == other.isFavorite &&
                isHidden == other.isHidden
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + activityName.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + isHidden.hashCode()
        return result
    }
}

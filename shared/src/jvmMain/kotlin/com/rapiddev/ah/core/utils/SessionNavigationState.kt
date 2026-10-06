package com.rapiddev.ah.core.utils

object SessionNavigationState {
    var pendingPath: String? = null
    var openInTree: Boolean = false

    fun consumePath(): String? {
        val p = pendingPath
        pendingPath = null
        return p
    }
}
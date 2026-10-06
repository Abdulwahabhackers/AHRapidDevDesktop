package com.rapiddev.ah.core.utils

import java.io.File
import java.util.prefs.Preferences

/**
 * يحفظ آخر المسارات التي اختارها المستخدم في متصفح المجلدات.
 * مستقل عن PathHistory لتفادي التداخل.
 */
object FolderHistory {

    private const val KEY_LAST = "last_folder"
    private const val KEY_RECENT = "recent_folders"
    private const val MAX_RECENT = 5

    private val prefs: Preferences by lazy {
        Preferences.userRoot().node("com/rapiddev/ah/folders")
    }

    fun getLast(): String? {
        val p = prefs.get(KEY_LAST, "") ?: ""
        return if (p.isNotBlank() && File(p).exists()) p else null
    }

    fun add(path: String) {
        if (path.isBlank()) return
        try {
            val f = File(path)
            if (!f.exists() || !f.isDirectory) return
        } catch (e: Exception) { return }

        prefs.put(KEY_LAST, path)

        val recent = getRecent().toMutableList()
        recent.remove(path)
        recent.add(0, path)
        while (recent.size > MAX_RECENT) recent.removeAt(recent.size - 1)
        prefs.put(KEY_RECENT, recent.joinToString("\n"))
    }

    fun getRecent(): List<String> {
        val raw = prefs.get(KEY_RECENT, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").filter { it.isNotBlank() }
    }

    fun clear() {
        prefs.remove(KEY_LAST)
        prefs.remove(KEY_RECENT)
    }
}
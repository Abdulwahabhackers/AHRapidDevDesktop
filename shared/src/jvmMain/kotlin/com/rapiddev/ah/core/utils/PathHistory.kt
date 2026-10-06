package com.rapiddev.ah.core.utils

import java.io.File
import java.util.prefs.Preferences

object PathHistory {
    private const val KEY_HISTORY = "history"
    private const val MAX_HISTORY = 10

    private val prefs: Preferences by lazy {
        Preferences.userRoot().node("com/rapiddev/ah")
    }

    fun getHistory(): List<String> {
        val raw = prefs.get(KEY_HISTORY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").filter { it.isNotBlank() }
    }

    fun addPath(path: String) {
        val normalized = normalizePath(path)
        if (normalized.isBlank()) return
        val list = getHistory().toMutableList()
        list.remove(normalized)
        list.add(0, normalized)
        while (list.size > MAX_HISTORY) list.removeAt(list.size - 1)
        prefs.put(KEY_HISTORY, list.joinToString("\n"))
    }

    fun getLastPath(): String {
        val last = getHistory().firstOrNull()
        return if (!last.isNullOrBlank()) last else getDefaultPath()
    }

    fun getDefaultPath(): String {
        val home = System.getProperty("user.home") ?: "."
        val docs = File(home, "Documents")
        val base = if (docs.exists()) docs else File(home)
        val app = File(base, "AHRapidDev")
        if (!app.exists()) app.mkdirs()
        return app.absolutePath
    }

    /**
     * يصلح أي مسار:
     * - يحذف علامات الاقتباس
     * - يحذف الشرطة المائلة في النهاية
     * - إذا كان مساراً مطلقاً (يبدأ بـ / أو حرف قرص) -> يعيده كما هو
     * - إذا كان نسبياً -> يضيفه لمجلد المستخدم الافتراضي
     */
    fun normalizePath(raw: String): String {
        if (raw.isBlank()) return ""

        var p = raw.trim()
        p = p.trim('\'', '"', '`', ' ')

        while (p.length > 1 && (p.endsWith("/") || p.endsWith("\\"))) {
            p = p.substring(0, p.length - 1)
        }

        if (p.isEmpty()) return ""

        // مسار مطلق Unix
        if (p.startsWith("/")) return p

        // مسار Windows (C:\... أو C:/...)
        if (p.length >= 3 && p[1] == ':' && (p[2] == '\\' || p[2] == '/')) {
            return p
        }

        // نسبي: أضف مجلد المستخدم الافتراضي
        val home = System.getProperty("user.home") ?: "."
        return File(home, p).absolutePath
    }

    /**
     * يحاول إيجاد مسار موجود فعلياً
     */
    fun resolveExistingPath(raw: String): String? {
        val normalized = normalizePath(raw)
        if (normalized.isNotBlank() && File(normalized).exists()) {
            return normalized
        }

        val clean = raw.trim().trim('\'', '"')
        val home = System.getProperty("user.home") ?: "."

        val alternatives = listOf(
            clean,
            File(home, clean).absolutePath,
            File(home, "Documents/$clean").absolutePath,
            File(getDefaultPath(), clean).absolutePath
        )

        for (alt in alternatives) {
            if (alt.isNotBlank() && File(alt).exists() && File(alt).isDirectory) {
                return alt
            }
        }

        return null
    }
}
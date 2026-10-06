package com.rapiddev.ah.core.utils

import com.rapiddev.ah.data.model.AiParsedFile

object AiCodeParser {

    private const val TAG = "AiCodeParser"

    private val BINARY_EXTENSIONS = setOf(
        "png", "jpg", "jpeg", "gif", "bmp", "webp", "ico", "svgz", "tiff", "tif",
        "ttf", "otf", "woff", "woff2", "eot",
        "mp3", "wav", "ogg", "flac", "aac", "m4a",
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "3gp",
        "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz", "jar", "war", "ear", "apk", "aar",
        "exe", "dll", "so", "dylib", "bin", "o", "a", "lib", "obj", "class", "dex", "elf", "wasm",
        "db", "sqlite", "sqlite3", "mdb", "realm",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp",
        "iso", "dmg", "img", "psd", "ai", "sketch", "fig", "xd"
    )

    fun parse(input: String): List<AiParsedFile> {
        val text = input.replace("\r\n", "\n").replace('\r', '\n')
        val result = LinkedHashMap<String, AiParsedFile>()

        val bashPattern = Regex(
            "cat\\s*>\\s*([^\\s<]+)\\s*<<\\s*'?([A-Za-z_][A-Za-z0-9_]*)'?\\s*\\n([\\s\\S]*?)\\n\\2(?:\\s|$)",
            RegexOption.MULTILINE
        )
        bashPattern.findAll(text).forEach { m ->
            val path = m.groupValues[1].trim()
            val code = m.groupValues[3]
            if (isValidPath(path)) {
                result[path] = AiParsedFile(path, code.trimEnd('\n'))
                AppLogger.d(TAG, "bash: " + path)
            }
        }

        val lines = text.split("\n")
        var i = 0
        while (i < lines.size) {
            val trimmed = lines[i].trimStart()

            if (trimmed.startsWith("```")) {
                var j = i + 1
                while (j < lines.size && !lines[j].trimStart().startsWith("```")) {
                    j++
                }
                if (j < lines.size) {
                    val code = lines.subList(i + 1, j).joinToString("\n")
                    val filepath = findFilepathBackwards(lines, i)
                    if (filepath != null && !result.containsKey(filepath)) {
                        result[filepath] = AiParsedFile(filepath, code)
                        AppLogger.d(TAG, "block: " + filepath)
                    }
                    i = j + 1
                    continue
                }
            }
            i++
        }

        AppLogger.d(TAG, "Total: " + result.size)
        return result.values.toList()
    }

    private fun findFilepathBackwards(lines: List<String>, blockStart: Int): String? {
        var k = blockStart - 1
        var checked = 0
        while (k >= 0 && checked < 8) {
            val line = lines[k].trim()
            k--
            if (line.isEmpty()) continue
            if (line.startsWith("```")) return null

            checked++
            val candidate = extractFilepath(line)
            if (candidate != null) return candidate
        }
        return null
    }

    /**
     * استخراج مسار ملف من سطر نصي.
     * يدعم:
     *  - مسارات Windows الكاملة: C:\Users\AH\AH RapidDev\Test\Hello.kt (مع الفراغات)
     *  - مسارات Unix المطلقة: /home/user/project/file.kt
     *  - المسارات النسبية: app/src/main/... (الطريقة القديمة)
     */
    private fun extractFilepath(line: String): String? {
        var s = line.trim()

        // ===== 1. مسار Windows كامل (C:\... أو C:/...) =====
        // نبحث من آخر النص لتجنب الالتقاط الخاطئ
        val winMatches = Regex(
            """[A-Za-z]:[\\/][^\r\n`"'<>|?*]*?[\\/][^\r\n`"'<>|?*\\/]+\.[A-Za-z0-9_]{1,10}""",
            RegexOption.IGNORE_CASE
        ).findAll(s).toList()
        if (winMatches.isNotEmpty()) {
            // نأخذ أطول نتيجة
            val best = winMatches.maxByOrNull { it.value.length }?.value
            if (best != null) {
                val cleaned = best.trim().trimEnd('`', '"', '\'', ' ', ',', ';', ')', ']')
                if (cleaned.isNotEmpty() && isValidPath(cleaned)) return cleaned
            }
        }

        // ===== 2. مسار Unix مطلق =====
        val unixMatches = Regex(
            """/(?:[^\r\n`"'<>|?*\s]+/)+[^\r\n`"'<>|?*\s]+\.[A-Za-z0-9_]{1,10}"""
        ).findAll(s).toList()
        if (unixMatches.isNotEmpty()) {
            val best = unixMatches.maxByOrNull { it.value.length }?.value
            if (best != null) {
                val cleaned = best.trim().trimEnd('`', '"', '\'', ' ', ',', ';', ')', ']')
                if (cleaned.isNotEmpty() && isValidPath(cleaned)) return cleaned
            }
        }

        // ===== 3. المسار النسبي (الطريقة القديمة) =====
        s = s.replace(Regex("^[^:]{0,20}[:\\uFF1A]\\s*"), "")
        val tokens = s.split(Regex("\\s+"))
        var best: String? = null
        var bestLen: Int = 0
        var bestHasSlash: Boolean = false

        for (raw in tokens) {
            val token = cleanToken(raw) ?: continue
            val hasSlash = token.contains('/') || token.contains('\\')
            val replace = when {
                best == null -> true
                hasSlash && !bestHasSlash -> true
                hasSlash == bestHasSlash && token.length > bestLen -> true
                else -> false
            }
            if (replace) {
                best = token
                bestLen = token.length
                bestHasSlash = hasSlash
            }
        }
        return best
    }

    private fun cleanToken(raw: String): String? {
        var token = raw.trim()

        while (token.isNotEmpty() && isPunct(token.first())) {
            token = token.substring(1)
        }
        while (token.isNotEmpty() && isPunct(token.last())) {
            token = token.substring(0, token.length - 1)
        }

        if (token.isEmpty()) return null
        if (hasArabic(token)) return null
        if (token.length > 300) return null
        if (token.startsWith("..")) return null

        val dotIdx = token.lastIndexOf('.')
        if (dotIdx <= 0 || dotIdx == token.length - 1) return null

        val ext = token.substring(dotIdx + 1).lowercase()
        if (ext.isEmpty() || ext.length > 10) return null
        if (!ext.all { it.isLetterOrDigit() || it == '_' }) return null
        if (ext in BINARY_EXTENSIONS) return null

        return token
    }

    private fun isPunct(c: Char): Boolean {
        return when (c) {
            '`', '"', '\'', '(', ')', '[', ']', '<', '>',
            ',', '.', ';', ':', '?', '!', '-', '+', '=', '*', '~', '#', '@',
            '|', '\\', '{', '}', '$', '%', '^', '&' -> true
            else -> false
        }
    }

    private fun hasArabic(s: String): Boolean {
        for (c in s) {
            val code = c.code
            if (code in 0x0600..0x06FF) return true
            if (code in 0x0750..0x077F) return true
            if (code in 0xFB50..0xFDFF) return true
            if (code in 0xFE70..0xFEFF) return true
        }
        return false
    }

    private fun isValidPath(path: String): Boolean {
        if (path.isEmpty() || path.length > 500) return false
        if (path.startsWith("..")) return false
        if (hasArabic(path)) return false

        val dotIdx = path.lastIndexOf('.')
        if (dotIdx <= 0 || dotIdx == path.length - 1) return false

        val ext = path.substring(dotIdx + 1).lowercase()
        if (ext.isEmpty() || ext.length > 10) return false
        if (!ext.all { it.isLetterOrDigit() || it == '_' }) return false
        if (ext in BINARY_EXTENSIONS) return false
        return true
    }
}
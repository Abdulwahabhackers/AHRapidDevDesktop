package com.rapiddev.ah.core.utils

import com.rapiddev.ah.data.model.AiParsedFile

object TreeParser {

    private const val TAG = "TreeParser"

    private val TREE_LINE_REGEX = Regex(
        "^((?:(?:\\u2502| )   )*)(?:\\u251C\\u2500+|\\u2514\\u2500+)\\s*(.+)$"
    )

    fun parse(text: String): List<AiParsedFile> {
        if (text.contains("```")) {
            val codeFiles = AiCodeParser.parse(text)
            if (codeFiles.isNotEmpty()) {
                AppLogger.d(TAG, "Used AiCodeParser: " + codeFiles.size)
                return codeFiles
            }
        }

        return try {
            val files = parseTreeStructure(text)
            AppLogger.d(TAG, "Tree parsed: " + files.size)
            files
        } catch (e: Exception) {
            AppLogger.e(TAG, "TreeParser error", e)
            emptyList()
        }
    }

    private fun parseTreeStructure(text: String): List<AiParsedFile> {
        val lines = text.lines()
        val result = mutableListOf<AiParsedFile>()
        val stack = mutableListOf<String>()
        var started = false

        for (rawLine in lines) {
            val parsed = parseTreeLine(rawLine)

            if (parsed == null) {
                val sanitized = sanitize(rawLine)
                if (started && sanitized.isNotBlank() && !hasTreeSymbols(sanitized)) {
                    break
                }
                continue
            }

            started = true
            val depth = parsed.first
            val name = parsed.second
            val isDir = parsed.third

            while (stack.size > depth && stack.isNotEmpty()) {
                stack.removeAt(stack.size - 1)
            }

            if (isDir) {
                stack.add(name)
                result.add(AiParsedFile(stack.joinToString("/"), "", isDirectory = true))
            } else {
                val parts = stack.toMutableList()
                parts.add(name)
                result.add(AiParsedFile(parts.joinToString("/"), ""))
            }
        }

        if (result.isEmpty()) return emptyList()

        val firstComp = result.first().path.substringBefore("/")
        if (firstComp.isNotEmpty() && result.all {
                it.path == firstComp || it.path.startsWith("$firstComp/")
            }) {
            return result.mapNotNull {
                if (it.path == firstComp) null
                else it.copy(path = it.path.removePrefix("$firstComp/"))
            }
        }

        return result
    }

    private fun parseTreeLine(rawLine: String): Triple<Int, String, Boolean>? {
        if (rawLine.isBlank()) return null

        val line = sanitize(rawLine)
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null

        if (!hasTreeSymbols(line) && isEmojiChar(trimmed[0])) return null

        val match = TREE_LINE_REGEX.find(line)
        if (match != null) {
            val prefix = match.groupValues[1]
            var name = match.groupValues[2].trim()
            if (name.isEmpty()) return null

            name = removeCommentSuffix(name)
            name = stripLeadingEmoji(name)
            name = name.trim()
            if (name.isEmpty()) return null

            val depth = prefix.length / 4 + 1
            val isDir = name.endsWith("/")
            return Triple(depth, name.trimEnd('/').trim(), isDir)
        }

        if (!hasTreeSymbols(line)) {
            val withoutComment = removeCommentSuffix(trimmed)
            if (withoutComment.endsWith("/")) {
                val name = withoutComment.trimEnd('/').trim()
                if (name.isNotEmpty() && name.any { it.isLetterOrDigit() }) {
                    return Triple(0, name, true)
                }
            }
        }

        return null
    }

    private fun hasTreeSymbols(line: String): Boolean {
        return line.contains('\u2502') || line.contains('\u251C') || line.contains('\u2514')
    }

    private fun removeCommentSuffix(name: String): String {
        val spaceHash = name.indexOf(" #")
        val tabHash = name.indexOf("\t#")

        var cutAt = -1
        if (spaceHash >= 0) cutAt = spaceHash
        if (tabHash >= 0 && (cutAt < 0 || tabHash < cutAt)) cutAt = tabHash

        return if (cutAt > 0) name.substring(0, cutAt).trim() else name.trim()
    }

    private fun sanitize(line: String): String {
        val sb = StringBuilder(line.length)
        for (c in line) {
            val code = c.code
            if (code == 0x200B || code == 0x200C || code == 0x200D) continue
            if (code == 0x200E || code == 0x200F) continue
            if (code in 0x202A..0x202E) continue
            if (code in 0x2066..0x2069) continue
            if (code == 0xFEFF) continue
            sb.append(c)
        }
        return sb.toString()
    }

    private fun stripLeadingEmoji(s: String): String {
        var i = 0
        while (i < s.length) {
            val c = s[i]
            val code = c.code
            if (code in 0xD800..0xDBFF) {
                i += 2
                continue
            }
            if (code in 0x2600..0x27BF || code in 0x2B00..0x2BFF ||
                code in 0x2300..0x23FF || code in 0x2190..0x21FF) {
                i += 1
                continue
            }
            break
        }
        val safe = if (i > s.length) s.length else i
        return s.substring(safe).trimStart()
    }

    private fun isEmojiChar(c: Char): Boolean {
        val code = c.code
        if (code in 0xD800..0xDBFF) return true
        if (code in 0x2600..0x27BF) return true
        if (code in 0x2B00..0x2BFF) return true
        if (code in 0x2300..0x23FF) return true
        if (code in 0x2190..0x21FF) return true
        return false
    }
}
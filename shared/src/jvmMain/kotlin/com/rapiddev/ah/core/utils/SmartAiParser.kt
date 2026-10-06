package com.rapiddev.ah.core.utils

import com.rapiddev.ah.data.model.AiOperation

object SmartAiParser {

    private const val TAG = "SmartAiParser"

    private val FILE_PATH_REGEX = Regex(
        """([\w\-./ ]+\.(?:kt|java|xml|gradle|kts|properties|pro|json|md|txt|html|css|js|py|sh))""",
        RegexOption.IGNORE_CASE
    )

    private val DELETE_FILE_REGEX = Regex(
        """(?:احذف|امسح|Delete|Remove)\s+(?:ملف|file)\s+([\w\-./ ]+\.\w+)""",
        RegexOption.IGNORE_CASE
    )
    private val DELETE_FOLDER_REGEX = Regex(
        """(?:احذف|امسح|Delete|Remove)\s+(?:مجلد|folder|dir)\s+([\w\-./ ]+)""",
        RegexOption.IGNORE_CASE
    )
    private val DELETE_FUNCTION_REGEX = Regex(
        """(?:احذف|امسح|Delete|Remove)\s+(?:ال)?(?:دالة|function|method)\s+(\w+)""",
        RegexOption.IGNORE_CASE
    )
    private val ENSURE_IMPORT_REGEX = Regex(
        """(?:تأكد|تأكّد|Ensure|Make\s+sure)\s+(?:من\s+)?(?:وجود\s+)?(import\s+[\w.]+)""",
        RegexOption.IGNORE_CASE
    )
    private val ADD_IMPORT_REGEX = Regex(
        """(?:أضف|اضف|Add)\s+(?:ال)?(?:استيراد|import)\s+(import\s+[\w.]+|[\w.]+)""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String, projectFiles: List<String> = emptyList()): List<AiOperation> {
        val normalized = text.replace("\r\n", "\n").replace('\r', '\n')
        val operations = mutableListOf<AiOperation>()
        val seen = mutableSetOf<String>()

        val fileIndex = buildFileIndex(projectFiles)
        AppLogger.d(TAG, "File index size: " + fileIndex.size)

        val codeBlocks = extractCodeBlocks(normalized, fileIndex)
        AppLogger.d(TAG, "Found " + codeBlocks.size + " code blocks")

        for (block in codeBlocks) {
            val op = classifyBlock(block)
            if (op != null && seen.add(opKey(op))) {
                operations.add(op)
            }
        }

        for (m in DELETE_FOLDER_REGEX.findAll(normalized)) {
            val path = m.groupValues[1].trim()
            val op = AiOperation.DeleteFolder(path)
            if (seen.add(opKey(op))) operations.add(op)
        }

        for (m in DELETE_FILE_REGEX.findAll(normalized)) {
            val rawPath = m.groupValues[1].trim()
            val path = resolvePath(rawPath, fileIndex)
            val op = AiOperation.DeleteFile(path)
            if (seen.add(opKey(op))) operations.add(op)
        }

        for (m in DELETE_FUNCTION_REGEX.findAll(normalized)) {
            val fn = m.groupValues[1].trim()
            val targetFile = findTargetFileNear(normalized, m.range.first, fileIndex) ?: continue
            val op = AiOperation.DeleteFunction(targetFile, fn)
            if (seen.add(opKey(op))) operations.add(op)
        }

        for (m in ENSURE_IMPORT_REGEX.findAll(normalized)) {
            val imp = m.groupValues[1].trim()
            val targetFile = findTargetFileNear(normalized, m.range.first, fileIndex) ?: continue
            val op = AiOperation.EnsureImport(targetFile, imp)
            if (seen.add(opKey(op))) operations.add(op)
        }

        for (m in ADD_IMPORT_REGEX.findAll(normalized)) {
            var imp = m.groupValues[1].trim()
            if (!imp.startsWith("import")) imp = "import " + imp
            val targetFile = findTargetFileNear(normalized, m.range.first, fileIndex) ?: continue
            val op = AiOperation.AddImport(targetFile, imp)
            if (seen.add(opKey(op))) operations.add(op)
        }

        val sorted = operations.sortedBy { op ->
            when (op) {
                is AiOperation.CreateFile,
                is AiOperation.ReplaceFile -> 0
                is AiOperation.AddImport,
                is AiOperation.EnsureImport -> 1
                is AiOperation.ReplaceCode,
                is AiOperation.AddCode -> 2
                is AiOperation.DeleteLine,
                is AiOperation.DeleteFunction -> 3
                is AiOperation.DeleteFile,
                is AiOperation.DeleteFolder -> 4
            }
        }

        AppLogger.d(TAG, "Total operations: " + sorted.size)
        return sorted
    }

    private fun buildFileIndex(projectFiles: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (path in projectFiles) {
            val normalized = path.replace('\\', '/').trimStart('/')
            if (normalized.isEmpty()) continue

            val fileName = normalized.substringAfterLast('/')
            if (fileName.isNotEmpty() && !map.containsKey(fileName)) {
                map[fileName] = normalized
            }

            val parts = normalized.split('/')
            if (parts.size >= 2) {
                val lastTwo = parts.takeLast(2).joinToString("/")
                if (!map.containsKey(lastTwo)) map[lastTwo] = normalized
            }

            if (parts.size >= 3) {
                val lastThree = parts.takeLast(3).joinToString("/")
                if (!map.containsKey(lastThree)) map[lastThree] = normalized
            }
        }
        return map
    }

    private fun resolvePath(rawPath: String, fileIndex: Map<String, String>): String {
        val clean = rawPath.trim().replace('\\', '/')

        val fixedPath = fixPathPrefix(clean)

        if (fixedPath.contains('/') && fixedPath.split('/').size >= 3) {
            return fixedPath
        }

        fileIndex[fixedPath]?.let { return it }
        fileIndex[clean]?.let { return it }

        val fileName = clean.substringAfterLast('/')
        fileIndex[fileName]?.let { return it }

        for ((_, value) in fileIndex) {
            if (value.endsWith("/" + clean) || value.endsWith(clean)) {
                return value
            }
        }

        return fixedPath
    }

    private fun fixPathPrefix(path: String): String {
        var p = path.trim()
        if (p.startsWith("src/") && !p.startsWith("app/src/")) {
            p = "app/$p"
        }
        if (p.startsWith("main/") && !p.startsWith("app/")) {
            p = "app/src/$p"
        }
        return p
    }

    private data class CodeBlock(
        val filePath: String,
        val code: String,
        val contextBefore: String,
        val isNewFile: Boolean
    )

    private fun extractCodeBlocks(
        text: String,
        fileIndex: Map<String, String>
    ): List<CodeBlock> {
        val blocks = mutableListOf<CodeBlock>()
        val lines = text.split("\n")
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trimStart()
            if (line.startsWith("```")) {
                var j = i + 1
                while (j < lines.size && !lines[j].trimStart().startsWith("```")) {
                    j++
                }
                if (j < lines.size) {
                    val code = lines.subList(i + 1, j).joinToString("\n")
                    val rawPath = findFilepathBackwards(lines, i)
                    if (rawPath != null) {
                        val resolvedPath = resolvePath(rawPath, fileIndex)

                        val contextStart = maxOf(0, i - 8)
                        val context = lines.subList(contextStart, i).joinToString("\n")

                        blocks.add(
                            CodeBlock(
                                filePath = resolvedPath,
                                code = code,
                                contextBefore = context,
                                isNewFile = !isModificationContext(context)
                            )
                        )
                    }
                    i = j + 1
                    continue
                }
            }
            i++
        }
        return blocks
    }

    private fun isModificationContext(context: String): Boolean {
        val modWords = listOf(
            "عدل", "عدّل", "تعديل", "استبدل", "استبدال", "غير", "غيّر",
            "Modify", "Edit", "Update", "Replace", "Change"
        )
        return modWords.any { context.contains(it, ignoreCase = true) }
    }

    private fun classifyBlock(block: CodeBlock): AiOperation? {
        return if (block.isNewFile) {
            AiOperation.CreateFile(block.filePath, block.code)
        } else {
            AiOperation.ReplaceFile(block.filePath, block.code)
        }
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

            val fullMatch = Regex(
                """(app/src/[\w\-./ ]+\.\w+|src/[\w\-./ ]+\.\w+)""",
                RegexOption.IGNORE_CASE
            ).find(line)
            if (fullMatch != null) {
                return fixPathPrefix(fullMatch.value.trim())
            }

            val match = FILE_PATH_REGEX.find(line)
            if (match != null) {
                val path = match.value.trim()
                if (isValidPath(path)) return path
            }
        }
        return null
    }

    private fun findTargetFileNear(
        text: String,
        position: Int,
        fileIndex: Map<String, String>
    ): String? {
        val start = maxOf(0, position - 400)
        val snippet = text.substring(start, minOf(text.length, position + 200))

        val fullPaths = Regex(
            """(app/src/[\w\-./ ]+\.\w+|src/[\w\-./ ]+\.\w+)""",
            RegexOption.IGNORE_CASE
        ).findAll(snippet).toList()

        if (fullPaths.isNotEmpty()) {
            return fixPathPrefix(
                fullPaths
                    .minByOrNull { Math.abs(it.range.first - (position - start)) }
                    ?.value
                    ?.trim()
                    ?: return null
            )
        }

        val matches = FILE_PATH_REGEX.findAll(snippet).toList()
        if (matches.isEmpty()) return null

        val rawPath = matches
            .filter { isValidPath(it.value) }
            .minByOrNull { Math.abs(it.range.first - (position - start)) }
            ?.value
            ?.trim()
            ?: return null

        return resolvePath(rawPath, fileIndex)
    }

    private fun isValidPath(path: String): Boolean {
        if (path.isEmpty() || path.length > 300) return false
        if (path.startsWith("/") || path.startsWith("..")) return false
        val ext = path.substringAfterLast('.', "").lowercase()
        if (ext.isEmpty() || ext.length > 10) return false
        return ext.all { it.isLetterOrDigit() }
    }

    private fun opKey(op: AiOperation): String {
        return when (op) {
            is AiOperation.CreateFile -> "create:" + op.targetPath
            is AiOperation.ReplaceFile -> "replace:" + op.targetPath
            is AiOperation.DeleteFile -> "delF:" + op.targetPath
            is AiOperation.DeleteFolder -> "delD:" + op.targetPath
            is AiOperation.AddImport -> "addImp:" + op.targetPath + ":" + op.importLine
            is AiOperation.EnsureImport -> "ensureImp:" + op.targetPath + ":" + op.importLine
            is AiOperation.ReplaceCode -> "repCode:" + op.targetPath + ":" + op.oldCode.hashCode()
            is AiOperation.AddCode -> "addCode:" + op.targetPath + ":" + op.code.hashCode()
            is AiOperation.DeleteLine -> "delLine:" + op.targetPath + ":" + op.lineContent
            is AiOperation.DeleteFunction -> "delFn:" + op.targetPath + ":" + op.functionName
        }
    }
}
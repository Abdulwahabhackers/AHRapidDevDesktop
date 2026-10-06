package com.rapiddev.ah.core.utils

import com.rapiddev.ah.data.model.AiOperation
import com.rapiddev.ah.data.model.OperationResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OperationExecutor(private val baseDir: File) {

    companion object {
        private const val TAG = "OperationExecutor"
        private const val BACKUP_DIR = ".smart_import_backup"
    }

    private val backupDir = File(baseDir, BACKUP_DIR).apply { mkdirs() }
    private val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    fun execute(op: AiOperation): OperationResult {
        return try {
            when (op) {
                is AiOperation.CreateFile -> createFile(op)
                is AiOperation.ReplaceFile -> replaceFile(op)
                is AiOperation.DeleteFile -> deleteFile(op)
                is AiOperation.DeleteFolder -> deleteFolder(op)
                is AiOperation.AddImport -> addImport(op)
                is AiOperation.EnsureImport -> ensureImport(op)
                is AiOperation.ReplaceCode -> replaceCode(op)
                is AiOperation.AddCode -> addCode(op)
                is AiOperation.DeleteLine -> deleteLine(op)
                is AiOperation.DeleteFunction -> deleteFunction(op)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "execute failed: ${op.description}", e)
            OperationResult(op, false, e.message)
        }
    }

    private fun createFile(op: AiOperation.CreateFile): OperationResult {
        val file = File(baseDir, op.targetPath)
        file.parentFile?.mkdirs()

        if (file.exists()) {
            backupIfExists(file)
        }
        file.writeText(op.content, Charsets.UTF_8)
        return OperationResult(op, true)
    }

    private fun replaceFile(op: AiOperation.ReplaceFile): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود: ${op.targetPath}")
        }
        val backup = backupIfExists(file)
        file.writeText(op.content, Charsets.UTF_8)
        return OperationResult(op, true, backupPath = backup)
    }

    private fun deleteFile(op: AiOperation.DeleteFile): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }
        val backup = backupIfExists(file)
        return if (file.delete()) {
            OperationResult(op, true, backupPath = backup)
        } else {
            OperationResult(op, false, "فشل الحذف")
        }
    }

    private fun deleteFolder(op: AiOperation.DeleteFolder): OperationResult {
        val dir = File(baseDir, op.targetPath)
        if (!dir.exists() || !dir.isDirectory) {
            return OperationResult(op, false, "المجلد غير موجود")
        }
        return if (dir.deleteRecursively()) {
            OperationResult(op, true)
        } else {
            OperationResult(op, false, "فشل حذف المجلد")
        }
    }

    private fun addImport(op: AiOperation.AddImport): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }

        val backup = backupIfExists(file)
        val lines = file.readLines().toMutableList()

        if (lines.any { it.trim() == op.importLine.trim() }) {
            return OperationResult(op, true, "موجود مسبقاً")
        }

        val lastImportIdx = lines.indexOfLast { it.trimStart().startsWith("import ") }

        val insertAt = if (lastImportIdx >= 0) {
            lastImportIdx + 1
        } else {
            val pkgIdx = lines.indexOfFirst { it.trimStart().startsWith("package ") }
            if (pkgIdx >= 0) pkgIdx + 1 else 0
        }

        lines.add(insertAt, op.importLine)
        file.writeText(lines.joinToString("\n"), Charsets.UTF_8)
        return OperationResult(op, true, backupPath = backup)
    }

    private fun ensureImport(op: AiOperation.EnsureImport): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }
        val content = file.readText(Charsets.UTF_8)
        if (content.contains(op.importLine.trim())) {
            return OperationResult(op, true, "موجود مسبقاً")
        }
        return addImport(AiOperation.AddImport(op.targetPath, op.importLine))
    }

    private fun replaceCode(op: AiOperation.ReplaceCode): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }

        val content = file.readText(Charsets.UTF_8)
        val oldTrimmed = op.oldCode.trim()
        val newTrimmed = op.newCode.trim()

        if (!content.contains(oldTrimmed)) {
            return OperationResult(op, false, "لم يُعثر على الكود القديم")
        }

        val backup = backupIfExists(file)
        val replaced = content.replace(oldTrimmed, newTrimmed)
        file.writeText(replaced, Charsets.UTF_8)
        return OperationResult(op, true, backupPath = backup)
    }

    private fun addCode(op: AiOperation.AddCode): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }

        val backup = backupIfExists(file)
        val content = file.readText(Charsets.UTF_8)

        val newContent = when (op.position) {
            AiOperation.InsertPosition.END_OF_FILE -> {
                content.trimEnd() + "\n\n" + op.code + "\n"
            }
            AiOperation.InsertPosition.BEFORE_LAST_BRACE -> {
                val lastBrace = content.lastIndexOf('}')
                if (lastBrace < 0) return OperationResult(op, false, "لا يوجد }")
                content.substring(0, lastBrace) +
                    "\n" + op.code + "\n" +
                    content.substring(lastBrace)
            }
            AiOperation.InsertPosition.AFTER_FILE_HEADER -> {
                val lines = content.lines().toMutableList()
                val lastImport = lines.indexOfLast { it.trimStart().startsWith("import ") }
                val insertAt = if (lastImport >= 0) lastImport + 1 else 0
                lines.add(insertAt, "")
                lines.add(insertAt + 1, op.code)
                lines.joinToString("\n")
            }
            AiOperation.InsertPosition.AFTER_SPECIFIC -> {
                content.trimEnd() + "\n" + op.code + "\n"
            }
        }

        file.writeText(newContent, Charsets.UTF_8)
        return OperationResult(op, true, backupPath = backup)
    }

    private fun deleteLine(op: AiOperation.DeleteLine): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }

        val content = file.readText(Charsets.UTF_8)
        val search = op.lineContent.trim()
        if (search.isEmpty()) {
            return OperationResult(op, false, "السطر المطلوب حذفه فارغ")
        }

        val lines = content.lines()
        val filtered = lines.filterNot { it.trim() == search }

        if (filtered.size == lines.size) {
            return OperationResult(op, false, "لم يُعثر على السطر")
        }

        val backup = backupIfExists(file)
        file.writeText(filtered.joinToString("\n"), Charsets.UTF_8)
        return OperationResult(op, true, backupPath = backup)
    }

    private fun deleteFunction(op: AiOperation.DeleteFunction): OperationResult {
        val file = File(baseDir, op.targetPath)
        if (!file.exists()) {
            return OperationResult(op, false, "الملف غير موجود")
        }

        val content = file.readText(Charsets.UTF_8)
        val name = op.functionName.trim()
        if (name.isEmpty()) return OperationResult(op, false, "اسم دالة فارغ")

        val patterns = listOf(
            Regex("""fun\s+$name\s*[(<]"""),
            Regex("""fun\s+$name\s*\("""),
            Regex("""def\s+$name\s*\("""),
            Regex("""(?:public|private|protected|internal)?\s*(?:\w+\s+)*$name\s*\(""")
        )

        val found = patterns.firstNotNullOfOrNull { it.find(content) }
            ?: return OperationResult(op, false, "لم يُعثر على دالة $name")

        val start = found.range.first
        var braceCount = 0
        var i = content.indexOf('{', start)
        if (i < 0) return OperationResult(op, false, "لا يوجد جسم للدالة")

        var end = -1
        var started = false
        while (i < content.length) {
            when (content[i]) {
                '{' -> { braceCount++; started = true }
                '}' -> {
                    braceCount--
                    if (started && braceCount == 0) { end = i + 1; break }
                }
            }
            i++
        }

        if (end < 0) return OperationResult(op, false, "لم يتم تحديد نهاية الدالة")

        val before = content.substring(0, start)
        val after = content.substring(end).trimStart('\n')
        val newContent = before.trimEnd('\n') + "\n\n" + after

        val backup = backupIfExists(file)
        file.writeText(newContent, Charsets.UTF_8)
        return OperationResult(op, true, backupPath = backup)
    }

    private fun backupIfExists(file: File): String? {
        if (!file.exists() || !file.isFile) return null
        try {
            val relative = file.absolutePath.removePrefix(baseDir.absolutePath)
                .trimStart('/')
                .replace('/', '_')
            val backupFile = File(backupDir, "${timestamp}__$relative")
            file.copyTo(backupFile, overwrite = true)
            return backupFile.absolutePath
        } catch (e: Exception) {
            AppLogger.w(TAG, "backup failed: ${file.name}", e)
            return null
        }
    }
}
package com.rapiddev.ah.presentation.smartimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.utils.AiCodeParser
import com.rapiddev.ah.core.utils.OperationExecutor
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.core.utils.SmartAiParser
import com.rapiddev.ah.data.model.AiOperation
import com.rapiddev.ah.data.model.SmartImportReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SmartImportViewModel : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input

    private val _operations = MutableStateFlow<List<AiOperation>>(emptyList())
    val operations: StateFlow<List<AiOperation>> = _operations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _report = MutableStateFlow<SmartImportReport?>(null)
    val report: StateFlow<SmartImportReport?> = _report

    fun setInput(text: String) { _input.value = text }
    fun clearMessage() { _message.value = null }
    fun clearReport() { _report.value = null }

    fun removeOperation(index: Int) {
        _operations.value = _operations.value.toMutableList().apply { removeAt(index) }
    }

    fun removeAll() { _operations.value = emptyList() }

    fun resolvePath(raw: String): String? {
        return PathHistory.resolveExistingPath(raw)
    }

    fun analyze(rawPath: String) {
        if (rawPath.isBlank()) {
            _message.value = "❌ أدخل مسار المشروع أولاً"
            return
        }

        val baseDir = File(rawPath)
        if (!baseDir.exists() || !baseDir.isDirectory) {
            _message.value = "❌ المجلد غير موجود: $rawPath"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _report.value = null

            val projectFiles = withContext(Dispatchers.IO) {
                collectProjectFiles(baseDir)
            }

            // 1) العمليات الذكية (تعديلات + استخراج الكود)
            val smartOps = withContext(Dispatchers.IO) {
                SmartAiParser.parse(_input.value, projectFiles)
            }

            // 2) الملفات الكاملة (كاحتياطي لتغطية أي ملف لم يكتشفه SmartAiParser)
            val fullFiles = withContext(Dispatchers.IO) {
                AiCodeParser.parse(_input.value)
            }

            // 3) دمج النتائج
            val merged = mergeOperations(smartOps, fullFiles, projectFiles)

            _operations.value = merged
            _message.value = when {
                merged.isEmpty() -> "❌ لم يتم العثور على أي عمليات (فهرس: ${projectFiles.size} ملف)"
                else -> "✅ تم استخراج ${merged.size} عملية" +
                    if (projectFiles.isEmpty()) " (المجلد فارغ — سيتم الإنشاء)"
                    else " (من أصل ${projectFiles.size} ملف)"
            }
            _isLoading.value = false
        }
    }

    /**
     * دمج نتائج المحلّلين:
     * - عمليات SmartAiParser أولاً (تحتفظ بترتيبها)
     * - أي ملف من AiCodeParser لم يُغطّ سيضاف كـ CreateFile أو ReplaceFile
     */
    private fun mergeOperations(
        smartOps: List<AiOperation>,
        fullFiles: List<com.rapiddev.ah.data.model.AiParsedFile>,
        projectFiles: List<String>
    ): List<AiOperation> {
        val result = mutableListOf<AiOperation>()
        val coveredPaths = mutableSetOf<String>()

        // نطبّع المسارات للمقارنة
        fun norm(p: String) = p.replace('\\', '/').trimStart('/').lowercase()

        // 1) نضيف عمليات SmartAiParser
        for (op in smartOps) {
            result.add(op)
            coveredPaths.add(norm(op.targetPath))
        }

        // 2) نضيف الملفات الكاملة للتي لم تُغطَّ
        for (file in fullFiles) {
            val key = norm(file.path)
            if (key in coveredPaths) continue

            val exists = projectFiles.any { pf ->
                norm(pf) == key || norm(pf).endsWith("/$key")
            }

            val op = if (exists) {
                AiOperation.ReplaceFile(file.path, file.content)
            } else {
                AiOperation.CreateFile(file.path, file.content)
            }
            result.add(op)
            coveredPaths.add(key)
        }

        // 3) ترتيب: إنشاء/استبدال → إضافة imports → تعديلات كود → حذف
        return result.sortedBy { op ->
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
    }

    private fun collectProjectFiles(baseDir: File): List<String> {
        val result = mutableListOf<String>()
        val ignoredDirs = setOf(
            "build", ".gradle", ".idea", ".kotlin", ".cxx", "node_modules",
            ".androidide", "intermediates", "outputs", "tmp", "generated",
            "caches", "snapshot", "logs", ".git", "captures",
            ".externalNativeBuild", ".smart_import_backup"
        )

        fun walk(dir: File) {
            val children = dir.listFiles() ?: return
            for (child in children) {
                if (child.name.startsWith(".") && child.isDirectory) continue
                if (child.isDirectory && child.name in ignoredDirs) continue

                if (child.isDirectory) {
                    walk(child)
                } else {
                    val relative = child.absolutePath
                        .removePrefix(baseDir.absolutePath)
                        .trimStart('/', '\\')
                    result.add(relative)
                }
            }
        }
        walk(baseDir)
        return result
    }

    fun execute(rawPath: String) {
        val ops = _operations.value
        if (ops.isEmpty()) {
            _message.value = "❌ لا توجد عمليات للتنفيذ"
            return
        }

        val baseDir = File(rawPath)
        if (!baseDir.exists() || !baseDir.isDirectory) {
            _message.value = "❌ المسار غير موجود"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _report.value = null

            val results = withContext(Dispatchers.IO) {
                val executor = OperationExecutor(baseDir)
                ops.map { op -> executor.execute(op) }
            }

            val succeeded = results.count { it.success }
            val failed = results.size - succeeded
            _report.value = SmartImportReport(
                total = results.size,
                succeeded = succeeded,
                failed = failed,
                results = results
            )

            _message.value = when {
                failed == 0 -> "✅ نجحت جميع العمليات ($succeeded)"
                succeeded == 0 -> "❌ فشل الجميع"
                else -> "⚠️ نجح $succeeded / فشل $failed"
            }
            _isLoading.value = false
        }
    }
}
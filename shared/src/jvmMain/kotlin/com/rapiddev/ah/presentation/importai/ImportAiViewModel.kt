package com.rapiddev.ah.presentation.importai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.utils.AiCodeParser
import com.rapiddev.ah.core.utils.StorageManager
import com.rapiddev.ah.data.model.AiParsedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImportAiViewModel : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input

    private val _parsed = MutableStateFlow<List<AiParsedFile>>(emptyList())
    val parsed: StateFlow<List<AiParsedFile>> = _parsed

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun setInput(text: String) { _input.value = text }
    fun clearMessage() { _message.value = null }
    fun removeFile(path: String) {
        _parsed.value = _parsed.value.filterNot { it.path == path }
    }
    fun removeAll() { _parsed.value = emptyList() }

    /**
     * @param basePath المجلد الهدف — يستخدم لتنظيف المسارات المطلقة.
     */
    fun parse(basePath: String = "") {
        val files = AiCodeParser.parse(_input.value)
        val normalized = files.map { f ->
            f.copy(path = normalizeAgainst(f.path, basePath))
        }
        _parsed.value = normalized
        _message.value = if (normalized.isEmpty()) "❌ لم يتم العثور على ملفات"
        else "✅ تم اكتشاف ${normalized.size} ملف"
    }

    fun writeToFolder(basePath: String) {
        val files = _parsed.value
        if (files.isEmpty()) {
            _message.value = "❌ لا توجد ملفات"
            return
        }

        val baseDir = File(basePath)
        if (!baseDir.exists() || !baseDir.isDirectory) {
            _message.value = "❌ المجلد غير موجود: $basePath"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            var success = 0
            var failed = 0
            val errors = mutableListOf<String>()

            withContext(Dispatchers.IO) {
                files.forEach { file ->
                    try {
                        StorageManager.writeFile(baseDir, file.path, file.content)
                        success++
                    } catch (e: Exception) {
                        failed++
                        errors.add("${file.path}: ${e.message}")
                    }
                }
            }

            _message.value = when {
                failed == 0 -> "✅ تم كتابة $success ملف"
                success == 0 -> "❌ فشل الجميع: ${errors.firstOrNull()}"
                else -> "⚠️ نجح: $success | فشل: $failed — ${errors.firstOrNull()}"
            }
            _isLoading.value = false
        }
    }

    /**
     * إذا كان المسار مطلقاً ويبدأ بـ basePath نحذف البادئة ليصير نسبياً.
     * مثال:
     *   path     = C:\Users\AH\AH RapidDev\Test\Hello.kt
     *   basePath = C:\Users\AH\AH RapidDev
     *   النتيجة = Test\Hello.kt
     */
    private fun normalizeAgainst(path: String, basePath: String): String {
        if (basePath.isBlank()) return path

        val p = path.replace('/', '\\').trim()
        val b = basePath.replace('/', '\\').trimEnd('\\')

        // إذا كان المسار مطلقاً ويبدأ بـ basePath
        if (p.startsWith(b, ignoreCase = true)) {
            val relative = p.substring(b.length).trimStart('\\', '/')
            if (relative.isNotEmpty()) return relative
        }

        return path
    }
}
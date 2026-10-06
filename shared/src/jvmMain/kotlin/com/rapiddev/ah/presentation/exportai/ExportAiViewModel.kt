package com.rapiddev.ah.presentation.exportai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.utils.ProjectExporter
import com.rapiddev.ah.data.model.ExportOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExportAiViewModel : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _progressText = MutableStateFlow("")
    val progressText: StateFlow<String> = _progressText

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() { _message.value = null }

    fun export(sourcePath: String, options: ExportOptions) {
        if (sourcePath.isBlank()) {
            _message.value = "❌ اختر مسار المشروع أولاً"
            return
        }

        val sourceDir = File(sourcePath)
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            _message.value = "❌ المسار غير موجود"
            return
        }

        viewModelScope.launch {
            _isExporting.value = true
            _progress.value = 0
            _progressText.value = "جاري التحضير..."

            val result = withContext(Dispatchers.IO) {
                ProjectExporter().export(sourcePath, options) { p ->
                    _progress.value = (p.current * 100) / p.total.coerceAtLeast(1)
                    _progressText.value = "📁 ${p.current}/${p.total}"
                }
            }

            _isExporting.value = false

            if (result.success) {
                _message.value = buildString {
                    append("✅ تم التصدير بنجاح!\n")
                    append("📁 ${result.totalFolders} مجلد\n")
                    append("📄 ${result.totalFiles} ملف\n")
                    append("📍 ${result.outputDir?.absolutePath}")
                }
            } else {
                _message.value = "❌ فشل: ${result.error}"
            }
        }
    }
}
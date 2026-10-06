package com.rapiddev.ah.presentation.merge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class MergeMode { SKIP, OVERWRITE, RENAME }

data class MergeReport(
    val copied: Int = 0,
    val skipped: Int = 0,
    val overwritten: Int = 0,
    val failed: Int = 0,
    val dirsCreated: Int = 0,
    val errors: List<String> = emptyList()
)

class MergeViewModel : ViewModel() {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _report = MutableStateFlow<MergeReport?>(null)
    val report: StateFlow<MergeReport?> = _report

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    fun clearMessage() { _message.value = null }

    fun clearReport() {
        _report.value = null
        _progress.value = 0
    }

    fun analyze(sourcePath: String, targetPath: String) {
        val source = File(sourcePath)
        val target = File(targetPath)

        if (!source.exists() || !source.isDirectory) {
            _message.value = "المصدر غير موجود"
            return
        }
        if (!target.exists() || !target.isDirectory) {
            _message.value = "الهدف غير موجود"
            return
        }
        if (source.absolutePath == target.absolutePath) {
            _message.value = "لا يمكن الدمج في نفس المجلد"
            return
        }
        if (target.absolutePath.startsWith(source.absolutePath + File.separator)) {
            _message.value = "الهدف داخل المصدر - غير مسموح"
            return
        }

        viewModelScope.launch {
            val counts = withContext(Dispatchers.IO) {
                var total = 0
                var exists = 0
                source.walkTopDown().forEach { f ->
                    if (f.isFile) {
                        total++
                        val rel = f.absolutePath.removePrefix(source.absolutePath)
                            .removePrefix(File.separator)
                        val t = File(target, rel)
                        if (t.exists()) exists++
                    }
                }
                Pair(total, exists)
            }
            _message.value = "المصدر: ${counts.first} ملف منها ${counts.second} موجود في الهدف"
        }
    }

    fun merge(sourcePath: String, targetPath: String, mode: MergeMode) {
        val source = File(sourcePath)
        val target = File(targetPath)

        if (!source.exists() || !source.isDirectory) {
            _message.value = "المصدر غير موجود"
            return
        }
        if (!target.exists() || !target.isDirectory) {
            _message.value = "الهدف غير موجود"
            return
        }
        if (source.absolutePath == target.absolutePath) {
            _message.value = "لا يمكن الدمج في نفس المجلد"
            return
        }
        if (target.absolutePath.startsWith(source.absolutePath + File.separator)) {
            _message.value = "الهدف داخل المصدر - غير مسموح"
            return
        }

        viewModelScope.launch {
            _isRunning.value = true
            _progress.value = 0
            _report.value = null

            val result = withContext(Dispatchers.IO) {
                var copied = 0
                var skipped = 0
                var overwritten = 0
                var failed = 0
                var dirsCreated = 0
                val errors = mutableListOf<String>()

                val allFiles = mutableListOf<File>()
                source.walkTopDown().forEach { if (it.isFile) allFiles.add(it) }
                val total = allFiles.size

                for ((index, file) in allFiles.withIndex()) {
                    try {
                        val rel = file.absolutePath.removePrefix(source.absolutePath)
                            .removePrefix(File.separator)
                        val targetFile = File(target, rel)
                        val targetParent = targetFile.parentFile

                        if (targetParent != null && !targetParent.exists()) {
                            if (targetParent.mkdirs()) dirsCreated++
                        }

                        if (targetFile.exists()) {
                            when (mode) {
                                MergeMode.SKIP -> skipped++
                                MergeMode.OVERWRITE -> {
                                    targetFile.delete()
                                    file.copyTo(targetFile, overwrite = true)
                                    overwritten++
                                }
                                MergeMode.RENAME -> {
                                    val newName = uniqueName(targetFile)
                                    val newTarget = File(targetParent, newName)
                                    file.copyTo(newTarget, overwrite = false)
                                    copied++
                                }
                            }
                        } else {
                            file.copyTo(targetFile, overwrite = false)
                            copied++
                        }
                    } catch (e: Exception) {
                        failed++
                        errors.add(file.name + ": " + e.message)
                    }

                    val pct = if (total > 0) ((index + 1) * 100) / total else 100
                    _progress.value = pct
                }

                MergeReport(
                    copied = copied,
                    skipped = skipped,
                    overwritten = overwritten,
                    failed = failed,
                    dirsCreated = dirsCreated,
                    errors = errors
                )
            }

            _report.value = result
            _isRunning.value = false
            _message.value = "انتهى الدمج"
        }
    }

    private fun uniqueName(file: File): String {
        val base = file.nameWithoutExtension
        val ext = file.extension
        val parent = file.parentFile ?: return file.name

        var counter = 1
        var candidate = file.name
        while (File(parent, candidate).exists()) {
            candidate = if (ext.isEmpty()) "${base}_$counter" else "${base}_$counter.$ext"
            counter++
        }
        return candidate
    }
}
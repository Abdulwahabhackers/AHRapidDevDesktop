package com.rapiddev.ah.presentation.newproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.templates.ProjectTemplates
import com.rapiddev.ah.core.utils.StorageManager
import com.rapiddev.ah.core.utils.TreeParser
import com.rapiddev.ah.data.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NewProjectViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _treeCount = MutableStateFlow(0)
    val treeCount: StateFlow<Int> = _treeCount

    fun clearMessage() { _message.value = null }

    fun analyzeTree(text: String) {
        val files = TreeParser.parse(text)
        val dirCount = files.count { it.isDirectory }
        val fileCount = files.count { !it.isDirectory }
        _treeCount.value = files.size
        _message.value = if (files.isEmpty()) "❌ لم يتم العثور على أي عناصر"
        else "✅ $dirCount مجلد + $fileCount ملف"
    }

    fun createProject(
        basePath: String,
        projectName: String,
        packageName: String,
        type: ProjectType,
        androidLanguage: ProjectType.AndroidLanguage,
        treeText: String
    ) {
        if (basePath.isBlank() || projectName.isBlank()) {
            _message.value = "❌ أدخل اسم المشروع والمسار"
            return
        }

        if (type == ProjectType.ANDROID) {
            if (!packageName.matches(Regex("""^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$"""))) {
                _message.value = "❌ اسم الحزمة غير صحيح (مثال: com.example.app)"
                return
            }
        }

        val baseDir = File(basePath)
        if (!baseDir.exists() || !baseDir.isDirectory) {
            _message.value = "❌ المجلد غير موجود: $basePath"
            return
        }

        val projectDir = File(baseDir, projectName)
        if (projectDir.exists() && projectDir.listFiles()?.isNotEmpty() == true) {
            _message.value = "❌ المجلد موجود وفيه ملفات"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val files = if (type == ProjectType.FROM_TREE) {
                TreeParser.parse(treeText)
            } else {
                ProjectTemplates.getFiles(
                    type = type,
                    appName = projectName,
                    packageName = packageName,
                    androidLanguage = androidLanguage
                )
            }

            if (files.isEmpty()) {
                _message.value = "❌ لا توجد عناصر للإنشاء"
                _isLoading.value = false
                return@launch
            }

            var dirsCreated = 0
            var filesCreated = 0
            var failed = 0
            val errors = mutableListOf<String>()

            withContext(Dispatchers.IO) {
                projectDir.mkdirs()

                files.filter { it.isDirectory }.forEach { dir ->
                    try {
                        StorageManager.createDirectory(projectDir, dir.path)
                        dirsCreated++
                    } catch (e: Exception) {
                        failed++
                        errors.add("مجلد ${dir.path}: ${e.message}")
                    }
                }

                files.filter { !it.isDirectory }.forEach { file ->
                    try {
                        StorageManager.writeFile(projectDir, file.path, file.content)
                        filesCreated++
                    } catch (e: Exception) {
                        failed++
                        errors.add("${file.path}: ${e.message}")
                    }
                }
            }

            _message.value = when {
                failed == 0 -> "✅ $dirsCreated مجلد + $filesCreated ملف\n📍 ${projectDir.absolutePath}"
                filesCreated + dirsCreated == 0 -> "❌ فشل الكل: ${errors.firstOrNull()}"
                else -> "⚠️ نجح: ${dirsCreated + filesCreated} | فشل: $failed"
            }

            _isLoading.value = false
        }
    }
}
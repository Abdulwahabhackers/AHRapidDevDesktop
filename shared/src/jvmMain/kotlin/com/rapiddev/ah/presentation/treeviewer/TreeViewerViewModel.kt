package com.rapiddev.ah.presentation.treeviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.utils.StorageManager
import com.rapiddev.ah.core.utils.TreeBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TreeViewerViewModel : ViewModel() {

    private val _treeText = MutableStateFlow("")
    val treeText: StateFlow<String> = _treeText

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _stats = MutableStateFlow("")
    val stats: StateFlow<String> = _stats

    fun clearMessage() { _message.value = null }

    fun clearTree() {
        _treeText.value = ""
        _stats.value = ""
    }

    fun generate(path: String, ignoreDirs: Boolean, showHidden: Boolean, maxDepth: Int) {
        val file = File(path)
        if (!file.exists() || !file.isDirectory) {
            _message.value = "المسار غير موجود أو ليس مجلداً"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val ignores = if (ignoreDirs) {
                        setOf(
                            ".git", ".gradle", ".idea", "build", "node_modules",
                            ".androidide", ".cxx", "__pycache__", ".dart_tool",
                            ".vscode", "dist", "out", "target", ".cache", ".kotlin"
                        )
                    } else emptySet()

                    val tree = TreeBuilder.buildTree(
                        root = file,
                        ignoreDirs = ignores,
                        maxDepth = maxDepth,
                        showHidden = showHidden
                    )

                    if (tree == null) Pair("", "")
                    else {
                        val counts = TreeBuilder.countEntries(tree)
                        Pair(
                            TreeBuilder.toTreeString(tree),
                            "${counts.first} مجلد، ${counts.second} ملف"
                        )
                    }
                }

                _treeText.value = result.first
                _stats.value = result.second

                _message.value = if (result.first.isEmpty()) "الشجرة فارغة" else "تم بناء الشجرة"
            } catch (e: Exception) {
                _message.value = "خطأ: " + e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveToFile(basePath: String, fileName: String) {
        val text = _treeText.value
        if (text.isEmpty()) {
            _message.value = "لا توجد شجرة لحفظها"
            return
        }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val base = File(basePath)
                    if (!base.exists() || !base.isDirectory) {
                        return@withContext "المجلد غير موجود"
                    }
                    try {
                        StorageManager.writeFile(base, fileName, text)
                        "تم الحفظ: $fileName"
                    } catch (e: Exception) {
                        "فشل الحفظ: " + e.message
                    }
                }
                _message.value = result
            } catch (e: Exception) {
                _message.value = "خطأ: " + e.message
            }
        }
    }
}
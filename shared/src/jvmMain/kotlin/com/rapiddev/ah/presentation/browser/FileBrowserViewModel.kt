package com.rapiddev.ah.presentation.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.utils.FileOperations
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class SortMode {
    NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC
}

class FileBrowserViewModel : ViewModel() {

    private val _currentPath = MutableStateFlow(PathHistory.getDefaultPath())
    val currentPath: StateFlow<String> = _currentPath

    private val _allItems = MutableStateFlow<List<FileItem>>(emptyList())
    private val _items = MutableStateFlow<List<FileItem>>(emptyList())
    val items: StateFlow<List<FileItem>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _showHidden = MutableStateFlow(false)
    val showHidden: StateFlow<Boolean> = _showHidden

    private val _foldersFirst = MutableStateFlow(true)
    val foldersFirst: StateFlow<Boolean> = _foldersFirst

    private val _sortMode = MutableStateFlow(SortMode.NAME_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode

    private val _textContent = MutableStateFlow<String?>(null)
    val textContent: StateFlow<String?> = _textContent

    private val _previewFileName = MutableStateFlow("")
    val previewFileName: StateFlow<String> = _previewFileName

    // ===== البحث =====
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // ===== التحديد المتعدد =====
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode

    private val _selectedPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedPaths: StateFlow<Set<String>> = _selectedPaths

    // ===== الإحصائيات =====
    private val _stats = MutableStateFlow("")
    val stats: StateFlow<String> = _stats

    fun clearMessage() { _message.value = null }

    fun clearPreview() {
        _textContent.value = null
        _previewFileName.value = ""
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
        applyFilters()
    }

    fun toggleSelectionMode() {
        _selectionMode.value = !_selectionMode.value
        if (!_selectionMode.value) _selectedPaths.value = emptySet()
    }

    fun toggleSelection(path: String) {
        val current = _selectedPaths.value.toMutableSet()
        if (current.contains(path)) current.remove(path) else current.add(path)
        _selectedPaths.value = current
    }

    fun selectAll() {
        _selectedPaths.value = _items.value.map { it.fullPath }.toSet()
    }

    fun clearSelection() {
        _selectedPaths.value = emptySet()
    }

    fun setShowHidden(value: Boolean) {
        _showHidden.value = value
        loadDirectory(_currentPath.value)
    }

    fun setFoldersFirst(value: Boolean) {
        _foldersFirst.value = value
        applyFilters()
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        applyFilters()
    }

    fun navigateTo(path: String) { loadDirectory(path) }

    fun navigateUp(): Boolean {
        val current = File(_currentPath.value)
        val parent = current.parentFile ?: return false
        loadDirectory(parent.absolutePath)
        return true
    }

    fun loadInitialPath() {
        val pending = com.rapiddev.ah.core.utils.SessionNavigationState.consumePath()
        val start = if (pending != null && pending.isNotBlank()) {
            PathHistory.addPath(pending)
            pending
        } else {
            PathHistory.getLastPath()
        }
        loadDirectory(start)
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val dir = File(path)
                    if (!dir.exists()) return@withContext Pair(false, "المجلد غير موجود")
                    if (!dir.isDirectory) return@withContext Pair(false, "المسار ليس مجلداً")

                    val children = dir.listFiles()
                        ?: return@withContext Pair(false, "لا يمكن قراءة المجلد")

                    val showHiddenValue = _showHidden.value

                    val items = children
                        .filter { f -> !(!showHiddenValue && f.name.startsWith(".")) }
                        .map { f ->
                            val ext = if (f.isDirectory) "" else {
                                val dot = f.name.lastIndexOf('.')
                                if (dot >= 0 && dot < f.name.length - 1)
                                    f.name.substring(dot + 1).lowercase(Locale.ROOT)
                                else ""
                            }
                            FileItem(
                                name = f.name,
                                fullPath = f.absolutePath,
                                isDirectory = f.isDirectory,
                                size = if (f.isDirectory) 0L else f.length(),
                                lastModified = f.lastModified(),
                                extension = ext
                            )
                        }

                    Pair(true, items)
                }

                if (result.first) {
                    @Suppress("UNCHECKED_CAST")
                    val list = result.second as List<FileItem>
                    _currentPath.value = path
                    _allItems.value = list
                    _selectedPaths.value = emptySet()
                    _searchQuery.value = ""
                    applyFilters()
                } else {
                    _message.value = result.second as String
                }
            } catch (e: Exception) {
                _message.value = "خطأ: " + e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyFilters() {
        val query = _searchQuery.value.trim().lowercase(Locale.ROOT)
        val foldersFirstValue = _foldersFirst.value
        val sortValue = _sortMode.value

        // فلترة البحث
        val filtered = if (query.isEmpty()) {
            _allItems.value
        } else {
            _allItems.value.filter {
                it.name.lowercase(Locale.ROOT).contains(query) ||
                it.extension.lowercase(Locale.ROOT).contains(query)
            }
        }

        // الفرز
        val sorted = filtered.sortedWith { a, b ->
            if (foldersFirstValue && a.isDirectory != b.isDirectory) {
                return@sortedWith if (a.isDirectory) -1 else 1
            }
            when (sortValue) {
                SortMode.NAME_ASC -> a.name.lowercase(Locale.ROOT).compareTo(b.name.lowercase(Locale.ROOT))
                SortMode.NAME_DESC -> b.name.lowercase(Locale.ROOT).compareTo(a.name.lowercase(Locale.ROOT))
                SortMode.SIZE_ASC -> a.size.compareTo(b.size)
                SortMode.SIZE_DESC -> b.size.compareTo(a.size)
                SortMode.DATE_ASC -> a.lastModified.compareTo(b.lastModified)
                SortMode.DATE_DESC -> b.lastModified.compareTo(a.lastModified)
            }
        }

        _items.value = sorted

        // الإحصائيات
        val dirCount = sorted.count { it.isDirectory }
        val fileCount = sorted.count { !it.isDirectory }
        val totalSize = sorted.filter { !it.isDirectory }.sumOf { it.size }
        _stats.value = buildString {
            if (query.isNotEmpty()) append("🔍 نتائج البحث: ")
            append("$dirCount مجلد • $fileCount ملف")
            if (totalSize > 0) append(" • ${formatSize(totalSize)}")
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }

    fun openFile(item: FileItem) {
        if (item.isDirectory) {
            loadDirectory(item.fullPath)
            return
        }
        if (item.size > 1024 * 1024 * 2) {
            _message.value = "الملف كبير (أكثر من 2MB)"
            return
        }
        if (item.extension in BINARY_PREVIEW_EXTENSIONS) {
            _message.value = "ملف ثنائي - لا يمكن معاينته"
            return
        }
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    File(item.fullPath).readText(Charsets.UTF_8)
                }
                _previewFileName.value = item.name
                _textContent.value = content
            } catch (e: Exception) {
                _message.value = "خطأ في القراءة: " + e.message
            }
        }
    }

    // ===== العمليات الفردية =====

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                FileOperations.createFolder(File(_currentPath.value), name)
            }
            result.fold(
                onSuccess = { _message.value = "تم إنشاء المجلد: $name"; loadDirectory(_currentPath.value) },
                onFailure = { _message.value = "خطأ: " + it.message }
            )
        }
    }

    fun createFile(name: String, content: String = "") {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                FileOperations.createFile(File(_currentPath.value), name, content)
            }
            result.fold(
                onSuccess = { _message.value = "تم إنشاء الملف: $name"; loadDirectory(_currentPath.value) },
                onFailure = { _message.value = "خطأ: " + it.message }
            )
        }
    }

    fun renameFile(item: FileItem, newName: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                FileOperations.rename(File(item.fullPath), newName)
            }
            result.fold(
                onSuccess = { _message.value = "تم التعديل"; loadDirectory(_currentPath.value) },
                onFailure = { _message.value = "خطأ: " + it.message }
            )
        }
    }

    fun deleteFile(item: FileItem) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                FileOperations.delete(File(item.fullPath))
            }
            result.fold(
                onSuccess = { _message.value = "تم الحذف"; loadDirectory(_currentPath.value) },
                onFailure = { _message.value = "خطأ: " + it.message }
            )
        }
    }

    fun copyFile(item: FileItem, targetDir: File) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                FileOperations.copy(File(item.fullPath), targetDir)
            }
            result.fold(
                onSuccess = {
                    _message.value = "تم النسخ إلى: " + targetDir.name
                    if (targetDir.absolutePath == _currentPath.value) loadDirectory(_currentPath.value)
                },
                onFailure = { _message.value = "خطأ: " + it.message }
            )
        }
    }

    fun moveFile(item: FileItem, targetDir: File) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                FileOperations.move(File(item.fullPath), targetDir)
            }
            result.fold(
                onSuccess = { _message.value = "تم النقل إلى: " + targetDir.name; loadDirectory(_currentPath.value) },
                onFailure = { _message.value = "خطأ: " + it.message }
            )
        }
    }

    // ===== العمليات الجماعية =====

    fun deleteSelected() {
        val paths = _selectedPaths.value
        if (paths.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            var success = 0
            var failed = 0

            withContext(Dispatchers.IO) {
                paths.forEach { path ->
                    val result = FileOperations.delete(File(path))
                    if (result.isSuccess) success++ else failed++
                }
            }

            _message.value = buildString {
                append("🗑️ حذف جماعي: ")
                append("✅ $success")
                if (failed > 0) append(" | ❌ $failed")
            }
            _selectedPaths.value = emptySet()
            _selectionMode.value = false
            _isLoading.value = false
            loadDirectory(_currentPath.value)
        }
    }

    fun copySelected(targetDir: File) {
        val paths = _selectedPaths.value
        if (paths.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            var success = 0
            var failed = 0

            withContext(Dispatchers.IO) {
                paths.forEach { path ->
                    val result = FileOperations.copy(File(path), targetDir)
                    if (result.isSuccess) success++ else failed++
                }
            }

            _message.value = buildString {
                append("📋 نسخ إلى ${targetDir.name}: ")
                append("✅ $success")
                if (failed > 0) append(" | ❌ $failed")
            }
            _selectedPaths.value = emptySet()
            _selectionMode.value = false
            _isLoading.value = false
            if (targetDir.absolutePath == _currentPath.value) loadDirectory(_currentPath.value)
        }
    }

    fun moveSelected(targetDir: File) {
        val paths = _selectedPaths.value
        if (paths.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            var success = 0
            var failed = 0

            withContext(Dispatchers.IO) {
                paths.forEach { path ->
                    val result = FileOperations.move(File(path), targetDir)
                    if (result.isSuccess) success++ else failed++
                }
            }

            _message.value = buildString {
                append("📦 نقل إلى ${targetDir.name}: ")
                append("✅ $success")
                if (failed > 0) append(" | ❌ $failed")
            }
            _selectedPaths.value = emptySet()
            _selectionMode.value = false
            _isLoading.value = false
            loadDirectory(_currentPath.value)
        }
    }

    companion object {
        private val BINARY_PREVIEW_EXTENSIONS = setOf(
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "ico",
            "ttf", "otf", "woff", "woff2",
            "mp3", "wav", "ogg", "mp4", "avi", "mkv",
            "zip", "rar", "7z", "tar", "gz",
            "exe", "dll", "so", "apk", "aar", "jar", "class", "dex",
            "db", "sqlite", "pdf", "doc", "docx", "xls", "xlsx",
            "psd", "ai", "pak", "dat", "sav", "bin"
        )
    }
}
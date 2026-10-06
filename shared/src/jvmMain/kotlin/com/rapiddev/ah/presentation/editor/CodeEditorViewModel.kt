package com.rapiddev.ah.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CodeEditorViewModel : ViewModel() {

    private val _filePath = MutableStateFlow("")
    val filePath: StateFlow<String> = _filePath

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _originalContent = MutableStateFlow("")
    private val _language = MutableStateFlow(CodeLanguage.PLAIN)
    val language: StateFlow<CodeLanguage> = _language

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges

    private val _readOnly = MutableStateFlow(false)
    val readOnly: StateFlow<Boolean> = _readOnly

    fun clearMessage() { _message.value = null }

    fun load(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val file = File(path)

                if (!file.exists()) {
                    _message.value = "❌ الملف غير موجود"
                    return@launch
                }

                if (file.length() > 2 * 1024 * 1024) {
                    _message.value = "❌ الملف كبير جداً (أكثر من 2MB)"
                    _readOnly.value = true
                } else {
                    _readOnly.value = false
                }

                val text = withContext(Dispatchers.IO) {
                    try {
                        file.readText(Charsets.UTF_8)
                    } catch (e: Exception) {
                        ""
                    }
                }

                val ext = file.extension.lowercase()
                _filePath.value = file.absolutePath
                _fileName.value = file.name
                _content.value = text
                _originalContent.value = text
                _language.value = CodeLanguage.fromExtension(ext)
                _hasUnsavedChanges.value = false

            } catch (e: Exception) {
                _message.value = "❌ خطأ في القراءة: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateContent(newContent: String) {
        if (_readOnly.value) return
        _content.value = newContent
        _hasUnsavedChanges.value = newContent != _originalContent.value
    }

    fun save() {
        val path = _filePath.value
        if (path.isBlank()) {
            _message.value = "❌ لا يوجد ملف"
            return
        }
        if (_readOnly.value) {
            _message.value = "❌ القراءة فقط"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    File(path).writeText(_content.value, Charsets.UTF_8)
                }
                _originalContent.value = _content.value
                _hasUnsavedChanges.value = false
                _message.value = "✅ تم الحفظ"
            } catch (e: Exception) {
                _message.value = "❌ فشل الحفظ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun revert() {
        _content.value = _originalContent.value
        _hasUnsavedChanges.value = false
        _message.value = "تم التراجع"
    }

    fun changeLanguage(lang: CodeLanguage) {
        _language.value = lang
    }

    fun toggleReadOnly() {
        _readOnly.value = !_readOnly.value
    }
}
package com.rapiddev.ah.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapiddev.ah.core.utils.SessionManager
import com.rapiddev.ah.data.model.Session
import com.rapiddev.ah.data.model.SessionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class SessionSort {
    RECENT,
    NAME,
    TYPE
}

class SessionsViewModel : ViewModel() {

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sort = MutableStateFlow(SessionSort.RECENT)
    val sort: StateFlow<SessionSort> = _sort

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() { _message.value = null }

    fun setSearch(q: String) { _searchQuery.value = q }

    fun setSort(s: SessionSort) { _sort.value = s }

    fun load() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { SessionManager.loadAll() }
            _sessions.value = list
        }
    }

    fun add(name: String, path: String, type: SessionType, note: String = "") {
        viewModelScope.launch {
            val session = Session(
                id = SessionManager.generateId(),
                name = name,
                path = path,
                type = type,
                note = note
            )
            val ok = withContext(Dispatchers.IO) {
                SessionManager.addSession(session)
            }
            _message.value = if (ok) "تمت الإضافة" else "فشل الحفظ"
            load()
        }
    }

    fun rename(session: Session, newName: String) {
        viewModelScope.launch {
            val updated = session.copy(name = newName)
            withContext(Dispatchers.IO) { SessionManager.updateSession(updated) }
            load()
        }
    }

    fun updateNote(session: Session, newNote: String) {
        viewModelScope.launch {
            val updated = session.copy(note = newNote)
            withContext(Dispatchers.IO) { SessionManager.updateSession(updated) }
            load()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                SessionManager.deleteSession(id)
            }
            _message.value = if (ok) "تم الحذف" else "فشل الحذف"
            load()
        }
    }

    fun touch(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { SessionManager.touchSession(id) }
            load()
        }
    }

    fun filtered(): List<Session> {
        val query = _searchQuery.value.trim().lowercase(Locale.ROOT)
        val list = _sessions.value

        val filtered = if (query.isEmpty()) list else {
            list.filter {
                it.name.lowercase(Locale.ROOT).contains(query) ||
                    it.path.lowercase(Locale.ROOT).contains(query)
            }
        }

        return when (_sort.value) {
            SessionSort.RECENT -> filtered.sortedByDescending { it.lastOpened }
            SessionSort.NAME -> filtered.sortedBy { it.name.lowercase(Locale.ROOT) }
            SessionSort.TYPE -> filtered.sortedBy { it.type.displayName }
        }
    }

    fun existsDirectory(path: String): Boolean {
        return try {
            File(path).exists() && File(path).isDirectory
        } catch (e: Exception) {
            false
        }
    }
    /**
     * يتحقق إذا كان المسار مسجلاً في جلسة سابقة.
     * @return الجلسة الموجودة أو null
     */
    fun findExistingByPath(path: String): Session? {
        if (path.isBlank()) return null
        return _sessions.value.firstOrNull {
            it.path.equals(path, ignoreCase = true)
        }
    }
    /**
     * يبحث عن جلسة موجودة بنفس المسار.
     */
}
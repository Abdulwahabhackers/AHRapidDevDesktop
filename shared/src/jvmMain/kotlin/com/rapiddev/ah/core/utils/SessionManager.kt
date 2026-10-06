package com.rapiddev.ah.core.utils

import com.rapiddev.ah.data.model.Session
import com.rapiddev.ah.data.model.SessionType
import java.io.File
import java.util.UUID

object SessionManager {

    private const val FILE_NAME = "sessions.json"

    private fun getFile(): File {
        val home = System.getProperty("user.home") ?: "."
        val appDir = File(home, ".ahrapiddev")
        if (!appDir.exists()) appDir.mkdirs()
        return File(appDir, FILE_NAME)
    }

    fun loadAll(): List<Session> {
        val file = getFile()
        if (!file.exists()) return emptyList()

        return try {
            val text = file.readText(Charsets.UTF_8)
            if (text.isBlank()) return emptyList()

            val arr = org.json.JSONArray(text)
            val list = mutableListOf<Session>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeName = obj.optString("type", "CUSTOM")
                val type = try {
                    SessionType.valueOf(typeName)
                } catch (e: Exception) {
                    SessionType.CUSTOM
                }
                list.add(
                    Session(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "بدون اسم"),
                        path = obj.optString("path", ""),
                        type = type,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        lastOpened = obj.optLong("lastOpened", System.currentTimeMillis()),
                        note = obj.optString("note", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(sessions: List<Session>): Boolean {
        return try {
            val arr = org.json.JSONArray()
            for (s in sessions) {
                val obj = org.json.JSONObject()
                obj.put("id", s.id)
                obj.put("name", s.name)
                obj.put("path", s.path)
                obj.put("type", s.type.name)
                obj.put("createdAt", s.createdAt)
                obj.put("lastOpened", s.lastOpened)
                obj.put("note", s.note)
                arr.put(obj)
            }
            getFile().writeText(arr.toString(), Charsets.UTF_8)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addSession(session: Session): Boolean {
        val list = loadAll().toMutableList()
        list.removeAll { it.path == session.path }
        list.add(0, session)
        return saveAll(list)
    }

    fun updateSession(session: Session): Boolean {
        val list = loadAll().toMutableList()
        val idx = list.indexOfFirst { it.id == session.id }
        if (idx < 0) return false
        list[idx] = session
        return saveAll(list)
    }

    fun deleteSession(id: String): Boolean {
        val list = loadAll().toMutableList()
        list.removeAll { it.id == id }
        return saveAll(list)
    }

    fun touchSession(id: String): Boolean {
        val list = loadAll().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx < 0) return false
        list[idx] = list[idx].copy(lastOpened = System.currentTimeMillis())
        return saveAll(list)
    }

    fun existsByPath(path: String): Session? {
        return loadAll().firstOrNull { it.path == path }
    }

    fun generateId(): String = UUID.randomUUID().toString()
}
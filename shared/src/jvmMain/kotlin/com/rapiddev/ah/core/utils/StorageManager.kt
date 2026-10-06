package com.rapiddev.ah.core.utils

import java.io.File

object StorageManager {

    /**
     * على سطح المكتب لا توجد صلاحيات — دائماً true
     */
    fun hasAllFilesAccess(): Boolean = true

    /**
     * على سطح المكتب لا توجد نوافذ صلاحيات — دالة فارغة
     */
    fun requestAllFilesAccess() {
        // لا شيء
    }

    fun createDirectory(baseDir: File, relativePath: String): File {
        val parts = relativePath.split('/', '\\').filter { it.isNotBlank() }
        if (parts.isEmpty()) return baseDir

        var current = baseDir
        for (part in parts) {
            current = File(current, part)
            if (!current.exists()) {
                current.mkdirs()
            }
        }
        return current
    }

    fun writeFile(baseDir: File, relativePath: String, content: String): File {
        val parts = relativePath.split('/', '\\').filter { it.isNotBlank() }
        if (parts.isEmpty()) throw Exception("مسار فارغ")

        var current = baseDir
        for (i in 0 until parts.size - 1) {
            current = File(current, parts[i])
            if (!current.exists()) {
                if (!current.mkdirs()) throw Exception("فشل إنشاء: ${current.absolutePath}")
            }
        }

        val file = File(current, parts.last())
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()
        file.writeText(content, Charsets.UTF_8)
        return file
    }
}
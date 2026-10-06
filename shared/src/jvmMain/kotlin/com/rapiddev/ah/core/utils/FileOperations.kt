package com.rapiddev.ah.core.utils

import java.io.File

object FileOperations {

    fun createFolder(parent: File, name: String): Result<File> {
        return try {
            val dir = File(parent, name)
            if (dir.exists()) {
                Result.failure(Exception("المجلد موجود مسبقاً"))
            } else {
                if (dir.mkdirs()) Result.success(dir)
                else Result.failure(Exception("فشل إنشاء المجلد"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createFile(parent: File, name: String, content: String = ""): Result<File> {
        return try {
            val file = File(parent, name)
            if (file.exists()) {
                Result.failure(Exception("الملف موجود مسبقاً"))
            } else {
                file.parentFile?.mkdirs()
                file.writeText(content, Charsets.UTF_8)
                Result.success(file)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun rename(file: File, newName: String): Result<File> {
        return try {
            val target = File(file.parentFile, newName)
            if (target.exists()) {
                Result.failure(Exception("الاسم مستخدم مسبقاً"))
            } else {
                if (file.renameTo(target)) Result.success(target)
                else Result.failure(Exception("فشل إعادة التسمية"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun delete(file: File): Result<Unit> {
        return try {
            if (deleteRecursive(file)) Result.success(Unit)
            else Result.failure(Exception("فشل الحذف"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun deleteRecursive(file: File): Boolean {
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    if (!deleteRecursive(child)) return false
                }
            }
        }
        return file.delete()
    }

    fun copy(source: File, targetDir: File, newName: String? = null): Result<File> {
        return try {
            val name = newName ?: source.name
            var target = File(targetDir, name)

            if (target.exists()) {
                var counter = 1
                val baseName = name.substringBeforeLast('.', name)
                val ext = if (name.contains('.')) "." + name.substringAfterLast('.') else ""
                while (target.exists()) {
                    target = File(targetDir, "${baseName}_copy$counter$ext")
                    counter++
                }
            }

            if (source.isDirectory) {
                copyDirectory(source, target)
            } else {
                source.copyTo(target, overwrite = false)
            }
            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyDirectory(source: File, target: File) {
        if (!target.mkdirs()) {
            throw Exception("فشل إنشاء: " + target.absolutePath)
        }
        val children = source.listFiles() ?: return
        for (child in children) {
            val targetChild = File(target, child.name)
            if (child.isDirectory) {
                copyDirectory(child, targetChild)
            } else {
                child.copyTo(targetChild, overwrite = false)
            }
        }
    }

    fun move(source: File, targetDir: File): Result<File> {
        return try {
            val target = File(targetDir, source.name)
            if (target.exists()) {
                Result.failure(Exception("الملف موجود في الهدف"))
            } else {
                if (source.renameTo(target)) {
                    Result.success(target)
                } else {
                    val copied = copy(source, targetDir)
                    if (copied.isSuccess) {
                        delete(source)
                        Result.success(copied.getOrThrow())
                    } else {
                        Result.failure(Exception("فشل النقل"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUniqueName(dir: File, baseName: String): String {
        var name = baseName
        var counter = 1
        val base = baseName.substringBeforeLast('.', baseName)
        val ext = if (baseName.contains('.')) "." + baseName.substringAfterLast('.') else ""
        while (File(dir, name).exists()) {
            name = "${base}_$counter$ext"
            counter++
        }
        return name
    }
}
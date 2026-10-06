package com.rapiddev.ah.core.utils

import java.io.File
import java.util.prefs.Preferences

object ShortcutManager {

    private const val KEY_ASKED = "shortcut_asked"
    private val prefs: Preferences by lazy {
        Preferences.userRoot().node("com/rapiddev/ah/shortcut")
    }

    fun hasAsked(): Boolean = prefs.getBoolean(KEY_ASKED, false)

    fun markAsked() {
        prefs.putBoolean(KEY_ASKED, true)
    }

    private fun getDesktopPath(): File? {
        val candidates = mutableListOf<File>()

        val userProfile = System.getenv("USERPROFILE")
        if (userProfile != null) {
            candidates.add(File(userProfile, "Desktop"))
            candidates.add(File(userProfile, "OneDrive\\Desktop"))
            candidates.add(File(userProfile, "OneDrive\\سطح المكتب"))
        }

        val home = System.getProperty("user.home")
        if (home != null) {
            candidates.add(File(home, "Desktop"))
            candidates.add(File(home, "سطح المكتب"))
        }

        try {
            val proc = ProcessBuilder(
                "powershell", "-NoProfile", "-Command",
                "[Environment]::GetFolderPath('Desktop')"
            ).redirectErrorStream(true).start()

            val output = proc.inputStream.bufferedReader().readText().trim()
            proc.waitFor()
            if (output.isNotBlank()) {
                val f = File(output)
                if (f.exists()) candidates.add(0, f)
            }
        } catch (_: Exception) {}

        return candidates.firstOrNull { it.exists() && it.isDirectory }
    }

    /**
     * يجد مسار مجلد التطبيق الحقيقي.
     * - إذا كنا في .exe حقيقي: ProcessHandle.command = مسار الـ exe
     * - إذا كنا في IDE أو java -jar: نستخدم java.class.path
     */
    private fun findAppDirectory(): File? {
        // 1) محاولة ProcessHandle
        try {
            val cmd = ProcessHandle.current().info().command().orElse(null)
            if (cmd != null) {
                val cmdFile = File(cmd)
                val cmdName = cmdFile.name.lowercase()
                // رفض java.exe/javaw.exe — ليست تطبيقنا
                if (!cmdName.startsWith("java")) {
                    val parent = cmdFile.parentFile
                    if (parent != null && File(parent, "AH RapidDev.exe").exists()) {
                        return parent
                    }
                    // إذا كان الـ exe داخل مجلد التطبيق
                    if (parent != null) return parent
                }
            }
        } catch (_: Exception) {}

        // 2) محاولة class path
        try {
            val cp = System.getProperty("java.class.path") ?: ""
            val entries = cp.split(File.pathSeparator)
            for (e in entries) {
                val f = File(e)
                val dir = if (f.isFile) f.parentFile else f
                if (dir != null && File(dir, "AH RapidDev.exe").exists()) {
                    return dir
                }
            }
        } catch (_: Exception) {}

        // 3) محاولة user.dir
        try {
            val userDir = File(System.getProperty("user.dir") ?: ".")
            if (File(userDir, "AH RapidDev.exe").exists()) return userDir
            val parent = userDir.parentFile
            if (parent != null && File(parent, "AH RapidDev.exe").exists()) return parent
        } catch (_: Exception) {}

        return null
    }

    fun createDesktopShortcut(): Boolean {
        return try {
            val desktop = getDesktopPath() ?: return false

            val appDir = findAppDirectory()
            if (appDir == null) {
                AppLogger.e("ShortcutManager", "Could not find app directory")
                return false
            }

            val exe = File(appDir, "AH RapidDev.exe")
            if (!exe.exists()) {
                AppLogger.e("ShortcutManager", "AH RapidDev.exe not found in $appDir")
                return false
            }

            val icoFile = File(appDir, "icon.ico")
            val iconLocation = if (icoFile.exists()) icoFile.absolutePath else "${exe.absolutePath},0"

            val lnkPath = File(desktop, "AH RapidDev.lnk").absolutePath

            // إزالة الاختصار القديم إن وجد
            File(lnkPath).takeIf { it.exists() }?.delete()

            val vbs = """
                Set oWS = WScript.CreateObject("WScript.Shell")
                sLinkFile = "$lnkPath"
                Set oLink = oWS.CreateShortcut(sLinkFile)
                oLink.TargetPath = "${exe.absolutePath}"
                oLink.WorkingDirectory = "${appDir.absolutePath}"
                oLink.Description = "AH RapidDev - Desktop Edition"
                oLink.IconLocation = "$iconLocation"
                oLink.Save
            """.trimIndent()

            val vbsFile = File.createTempFile("create_shortcut_", ".vbs")
            vbsFile.writeText(vbs, Charsets.UTF_8)

            val proc = ProcessBuilder("wscript.exe", vbsFile.absolutePath)
                .redirectErrorStream(true)
                .start()
            val exitCode = proc.waitFor()

            vbsFile.delete()

            exitCode == 0 && File(lnkPath).exists()
        } catch (e: Exception) {
            AppLogger.e("ShortcutManager", "createDesktopShortcut failed", e)
            false
        }
    }

    fun shortcutExists(): Boolean {
        val desktop = getDesktopPath() ?: return false
        return File(desktop, "AH RapidDev.lnk").exists()
    }

    fun deleteShortcut(): Boolean {
        return try {
            val desktop = getDesktopPath() ?: return false
            val lnk = File(desktop, "AH RapidDev.lnk")
            if (lnk.exists()) lnk.delete() else false
        } catch (_: Exception) {
            false
        }
    }
}
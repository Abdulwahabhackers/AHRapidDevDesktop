package com.rapiddev.ah.core.utils

import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * نافذة اختيار مجلد أصلية من النظام (JFileChooser).
 * تعمل بشكل async — لا تجمّد الواجهة.
 */
object FolderPicker {

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (_: Exception) {}
    }

    fun pickFolder(
        title: String = "اختر مجلداً",
        initialDir: File? = null,
        onResult: (File?) -> Unit
    ) {
        SwingUtilities.invokeLater {
            try {
                val chooser = JFileChooser()
                chooser.dialogTitle = title
                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                chooser.isMultiSelectionEnabled = false

                val startDir = when {
                    initialDir != null && initialDir.exists() && initialDir.isDirectory -> initialDir
                    else -> {
                        val home = File(System.getProperty("user.home") ?: ".")
                        val docs = File(home, "Documents")
                        if (docs.exists()) docs else home
                    }
                }
                chooser.currentDirectory = startDir

                val resultCode = chooser.showOpenDialog(null)
                val result = if (resultCode == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
                onResult(result)
            } catch (e: Exception) {
                AppLogger.e("FolderPicker", "pickFolder failed", e)
                onResult(null)
            }
        }
    }
}
package com.rapiddev.ah.core.utils

import com.rapiddev.ah.data.model.ExportOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectExporter {

    companion object {
        private val IGNORED_DIRS = setOf(
            "build", ".gradle", ".idea", ".kotlin", ".cxx", "node_modules",
            ".androidide", "intermediates", "outputs", "tmp", "generated",
            "caches", "snapshot", "logs", ".git", "captures",
            ".externalNativeBuild", "debug", "release", ".fleet"
        )

        private val TEXT_EXTENSIONS = setOf(
            ".kt", ".java", ".xml", ".gradle", ".kts", ".properties", ".pro",
            ".md", ".txt", ".json", ".yml", ".yaml", ".html", ".css", ".js",
            ".ts", ".tsx", ".jsx", ".sh", ".py", ".c", ".cpp", ".h", ".hpp",
            ".cs", ".rb", ".php", ".go", ".rs", ".swift", ".dart", ".lua",
            ".sql", ".conf", ".ini", ".toml", ".env", ".gitignore", ".svg",
            ".cmake", ".mk", ".version"
        )
    }

    data class Progress(
        val current: Int,
        val total: Int,
        val currentPath: String
    )

    data class Result(
        val success: Boolean,
        val totalFolders: Int,
        val totalFiles: Int,
        val outputDir: File?,
        val error: String? = null
    )

    fun export(
        sourcePath: String,
        options: ExportOptions,
        onProgress: (Progress) -> Unit = {}
    ): Result {
        return try {
            val sourceDir = File(sourcePath)
            if (!sourceDir.exists() || !sourceDir.isDirectory) {
                return Result(false, 0, 0, null, "المسار غير موجود")
            }

            val outputDir = File(sourceDir, options.outputFolderName)
            if (outputDir.exists()) {
                outputDir.deleteRecursively()
            }
            outputDir.mkdirs()

            val allFolders = mutableListOf<File>()
            collectFolders(sourceDir, outputDir, options, 0, allFolders)

            val total = allFolders.size
            var totalFiles = 0

            if (options.splitByFolder) {
                allFolders.forEachIndexed { index, folder ->
                    onProgress(Progress(index + 1, total, folder.absolutePath))
                    val fileCount = exportFolderToTxt(
                        folder = folder,
                        sourceRoot = sourceDir,
                        outputDir = outputDir,
                        options = options
                    )
                    totalFiles += fileCount
                }
            } else {
                onProgress(Progress(1, 1, sourceDir.absolutePath))
                val fileCount = exportAsSingleFile(
                    sourceDir = sourceDir,
                    outputDir = outputDir,
                    options = options,
                    folders = allFolders
                )
                totalFiles += fileCount
            }

            if (options.includeIndex) {
                writeIndexFile(sourceDir, outputDir, allFolders)
            }

            if (options.includePrompt) {
                writePromptFile(outputDir, sourceDir.name)
            }

            Result(true, total, totalFiles, outputDir)
        } catch (e: Exception) {
            Result(false, 0, 0, null, e.message ?: "خطأ غير معروف")
        }
    }

    private fun collectFolders(
        dir: File,
        outputDir: File,
        options: ExportOptions,
        currentDepth: Int,
        out: MutableList<File>
    ) {
        if (options.maxDepth > 0 && currentDepth > options.maxDepth) return
        if (dir.absolutePath.startsWith(outputDir.absolutePath)) return

        out.add(dir)

        val children = dir.listFiles() ?: return
        children.filter { it.isDirectory }
            .filter { child ->
                !(options.ignoreBuildDirs && child.name in IGNORED_DIRS) &&
                !child.name.startsWith(".")
            }
            .sortedBy { it.name.lowercase() }
            .forEach { collectFolders(it, outputDir, options, currentDepth + 1, out) }
    }

    private fun exportFolderToTxt(
        folder: File,
        sourceRoot: File,
        outputDir: File,
        options: ExportOptions
    ): Int {
        val relPath = folder.absolutePath
            .removePrefix(sourceRoot.absolutePath)
            .trimStart('/')
            .ifEmpty { "ROOT" }

        val fileName = sanitizeName(relPath) + ".txt"
        val txtFile = File(outputDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("=".repeat(70))
        sb.appendLine("\uD83D\uDCC1 المجلد: $relPath")
        sb.appendLine("\uD83D\uDCC2 المسار: ${folder.absolutePath}")
        sb.appendLine("=".repeat(70))
        sb.appendLine()

        val subDirs = folder.listFiles()?.filter { it.isDirectory }
            ?.filter {
                !(options.ignoreBuildDirs && it.name in IGNORED_DIRS) &&
                !it.name.startsWith(".")
            }
            ?.sortedBy { it.name.lowercase() } ?: emptyList()

        sb.appendLine("\uD83D\uDCC2 المجلدات الفرعية:")
        if (subDirs.isEmpty()) {
            sb.appendLine("   (لا يوجد)")
        } else {
            subDirs.forEach { sb.appendLine("   \u2022 ${it.name}/") }
        }
        sb.appendLine()

        val files = folder.listFiles()?.filter { it.isFile }
            ?.sortedBy { it.name.lowercase() } ?: emptyList()

        sb.appendLine("\uD83D\uDCC4 الملفات:")
        if (files.isEmpty()) {
            sb.appendLine("   (لا يوجد)")
        } else {
            files.forEach { sb.appendLine("   \u2022 ${it.name}") }
        }
        sb.appendLine()
        sb.appendLine("=".repeat(70))
        sb.appendLine()

        if (options.includeCode) {
            val textFiles = files.filter { isTextFile(it.name) }
            if (textFiles.isNotEmpty()) {
                sb.appendLine("\uD83D\uDCDD محتوى الملفات النصية:")
                sb.appendLine()
                textFiles.forEach { file ->
                    sb.appendLine("\u2500".repeat(60))
                    sb.appendLine("\uD83D\uDCC4 الملف: ${file.name}")
                    sb.appendLine("\uD83D\uDCCD المسار: ${file.absolutePath}")
                    sb.appendLine("\u2500".repeat(60))
                    try {
                        val content = file.readText(Charsets.UTF_8)
                        sb.appendLine(content.ifBlank { "(ملف فارغ)" })
                    } catch (e: Exception) {
                        sb.appendLine("(خطأ في القراءة: ${e.message})")
                    }
                    sb.appendLine()
                    sb.appendLine("\u2500".repeat(60))
                    sb.appendLine()
                }
            }
        }

        txtFile.writeText(sb.toString(), Charsets.UTF_8)
        return files.size
    }

    private fun exportAsSingleFile(
        sourceDir: File,
        outputDir: File,
        options: ExportOptions,
        folders: List<File>
    ): Int {
        val outputFile = File(outputDir, "${sourceDir.name}_FULL.txt")
        val sb = StringBuilder()
        var totalFiles = 0

        sb.appendLine("=".repeat(70))
        sb.appendLine("\uD83D\uDCE6 المشروع الكامل: ${sourceDir.name}")
        sb.appendLine("\uD83D\uDCC2 المسار: ${sourceDir.absolutePath}")
        sb.appendLine("\uD83D\uDD50 التاريخ: ${getCurrentDateTime()}")
        sb.appendLine("=".repeat(70))
        sb.appendLine()

        folders.forEach { folder ->
            val relPath = folder.absolutePath
                .removePrefix(sourceDir.absolutePath)
                .trimStart('/')
                .ifEmpty { "ROOT" }

            sb.appendLine()
            sb.appendLine("\u2588".repeat(70))
            sb.appendLine("\uD83D\uDCC1 $relPath/")
            sb.appendLine("\u2588".repeat(70))
            sb.appendLine()

            val files = folder.listFiles()?.filter { it.isFile }
                ?.sortedBy { it.name.lowercase() } ?: emptyList()

            if (options.includeCode) {
                files.filter { isTextFile(it.name) }.forEach { file ->
                    sb.appendLine("\u2500".repeat(60))
                    sb.appendLine("\uD83D\uDCC4 ${file.absolutePath}")
                    sb.appendLine("\u2500".repeat(60))
                    try {
                        sb.appendLine(file.readText(Charsets.UTF_8).ifBlank { "(فارغ)" })
                    } catch (e: Exception) {
                        sb.appendLine("(خطأ: ${e.message})")
                    }
                    sb.appendLine()
                }
                totalFiles += files.size
            }
        }

        outputFile.writeText(sb.toString(), Charsets.UTF_8)
        return totalFiles
    }

    private fun writeIndexFile(
        sourceDir: File,
        outputDir: File,
        folders: List<File>
    ) {
        val indexFile = File(outputDir, "_INDEX.txt")
        val sb = StringBuilder()

        sb.appendLine("=".repeat(70))
        sb.appendLine("\uD83D\uDCE6 فهرس المشروع: ${sourceDir.name}")
        sb.appendLine("=".repeat(70))
        sb.appendLine("\uD83D\uDCC2 المسار الأصلي: ${sourceDir.absolutePath}")
        sb.appendLine("\uD83D\uDD50 تاريخ التصدير: ${getCurrentDateTime()}")
        sb.appendLine("\uD83D\uDCC1 عدد المجلدات: ${folders.size}")
        sb.appendLine()

        sb.appendLine("=".repeat(70))
        sb.appendLine("\uD83D\uDCCB قائمة الملفات المُنتجة")
        sb.appendLine("=".repeat(70))
        sb.appendLine()

        folders.forEach { folder ->
            val relPath = folder.absolutePath
                .removePrefix(sourceDir.absolutePath)
                .trimStart('/')
                .ifEmpty { "ROOT" }

            val fileName = sanitizeName(relPath) + ".txt"
            val fileCount = folder.listFiles()?.count { it.isFile } ?: 0

            sb.appendLine("\uD83D\uDCC1 $relPath/  \u2192  $fileName  ($fileCount ملف)")
        }

        sb.appendLine()
        sb.appendLine("=".repeat(70))
        sb.appendLine("\uD83D\uDCA1 كيفية الاستخدام:")
        sb.appendLine("=".repeat(70))
        sb.appendLine("1. افتح أي ملف .txt لرؤية محتوى مجلد معين")
        sb.appendLine("2. ابدأ بـ _PROMPT.txt للحصول على برومبت جاهز")
        sb.appendLine("3. انسخ محتوى الملفات إلى AI لفهم المشروع")
        sb.appendLine("=".repeat(70))

        indexFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    private fun writePromptFile(outputDir: File, projectName: String) {
        val promptFile = File(outputDir, "_PROMPT.txt")
        val sb = StringBuilder()

        sb.appendLine("\u2554" + "\u2550".repeat(68) + "\u2557")
        sb.appendLine("\u2551  \uD83E\uDD16 برومبت جاهز للذكاء الاصطناعي" + " ".repeat(34) + "\u2551")
        sb.appendLine("\u255A" + "\u2550".repeat(68) + "\u255D")
        sb.appendLine()
        sb.appendLine("انسخ النص التالي وأرسله إلى DeepSeek أو ChatGPT:")
        sb.appendLine()
        sb.appendLine("\u2550".repeat(70))
        sb.appendLine()
        sb.appendLine("لدي مشروع اسمه: $projectName")
        sb.appendLine()
        sb.appendLine("سأرفق لك محتوى المشروع كاملاً (ملفات .txt).")
        sb.appendLine("كل ملف .txt يمثل مجلداً في المشروع.")
        sb.appendLine()
        sb.appendLine("اطلب مني:")
        sb.appendLine("1. فهم بنية المشروع")
        sb.appendLine("2. تعديل ميزة معينة")
        sb.appendLine("3. إضافة ميزة جديدة")
        sb.appendLine("4. إصلاح خطأ")
        sb.appendLine()
        sb.appendLine("قواعد:")
        sb.appendLine("- احتفظ بنفس البنية عند إعادة الأكواد")
        sb.appendLine("- اذكر المسار الكامل لكل ملف")
        sb.appendLine("- لا تختصر الأكواد (اكتبها كاملة)")
        sb.appendLine()
        sb.appendLine("ابدأ بتحليل البنية الآن.")
        sb.appendLine()
        sb.appendLine("\u2550".repeat(70))
        sb.appendLine()
        sb.appendLine("\uD83D\uDCA1 نصائح:")
        sb.appendLine("\u2022 ارفع الملفات واحداً واحداً أو كـ ZIP")
        sb.appendLine("\u2022 ابدأ بـ _INDEX.txt")
        sb.appendLine("\u2022 إذا كان المشروع كبيراً ارفع مجلداً مجلداً")
        sb.appendLine()
        sb.appendLine("=".repeat(70))

        promptFile.writeText(sb.toString(), Charsets.UTF_8)
    }

    private fun isTextFile(filename: String): Boolean {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ".$ext" in TEXT_EXTENSIONS
    }

    private fun sanitizeName(name: String): String {
        return name
            .replace(Regex("[^\\w\\s\\-.]"), "_")
            .replace(Regex("\\s+"), "_")
            .take(80)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return sdf.format(Date())
    }
}
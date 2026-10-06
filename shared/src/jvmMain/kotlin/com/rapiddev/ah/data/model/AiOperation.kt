package com.rapiddev.ah.data.model

sealed class AiOperation {

    abstract val targetPath: String
    abstract val description: String

    data class CreateFile(
        override val targetPath: String,
        val content: String
    ) : AiOperation() {
        override val description: String
            get() = "\uD83D\uDCC4 إنشاء ملف: $targetPath"
    }

    data class ReplaceFile(
        override val targetPath: String,
        val content: String
    ) : AiOperation() {
        override val description: String
            get() = "\uD83D\uDD04 استبدال كامل: $targetPath"
    }

    data class DeleteFile(
        override val targetPath: String
    ) : AiOperation() {
        override val description: String
            get() = "\uD83D\uDDD1\uFE0F حذف ملف: $targetPath"
    }

    data class DeleteFolder(
        override val targetPath: String
    ) : AiOperation() {
        override val description: String
            get() = "\uD83D\uDDD1\uFE0F حذف مجلد: $targetPath"
    }

    data class AddImport(
        override val targetPath: String,
        val importLine: String
    ) : AiOperation() {
        override val description: String
            get() = "\u2795 إضافة استيراد إلى: $targetPath"
    }

    data class EnsureImport(
        override val targetPath: String,
        val importLine: String
    ) : AiOperation() {
        override val description: String
            get() = "\u2713 تأكيد استيراد في: $targetPath"
    }

    data class ReplaceCode(
        override val targetPath: String,
        val oldCode: String,
        val newCode: String
    ) : AiOperation() {
        override val description: String
            get() = "\uD83D\uDD01 استبدال كود في: $targetPath"
    }

    data class AddCode(
        override val targetPath: String,
        val code: String,
        val position: InsertPosition
    ) : AiOperation() {
        override val description: String
            get() = "\u2795 إضافة كود في: $targetPath"
    }

    data class DeleteLine(
        override val targetPath: String,
        val lineContent: String
    ) : AiOperation() {
        override val description: String
            get() = "\u2702\uFE0F حذف سطر من: $targetPath"
    }

    data class DeleteFunction(
        override val targetPath: String,
        val functionName: String
    ) : AiOperation() {
        override val description: String
            get() = "\u2702\uFE0F حذف دالة $functionName من: $targetPath"
    }

    enum class InsertPosition {
        AFTER_FILE_HEADER,
        BEFORE_LAST_BRACE,
        END_OF_FILE,
        AFTER_SPECIFIC
    }
}

data class OperationResult(
    val operation: AiOperation,
    val success: Boolean,
    val error: String? = null,
    val backupPath: String? = null
)

data class SmartImportReport(
    val total: Int,
    val succeeded: Int,
    val failed: Int,
    val results: List<OperationResult>
)
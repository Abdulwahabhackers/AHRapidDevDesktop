package com.rapiddev.ah.data.model

data class ExportOptions(
    val ignoreBuildDirs: Boolean = true,
    val maxDepth: Int = 0,
    val includeCode: Boolean = true,
    val splitByFolder: Boolean = true,
    val includeIndex: Boolean = true,
    val includePrompt: Boolean = true,
    val outputFolderName: String = "_AI_EXPORT"
)

object ExportPresets {
    val DEFAULT = ExportOptions()

    val FOR_AI = ExportOptions(
        ignoreBuildDirs = true,
        maxDepth = 0,
        includeCode = true,
        splitByFolder = true,
        includeIndex = true,
        includePrompt = true,
        outputFolderName = "_AI_EXPORT"
    )

    val COMPACT = ExportOptions(
        ignoreBuildDirs = true,
        maxDepth = 5,
        includeCode = true,
        splitByFolder = true,
        includeIndex = true,
        includePrompt = true,
        outputFolderName = "_AI_EXPORT_COMPACT"
    )

    val SINGLE_FILE = ExportOptions(
        ignoreBuildDirs = true,
        maxDepth = 0,
        includeCode = true,
        splitByFolder = false,
        includeIndex = false,
        includePrompt = false,
        outputFolderName = "_AI_EXPORT_SINGLE"
    )
}
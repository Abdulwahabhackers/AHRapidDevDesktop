package com.rapiddev.ah.presentation.navigation

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Home : Screen()
    object NewProject : Screen()
    object Browser : Screen()
    object TreeViewer : Screen()
    object ImportAi : Screen()
    object SmartImport : Screen()
    object Merge : Screen()
    object Sessions : Screen()
    object Settings : Screen()
    object Guide : Screen()
    object ExportAi : Screen()
    object About : Screen()
    data class CodeEditor(val filePath: String) : Screen()
}
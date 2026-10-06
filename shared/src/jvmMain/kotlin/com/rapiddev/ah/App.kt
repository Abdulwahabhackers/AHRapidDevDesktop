package com.rapiddev.ah

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rapiddev.ah.core.utils.ShortcutManager
import com.rapiddev.ah.data.local.preferences.SettingsPreferences
import com.rapiddev.ah.presentation.about.AboutScreen
import com.rapiddev.ah.presentation.browser.BrowserScreen
import com.rapiddev.ah.presentation.components.ShortcutDialog
import com.rapiddev.ah.presentation.editor.CodeEditorScreen
import com.rapiddev.ah.presentation.exportai.ExportAiScreen
import com.rapiddev.ah.presentation.guide.GuideScreen
import com.rapiddev.ah.presentation.home.HomeScreen
import com.rapiddev.ah.presentation.importai.ImportAiScreen
import com.rapiddev.ah.presentation.merge.MergeScreen
import com.rapiddev.ah.presentation.navigation.Screen
import com.rapiddev.ah.presentation.navigation.rememberNavController
import com.rapiddev.ah.presentation.newproject.NewProjectScreen
import com.rapiddev.ah.presentation.onboarding.OnboardingScreen
import com.rapiddev.ah.presentation.sessions.SessionsScreen
import com.rapiddev.ah.presentation.settings.SettingsScreen
import com.rapiddev.ah.presentation.smartimport.SmartImportScreen
import com.rapiddev.ah.presentation.splash.SplashScreen
import com.rapiddev.ah.presentation.theme.AHRapidDevTheme
import com.rapiddev.ah.presentation.treeviewer.TreeViewerScreen

@Composable
fun App() {
    val settings = remember { SettingsPreferences() }
    val themeMode by settings.themeMode.collectAsState()
    val onboardingSeen by settings.onboardingSeen.collectAsState()
    val nav = rememberNavController(Screen.Splash)

    var showShortcutDialog by remember {
        mutableStateOf(!ShortcutManager.hasAsked() && !ShortcutManager.shortcutExists())
    }

    AHRapidDevTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val screen = nav.current) {
                is Screen.Splash -> SplashScreen(
                    onFinished = {
                        // إذا لم يرى Onboarding → اعرضها. وإلا → Home
                        if (!onboardingSeen) {
                            nav.resetTo(Screen.Onboarding)
                        } else {
                            nav.resetTo(Screen.Home)
                        }
                    }
                )
                is Screen.Onboarding -> OnboardingScreen(
                    onFinish = {
                        settings.markOnboardingSeen()
                        nav.resetTo(Screen.Home)
                    }
                )
                is Screen.Home -> HomeScreen(
                    onNavigate = { nav.navigate(it) }
                )
                is Screen.Guide -> GuideScreen(
                    onBack = { nav.back() }
                )
                is Screen.Settings -> SettingsScreen(
                    settings = settings,
                    onBack = { nav.back() },
                    onOpenAbout = { nav.navigate(Screen.About) }
                )
                is Screen.About -> AboutScreen(
                    onBack = { nav.back() }
                )
                is Screen.Sessions -> SessionsScreen(
                    onBack = { nav.back() },
                    onOpenInBrowser = { path ->
                        com.rapiddev.ah.core.utils.SessionNavigationState.pendingPath = path
                        nav.navigate(Screen.Browser)
                    },
                    onOpenInTreeViewer = { path ->
                        com.rapiddev.ah.core.utils.SessionNavigationState.pendingPath = path
                        nav.navigate(Screen.TreeViewer)
                    }
                )
                is Screen.Browser -> BrowserScreen(
                    onBack = { nav.back() },
                    onOpenInEditor = { path -> nav.navigate(Screen.CodeEditor(path)) }
                )
                is Screen.TreeViewer -> TreeViewerScreen(
                    onBack = { nav.back() }
                )
                is Screen.NewProject -> NewProjectScreen(
                    onBack = { nav.back() }
                )
                is Screen.ExportAi -> ExportAiScreen(
                    onBack = { nav.back() }
                )
                is Screen.ImportAi -> ImportAiScreen(
                    onBack = { nav.back() }
                )
                is Screen.SmartImport -> SmartImportScreen(
                    onBack = { nav.back() }
                )
                is Screen.Merge -> MergeScreen(
                    onBack = { nav.back() }
                )
                is Screen.CodeEditor -> CodeEditorScreen(
                    filePath = screen.filePath,
                    onBack = { nav.back() }
                )
            }

            if (showShortcutDialog) {
                ShortcutDialog(
                    onDismiss = {
                        ShortcutManager.markAsked()
                        showShortcutDialog = false
                    },
                    onCreate = {
                        ShortcutManager.createDesktopShortcut()
                        ShortcutManager.markAsked()
                        showShortcutDialog = false
                    }
                )
            }
        }
    }
}
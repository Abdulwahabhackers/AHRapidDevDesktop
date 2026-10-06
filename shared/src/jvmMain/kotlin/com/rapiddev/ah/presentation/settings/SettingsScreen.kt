package com.rapiddev.ah.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.ShortcutManager
import com.rapiddev.ah.data.local.preferences.SettingsPreferences
import com.rapiddev.ah.data.model.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsPreferences,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit = {}
) {
    val currentMode by settings.themeMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var shortcutExists by remember { mutableStateOf(ShortcutManager.shortcutExists()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ===== المظهر =====
            SettingsSection(title = "المظهر", icon = Icons.Default.Star) {
                Column(Modifier.selectableGroup()) {
                    ThemeOption("فاتح", currentMode == ThemeMode.LIGHT) {
                        settings.setThemeMode(ThemeMode.LIGHT)
                    }
                    ThemeOption("داكن", currentMode == ThemeMode.DARK) {
                        settings.setThemeMode(ThemeMode.DARK)
                    }
                    ThemeOption("حسب النظام", currentMode == ThemeMode.SYSTEM) {
                        settings.setThemeMode(ThemeMode.SYSTEM)
                    }
                }
            }

            // ===== سطح المكتب =====
            SettingsSection(title = "سطح المكتب", icon = Icons.Default.DesktopWindows) {
                SettingsItem(
                    icon = if (shortcutExists) Icons.Default.CheckCircle else Icons.Default.Add,
                    iconTint = if (shortcutExists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    title = if (shortcutExists) "اختصار موجود على سطح المكتب" else "إنشاء اختصار على سطح المكتب",
                    subtitle = if (shortcutExists) "اضغط لحذف الاختصار" else "افتح التطبيق بنقرة واحدة",
                    onClick = {
                        scope.launch {
                            if (shortcutExists) {
                                val ok = ShortcutManager.deleteShortcut()
                                if (ok) {
                                    shortcutExists = false
                                    snackbarHostState.showSnackbar("تم حذف الاختصار")
                                } else {
                                    snackbarHostState.showSnackbar("فشل حذف الاختصار")
                                }
                            } else {
                                val ok = ShortcutManager.createDesktopShortcut()
                                if (ok) {
                                    shortcutExists = true
                                    snackbarHostState.showSnackbar("✅ تم إنشاء الاختصار")
                                } else {
                                    snackbarHostState.showSnackbar("❌ فشل إنشاء الاختصار")
                                }
                            }
                        }
                    }
                )
            }

            // ===== المعلومات =====
            SettingsSection(title = "المعلومات", icon = Icons.Default.Info) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "حول التطبيق",
                    subtitle = "معلومات الفريق الميزات",
                    onClick = onOpenAbout
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
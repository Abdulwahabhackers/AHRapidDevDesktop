package com.rapiddev.ah.presentation.exportai

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.data.model.ExportPresets
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportAiScreen(onBack: () -> Unit) {

    val viewModel: ExportAiViewModel = remember { ExportAiViewModel() }

    val isExporting by viewModel.isExporting.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val progressText by viewModel.progressText.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var sourcePath by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(ExportPresets.FOR_AI) }
    var showBrowser by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("FOR_AI") }

    LaunchedEffect(message) {
        message?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تصدير مشروع للذكاء الاصطناعي") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("📂 المشروع", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sourcePath,
                    onValueChange = { sourcePath = it },
                    label = { Text("مسار المشروع") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                FilledIconButton(
                    onClick = { showBrowser = true },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                }
            }

            Spacer(Modifier.height(4.dp))

            Text("⚡ قوالب سريعة", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPreset == "FOR_AI",
                    onClick = { selectedPreset = "FOR_AI"; options = ExportPresets.FOR_AI },
                    label = { Text("افتراضي", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedPreset == "COMPACT",
                    onClick = { selectedPreset = "COMPACT"; options = ExportPresets.COMPACT },
                    label = { Text("مضغوط", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedPreset == "SINGLE",
                    onClick = { selectedPreset = "SINGLE"; options = ExportPresets.SINGLE_FILE },
                    label = { Text("ملف واحد", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text("⚙️ خيارات متقدمة", style = MaterialTheme.typography.titleMedium)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    OptionSwitch(
                        label = "تجاهل مجلدات البناء",
                        description = "build, .gradle, .idea, ...",
                        checked = options.ignoreBuildDirs
                    ) { options = options.copy(ignoreBuildDirs = it) }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    OptionSwitch(
                        label = "تضمين الأكواد",
                        description = "نسخ محتوى الملفات النصية",
                        checked = options.includeCode
                    ) { options = options.copy(includeCode = it) }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    OptionSwitch(
                        label = "ملف لكل مجلد",
                        description = "بدلاً من ملف واحد كبير",
                        checked = options.splitByFolder
                    ) { options = options.copy(splitByFolder = it) }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    OptionSwitch(
                        label = "إنشاء _INDEX.txt",
                        description = "فهرس المشروع الكامل",
                        checked = options.includeIndex
                    ) { options = options.copy(includeIndex = it) }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    OptionSwitch(
                        label = "إنشاء _PROMPT.txt",
                        description = "برومبت جاهز للـ AI",
                        checked = options.includePrompt
                    ) { options = options.copy(includePrompt = it) }
                }
            }

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "📏 العمق الأقصى: " + if (options.maxDepth == 0) "بلا حدود" else "${options.maxDepth} مستويات",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0, 2, 3, 5, 8).forEach { depth ->
                            FilterChip(
                                selected = options.maxDepth == depth,
                                onClick = { options = options.copy(maxDepth = depth) },
                                label = {
                                    Text(
                                        if (depth == 0) "∞" else "$depth",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    PathHistory.addPath(sourcePath.trim())
                    viewModel.export(sourcePath.trim(), options)
                },
                enabled = sourcePath.isNotBlank() && !isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("$progress% - $progressText")
                } else {
                    Text("🚀 تصدير المشروع")
                }
            }

            if (isExporting) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showBrowser) {
        FolderPickerDialog(
            title = "اختر مجلد المشروع",
            initialPath = sourcePath.ifBlank { PathHistory.getLastPath() },
            onDismiss = { showBrowser = false },
            onSelect = { sourcePath = it; showBrowser = false }
        )
    }
}

@Composable
private fun OptionSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
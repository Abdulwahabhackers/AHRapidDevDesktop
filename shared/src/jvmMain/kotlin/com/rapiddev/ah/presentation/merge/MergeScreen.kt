package com.rapiddev.ah.presentation.merge

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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeScreen(onBack: () -> Unit) {

    val viewModel: MergeViewModel = remember { MergeViewModel() }

    val isRunning by viewModel.isRunning.collectAsState()
    val message by viewModel.message.collectAsState()
    val report by viewModel.report.collectAsState()
    val progress by viewModel.progress.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var sourcePath by remember { mutableStateOf("") }
    var targetPath by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(MergeMode.SKIP) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دمج المشاريع") },
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("المصدر (من)", style = MaterialTheme.typography.titleMedium)
            PathField(
                value = sourcePath,
                onValueChange = { sourcePath = it },
                label = "مسار المصدر",
                onBrowse = { showSourcePicker = true }
            )

            Text("الهدف (إلى)", style = MaterialTheme.typography.titleMedium)
            PathField(
                value = targetPath,
                onValueChange = { targetPath = it },
                label = "مسار الهدف",
                onBrowse = { showTargetPicker = true }
            )

            Text("عند وجود ملف بنفس الاسم", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == MergeMode.SKIP,
                    onClick = { mode = MergeMode.SKIP },
                    label = { Text("تخطي") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = mode == MergeMode.OVERWRITE,
                    onClick = { mode = MergeMode.OVERWRITE },
                    label = { Text("استبدال") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = mode == MergeMode.RENAME,
                    onClick = { mode = MergeMode.RENAME },
                    label = { Text("إعادة تسمية") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { viewModel.analyze(sourcePath.trim(), targetPath.trim()) },
                    enabled = sourcePath.isNotBlank() && targetPath.isNotBlank() && !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تحليل")
                }
                Button(
                    onClick = {
                        PathHistory.addPath(sourcePath.trim())
                        PathHistory.addPath(targetPath.trim())
                        viewModel.merge(sourcePath.trim(), targetPath.trim(), mode)
                    },
                    enabled = sourcePath.isNotBlank() && targetPath.isNotBlank() && !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("جارٍ...")
                    } else {
                        Text("بدء الدمج")
                    }
                }
            }

            if (isRunning) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("$progress%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (report != null) {
                val r = report ?: return@Column
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("تقرير الدمج", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        ReportRow("مجلدات منشأة", r.dirsCreated)
                        ReportRow("ملفات منسوخة", r.copied)
                        ReportRow("ملفات مستبدلة", r.overwritten)
                        ReportRow("ملفات متجاهلة", r.skipped)
                        ReportRow("أخطاء", r.failed)
                        if (r.errors.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("بعض الأخطاء:", style = MaterialTheme.typography.bodySmall)
                            r.errors.take(5).forEach { err ->
                                Text("- $err", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
                TextButton(onClick = { viewModel.clearReport() }, modifier = Modifier.fillMaxWidth()) {
                    Text("إخفاء التقرير")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showSourcePicker) {
        FolderPickerDialog(
            title = "اختر مجلد المصدر",
            initialPath = sourcePath.ifBlank { PathHistory.getLastPath() },
            onDismiss = { showSourcePicker = false },
            onSelect = { sourcePath = it; showSourcePicker = false }
        )
    }

    if (showTargetPicker) {
        FolderPickerDialog(
            title = "اختر مجلد الهدف",
            initialPath = targetPath.ifBlank { PathHistory.getLastPath() },
            onDismiss = { showTargetPicker = false },
            onSelect = { targetPath = it; showTargetPicker = false }
        )
    }
}

@Composable
private fun PathField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onBrowse: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
        )
        FilledIconButton(
            onClick = onBrowse,
            modifier = Modifier.size(52.dp)
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
        }
    }
}

@Composable
private fun ReportRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}
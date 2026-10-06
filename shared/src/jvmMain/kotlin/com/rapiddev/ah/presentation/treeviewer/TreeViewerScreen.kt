package com.rapiddev.ah.presentation.treeviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeViewerScreen(onBack: () -> Unit) {

    val viewModel: TreeViewerViewModel = remember { TreeViewerViewModel() }

    val treeText by viewModel.treeText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var path by remember {
        mutableStateOf(PathHistory.getLastPath())
    }
    var ignoreDirs by remember { mutableStateOf(true) }
    var showHidden by remember { mutableStateOf(false) }
    var maxDepthText by remember { mutableStateOf("0") }

    var showBrowser by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val pending = com.rapiddev.ah.core.utils.SessionNavigationState.consumePath()
        if (pending != null && pending.isNotBlank()) {
            path = pending
            val depth = maxDepthText.toIntOrNull() ?: 0
            val realDepth = if (depth <= 0) Int.MAX_VALUE else depth
            viewModel.generate(pending, ignoreDirs, showHidden, realDepth)
        }
    }

    LaunchedEffect(message) {
        message?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عرض شجرة") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("مسار المجلد") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
                FilledIconButton(
                    onClick = { showBrowser = true },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = ignoreDirs, onCheckedChange = { ignoreDirs = it })
                    Spacer(Modifier.width(4.dp))
                    Text("تجاهل البناء", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = showHidden, onCheckedChange = { showHidden = it })
                    Spacer(Modifier.width(4.dp))
                    Text("المخفية", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = maxDepthText,
                    onValueChange = { s -> if (s.length <= 3 && s.all { it.isDigit() }) maxDepthText = s },
                    label = { Text("العمق (0=∞)") },
                    modifier = Modifier.width(130.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        PathHistory.addPath(path.trim())
                        val depth = maxDepthText.toIntOrNull() ?: 0
                        val realDepth = if (depth <= 0) Int.MAX_VALUE else depth
                        viewModel.generate(path.trim(), ignoreDirs, showHidden, realDepth)
                    },
                    enabled = !isLoading && path.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("جارٍ البناء...")
                    } else {
                        Text("🌳 إنشاء الشجرة")
                    }
                }
            }

            if (stats.isNotEmpty()) {
                Text(
                    text = "📊 $stats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (treeText.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(treeText))
                            scope.launch { snackbarHostState.showSnackbar("تم النسخ") }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("نسخ")
                    }
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("حفظ")
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (treeText.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("ادخل مساراً ثم اضغط إنشاء الشجرة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val lines = remember(treeText) { treeText.split("\n") }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(lines) { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBrowser) {
        FolderPickerDialog(
            title = "اختر مجلد الشجرة",
            initialPath = path,
            onDismiss = { showBrowser = false },
            onSelect = { path = it; showBrowser = false }
        )
    }

    if (showSaveDialog) {
        SaveTreeDialog(
            defaultName = File(path).name + "_tree.txt",
            defaultPath = File(path).parent ?: PathHistory.getDefaultPath(),
            onDismiss = { showSaveDialog = false },
            onSave = { basePath, fileName ->
                viewModel.saveToFile(basePath, fileName)
                showSaveDialog = false
            }
        )
    }
}

@Composable
private fun SaveTreeDialog(
    defaultName: String,
    defaultPath: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var basePath by remember { mutableStateOf(defaultPath) }
    var fileName by remember { mutableStateOf(defaultName) }
    var showPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حفظ الشجرة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("اسم الملف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = basePath,
                        onValueChange = { basePath = it },
                        label = { Text("المجلد") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    IconButton(onClick = { showPicker = true }, modifier = Modifier.size(52.dp)) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank() && basePath.isNotBlank()) {
                        onSave(basePath.trim(), fileName.trim())
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )

    if (showPicker) {
        FolderPickerDialog(
            title = "اختر مجلد الحفظ",
            initialPath = basePath,
            onDismiss = { showPicker = false },
            onSelect = { basePath = it; showPicker = false }
        )
    }
}
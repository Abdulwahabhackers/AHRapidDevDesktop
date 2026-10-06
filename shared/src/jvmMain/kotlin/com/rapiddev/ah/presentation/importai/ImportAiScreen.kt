package com.rapiddev.ah.presentation.importai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportAiScreen(onBack: () -> Unit) {

    val viewModel: ImportAiViewModel = remember { ImportAiViewModel() }

    val input by viewModel.input.collectAsState()
    val parsed by viewModel.parsed.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var basePath by remember { mutableStateOf(PathHistory.getLastPath()) }
    var previewFile by remember { mutableStateOf<String?>(null) }
    var previewContent by remember { mutableStateOf("") }
    var showBrowser by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("استيراد أكواد AI") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OutlinedTextField(
                value = input,
                onValueChange = viewModel::setInput,
                label = { Text("الصق رسالة الذكاء الاصطناعي هنا") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                maxLines = Int.MAX_VALUE,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Default
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val text = clipboard.getText()?.text ?: ""
                        viewModel.setInput(input + text)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("لصق")
                }
                OutlinedButton(
                    onClick = { viewModel.setInput("") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("مسح")
                }
            }

            Button(
                onClick = { viewModel.parse(basePath) },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank()
            ) {
                Text("🔍 تحليل الرسالة")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = basePath,
                    onValueChange = { basePath = it },
                    label = { Text("مسار المجلد الهدف") },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    singleLine = true
                )
                FilledIconButton(
                    onClick = { showBrowser = true },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "تصفح")
                }
            }

            if (parsed.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📁 الملفات المكتشفة (${parsed.size})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val names = parsed.joinToString("\n") { it.path }
                                clipboard.setText(AnnotatedString(names))
                                scope.launch { snackbarHostState.showSnackbar("تم نسخ ${parsed.size} اسم") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الأسماء", tint = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { viewModel.removeAll() }) {
                                Text("إزالة الكل", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            items(parsed) { f ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "• ${f.path}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        previewFile = f.path
                                        previewContent = f.content
                                    }) {
                                        Text("👁", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { viewModel.removeFile(f.path) }) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        PathHistory.addPath(basePath.trim())
                        viewModel.writeToFolder(basePath.trim())
                    },
                    enabled = !isLoading && basePath.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("جارٍ الكتابة...")
                    } else {
                        Text("📥 استيراد الملفات")
                    }
                }
            }
        }
    }

    if (showBrowser) {
        FolderPickerDialog(
            title = "اختر مجلد الهدف",
            initialPath = basePath,
            onDismiss = { showBrowser = false },
            onSelect = { basePath = it; showBrowser = false }
        )
    }

    previewFile?.let { fileName ->
        AlertDialog(
            onDismissRequest = { previewFile = null },
            title = { Text(fileName, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(previewContent, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = { TextButton(onClick = { previewFile = null }) { Text("إغلاق") } },
            dismissButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(previewContent))
                    scope.launch { snackbarHostState.showSnackbar("تم نسخ الكود") }
                }) {
                    Text("نسخ الكود")
                }
            }
        )
    }
}
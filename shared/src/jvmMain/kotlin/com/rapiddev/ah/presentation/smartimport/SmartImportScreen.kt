package com.rapiddev.ah.presentation.smartimport

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.data.model.AiOperation
import com.rapiddev.ah.data.model.SmartImportReport
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartImportScreen(onBack: () -> Unit) {

    val viewModel: SmartImportViewModel = remember { SmartImportViewModel() }

    val input by viewModel.input.collectAsState()
    val operations by viewModel.operations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val report by viewModel.report.collectAsState()

    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var basePath by remember { mutableStateOf(PathHistory.getLastPath()) }
    var showBrowser by remember { mutableStateOf(false) }
    var previewOperation by remember { mutableStateOf<AiOperation?>(null) }

    LaunchedEffect(message) {
        message?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧠 Smart Import (تجريبي)") },
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
                label = { Text("الصق رسالة AI (مع التعليمات)") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = basePath,
                    onValueChange = { basePath = it },
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

            Button(
                onClick = {
                    val resolved = viewModel.resolvePath(basePath.trim())
                    if (resolved != null) {
                        basePath = resolved
                        PathHistory.addPath(resolved)
                        viewModel.analyze(resolved)
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("❌ المسار غير موجود") }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank() && basePath.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("جارٍ التحليل...")
                } else {
                    Text("🧠 تحليل ذكي")
                }
            }

            if (operations.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "📋 العمليات المستخرجة (${operations.size})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val text = buildOperationsText(operations)
                                clipboard.setText(AnnotatedString(text))
                                scope.launch { snackbarHostState.showSnackbar("✅ تم نسخ ${operations.size} عملية") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ العمليات", tint = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { viewModel.removeAll() }) {
                                Text("إزالة الكل", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        LazyColumn {
                            itemsIndexed(operations) { idx, op ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${idx + 1}.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            op.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { previewOperation = op },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Text("👁", style = MaterialTheme.typography.bodySmall)
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeOperation(idx) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    if (op.targetPath.isNotBlank()) {
                                        Text(
                                            "📍 ${op.targetPath}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.execute(basePath.trim()) },
                    enabled = !isLoading && basePath.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("جارٍ التنفيذ...")
                    } else {
                        Text("⚡ تنفيذ العمليات")
                    }
                }
            }

            report?.let { r ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "📊 التقرير: ${r.succeeded}/${r.total} نجحت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val text = buildReportText(r)
                                clipboard.setText(AnnotatedString(text))
                                scope.launch { snackbarHostState.showSnackbar("✅ تم نسخ التقرير") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ التقرير", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            r.results.forEachIndexed { idx, res ->
                                val icon = if (res.success) "✅" else "❌"
                                Column(Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        "$icon ${idx + 1}. ${res.operation.targetPath}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (!res.success && res.error != null) {
                                        Text(
                                            "   ↳ ${res.error}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBrowser) {
        FolderPickerDialog(
            title = "اختر مجلد المشروع",
            initialPath = basePath,
            onDismiss = { showBrowser = false },
            onSelect = { basePath = it; showBrowser = false }
        )
    }

    previewOperation?.let { op ->
        val data = getPreviewData(op)
        AlertDialog(
            onDismissRequest = { previewOperation = null },
            title = {
                Column {
                    Text(data.first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (data.second.isNotEmpty()) {
                        Text(
                            "📍 ${data.second}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 450.dp).verticalScroll(rememberScrollState())) {
                    if (data.third.isBlank()) {
                        Text("(لا يوجد محتوى للمعاينة)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(data.third, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { previewOperation = null }) { Text("إغلاق") } },
            dismissButton = {
                if (data.third.isNotBlank()) {
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(data.third))
                        scope.launch { snackbarHostState.showSnackbar("✅ تم النسخ") }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("نسخ")
                    }
                }
            }
        )
    }
}

private fun buildOperationsText(operations: List<AiOperation>): String {
    val sb = StringBuilder()
    sb.append("📋 العمليات المستخرجة (${operations.size})\n")
    sb.append("=".repeat(60)).append("\n\n")
    operations.forEachIndexed { idx, op ->
        sb.append("${idx + 1}. ${op.description}\n")
        if (op.targetPath.isNotBlank()) sb.append("   📍 ${op.targetPath}\n")
        sb.append("\n")
    }
    sb.append("=".repeat(60)).append("\n")
    return sb.toString()
}

private fun buildReportText(report: SmartImportReport): String {
    val sb = StringBuilder()
    sb.append("📊 تقرير التنفيذ\n")
    sb.append("=".repeat(60)).append("\n")
    sb.append("✅ نجح: ${report.succeeded}\n")
    sb.append("❌ فشل: ${report.failed}\n")
    sb.append("📋 الإجمالي: ${report.total}\n")
    sb.append("=".repeat(60)).append("\n\n")
    report.results.forEachIndexed { idx, res ->
        val icon = if (res.success) "✅" else "❌"
        sb.append("$icon ${idx + 1}. ${res.operation.description}\n")
        sb.append("   📍 ${res.operation.targetPath}\n")
        if (!res.success && res.error != null) sb.append("   ⚠️ السبب: ${res.error}\n")
        sb.append("\n")
    }
    sb.append("=".repeat(60)).append("\n")
    return sb.toString()
}

private fun getPreviewData(op: AiOperation): Triple<String, String, String> {
    return when (op) {
        is AiOperation.CreateFile -> Triple("📄 إنشاء ملف", op.targetPath, op.content)
        is AiOperation.ReplaceFile -> Triple("🔄 استبدال كامل", op.targetPath, op.content)
        is AiOperation.DeleteFile -> Triple("🗑️ حذف ملف", op.targetPath, "(سيتم حذف هذا الملف)")
        is AiOperation.DeleteFolder -> Triple("🗑️ حذف مجلد", op.targetPath, "(سيتم حذف المجلد وكل محتوياته)")
        is AiOperation.AddImport -> Triple("➕ إضافة استيراد", op.targetPath, "سيتم إضافة:\n\n${op.importLine}")
        is AiOperation.EnsureImport -> Triple("✓ تأكيد استيراد", op.targetPath, "الاستيراد المطلوب:\n\n${op.importLine}\n\n(إن لم يكن موجوداً سيُضاف)")
        is AiOperation.ReplaceCode -> Triple(
            "🔁 استبدال كود",
            op.targetPath,
            buildString {
                append("─── الكود القديم ───\n")
                append(op.oldCode)
                append("\n\n─── الكود الجديد ───\n")
                append(op.newCode)
            }
        )
        is AiOperation.AddCode -> Triple("➕ إضافة كود", op.targetPath, op.code)
        is AiOperation.DeleteLine -> Triple("✂️ حذف سطر", op.targetPath, "السطر المراد حذفه:\n\n${op.lineContent}")
        is AiOperation.DeleteFunction -> Triple("✂️ حذف دالة", op.targetPath, "اسم الدالة: ${op.functionName}\n\n(سيتم حذف الدالة كاملة)")
    }
}
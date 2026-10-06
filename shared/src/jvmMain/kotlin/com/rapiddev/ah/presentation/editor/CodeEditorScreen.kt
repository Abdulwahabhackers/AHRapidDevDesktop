package com.rapiddev.ah.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    filePath: String,
    onBack: () -> Unit
) {

    val viewModel: CodeEditorViewModel = remember { CodeEditorViewModel() }

    val fileName by viewModel.fileName.collectAsState()
    val currentContent by viewModel.content.collectAsState()
    val language by viewModel.language.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val readOnly by viewModel.readOnly.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var showLangMenu by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(filePath) {
        viewModel.load(filePath)
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
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                fileName.ifBlank { "محرر الكود" },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            if (hasUnsavedChanges) {
                                Spacer(Modifier.width(6.dp))
                                Text("●", color = MaterialTheme.colorScheme.tertiary, fontSize = 20.sp)
                            }
                        }
                        if (readOnly) {
                            Text(
                                "🔒 قراءة فقط",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) showBackConfirm = true else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    // نوع اللغة
                    Box {
                        TextButton(onClick = { showLangMenu = true }) {
                            Text(
                                language.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            CodeLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            lang.displayName,
                                            fontWeight = if (lang == language) FontWeight.Bold else FontWeight.Normal,
                                            color = if (lang == language) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.changeLanguage(lang)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // قفل/فتح
                    IconButton(onClick = { viewModel.toggleReadOnly() }) {
                        Icon(
                            if (readOnly) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (readOnly) "فتح للتعديل" else "قفل",
                            tint = if (readOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // تراجع
                    if (hasUnsavedChanges) {
                        IconButton(onClick = { showRevertDialog = true }) {
                            Icon(Icons.Default.Refresh, contentDescription = "تراجع")
                        }
                    }

                    // نسخ الكل
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(currentContent))
                        scope.launch { snackbarHostState.showSnackbar("تم نسخ الكود كاملاً") }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الكل")
                    }

                    // حفظ
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = hasUnsavedChanges && !readOnly
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "حفظ",
                            tint = if (hasUnsavedChanges && !readOnly)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            // ===== شريط معلومات =====
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "📄 ${currentContent.lines().size} سطر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "📝 ${currentContent.length} حرف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "🎨 ${language.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // ===== المحرر =====
            CodeEditor(
                code = currentContent,
                language = language,
                readOnly = readOnly,
                onCodeChange = { viewModel.updateContent(it) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // حوار التراجع
    if (showRevertDialog) {
        AlertDialog(
            onDismissRequest = { showRevertDialog = false },
            title = { Text("التراجع عن التعديلات؟") },
            text = { Text("سيتم فقدان جميع التعديلات غير المحفوظة.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.revert()
                    showRevertDialog = false
                }) {
                    Text("تراجع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevertDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // حوار الخروج مع تعديلات
    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            title = { Text("تعديلات غير محفوظة") },
            text = { Text("هل تريد الحفظ قبل الخروج؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.save()
                    showBackConfirm = false
                    onBack()
                }) {
                    Text("حفظ وخروج")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showBackConfirm = false
                        onBack()
                    }) {
                        Text("خروج بدون حفظ", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showBackConfirm = false }) {
                        Text("إلغاء")
                    }
                }
            }
        )
    }
}

@Composable
private fun CodeEditor(
    code: String,
    language: CodeLanguage,
    readOnly: Boolean,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val scrollState = rememberScrollState()
    val hScrollState = rememberScrollState()

    // نحتفظ بـ TextFieldValue لتفادي مشاكل المؤشر مع highlight
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(code))
    }

    // مزامنة عند تغيير الملف
    LaunchedEffect(code) {
        if (textFieldValue.text != code) {
            val start = textFieldValue.selection.start.coerceIn(0, code.length)
            val end = textFieldValue.selection.end.coerceIn(0, code.length)
            textFieldValue = TextFieldValue(
                text = code,
                selection = androidx.compose.ui.text.TextRange(start, end)
            )
        }
    }

    val lineCount = remember(code) { code.lines().size }
    val lineNumbersWidth = remember(lineCount) {
        val digits = lineCount.toString().length
        (digits * 10 + 20).dp
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A))
            .verticalScroll(scrollState)
    ) {

        // ===== أرقام الأسطر =====
        Column(
            modifier = Modifier
                .width(lineNumbersWidth)
                .background(Color(0xFF0F1520))
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            (1..lineCount).forEach { n ->
                Text(
                    text = n.toString(),
                    color = Color(0xFF4B5563),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }

        // ===== منطقة الكود =====
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(hScrollState)
                .padding(8.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onCodeChange(newValue.text)
                },
                enabled = !readOnly,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFE5E7EB)
                ),
                cursorBrush = SolidColor(Color(0xFF22D3EE)),
                visualTransformation = { annotated ->
                    // نطبّق التلوين هنا
                    val highlighted = CodeHighlighter.highlight(
                        annotated.text,
                        language
                    )
                    androidx.compose.ui.text.input.TransformedText(
                        highlighted,
                        androidx.compose.ui.text.input.OffsetMapping.Identity
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
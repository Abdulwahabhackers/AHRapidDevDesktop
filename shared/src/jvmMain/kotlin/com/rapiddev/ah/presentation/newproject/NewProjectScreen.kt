package com.rapiddev.ah.presentation.newproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.PathHistory
import com.rapiddev.ah.data.model.ProjectType
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(onBack: () -> Unit) {

    val viewModel: NewProjectViewModel = remember { NewProjectViewModel() }
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val treeCount by viewModel.treeCount.collectAsState()

    var projectName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("com.example.app") }
    var basePath by remember { mutableStateOf(PathHistory.getLastPath()) }
    var selectedType by remember { mutableStateOf(ProjectType.ANDROID) }
    var androidLang by remember { mutableStateOf(ProjectType.AndroidLanguage.KOTLIN) }
    var treeText by remember { mutableStateOf("") }
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
                title = { Text("مشروع جديد") },
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

            Text("نوع المشروع", style = MaterialTheme.typography.titleMedium)
            ProjectType.entries.forEach { type ->
                TypeCard(
                    type = type,
                    selected = selectedType == type,
                    onClick = { selectedType = type }
                )
            }

            if (selectedType == ProjectType.ANDROID) {
                Text("لغة المشروع", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectType.AndroidLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = androidLang == lang,
                            onClick = { androidLang = lang },
                            label = { Text(lang.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("تفاصيل المشروع", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("اسم المشروع") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (selectedType == ProjectType.ANDROID) {
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("اسم الحزمة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
            }

            if (selectedType == ProjectType.FROM_TREE) {
                OutlinedTextField(
                    value = treeText,
                    onValueChange = { treeText = it },
                    label = { Text("الصق الشجرة أو رسالة AI هنا") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 300.dp),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    maxLines = Int.MAX_VALUE
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clip = clipboard.getText()?.text ?: ""
                            treeText += clip
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("لصق")
                    }
                    OutlinedButton(
                        onClick = { treeText = ""; viewModel.clearMessage() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مسح")
                    }
                    OutlinedButton(
                        onClick = { viewModel.analyzeTree(treeText) },
                        modifier = Modifier.weight(1f),
                        enabled = treeText.isNotBlank()
                    ) {
                        Text("تحليل")
                    }
                }

                if (treeCount > 0) {
                    Text(
                        "📁 سيتم إنشاء $treeCount ملف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                    label = { Text("مجلد الإنشاء") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                FilledIconButton(
                    onClick = { showBrowser = true },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "تصفح")
                }
            }

            Button(
                onClick = {
                    PathHistory.addPath(basePath.trim())
                    viewModel.createProject(
                        basePath = basePath.trim(),
                        projectName = projectName.trim(),
                        packageName = packageName.trim(),
                        type = selectedType,
                        androidLanguage = androidLang,
                        treeText = treeText
                    )
                },
                enabled = !isLoading && projectName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("جارٍ الإنشاء...")
                } else {
                    Text("🚀 إنشاء المشروع")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showBrowser) {
        FolderPickerDialog(
            title = "اختر مجلد الإنشاء",
            initialPath = basePath,
            onDismiss = { showBrowser = false },
            onSelect = { basePath = it; showBrowser = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeCard(type: ProjectType, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(type.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(type.displayName, style = MaterialTheme.typography.titleMedium)
                Text(type.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) RadioButton(selected = true, onClick = null)
        }
    }
}
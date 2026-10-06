package com.rapiddev.ah.presentation.browser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.data.model.FileItem
import com.rapiddev.ah.presentation.components.FolderPickerDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BrowserScreen(
    onBack: () -> Unit,
    onOpenInEditor: (String) -> Unit = {}
) {

    val viewModel: FileBrowserViewModel = remember { FileBrowserViewModel() }

    val currentPath by viewModel.currentPath.collectAsState()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val showHidden by viewModel.showHidden.collectAsState()
    val foldersFirst by viewModel.foldersFirst.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val previewText by viewModel.textContent.collectAsState()
    val previewName by viewModel.previewFileName.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedPaths by viewModel.selectedPaths.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }
    var actionItem by remember { mutableStateOf<FileItem?>(null) }
    var showActions by remember { mutableStateOf(false) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showNewFile by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showCopyPicker by remember { mutableStateOf(false) }
    var showMovePicker by remember { mutableStateOf(false) }

    // عمليات جماعية
    var showBulkDelete by remember { mutableStateOf(false) }
    var showBulkCopyPicker by remember { mutableStateOf(false) }
    var showBulkMovePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInitialPath()
    }

    LaunchedEffect(message) {
        message?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                // ====== وضع التحديد ======
                TopAppBar(
                    title = {
                        Text(
                            "محدد: ${selectedPaths.size}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء التحديد")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "تحديد الكل")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Deselect, contentDescription = "إلغاء التحديد")
                        }

                        if (selectedPaths.isNotEmpty()) {
                            IconButton(onClick = { showBulkCopyPicker = true }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                            }
                            IconButton(onClick = { showBulkMovePicker = true }) {
                                Icon(Icons.Default.DriveFileMove, contentDescription = "نقل")
                            }
                            IconButton(onClick = { showBulkDelete = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                // ====== الوضع العادي ======
                TopAppBar(
                    title = { Text("تصفح الملفات") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = "تحديد متعدد")
                        }
                        IconButton(onClick = {
                            val home = System.getProperty("user.home") ?: "/"
                            viewModel.navigateTo(home)
                        }) {
                            Icon(Icons.Default.Home, contentDescription = "الرئيسية")
                        }

                        Box {
                            IconButton(onClick = { showCreateMenu = true }) {
                                Icon(Icons.Default.Add, contentDescription = "جديد")
                            }
                            DropdownMenu(
                                expanded = showCreateMenu,
                                onDismissRequest = { showCreateMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("مجلد جديد")
                                        }
                                    },
                                    onClick = { showCreateMenu = false; showNewFolder = true }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("ملف جديد")
                                        }
                                    },
                                    onClick = { showCreateMenu = false; showNewFile = true }
                                )
                            }
                        }

                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = null)
                        }
                        IconButton(onClick = { viewModel.loadDirectory(currentPath) }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }

                        Box {
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortMenuItem("الاسم (A-Z)", sortMode == SortMode.NAME_ASC) {
                                    viewModel.setSortMode(SortMode.NAME_ASC); showSortMenu = false
                                }
                                SortMenuItem("الاسم (Z-A)", sortMode == SortMode.NAME_DESC) {
                                    viewModel.setSortMode(SortMode.NAME_DESC); showSortMenu = false
                                }
                                SortMenuItem("الأصغر أولاً", sortMode == SortMode.SIZE_ASC) {
                                    viewModel.setSortMode(SortMode.SIZE_ASC); showSortMenu = false
                                }
                                SortMenuItem("الأكبر أولاً", sortMode == SortMode.SIZE_DESC) {
                                    viewModel.setSortMode(SortMode.SIZE_DESC); showSortMenu = false
                                }
                                SortMenuItem("الأقدم أولاً", sortMode == SortMode.DATE_ASC) {
                                    viewModel.setSortMode(SortMode.DATE_ASC); showSortMenu = false
                                }
                                SortMenuItem("الأحدث أولاً", sortMode == SortMode.DATE_DESC) {
                                    viewModel.setSortMode(SortMode.DATE_DESC); showSortMenu = false
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(checked = showHidden, onCheckedChange = { viewModel.setShowHidden(it) })
                                            Spacer(Modifier.width(8.dp))
                                            Text("عرض المخفية")
                                        }
                                    },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(checked = foldersFirst, onCheckedChange = { viewModel.setFoldersFirst(it) })
                                            Spacer(Modifier.width(8.dp))
                                            Text("المجلدات أولاً")
                                        }
                                    },
                                    onClick = { }
                                )
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ===== شريط المسار =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val parts = currentPath.trim('/').trim('\\').split('/', '\\')
                var acc = ""
                parts.forEachIndexed { idx, part ->
                    val partPath = if (idx == 0 && currentPath.startsWith("/")) "/$part" else {
                        acc = if (acc.isEmpty()) part else "$acc/$part"
                        acc
                    }
                    if (part.isNotEmpty()) {
                        Text(
                            text = part,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    try {
                                        if (File(partPath).exists()) viewModel.navigateTo(partPath)
                                    } catch (e: Exception) {}
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        if (idx < parts.size - 1) {
                            Text("/", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ===== حقل البحث =====
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("بحث في هذا المجلد...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                singleLine = true
            )

            // ===== الإحصائيات =====
            if (stats.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stats,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val parent = File(currentPath).parentFile
                    if (parent != null && !selectionMode) {
                        item {
                            FileRow(
                                icon = Icons.Default.ArrowBack,
                                iconColor = MaterialTheme.colorScheme.tertiary,
                                title = ".. الرجوع",
                                subtitle = parent.absolutePath,
                                isSelected = false,
                                selectionMode = false,
                                onClick = { viewModel.navigateUp() },
                                onLongClick = { },
                                onToggleSelect = { }
                            )
                            HorizontalDivider()
                        }
                    }

                    items(items, key = { it.fullPath }) { item ->
                        val isSelected = selectedPaths.contains(item.fullPath)
                        FileRow(
                            icon = if (item.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                            iconColor = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            title = item.name,
                            subtitle = if (item.isDirectory) "مجلد"
                            else formatSize(item.size) + " • " + formatDate(item.lastModified),
                            isSelected = isSelected,
                            selectionMode = selectionMode,
                            onClick = {
                                if (selectionMode) {
                                    viewModel.toggleSelection(item.fullPath)
                                } else if (item.isDirectory) {
                                    viewModel.navigateTo(item.fullPath)
                                } else if (isEditableFile(item)) {
                                    onOpenInEditor(item.fullPath)
                                } else {
                                    viewModel.openFile(item)
                                }
                            },
                            onLongClick = {
                                if (selectionMode) {
                                    viewModel.toggleSelection(item.fullPath)
                                } else {
                                    actionItem = item
                                    showActions = true
                                }
                            },
                            onToggleSelect = { viewModel.toggleSelection(item.fullPath) }
                        )
                        HorizontalDivider()
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ===== قائمة العمليات الفردية =====
    if (showActions && actionItem != null) {
        val item: FileItem = actionItem ?: return
        AlertDialog(
            onDismissRequest = { showActions = false; actionItem = null },
            title = {
                Text(item.name, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column {
                    ActionRow(Icons.Default.Edit, "إعادة تسمية") { showActions = false; showRename = true }
                    ActionRow(Icons.Default.ContentCopy, "نسخ إلى...") { showActions = false; showCopyPicker = true }
                    ActionRow(Icons.Default.DriveFileMove, "نقل إلى...") { showActions = false; showMovePicker = true }
                    ActionRow(Icons.Default.CheckBox, "تحديد") {
                        showActions = false
                        if (!selectionMode) viewModel.toggleSelectionMode()
                        viewModel.toggleSelection(item.fullPath)
                        actionItem = null
                    }
                    if (!item.isDirectory) {
                        ActionRow(Icons.Default.FolderOpen, "فتح بالنظام") {
                            showActions = false
                            openWithSystem(item.fullPath)
                            actionItem = null
                        }
                    }
                    ActionRow(Icons.Default.Delete, "حذف", isDanger = true) {
                        showActions = false
                        showDelete = true
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActions = false; actionItem = null }) { Text("إلغاء") }
            }
        )
    }

    if (showNewFolder) {
        InputDialog("مجلد جديد", "اسم المجلد", "",
            onDismiss = { showNewFolder = false },
            onConfirm = { name -> viewModel.createFolder(name); showNewFolder = false })
    }

    if (showNewFile) {
        InputDialog("ملف جديد", "اسم الملف (مع الامتداد)", "untitled.txt",
            onDismiss = { showNewFile = false },
            onConfirm = { name -> viewModel.createFile(name); showNewFile = false })
    }

    if (showRename && actionItem != null) {
        val item: FileItem = actionItem ?: return
        InputDialog("إعادة تسمية", "الاسم الجديد", item.name,
            onDismiss = { showRename = false; actionItem = null },
            onConfirm = { newName -> viewModel.renameFile(item, newName); showRename = false; actionItem = null })
    }

    if (showDelete && actionItem != null) {
        val item: FileItem = actionItem ?: return
        AlertDialog(
            onDismissRequest = { showDelete = false; actionItem = null },
            title = { Text("تأكيد الحذف") },
            text = { Text(if (item.isDirectory) "سيتم حذف المجلد وكل محتواه:\n" + item.name else "سيتم حذف الملف:\n" + item.name) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteFile(item); showDelete = false; actionItem = null }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false; actionItem = null }) { Text("إلغاء") } }
        )
    }

    if (showCopyPicker && actionItem != null) {
        val item: FileItem = actionItem ?: return
        FolderPickerDialog(
            title = "نسخ إلى",
            initialPath = currentPath,
            onDismiss = { showCopyPicker = false },
            onSelect = { targetDir ->
                viewModel.copyFile(item, File(targetDir)); showCopyPicker = false; actionItem = null
            }
        )
    }

    if (showMovePicker && actionItem != null) {
        val item: FileItem = actionItem ?: return
        FolderPickerDialog(
            title = "نقل إلى",
            initialPath = currentPath,
            onDismiss = { showMovePicker = false },
            onSelect = { targetDir ->
                viewModel.moveFile(item, File(targetDir)); showMovePicker = false; actionItem = null
            }
        )
    }

    // ===== العمليات الجماعية =====
    if (showBulkDelete) {
        AlertDialog(
            onDismissRequest = { showBulkDelete = false },
            title = { Text("تأكيد الحذف الجماعي") },
            text = {
                Text("سيتم حذف ${selectedPaths.size} عنصر.\nلا يمكن التراجع عن هذه العملية!")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showBulkDelete = false
                }) {
                    Text("حذف الكل", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showBulkDelete = false }) { Text("إلغاء") } }
        )
    }

    if (showBulkCopyPicker) {
        FolderPickerDialog(
            title = "نسخ ${selectedPaths.size} عنصر إلى",
            initialPath = currentPath,
            onDismiss = { showBulkCopyPicker = false },
            onSelect = { targetDir ->
                viewModel.copySelected(File(targetDir))
                showBulkCopyPicker = false
            }
        )
    }

    if (showBulkMovePicker) {
        FolderPickerDialog(
            title = "نقل ${selectedPaths.size} عنصر إلى",
            initialPath = currentPath,
            onDismiss = { showBulkMovePicker = false },
            onSelect = { targetDir ->
                viewModel.moveSelected(File(targetDir))
                showBulkMovePicker = false
            }
        )
    }

    if (previewText != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearPreview() },
            title = { Text(previewName, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
                    Text(previewText ?: "", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.clearPreview() }) { Text("إغلاق") } },
            dismissButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(previewText ?: ""))
                    scope.launch { snackbarHostState.showSnackbar("تم النسخ") }
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("نسخ")
                }
            }
        )
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InputDialog(
    title: String,
    label: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) }, enabled = value.isNotBlank()) { Text("تأكيد") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun SortMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = onClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectionMode) {
            IconButton(
                onClick = onToggleSelect,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
        }

        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

private fun openWithSystem(path: String) {
    try {
        java.awt.Desktop.getDesktop().open(File(path))
    } catch (e: Exception) {
        // silent
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
}
private val EDITABLE_EXTENSIONS = setOf(
    "kt", "kts", "java", "xml", "html", "htm", "css", "js", "ts", "jsx", "tsx",
    "json", "gradle", "properties", "md", "txt", "yml", "yaml", "sh", "py",
    "c", "cpp", "h", "hpp", "cs", "rb", "php", "go", "rs", "swift", "dart",
    "lua", "sql", "conf", "ini", "toml", "env", "gitignore", "pro", "svg",
    "editorconfig", "makefile"
)

private fun isEditableFile(item: FileItem): Boolean =
    item.extension.lowercase() in EDITABLE_EXTENSIONS

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    return sdf.format(Date(timestamp))
}
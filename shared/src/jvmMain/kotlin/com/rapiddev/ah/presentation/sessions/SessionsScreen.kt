package com.rapiddev.ah.presentation.sessions

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.data.model.Session
import com.rapiddev.ah.data.model.SessionType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.foundation.layout.size
import com.rapiddev.ah.presentation.components.FolderPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    onBack: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onOpenInTreeViewer: (String) -> Unit
) {

    val viewModel: SessionsViewModel = remember { SessionsViewModel() }

    val search by viewModel.searchQuery.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val message by viewModel.message.collectAsState()
    val sessionsList by viewModel.sessions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSortMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var actionSession by remember { mutableStateOf<Session?>(null) }
    var showActions by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الجلسات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortItem("الأحدث أولاً", sort == SessionSort.RECENT) {
                                viewModel.setSort(SessionSort.RECENT); showSortMenu = false
                            }
                            SortItem("بالاسم", sort == SessionSort.NAME) {
                                viewModel.setSort(SessionSort.NAME); showSortMenu = false
                            }
                            SortItem("بالنوع", sort == SessionSort.TYPE) {
                                viewModel.setSort(SessionSort.TYPE); showSortMenu = false
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { viewModel.setSearch(it) },
                label = { Text("بحث في الجلسات") },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                singleLine = true
            )

            if (message != null) {
                Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(message ?: "", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("حسناً", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            val filtered = viewModel.filtered()

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📂", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (sessionsList.isEmpty()) "لا توجد جلسات بعد" else "لا نتائج للبحث",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "أضف جلستك الأولى بزر + في الأسفل",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { session ->
                        SessionRow(
                            session = session,
                            onOpen = {
                                viewModel.touch(session.id)
                                val exists = viewModel.existsDirectory(session.path)
                                if (!exists) {
                                    scope.launch { snackbarHostState.showSnackbar("المسار غير موجود") }
                                } else {
                                    onOpenInBrowser(session.path)
                                }
                            },
                            onLongClick = {
                                actionSession = session
                                showActions = true
                            }
                        )
                        HorizontalDivider()
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showActions && actionSession != null) {
        val s: Session = actionSession ?: return
        AlertDialog(
            onDismissRequest = { showActions = false; actionSession = null },
            title = {
                Column {
                    Text(s.name, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium)
                    Text(s.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column {
                    ActionItem("📂 فتح في تصفح الملفات") {
                        showActions = false
                        viewModel.touch(s.id)
                        actionSession = null
                        onOpenInBrowser(s.path)
                    }
                    ActionItem("🌳 عرض في عارض الشجرة") {
                        showActions = false
                        viewModel.touch(s.id)
                        actionSession = null
                        onOpenInTreeViewer(s.path)
                    }
                    ActionItem("✏️ إعادة تسمية") { showActions = false; showRenameDialog = true }
                    ActionItem("📝 تعديل الملاحظة") { showActions = false; showNoteDialog = true }
                    ActionItem("🗑️ حذف", isDanger = true) { showActions = false; showDeleteConfirm = true }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActions = false; actionSession = null }) { Text("إلغاء") }
            }
        )
    }

    if (showAddDialog) {
        AddSessionDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onAdd = { name, path, type ->
                viewModel.add(name, path, type)
                showAddDialog = false
            }
        )
    }

    if (showRenameDialog && actionSession != null) {
        val s: Session = actionSession ?: return
        RenameDialog(
            current = s.name,
            onDismiss = { showRenameDialog = false; actionSession = null },
            onConfirm = { newName -> viewModel.rename(s, newName); showRenameDialog = false; actionSession = null }
        )
    }

    if (showNoteDialog && actionSession != null) {
        val s: Session = actionSession ?: return
        NoteDialog(
            current = s.note,
            onDismiss = { showNoteDialog = false; actionSession = null },
            onConfirm = { newNote -> viewModel.updateNote(s, newNote); showNoteDialog = false; actionSession = null }
        )
    }

    if (showDeleteConfirm && actionSession != null) {
        val s: Session = actionSession ?: return
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false; actionSession = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("سيتم حذف الجلسة: " + s.name + "\n(لن تُحذف الملفات الفعلية)") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(s.id); showDeleteConfirm = false; actionSession = null }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false; actionSession = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun SortItem(label: String, selected: Boolean, onClick: () -> Unit) {
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

@Composable
private fun ActionItem(label: String, isDanger: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SessionRow(
    session: Session,
    onOpen: () -> Unit,
    onLongClick: () -> Unit
) {
    val exists = remember(session.path) {
        try {
            val f = File(session.path)
            f.exists() && f.isDirectory
        } catch (e: Exception) { false }
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() }.padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(session.type.emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(session.name, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                if (!exists) {
                    Spacer(Modifier.width(6.dp))
                    Text("⚠️", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(session.path, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Row {
                Text(session.type.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(" • " + formatTime(session.lastOpened), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (session.note.isNotBlank()) {
                Text(session.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1)
            }
        }
        IconButton(onClick = onLongClick) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
    }
}

@Composable
private fun AddSessionDialog(
    viewModel: SessionsViewModel,
    onDismiss: () -> Unit,
    onAdd: (String, String, SessionType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(SessionType.ANDROID) }
    var showFolderPicker by remember { mutableStateOf(false) }
    val existing = viewModel.findExistingByPath(path)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة جلسة") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشروع") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        label = { Text("المسار") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    IconButton(
                        onClick = {
                            showFolderPicker = true
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "تصفح المجلدات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (existing != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "⚠️ المسار مسجّل مسبقاً",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    existing.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Text("النوع", style = MaterialTheme.typography.titleSmall)
                SessionType.entries.forEach { t ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { type = t }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t.emoji)
                        Spacer(Modifier.width(8.dp))
                        Text(t.displayName)
                        Spacer(Modifier.weight(1f))
                        if (type == t) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && path.isNotBlank()) {
                        if (existing != null) {
                            // استبدال الجلسة القديمة
                            viewModel.delete(existing.id)
                        }
                        onAdd(name.trim(), path.trim(), type)
                    }
                },
                enabled = name.isNotBlank() && path.isNotBlank()
            ) {
                Text(if (existing != null) "استبدال" else "إضافة")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )

    if (showFolderPicker) {
        FolderPickerDialog(
            title = "اختر مجلد المشروع",
            initialPath = if (path.isNotBlank()) path else null,
            onDismiss = { showFolderPicker = false },
            onSelect = { picked ->
                path = picked
                showFolderPicker = false
            }
        )
    }
}

@Composable
private fun RenameDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعادة التسمية") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("الاسم الجديد") },
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
private fun NoteDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ملاحظة") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("الملاحظة") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines = 5
            )
        },
        confirmButton = { Button(onClick = { onConfirm(value.trim()) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

private fun formatTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "الآن"
        diff < hour -> "${diff / minute} د"
        diff < day -> "${diff / hour} س"
        diff < 30 * day -> "${diff / day} يوم"
        else -> {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.format(Date(timestamp))
        }
    }
}
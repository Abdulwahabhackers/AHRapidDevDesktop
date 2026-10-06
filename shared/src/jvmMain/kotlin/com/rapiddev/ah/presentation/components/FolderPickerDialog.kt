package com.rapiddev.ah.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.core.utils.FolderHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FolderPickerDialog(
    title: String = "اختر مجلداً",
    initialPath: String? = null,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {

    val home = remember { System.getProperty("user.home") ?: "/" }

    val defaultStart = remember {
        when {
            initialPath != null && File(initialPath).exists() && File(initialPath).isDirectory -> initialPath
            FolderHistory.getLast().let { it != null && File(it).exists() } -> FolderHistory.getLast()!!
            File(home, "Documents").exists() -> File(home, "Documents").absolutePath
            else -> home
        }
    }

    var currentPath by remember { mutableStateOf(defaultStart) }
    var folders by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    fun reload() {
        val old = currentPath
        currentPath = ""
        currentPath = old
    }

    LaunchedEffect(currentPath) {
        if (currentPath.isBlank()) return@LaunchedEffect
        isLoading = true
        folders = withContext(Dispatchers.IO) {
            try {
                val dir = File(currentPath)
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()
                        ?.filter { it.isDirectory && !it.name.startsWith(".") }
                        ?.sortedBy { it.name.lowercase() }
                        ?: emptyList()
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            currentPath,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { reload() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { showCreateDialog = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "مجلد جديد", modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 340.dp, max = 540.dp)
            ) {

                // ===== اختصارات سريعة =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickButton(Icons.Default.Home, "المستخدم") {
                        currentPath = home
                    }
                    QuickButton(Icons.Default.Computer, "C:") {
                        val roots = File.listRoots()
                        val cDrive = roots.firstOrNull { it.absolutePath.startsWith("C", ignoreCase = true) }
                        if (cDrive != null) currentPath = cDrive.absolutePath
                    }
                    QuickButton(Icons.Default.PhoneAndroid, "AS Projects") {
                        val asProjects = File(home, "AndroidStudioProjects")
                        if (asProjects.exists()) currentPath = asProjects.absolutePath
                    }
                    QuickButton(Icons.Default.Download, "التنزيلات") {
                        val dl = File(home, "Downloads")
                        if (dl.exists()) currentPath = dl.absolutePath
                    }
                }

                // ===== المسارات المحفوظة =====
                val recent = FolderHistory.getRecent().filter { File(it).exists() }.take(3)
                if (recent.isNotEmpty() && !recent.contains(currentPath)) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        recent.forEach { path ->
                            RecentButton(path) { currentPath = path }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                HorizontalDivider()

                // المجلد الأعلى
                val parent = File(currentPath).parentFile
                if (parent != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentPath = parent.absolutePath }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(".. الرجوع", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        Text(
                            parent.name.ifBlank { parent.absolutePath },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    HorizontalDivider()
                }

                // القائمة
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (folders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "لا توجد مجلدات فرعية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(folders) { folder ->
                            FolderRow(folder) { currentPath = folder.absolutePath }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                FolderHistory.add(currentPath)
                onSelect(currentPath)
            }) {
                Text("اختيار هذا المجلد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )

    if (showCreateDialog) {
        CreateFolderDialog(
            currentPath = currentPath,
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                val newDir = File(currentPath, name)
                val ok = try { newDir.mkdirs() } catch (e: Exception) { false }
                showCreateDialog = false
                if (ok) {
                    reload()
                }
            }
        )
    }
}

@Composable
private fun FolderRow(folder: File, onClick: () -> Unit) {
    val stats = remember(folder.absolutePath) {
        try {
            val files = folder.listFiles() ?: emptyArray()
            val dirsCount = files.count { it.isDirectory }
            val filesCount = files.count { it.isFile }
            Pair(dirsCount, filesCount)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }

    val hasChildren = stats.first > 0 || stats.second > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            folder.name,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        if (hasChildren) {
            Text(
                buildString {
                    if (stats.first > 0) append("📁${stats.first}")
                    if (stats.first > 0 && stats.second > 0) append(" ")
                    if (stats.second > 0) append("📄${stats.second}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RowScope.QuickButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun RowScope.RecentButton(path: String, onClick: () -> Unit) {
    val name = remember(path) { File(path).name.ifBlank { path } }
    TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.width(2.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun CreateFolderDialog(
    currentPath: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مجلد جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    currentPath,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المجلد") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("إنشاء") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
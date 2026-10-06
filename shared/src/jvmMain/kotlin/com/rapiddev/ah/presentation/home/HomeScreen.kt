package com.rapiddev.ah.presentation.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rapiddev.ah.presentation.navigation.Screen

data class HomeAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    val actions = listOf(
        HomeAction("دليل الاستخدام", "تعلم كيف تحول فكرتك إلى تطبيق", Icons.Default.MenuBook, Screen.Guide),
        HomeAction("استيراد أكواد AI", "الصق رد الذكاء الاصطناعي واستخرج الأكواد", Icons.Default.AutoAwesome, Screen.ImportAi),
        HomeAction("Smart Import (تجريبي)", "تحليل ذكي للرسائل مع تعديلات دقيقة", Icons.Default.Psychology, Screen.SmartImport),
        HomeAction("تصدير مشروع للـ AI", "حوّل مشروعك لملفات txt جاهزة للذكاء الاصطناعي", Icons.Default.Upload, Screen.ExportAi),
        HomeAction("مشروع جديد", "أنشئ مشروع Android أو ويب أو من شجرة", Icons.Default.Add, Screen.NewProject),
        HomeAction("تصفح الملفات", "تصفح وإدارة ملفات الحاسوب", Icons.Default.Folder, Screen.Browser),
        HomeAction("عرض شجرة", "اعرض شجرة أي مجلد واحفظها", Icons.Default.AccountTree, Screen.TreeViewer),
        HomeAction("دمج المشاريع", "ادمج مشروع محدّث مع مشروعك الحالي", Icons.Default.Merge, Screen.Merge),
        HomeAction("الجلسات", "مشاريعك المحفوظة للوصول السريع", Icons.Default.History, Screen.Sessions),
        HomeAction("الإعدادات", "المظهر والإعدادات العامة", Icons.Default.Settings, Screen.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AH RapidDev", fontWeight = FontWeight.Black)
                        Text(
                            "من الفكرة إلى تطبيق — Desktop",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(actions) { action ->
                ActionCard(action = action, onClick = { onNavigate(action.screen) })
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ActionCard(action: HomeAction, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(action.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    action.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
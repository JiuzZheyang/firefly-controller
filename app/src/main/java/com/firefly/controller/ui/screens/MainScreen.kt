package com.firefly.controller.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firefly.controller.data.model.*
import com.firefly.controller.ui.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState,
    onCommandChange: (String) -> Unit = {},
    onSendCommand: () -> Unit = {},
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("萤火状态", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    ConnectionBadge(isConnected = state.isConnected)
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            item { Spacer(Modifier.height(4.dp)) }
            
            // Error Banner
            state.error?.let { error ->
                item {
                    ErrorBanner(error = error, onDismiss = onClearError)
                }
            }
            
            // System Status Card
            state.status?.system?.let { sys ->
                item {
                    SystemStatusCard(system = sys)
                }
            }
            
            // Session Info Card
            state.status?.session?.let { session ->
                item {
                    SessionInfoCard(session = session)
                }
            }
            
            // Recent Tasks
            val recentTasks = state.status?.recentTasks ?: emptyList()
            if (recentTasks.isNotEmpty()) {
                item {
                    Text(
                        "最近任务",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(recentTasks) { task ->
                    TaskItem(task = task)
                }
            }
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SystemStatusCard(system: SystemStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "萤火手机状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // 电池 & 温度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.BatteryFull,
                    label = "电量",
                    value = "${system.battery}%",
                    color = when {
                        system.battery < 20 -> Color.Red
                        system.battery < 50 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
                StatItem(
                    icon = Icons.Default.Thermostat,
                    label = "温度",
                    value = "${system.temperature}°C",
                    color = when {
                        system.temperature > 45 -> Color.Red
                        system.temperature > 40 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
                StatItem(
                    icon = Icons.Default.Memory,
                    label = "内存",
                    value = "${system.memory_percent}%",
                    color = when {
                        system.memory_percent > 80 -> Color.Red
                        system.memory_percent > 60 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // 存储 & 负载
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Storage,
                    label = "存储",
                    value = "${system.disk_percent}%",
                    color = when {
                        system.disk_percent > 80 -> Color.Red
                        system.disk_percent > 60 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
                StatItem(
                    icon = Icons.Default.Speed,
                    label = "负载",
                    value = system.cpu_load.ifEmpty { "N/A" },
                    color = Color(0xFF2196F3)
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    label = "运行",
                    value = system.uptime.ifEmpty { "N/A" },
                    color = Color(0xFF2196F3)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // 详细数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "内存: ${formatMemory(system.memory_used)} / ${formatMemory(system.memory_total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "存储: ${formatDisk(system.disk_used)} / ${formatDisk(system.disk_total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SessionInfoCard(session: SessionInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "会话信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "模型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        session.model ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Tokens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatNumber(session.tokens ?: 0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (task.status) {
                            "succeeded" -> Color(0xFF4CAF50)
                            "failed" -> Color.Red
                            else -> Color(0xFFFF9800)
                        }
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.task.ifEmpty { "Unknown task" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                task.endedAt?.let {
                    Text(
                        formatTime(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                task.status,
                style = MaterialTheme.typography.bodySmall,
                color = when (task.status) {
                    "succeeded" -> Color(0xFF4CAF50)
                    "failed" -> Color.Red
                    else -> Color(0xFFFF9800)
                }
            )
        }
    }
}

@Composable
fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(8.dp))
            Text(
                error,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun ConnectionBadge(isConnected: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isConnected) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color.Red.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Color(0xFF4CAF50) else Color.Red)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (isConnected) "在线" else "离线",
                style = MaterialTheme.typography.bodySmall,
                color = if (isConnected) Color(0xFF4CAF50) else Color.Red
            )
        }
    }
}

private fun formatMemory(mb: Int): String {
    return if (mb >= 1024) "%.1fG".format(mb / 1024.0) else "${mb}M"
}

private fun formatDisk(gb: Double): String {
    return "%.1fG".format(gb)
}

private fun formatNumber(num: Int): String {
    return when {
        num >= 1000000 -> "%.1fM".format(num / 1000000.0)
        num >= 1000 -> "%.1fK".format(num / 1000.0)
        else -> num.toString()
    }
}

private fun formatTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp * 1000))
    } catch (e: Exception) {
        ""
    }
}

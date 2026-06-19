package com.firefly.controller.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firefly.controller.data.model.*
import com.firefly.controller.ui.UiState
import com.firefly.controller.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState,
    onCommandChange: (String) -> Unit,
    onSendCommand: () -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Firefly Controller", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // 连接状态
                    ConnectionBadge(isConnected = state.isConnected)
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Error Banner
            state.error?.let { error ->
                ErrorBanner(error = error, onDismiss = onClearError)
                Spacer(Modifier.height(8.dp))
            }
            
            // 状态卡片
            StatusCard(state = state)
            Spacer(Modifier.height(12.dp))
            
            // 命令输入区
            CommandInput(
                text = state.commandText,
                isSending = state.isSending,
                response = state.commandResponse,
                onTextChange = onCommandChange,
                onSend = onSendCommand
            )
            
            Spacer(Modifier.height(12.dp))
            
            // 任务列表
            Text(
                "Recent Tasks",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            
            TaskList(
                activeTasks = state.status?.activeTasks ?: emptyList(),
                recentTasks = state.status?.recentTasks ?: emptyList()
            )
        }
    }
}

@Composable
fun ConnectionBadge(isConnected: Boolean) {
    val color = if (isConnected) FireflyGreen else FireflyRed
    val text = if (isConnected) "Online" else "Offline"
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusCard(state: UiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "萤火状态",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (state.status?.activeTasks?.isNotEmpty() == true) "执行中" else "空闲",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.status?.activeTasks?.isNotEmpty() == true) FireflyOrange else FireflyGreen
                    )
                }
                
                state.status?.session?.let { session ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Tokens",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatNumber(session.tokens ?: 0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        session.model?.let {
                            Text(
                                it.substringAfter("/").substringBefore("-"),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 活跃任务
            state.status?.activeTasks?.firstOrNull()?.let { task ->
                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = FireflyOrange
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "当前任务",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            task.task ?: "Unknown",
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommandInput(
    text: String,
    isSending: Boolean,
    response: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "发送命令",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter command...") },
                    enabled = !isSending,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() })
                )
                Spacer(Modifier.width(8.dp))
                
                FilledIconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isSending,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = FireflyOrange
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // 响应
            AnimatedVisibility(visible = response.isNotBlank()) {
                Text(
                    response,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 13.sp,
                    color = FireflyGreen
                )
            }
        }
    }
}

@Composable
fun TaskList(activeTasks: List<TaskInfo>, recentTasks: List<TaskInfo>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 活跃任务优先
        if (activeTasks.isNotEmpty()) {
            item {
                Text(
                    "Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = FireflyOrange
                )
            }
            items(activeTasks) { task ->
                TaskItem(task = task, isActive = true)
            }
        }
        
        // 最近任务
        if (recentTasks.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Recent",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(recentTasks) { task ->
                TaskItem(task = task, isActive = false)
            }
        }
        
        if (activeTasks.isEmpty() && recentTasks.isEmpty()) {
            item {
                Text(
                    "No tasks",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskInfo, isActive: Boolean) {
    val statusColor = when (task.status) {
        "running", "in_progress" -> FireflyOrange
        "pending" -> FireflyBlue
        "succeeded" -> FireflyGreen
        "failed" -> FireflyRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                statusColor.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
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
                    .background(statusColor)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.task ?: "Unknown task",
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatTime(task.endedAt ?: task.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                task.status,
                fontSize = 11.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FireflyRed.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = FireflyRed
            )
            Spacer(Modifier.width(8.dp))
            Text(
                error,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = FireflyRed
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = FireflyRed
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatNumber(n: Int): String {
    return when {
        n >= 1000000 -> String.format("%.1fM", n / 1000000.0)
        n >= 1000 -> String.format("%.1fK", n / 1000.0)
        else -> n.toString()
    }
}

package com.firefly.controller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firefly.controller.data.repository.FireflyRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: FireflyRepository) : ViewModel() {
    
    var serverUrl by mutableStateOf(repository.getServerUrl())
        private set
    
    var isTesting by mutableStateOf(false)
    var testResult by mutableStateOf<String?>(null)
        private set
    
    fun updateServerUrl(url: String) {
        serverUrl = url
    }
    
    fun saveAndTest() {
        viewModelScope.launch {
            isTesting = true
            testResult = null
            
            repository.updateServer(serverUrl)
            
            val success = repository.checkHealth()
            testResult = if (success) "✅ 连接成功！" else "❌ 连接失败"
            isTesting = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 服务器地址
            OutlinedTextField(
                value = viewModel.serverUrl,
                onValueChange = { viewModel.updateServerUrl(it) },
                label = { Text("服务器地址") },
                placeholder = { Text("http://192.168.1.215:5000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Cloud, contentDescription = null)
                }
            )
            
            // 服务器地址说明
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "可用地址：",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "• 本地: http://192.168.1.215:5000\n" +
                        "• 远程: http://120.48.26.76:25000",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // 测试按钮
            Button(
                onClick = { viewModel.saveAndTest() },
                enabled = !viewModel.isTesting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("测试中...")
                } else {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存并测试")
                }
            }
            
            // 测试结果
            viewModel.testResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.contains("成功"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        result,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 版本信息
            Text(
                "萤火控制器 v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

package com.firefly.controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.firefly.controller.data.repository.FireflyRepository
import com.firefly.controller.ui.MainViewModel
import com.firefly.controller.ui.ViewModelFactory
import com.firefly.controller.ui.screens.MainScreen
import com.firefly.controller.ui.screens.SettingsScreen
import com.firefly.controller.ui.screens.SettingsViewModel
import com.firefly.controller.ui.theme.FireflyTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = FireflyRepository(applicationContext)
        
        setContent {
            FireflyTheme {
                var showSettings by remember { mutableStateOf(false) }
                
                if (showSettings) {
                    val settingsViewModel = remember { SettingsViewModel(repository) }
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { showSettings = false }
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val viewModel: MainViewModel = viewModel(
                            factory = ViewModelFactory(repository)
                        )
                        val state by viewModel.uiState.collectAsState()
                        
                        MainScreen(
                            state = state,
                            onRefresh = viewModel::checkConnection,
                            onClearError = viewModel::clearError,
                            onSettingsClick = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}

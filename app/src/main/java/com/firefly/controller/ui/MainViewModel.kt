package com.firefly.controller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firefly.controller.data.model.*
import com.firefly.controller.data.repository.FireflyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val serverUrl: String = "",
    val status: StatusResponse? = null,
    val commandText: String = "",
    val commandResponse: String = "",
    val error: String? = null,
    val isSending: Boolean = false
)

class MainViewModel(private val repository: FireflyRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        _uiState.value = _uiState.value.copy(
            serverUrl = repository.getServerUrl()
        )
        checkConnection()
    }
    
    fun checkConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val connected = repository.checkHealth()
            _uiState.value = _uiState.value.copy(isConnected = connected)
            
            if (connected) {
                loadStatus()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Cannot connect to server"
                )
            }
        }
    }
    
    fun loadStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getStatus()
                .onSuccess { status ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = true,
                        status = status
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }
    
    fun updateCommandText(text: String) {
        _uiState.value = _uiState.value.copy(commandText = text)
    }
    
    fun sendCommand() {
        val command = _uiState.value.commandText.trim()
        if (command.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                commandResponse = "",
                error = null
            )
            
            repository.sendCommand(command)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        commandText = "",
                        commandResponse = "✓ ${response.message ?: "Command sent"}"
                    )
                    // 延迟刷新状态
                    kotlinx.coroutines.delay(2000)
                    loadStatus()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = e.message
                    )
                }
        }
    }
    
    fun updateServer(url: String) {
        repository.updateServer(url)
        _uiState.value = _uiState.value.copy(serverUrl = url)
        checkConnection()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class ViewModelFactory(private val repository: FireflyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}

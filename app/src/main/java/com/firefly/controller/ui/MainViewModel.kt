package com.firefly.controller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firefly.controller.data.model.StatusResponse
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
    val error: String? = null
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

package com.wasama.hustlehub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasama.hustlehub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userId: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInOrRegister(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            // Attempt login first
            val loginResult = repository.login(email, pass)
            
            if (loginResult.isSuccess) {
                _uiState.value = AuthUiState.Success(loginResult.getOrThrow())
            } else {
                // If login fails, attempt registration
                val registerResult = repository.register(email, pass)
                if (registerResult.isSuccess) {
                    _uiState.value = AuthUiState.Success(registerResult.getOrThrow())
                } else {
                    _uiState.value = AuthUiState.Error(
                        registerResult.exceptionOrNull()?.message ?: "Authentication failed"
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

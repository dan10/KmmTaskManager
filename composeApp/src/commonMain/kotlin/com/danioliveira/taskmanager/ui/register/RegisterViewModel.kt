package com.danioliveira.taskmanager.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.domain.usecase.register.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // Navigation callback to be set from outside
    var onRegistrationSuccess: () -> Unit = {}

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    private fun register() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = with(uiState.value) {
                registerUseCase(
                    email = email.text.toString(),
                    password = password.text.toString(),
                    displayName = name.text.toString()
                )
            }

            result.fold(
                onSuccess = {
                    onRegistrationSuccess()
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "An error occurred during registration"
                        )
                    }
                }
            )
        }
    }

    fun handleActions(action: RegisterAction) {
        when (action) {
            is RegisterAction.Register -> register()
        }
    }
}

package com.danioliveira.taskmanager.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class RegisterViewModel : ViewModel() {
    var state by mutableStateOf(RegisterState())
        private set

    var emailText by mutableStateOf("")
        private set

    var passwordText by mutableStateOf("")
        private set

    var confirmPasswordText by mutableStateOf("")
        private set


    val emailHasError: StateFlow<Boolean> = snapshotFlow { emailText }
        .mapLatest { !it.contains("@") || !it.contains(".") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val passwordHasError: StateFlow<Boolean> = snapshotFlow { passwordText }
        .mapLatest { it.length < 6 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val confirmPasswordHasError: StateFlow<Boolean> = snapshotFlow { confirmPasswordText }
        .mapLatest { it != passwordText }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isFormValid: StateFlow<Boolean> = combine(
        emailHasError,
        passwordHasError,
        confirmPasswordHasError
    ) { emailError, passwordError, confirmError ->
        !emailError && !passwordError && !confirmError && emailText.isNotBlank() &&
                passwordText.isNotBlank() && confirmPasswordText.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private fun register() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, errorMessage = null)

                // TODO: Implement actual registration logic here
                delay(2000) // Simulate network delay

                // Registration success
                // TODO: Navigate to next screen

            } catch (e: Exception) {
                state = state.copy(
                    errorMessage = e.message ?: "An error occurred during registration"
                )
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun handleActions(action: RegisterAction) {
        when (action) {
            is RegisterAction.UpdateEmail -> emailText = action.email
            is RegisterAction.UpdatePassword -> passwordText = action.password
            is RegisterAction.UpdateConfirmPassword -> confirmPasswordText = action.confirmPassword
            is RegisterAction.Register -> register()
        }
    }
}
package com.danioliveira.taskmanager.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.domain.usecase.register.RegisterUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // Navigation callback to be set from outside
    var onRegistrationSuccess: () -> Unit = {}

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

                // Use the display name as the email username if not provided
                val displayName = emailText.substringBefore("@")

                // Call the register use case
                val result = registerUseCase(
                    email = emailText,
                    password = passwordText,
                    displayName = displayName
                )

                result.fold(
                    onSuccess = {
                        // Registration success - navigate to next screen
                        onRegistrationSuccess()
                    },
                    onFailure = { error ->
                        state = state.copy(
                            errorMessage = error.message ?: "An error occurred during registration"
                        )
                    }
                )
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

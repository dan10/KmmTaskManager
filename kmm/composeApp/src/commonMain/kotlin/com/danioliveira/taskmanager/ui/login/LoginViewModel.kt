package com.danioliveira.taskmanager.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.domain.usecase.login.LoginUseCase
import com.danioliveira.taskmanager.ui.validation.ValidationUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    var navigateToHome: () -> Unit = {}

    var uiState: LoginState by mutableStateOf(LoginState())
        private set

    var loginText = TextFieldState()

    val passwordText = TextFieldState()

    val emailHasError: StateFlow<Boolean> = snapshotFlow { loginText.text }
        .mapLatest { !ValidationUtils.isEmailValid(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val passwordHasError: StateFlow<Boolean> = snapshotFlow { passwordText.text }
        .mapLatest { !ValidationUtils.isPasswordValid(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isFormValid: StateFlow<Boolean> = combine(
        emailHasError,
        passwordHasError
    ) { emailError, passwordError ->
        !emailError && !passwordError
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )


    private fun login() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            loginUseCase(loginText.text.toString(), passwordText.text.toString())
                .onSuccess {
                    uiState = uiState.copy(isLoading = false)
                    navigateToHome()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed"
                    )
                }
        }
    }

    fun handleActions(action: LoginAction) {
        when(action) {
            is LoginAction.Login -> login()
        }
    }

}

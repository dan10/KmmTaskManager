package com.danioliveira.taskmanager.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@ExperimentalCoroutinesApi
class LoginViewModel: ViewModel() {

    var uiState: LoginState by mutableStateOf(LoginState())
        private set

    var loginText by mutableStateOf("")
        private set

    var passwordText by mutableStateOf("")
        private set


    val emailHasError: StateFlow<Boolean> = snapshotFlow { loginText }
        .mapLatest { it.matches(emailAddressRegex) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val passwordHasError: StateFlow<Boolean> = snapshotFlow { passwordText }
        .mapLatest { it.length < 8 }
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

    }

    fun handleActions(action: LoginAction) {
        when(action) {
            is LoginAction.UpdateEmail -> loginText = action.email
            is LoginAction.UpdatePassword -> passwordText = action.password
            is LoginAction.Login -> login()
        }
    }

    companion object {
        private val emailAddressRegex = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }
}
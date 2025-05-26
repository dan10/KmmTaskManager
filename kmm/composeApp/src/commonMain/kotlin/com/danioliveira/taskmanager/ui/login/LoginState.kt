package com.danioliveira.taskmanager.ui.login

data class LoginState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginAction {
    data class UpdateEmail(val email: String) : LoginAction
    data object Login : LoginAction
}
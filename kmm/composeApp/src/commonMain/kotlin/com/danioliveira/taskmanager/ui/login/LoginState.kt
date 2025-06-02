package com.danioliveira.taskmanager.ui.login

data class LoginState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginAction {
    data object Login : LoginAction
}
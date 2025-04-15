package com.danioliveira.taskmanager.ui.register

data class RegisterState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface RegisterAction {
    data class UpdateEmail(val email: String) : RegisterAction
    data class UpdatePassword(val password: String) : RegisterAction
    data class UpdateConfirmPassword(val confirmPassword: String) : RegisterAction
    data object Register : RegisterAction
}

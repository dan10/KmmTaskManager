package com.danioliveira.taskmanager.ui.register

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.ui.validation.ValidationUtils

data class RegisterState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: TextFieldState = TextFieldState(),
    val email: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),
    val confirmPassword: TextFieldState = TextFieldState(),
) {

    val nameHasError
        get() = name.text.isEmpty()

    val emailHasError
        get() = !ValidationUtils.isEmailValid(email.text)

    val passwordHasError
        get() = !ValidationUtils.isPasswordValid(password.text)

    val confirmPasswordHasError
        get() = !ValidationUtils.isConfirmPasswordValid(password.text, confirmPassword.text)

    private val emailIsNotEmpty
        get() = email.text.isNotEmpty()

    private val passwordIsNotEmpty
        get() = password.text.isNotEmpty()

    private val confirmPasswordIsNotEmpty
        get() = confirmPassword.text.isNotEmpty()

    val isFormValid
        get() = emailIsNotEmpty && passwordIsNotEmpty && confirmPasswordIsNotEmpty &&
                !emailHasError && !passwordHasError && !confirmPasswordHasError

    val isButtonEnabled
        get() = isFormValid && !isLoading
}

sealed interface RegisterAction {
    data object Register : RegisterAction
}

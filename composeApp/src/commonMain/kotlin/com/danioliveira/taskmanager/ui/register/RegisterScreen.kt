package com.danioliveira.taskmanager.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.components.TrackItButton
import com.danioliveira.taskmanager.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.components.TrackItPasswordField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.app_name
import kmmtaskmanager.composeapp.generated.resources.ic_app_logo
import kmmtaskmanager.composeapp.generated.resources.title_confirm_password
import kmmtaskmanager.composeapp.generated.resources.title_confirm_password_error
import kmmtaskmanager.composeapp.generated.resources.title_email
import kmmtaskmanager.composeapp.generated.resources.title_email_error
import kmmtaskmanager.composeapp.generated.resources.title_password
import kmmtaskmanager.composeapp.generated.resources.title_password_error
import kmmtaskmanager.composeapp.generated.resources.title_register_button
import kmmtaskmanager.composeapp.generated.resources.title_already_have_account
import kmmtaskmanager.composeapp.generated.resources.button_sign_in
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(viewModel: RegisterViewModel = RegisterViewModel()) {
    RegisterScreen(
        state = viewModel.state,
        email = viewModel.emailText,
        password = viewModel.passwordText,
        confirmPassword = viewModel.confirmPasswordText,
        emailHasError = viewModel.emailHasError.value,
        passwordHasError = viewModel.passwordHasError.value,
        confirmPasswordHasError = viewModel.confirmPasswordHasError.value,
        isFormValid = viewModel.isFormValid.value,
        onAction = viewModel::handleActions
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    email: String,
    password: String,
    confirmPassword: String,
    emailHasError: Boolean,
    passwordHasError: Boolean,
    confirmPasswordHasError: Boolean,
    isFormValid: Boolean,
    onAction: (RegisterAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primaryVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_app_logo),
                    tint = Color.Unspecified,
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.h4
                )

                TrackItInputField(
                    value = email,
                    onValueChange = { onAction(RegisterAction.UpdateEmail(it)) },
                    label = stringResource(Res.string.title_email),
                    enabled = !state.isLoading,
                    isError = emailHasError,
                    errorMessage = stringResource(Res.string.title_email_error),
                )

                TrackItPasswordField(
                    value = password,
                    onValueChange = { onAction(RegisterAction.UpdatePassword(it)) },
                    label = stringResource(Res.string.title_password),
                    enabled = !state.isLoading,
                    isError = passwordHasError,
                    errorMessage = stringResource(Res.string.title_password_error),
                )

                TrackItPasswordField(
                    value = confirmPassword,
                    onValueChange = { onAction(RegisterAction.UpdateConfirmPassword(it)) },
                    label = stringResource(Res.string.title_confirm_password),
                    enabled = !state.isLoading,
                    isError = confirmPasswordHasError,
                    errorMessage = stringResource(Res.string.title_confirm_password_error),
                )

                TrackItButton(
                    label = stringResource(Res.string.title_register_button),
                    onClick = { onAction(RegisterAction.Register) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !state.isLoading,
                    isLoading = state.isLoading
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.title_already_have_account),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    TextButton(onClick = { /* TODO: Navigate to login */ }) {
                        Text(
                            text = stringResource(Res.string.button_sign_in),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.button
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    TaskItTheme {
        RegisterScreen(
            state = RegisterState(),
            email = "email",
            password = "password",
            confirmPassword = "password",
            emailHasError = false,
            passwordHasError = false,
            confirmPasswordHasError = false,
            isFormValid = true,
            onAction = {}
        )
    }
}
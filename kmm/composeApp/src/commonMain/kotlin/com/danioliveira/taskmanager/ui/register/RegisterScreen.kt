package com.danioliveira.taskmanager.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kmmtaskmanager.composeapp.generated.resources.button_sign_in
import kmmtaskmanager.composeapp.generated.resources.ic_app_logo
import kmmtaskmanager.composeapp.generated.resources.title_already_have_account
import kmmtaskmanager.composeapp.generated.resources.title_confirm_password
import kmmtaskmanager.composeapp.generated.resources.title_confirm_password_error
import kmmtaskmanager.composeapp.generated.resources.title_email
import kmmtaskmanager.composeapp.generated.resources.title_email_error
import kmmtaskmanager.composeapp.generated.resources.title_name
import kmmtaskmanager.composeapp.generated.resources.title_name_error
import kmmtaskmanager.composeapp.generated.resources.title_password
import kmmtaskmanager.composeapp.generated.resources.title_password_error
import kmmtaskmanager.composeapp.generated.resources.title_register_button
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit
) {
    // Set the navigation callback for successful registration
    viewModel.onRegistrationSuccess = navigateToHome

    // Collect state
    val state by viewModel.uiState.collectAsState()

    RegisterScreen(
        state = state,
        navigateToLogin = navigateToLogin,
        onAction = viewModel::handleActions
    )
}

@Composable
private fun RegisterScreen(
    state: RegisterState,
    navigateToLogin: () -> Unit,
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
                .padding(8.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegisterHeader()

                RegisterForm(
                    name = state.name,
                    email = state.email,
                    password = state.password,
                    confirmPassword = state.confirmPassword,
                    isLoading = state.isLoading,
                    nameHasError = { state.nameHasError },
                    emailHasError = { state.emailHasError },
                    passwordHasError = { state.passwordHasError },
                    confirmPasswordHasError = { state.confirmPasswordHasError },
                    buttonEnabled = { state.isButtonEnabled },
                    onRegisterClick = { onAction(RegisterAction.Register) }
                )

                LoginNavigation(navigateToLogin = navigateToLogin)
            }
        }
    }
}

@Composable
private fun RegisterHeader() {
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
}

@Composable
private fun RegisterForm(
    name: TextFieldState,
    email: TextFieldState,
    password: TextFieldState,
    confirmPassword: TextFieldState,
    isLoading: Boolean,
    nameHasError: () -> Boolean,
    emailHasError: () -> Boolean,
    passwordHasError: () -> Boolean,
    confirmPasswordHasError: () -> Boolean,
    buttonEnabled: () -> Boolean,
    onRegisterClick: () -> Unit
) {
    TrackItInputField(
        state = name,
        label = stringResource(Res.string.title_name),
        enabled = !isLoading,
        isError = nameHasError(),
        lineLimits = TextFieldLineLimits.SingleLine,
        errorMessage = stringResource(Res.string.title_name_error),
    )

    TrackItInputField(
        state = email,
        label = stringResource(Res.string.title_email),
        enabled = !isLoading,
        isError = emailHasError(),
        lineLimits = TextFieldLineLimits.SingleLine,
        errorMessage = stringResource(Res.string.title_email_error),
    )

    TrackItPasswordField(
        state = password,
        label = stringResource(Res.string.title_password),
        enabled = !isLoading,
        isError = passwordHasError(),
        errorMessage = stringResource(Res.string.title_password_error),
    )

    TrackItPasswordField(
        state = confirmPassword,
        label = stringResource(Res.string.title_confirm_password),
        enabled = !isLoading,
        isError = confirmPasswordHasError(),
        errorMessage = stringResource(Res.string.title_confirm_password_error),
    )

    TrackItButton(
        label = stringResource(Res.string.title_register_button),
        onClick = onRegisterClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = buttonEnabled(),
        isLoading = isLoading
    )
}

@Composable
private fun LoginNavigation(navigateToLogin: () -> Unit) {
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
        TextButton(onClick = { navigateToLogin() }) {
            Text(
                text = stringResource(Res.string.button_sign_in),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
private fun RegisterScreenPreview() {
    TaskItTheme {
        RegisterScreen(
            state = RegisterState(),
            navigateToLogin = {},
            onAction = {}
        )
    }
}

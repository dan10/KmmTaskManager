package com.danioliveira.taskmanager.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kmmtaskmanager.composeapp.generated.resources.button_sign_up
import kmmtaskmanager.composeapp.generated.resources.ic_app_logo
import kmmtaskmanager.composeapp.generated.resources.title_email
import kmmtaskmanager.composeapp.generated.resources.title_email_error
import kmmtaskmanager.composeapp.generated.resources.title_login_button
import kmmtaskmanager.composeapp.generated.resources.title_password
import kmmtaskmanager.composeapp.generated.resources.title_password_error
import kmmtaskmanager.composeapp.generated.resources.title_without_account
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    navigateToRegister: () -> Unit = {}
) {
    LoginScreen(
        state = LoginState(),
        email = viewModel.loginText,
        emailHasError = viewModel.emailHasError.value,
        password = viewModel.passwordText,
        passwordHasError = viewModel.passwordHasError.value,
        isFormValid = viewModel.isFormValid.value,
        onAction = viewModel::handleActions,
        navigateToRegister = navigateToRegister
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    email: String,
    emailHasError: Boolean,
    password: String,
    passwordHasError: Boolean,
    isFormValid: Boolean,
    onAction: (LoginAction) -> Unit,
    navigateToRegister: () -> Unit = {}
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
                    contentDescription = null
                )

                Text(
                    text = stringResource(Res.string.app_name),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.h3
                )

                TrackItInputField(
                    value = email,
                    onValueChange = { onAction(LoginAction.UpdateEmail(it)) },
                    label = stringResource(Res.string.title_email),
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = emailHasError,
                    errorMessage = stringResource(Res.string.title_email_error)
                )

                TrackItPasswordField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { onAction(LoginAction.UpdatePassword(it)) },
                    label = stringResource(Res.string.title_password),
                    isError = passwordHasError,
                    errorMessage = stringResource(Res.string.title_password_error),
                    singleLine = true,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(0.dp))
                TrackItButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.title_login_button),
                    onClick = { onAction(LoginAction.Login) },
                    enabled = isFormValid,
                    isLoading = state.isLoading
                )


                LoginAccountLink(
                    modifier = Modifier.fillMaxWidth(),
                    onLinkClick = navigateToRegister
                )
            }
        }
    }
}

@Composable
fun LoginAccountLink(
    modifier: Modifier = Modifier,
    onLinkClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(Res.string.title_without_account),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        TextButton(onClick = onLinkClick) {
            Text(
                stringResource(Res.string.button_sign_up),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    TaskItTheme {
        LoginScreen(
            state = LoginState(),
            email = email,
            emailHasError = true,
            password = password,
            passwordHasError = true,
            isFormValid = true,
            onAction = {
                when (it) {
                    is LoginAction.UpdateEmail -> email = it.email
                    is LoginAction.UpdatePassword -> password = it.password
                    is LoginAction.Login -> Unit
                }
            },
            navigateToRegister = {}
        )
    }
}

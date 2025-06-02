package com.danioliveira.taskmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedSecureTextField
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_hide_password_icon
import kmmtaskmanager.composeapp.generated.resources.content_description_show_password_icon
import kmmtaskmanager.composeapp.generated.resources.ic_visibility
import kmmtaskmanager.composeapp.generated.resources.ic_visibility_off
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrackItInputField(
    state: TextFieldState,
    label: String,
    isError: Boolean,
    errorMessage: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            label = {
                Text(
                    text = label,
                    color = MaterialTheme.colors.onSurface
                )
            },
            lineLimits = lineLimits,
            enabled = enabled,
            isError = isError,
            trailingIcon = trailingIcon
        )
        AnimatedVisibility(visible = isError) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
fun TrackItPasswordField(
    state: TextFieldState,
    label: String,
    isError: Boolean,
    errorMessage: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedSecureTextField(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            label = {
                Text(
                    text = label,
                    color = MaterialTheme.colors.onSurface
                )
            },
            enabled = enabled,
            isError = isError,
            textObfuscationMode = if (passwordVisible) TextObfuscationMode.Visible
            else TextObfuscationMode.RevealLastTyped,
            trailingIcon = {
                PasswordIcon(
                    passwordVisible = passwordVisible,
                    onClick = { passwordVisible = !passwordVisible }
                )
            }
        )
        AnimatedVisibility(visible = isError) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
private fun PasswordIcon(
    passwordVisible: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        val icon = if (passwordVisible) Res.drawable.ic_visibility_off
        else Res.drawable.ic_visibility
        val description = if (passwordVisible)
            stringResource(Res.string.content_description_hide_password_icon)
        else stringResource(Res.string.content_description_show_password_icon)
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = MaterialTheme.colors.onSurface
        )
    }
}
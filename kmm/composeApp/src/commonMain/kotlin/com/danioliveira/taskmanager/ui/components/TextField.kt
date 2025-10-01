package com.danioliveira.taskmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
            value = state.text.toString(),
            onValueChange = { value -> state.setText(value) },
            singleLine = lineLimits == TextFieldLineLimits.SingleLine,
            label = { Text(text = label) },
            isError = isError,
            enabled = enabled,
            trailingIcon = trailingIcon,
        )

        AnimatedVisibility(visible = isError) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
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
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.text.toString(),
            onValueChange = { value -> state.setText(value) },
            label = { Text(text = label) },
            enabled = enabled,
            isError = isError,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                PasswordIcon(
                    passwordVisible = passwordVisible,
                    onClick = { passwordVisible = !passwordVisible }
                )
            },
        )

        AnimatedVisibility(visible = isError) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun TextFieldState.setText(value: String) {
    if (text.toString() == value) return
    // reset to the new value by replacing all content
    edit {
        replace(0, length, value)
    }
}

@Composable
private fun PasswordIcon(
    passwordVisible: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        val iconRes =
            if (passwordVisible) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility
        val description = if (passwordVisible) {
            stringResource(Res.string.content_description_hide_password_icon)
        } else {
            stringResource(Res.string.content_description_show_password_icon)
        }

        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
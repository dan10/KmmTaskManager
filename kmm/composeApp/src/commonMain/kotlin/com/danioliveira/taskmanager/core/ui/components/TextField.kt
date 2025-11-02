package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_hide_password_icon
import kmmtaskmanager.composeapp.generated.resources.content_description_show_password_icon
import kmmtaskmanager.composeapp.generated.resources.ic_visibility
import kmmtaskmanager.composeapp.generated.resources.ic_visibility_off
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Standard text input field for TaskIt app.
 *
 * Provides consistent styling and error handling across the app.
 * Supports single-line and multi-line input via [lineLimits] parameter.
 *
 * @param state Text field state managing the input value
 * @param label Label text displayed above the field
 * @param modifier Modifier to be applied to the field
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display when [isError] is true
 * @param enabled Whether the field accepts user input
 * @param lineLimits Line limits for the text field
 * @param trailingIcon Optional trailing icon composable
 */
@Composable
fun TaskItInputField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            lineLimits = lineLimits,
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

/**
 * Password input field with visibility toggle for TaskIt app.
 *
 * Provides a secure password input with show/hide functionality.
 * Automatically manages password visibility state internally.
 *
 * @param state Text field state managing the password value
 * @param label Label text displayed above the field
 * @param modifier Modifier to be applied to the field
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display when [isError] is true
 * @param enabled Whether the field accepts user input
 */
@Composable
fun TaskItPasswordField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true
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

// ============================================================================
// Preview Functions
// ============================================================================

@Preview
@Composable
private fun TaskItInputFieldPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Normal state
            TaskItInputField(
                state = rememberTextFieldState("Sample text"),
                label = "Email"
            )
            
            // Error state
            TaskItInputField(
                state = rememberTextFieldState("invalid@"),
                label = "Email",
                isError = true,
                errorMessage = "Please enter a valid email address"
            )
            
            // Disabled state
            TaskItInputField(
                state = rememberTextFieldState("Disabled field"),
                label = "Username",
                enabled = false
            )
            
            // Multi-line
            TaskItInputField(
                state = rememberTextFieldState("This is a longer text that spans multiple lines to demonstrate the multi-line capability"),
                label = "Description",
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 5)
            )
            
            // With trailing icon
            TaskItInputField(
                state = rememberTextFieldState("Search query"),
                label = "Search",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun TaskItPasswordFieldPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Normal state
            TaskItPasswordField(
                state = rememberTextFieldState("password123"),
                label = "Password"
            )
            
            // Error state
            TaskItPasswordField(
                state = rememberTextFieldState("short"),
                label = "Password",
                isError = true,
                errorMessage = "Password must be at least 8 characters"
            )
            
            // Disabled state
            TaskItPasswordField(
                state = rememberTextFieldState("disabled"),
                label = "Password",
                enabled = false
            )
        }
    }
}

@Preview
@Composable
private fun TaskItInputFieldStatesPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Input Field States",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            TaskItInputField(
                state = rememberTextFieldState("Normal"),
                label = "Normal State"
            )
            
            TaskItInputField(
                state = rememberTextFieldState("Error"),
                label = "Error State",
                isError = true,
                errorMessage = "This field has an error"
            )
            
            TaskItInputField(
                state = rememberTextFieldState("Disabled"),
                label = "Disabled State",
                enabled = false
            )
        }
    }
}

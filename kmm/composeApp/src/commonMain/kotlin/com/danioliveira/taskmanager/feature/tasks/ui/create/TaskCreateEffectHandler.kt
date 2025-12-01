package com.danioliveira.taskmanager.feature.tasks.ui.create

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun TaskCreateEffectHandler(
    viewModel: TaskCreateViewModel,
    snackbarHostState: SnackbarHostState,
    onDismiss: (Boolean) -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TaskCreateEffect.ShowSuccessSnackbar -> {
                    // Don't show success snackbar, just dismiss with true
                }
                is TaskCreateEffect.ShowErrorSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        effect.onAction?.invoke()
                    }
                }
                is TaskCreateEffect.TaskCreatedSuccessfully -> {
                    onDismiss(true)
                }
            }
        }
    }
}
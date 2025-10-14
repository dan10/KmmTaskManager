package com.danioliveira.taskmanager.feature.tasks.ui.edit

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles the side effects (effects) from the EditTaskViewModel.
 *
 * @param viewModel The EditTaskViewModel
 * @param snackbarHostState The SnackbarHostState for showing snackbars
 * @param onTaskUpdated Callback when the task is successfully updated
 * @param onTaskDeleted Callback when the task is successfully deleted
 */
@Composable
fun EditTaskEffectHandler(
    viewModel: EditTaskViewModel,
    snackbarHostState: SnackbarHostState,
    onTaskUpdated: () -> Unit,
    onTaskDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is EditTaskEffect.ShowSuccessSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is EditTaskEffect.ShowErrorSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                is EditTaskEffect.TaskUpdatedSuccessfully -> {
                    onTaskUpdated()
                }
                is EditTaskEffect.TaskDeletedSuccessfully -> {
                    onTaskDeleted()
                }
            }
        }
    }
}


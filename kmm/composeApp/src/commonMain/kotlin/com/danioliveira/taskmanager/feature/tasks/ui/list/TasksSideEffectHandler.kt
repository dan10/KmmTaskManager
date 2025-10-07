package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlin.uuid.Uuid

@Composable
fun TasksSideEffectHandler(
    viewModel: TasksViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToTaskDetail: (Uuid) -> Unit,
    onShowCreateTaskBottomSheet: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TasksViewModel.TaskUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is TasksViewModel.TaskSideEffect.ShowConfirmationSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = sideEffect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        sideEffect.onAction.invoke()
                    }
                }
                is TasksViewModel.TaskSideEffect.ShowErrorSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = sideEffect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        sideEffect.onAction?.invoke()
                    }
                }
                is TasksViewModel.TaskSideEffect.ShowSuccessSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is TasksViewModel.TaskSideEffect.NavigateToTaskDetail -> {
                    onNavigateToTaskDetail(sideEffect.taskId)
                }
                is TasksViewModel.TaskSideEffect.ShowCreateTaskBottomSheet -> {
                    onShowCreateTaskBottomSheet()
                }
            }
        }
    }
}
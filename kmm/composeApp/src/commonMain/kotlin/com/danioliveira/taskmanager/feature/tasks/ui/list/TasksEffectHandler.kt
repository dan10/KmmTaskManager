package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlin.uuid.Uuid

@Composable
fun TasksEffectHandler(
    viewModel: TasksViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToTaskDetail: (Uuid) -> Unit,
    onShowCreateTaskBottomSheet: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TasksEffect.ShowConfirmationSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        effect.onAction.invoke()
                    }
                }
                is TasksEffect.ShowErrorSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        effect.onAction?.invoke()
                    }
                }
                is TasksEffect.ShowSuccessSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is TasksEffect.NavigateToTaskDetail -> {
                    onNavigateToTaskDetail(effect.taskId)
                }
                is TasksEffect.ShowCreateTaskBottomSheet -> {
                    onShowCreateTaskBottomSheet()
                }
            }
        }
    }
}
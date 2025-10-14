package com.danioliveira.taskmanager.feature.tasks.ui.edit

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.ui.components.TaskItCreateEditButtons
import com.danioliveira.taskmanager.core.ui.components.TaskItErrorMessage
import com.danioliveira.taskmanager.core.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.util.HapticFeedbackType
import com.danioliveira.taskmanager.util.rememberHapticFeedback
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_delete
import kmmtaskmanager.composeapp.generated.resources.edit_task
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditTaskScreen(
    taskId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit,
    viewModel: EditTaskViewModel = koinViewModel(key = "edit-task-$taskId") { parametersOf(taskId) }
) {
    val haptic = rememberHapticFeedback()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    EditTaskEffectHandler(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onTaskUpdated = {
            haptic.performHapticFeedback(HapticFeedbackType.Success)
            onBack()
        },
        onTaskDeleted = onBack
    )

    val state by viewModel.uiState.collectAsState()
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Surface(color = Color(0XFFF1F5F9)) {
        Scaffold(
            topBar = {
                TaskItTopAppBar(
                    title = stringResource(Res.string.edit_task),
                    onNavigateBack = onBack,
                    actions = {
                        IconButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Error)
                            viewModel.handleActions(EditTaskAction.DeleteTask) 
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.content_description_delete)
                            )
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
            with(sharedTransitionScope) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "task-card-$taskId"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    // Error message
                    TaskItErrorMessage(errorMessage = state.errorMessage)

                    // Form fields
                    EditTaskFormFields(
                        state = state,
                        priorityDropdownExpanded = priorityDropdownExpanded,
                        onPriorityDropdownExpandedChange = { priorityDropdownExpanded = it },
                        onPrioritySelected = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            viewModel.handleActions(EditTaskAction.SetPriority(it)) 
                        },
                        statusDropdownExpanded = statusDropdownExpanded,
                        onStatusDropdownExpandedChange = { statusDropdownExpanded = it },
                        onStatusSelected = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            viewModel.handleActions(EditTaskAction.SetStatus(it)) 
                        },
                        onDateSelected = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            viewModel.handleActions(EditTaskAction.SetDate(it)) 
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Buttons
                    TaskItCreateEditButtons(
                        isCreating = false,
                        isLoading = state.isLoading,
                        isButtonEnabled = state.isButtonEnabled,
                        onCancel = {
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            onBack()
                        },
                        onCreateOrUpdate = {
                            haptic.performHapticFeedback(HapticFeedbackType.Success)
                            viewModel.handleActions(EditTaskAction.UpdateTask)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


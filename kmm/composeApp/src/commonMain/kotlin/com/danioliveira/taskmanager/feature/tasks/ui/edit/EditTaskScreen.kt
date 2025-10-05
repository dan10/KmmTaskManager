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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskCreateEditAction
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskCreateEditViewModel
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskFormFields
import com.danioliveira.taskmanager.util.HapticFeedbackType
import com.danioliveira.taskmanager.util.rememberHapticFeedback
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_delete
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditTaskScreen(
    taskId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit,
    viewModel: TaskCreateEditViewModel = koinViewModel(key = "edit-task-$taskId")
) {
    val haptic = rememberHapticFeedback()
    
    // Initialize the ViewModel with the task ID
    LaunchedEffect(taskId) {
        viewModel.initialize(taskId, null)
    }

    viewModel.onTaskUpdated = {
        haptic.performHapticFeedback(HapticFeedbackType.Success)
        onBack()
    }
    viewModel.onTaskDeleted = onBack

    val state by viewModel.uiState.collectAsState()
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Surface(color = Color(0XFFF1F5F9)) {
        Scaffold(
            topBar = {
                TaskItTopAppBar(
                    title = "Edit Task",
                    onNavigateBack = onBack,
                    actions = {
                        IconButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Error)
                            viewModel.handleActions(TaskCreateEditAction.DeleteTask) 
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.content_description_delete)
                            )
                        }
                    }
                )
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
                    TaskFormFields(
                        state = state,
                        priorityDropdownExpanded = priorityDropdownExpanded,
                        onPriorityDropdownExpandedChange = { priorityDropdownExpanded = it },
                        onPrioritySelected = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            viewModel.handleActions(TaskCreateEditAction.SetPriority(it)) 
                        },
                        statusDropdownExpanded = statusDropdownExpanded,
                        onStatusDropdownExpandedChange = { statusDropdownExpanded = it },
                        onStatusSelected = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            viewModel.handleActions(TaskCreateEditAction.SetStatus(it)) 
                        },
                        onDateSelected = { 
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            viewModel.handleActions(TaskCreateEditAction.SetDate(it)) 
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Buttons
                    TaskItCreateEditButtons(
                        isCreating = state.isCreating,
                        isLoading = state.isLoading,
                        isButtonEnabled = state.isButtonEnabled,
                        onCancel = {
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            onBack()
                        },
                        onCreateOrUpdate = {
                            haptic.performHapticFeedback(HapticFeedbackType.Success)
                            viewModel.handleActions(TaskCreateEditAction.UpdateTask)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


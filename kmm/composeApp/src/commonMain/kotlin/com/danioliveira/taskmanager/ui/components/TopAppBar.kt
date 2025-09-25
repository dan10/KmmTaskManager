package com.danioliveira.taskmanager.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.content_description_delete
import org.jetbrains.compose.resources.stringResource

/**
 * Standard top app bar with back navigation and title.
 */
@Composable
fun TaskItTopAppBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        }
    )
}

/**
 * Top app bar with back navigation, title, and optional action buttons.
 */
@Composable
fun TaskItTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        },
        actions = { actions() }
    )
}

/**
 * Top app bar for create/edit screens with optional delete action.
 */
@Composable
fun TaskItCreateEditTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    showDeleteAction: Boolean = false,
    onDelete: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        },
        actions = {
            if (showDeleteAction) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.content_description_delete)
                    )
                }
            }
        }
    )
}

/**
 * Top app bar with a single action icon.
 */
@Composable
fun TaskItTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    actionIcon: ImageVector,
    actionContentDescription: String,
    onActionClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionContentDescription
                )
            }
        }
    )
} 
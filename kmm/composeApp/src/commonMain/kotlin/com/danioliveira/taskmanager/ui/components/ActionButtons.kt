package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_delete_task
import kmmtaskmanager.composeapp.generated.resources.content_description_edit_task
import kmmtaskmanager.composeapp.generated.resources.task_cancel_button
import kmmtaskmanager.composeapp.generated.resources.task_create_button
import kmmtaskmanager.composeapp.generated.resources.task_delete_button
import kmmtaskmanager.composeapp.generated.resources.task_edit_button
import kmmtaskmanager.composeapp.generated.resources.task_update_button
import org.jetbrains.compose.resources.stringResource

/**
 * Primary and secondary action buttons layout.
 */
@Composable
fun TaskItActionButtons(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    secondaryText: String,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier.weight(1f),
            enabled = secondaryEnabled
        ) {
            Text(secondaryText)
        }

        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.weight(1f),
            enabled = primaryEnabled
        ) {
            if (isLoading) {
                TaskItSmallLoadingIndicator()
            } else {
                Text(
                    text = primaryText,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Create/Edit screen action buttons (Cancel/Save).
 */
@Composable
fun TaskItCreateEditButtons(
    isCreating: Boolean,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onCancel: () -> Unit,
    onCreateOrUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    TaskItActionButtons(
        primaryText = stringResource(
            if (isCreating) Res.string.task_create_button else Res.string.task_update_button
        ),
        onPrimaryClick = onCreateOrUpdate,
        secondaryText = stringResource(Res.string.task_cancel_button),
        onSecondaryClick = onCancel,
        modifier = modifier,
        primaryEnabled = isButtonEnabled,
        secondaryEnabled = !isLoading,
        isLoading = isLoading
    )
}

/**
 * Edit and Delete action buttons with icons.
 */
@Composable
fun TaskItEditDeleteButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isDeleting: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onEdit,
            modifier = Modifier.weight(1f),
            enabled = enabled && !isDeleting,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(Res.string.content_description_edit_task),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(stringResource(Res.string.task_edit_button))
        }
        
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.error
            )
        ) {
            if (isDeleting) {
                TaskItSmallLoadingIndicator()
            } else {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.content_description_delete_task),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Text(stringResource(Res.string.task_delete_button))
        }
    }
}

/**
 * Single action button with icon and text.
 */
@Composable
fun TaskItActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isPrimary: Boolean = true
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            if (isLoading) {
                TaskItSmallLoadingIndicator()
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            if (isLoading) {
                TaskItSmallLoadingIndicator()
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text)
            }
        }
    }
} 
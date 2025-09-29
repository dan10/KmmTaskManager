package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ActionButtonContent(
    icon: ImageVector,
    contentDescription: String?,
    text: String
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
    Text(text)
}

@Composable
fun TaskItPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = colors
    ) {
        if (isLoading) {
            TaskItSmallLoadingIndicator() // Assumes this is defined (e.g., in States.kt)
            Spacer(modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
            Text(text)
        } else if (icon != null) {
            ActionButtonContent(
                icon = icon,
                contentDescription = iconContentDescription,
                text = text
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun TaskItSecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.outlinedButtonColors()
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = colors
    ) {
        if (isLoading) {
            TaskItSmallLoadingIndicator() // Assumes this is defined (e.g., in States.kt)
            Spacer(modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
            Text(text)
        } else if (icon != null) {
            ActionButtonContent(
                icon = icon,
                contentDescription = iconContentDescription,
                text = text
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun TaskItActionButtons(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    secondaryText: String,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    primaryIsLoading: Boolean = false,
    secondaryIsLoading: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskItSecondaryActionButton(
            text = secondaryText,
            onClick = onSecondaryClick,
            modifier = Modifier.weight(1f),
            enabled = secondaryEnabled,
            isLoading = secondaryIsLoading
        )
        TaskItPrimaryActionButton(
            text = primaryText,
            onClick = onPrimaryClick,
            modifier = Modifier.weight(1f),
            enabled = primaryEnabled,
            isLoading = primaryIsLoading
        )
    }
}

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
        primaryIsLoading = isLoading,
        secondaryIsLoading = false
    )
}

@Composable
fun TaskItEditDeleteButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    editEnabled: Boolean = true,
    deleteEnabled: Boolean = true,
    isDeleting: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskItPrimaryActionButton(
            text = stringResource(Res.string.task_edit_button),
            icon = Icons.Filled.Edit,
            iconContentDescription = stringResource(Res.string.content_description_edit_task),
            onClick = onEdit,
            modifier = Modifier.weight(1f),
            enabled = editEnabled && !isDeleting,
            isLoading = false
        )
        TaskItSecondaryActionButton(
            text = stringResource(Res.string.task_delete_button),
            icon = Icons.Filled.Delete,
            iconContentDescription = stringResource(Res.string.content_description_delete_task),
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            enabled = deleteEnabled,
            isLoading = isDeleting,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        )
    }
}


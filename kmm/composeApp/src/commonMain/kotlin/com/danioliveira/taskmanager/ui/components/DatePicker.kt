package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.danioliveira.taskmanager.util.DateFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.date_picker_cancel
import kmmtaskmanager.composeapp.generated.resources.date_picker_ok
import kmmtaskmanager.composeapp.generated.resources.date_picker_placeholder
import kmmtaskmanager.composeapp.generated.resources.date_picker_select_date
import kmmtaskmanager.composeapp.generated.resources.task_due_date_label
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    label: String = stringResource(Res.string.task_due_date_label),
    placeholder: String = stringResource(Res.string.date_picker_placeholder),
    selectedDate: LocalDateTime? = null,
    onDateSelected: (LocalDateTime) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    
    val formattedDate = selectedDate?.let { formatDate(it) } ?: ""

    OutlinedTextField(
        value = formattedDate,
        onValueChange = { },
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        trailingIcon = {
            Icon(
                Icons.Default.DateRange,
                contentDescription = stringResource(Res.string.date_picker_select_date),
                modifier = Modifier.clickable { showModal = true }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showModal = true },
        enabled = false
    )

    if (showModal) {
        Material3DatePickerModal(
            initialDate = selectedDate,
            onDateSelected = { selectedLocalDate ->
                onDateSelected(selectedLocalDate)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun Material3DatePickerModal(
    initialDate: LocalDateTime?,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDateMillis = initialDate?.date?.atTime(12, 0)?.toInstant(TimeZone.UTC)
        ?.toEpochMilliseconds()
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(localDate.atTime(0, 0))
                    }
                }
            ) {
                Text(stringResource(Res.string.date_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.date_picker_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun formatDate(date: LocalDateTime): String {
    return DateFormatter.formatDate(date)
}
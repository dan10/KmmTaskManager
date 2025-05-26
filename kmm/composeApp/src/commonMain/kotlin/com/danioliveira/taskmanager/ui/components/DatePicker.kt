package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    label: String = "Date",
    placeholder: String = "MM/DD/YYYY",
    selectedDate: LocalDateTime? = null,
    onDateSelected: (LocalDateTime) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { formatDate(it) } ?: "",
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Select date",
                modifier = Modifier.clickable { showModal = true }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showModal = true },
        enabled = false // Make the field read-only but still clickable
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = {
                onDateSelected(it)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun DatePickerModal(
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    // In a real app, you would implement a proper date picker dialog
    // For now, we'll just use a simple dialog that selects today's date

    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Text("This is a placeholder for a date picker. In a real app, you would see a calendar here.")
        },
        confirmButton = {
            Button(onClick = { onDateSelected(currentDate) }) {
                Text("Select Today")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Format date as DD/MM/YYYY
fun formatDate(date: LocalDateTime): String {
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${
        date.monthNumber.toString().padStart(2, '0')
    }/${date.year}"
}

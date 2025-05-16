package com.danioliveira.taskmanager.util

import androidx.compose.runtime.Composable
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.date_format_pattern
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.stringResource

/**
 * Utility class for formatting dates according to location.
 */
object DateFormatter {

    /**
     * Formats a LocalDateTime according to the user's locale.
     *
     * @param date The date to format
     * @return The formatted date string
     */
    @OptIn(FormatStringsInDatetimeFormats::class)
    @Composable
    fun formatDate(date: LocalDateTime): String {
        val pattern = stringResource(Res.string.date_format_pattern)
        val format = LocalDateTime.Format { byUnicodePattern(pattern) }
        return format.format(date)
    }
}

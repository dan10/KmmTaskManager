package com.danioliveira.taskmanager.feature.tasks.domain.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Default implementation of TaskSearchHandler.
 * Provides debounced and distinct search query processing.
 */
class TaskSearchHandlerImpl : TaskSearchHandler {

    override fun observeSearchQuery(searchFieldState: TextFieldState): Flow<String?> {
        return snapshotFlow { searchFieldState.text }
            .map { text -> processSearchQuery(text.toString()) }
            .distinctUntilChanged()
    }

    override fun processSearchQuery(searchText: String): String? {
        return searchText.trim().takeIf { it.isNotBlank() }
    }
}

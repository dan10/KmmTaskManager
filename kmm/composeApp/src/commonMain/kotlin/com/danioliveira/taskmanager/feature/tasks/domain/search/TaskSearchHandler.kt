package com.danioliveira.taskmanager.feature.tasks.domain.search

import androidx.compose.foundation.text.input.TextFieldState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for handling task search functionality.
 * Provides reactive search capabilities with debouncing and query transformation.
 */
interface TaskSearchHandler {
    /**
     * Observes search query changes from a TextFieldState and emits processed search queries.
     *
     * @param searchFieldState The text field state to observe
     * @return Flow of search queries (null or blank queries are filtered out)
     */
    fun observeSearchQuery(searchFieldState: TextFieldState): Flow<String?>

    /**
     * Processes raw search text into a usable query.
     *
     * @param searchText The raw search text
     * @return The processed search query, or null if the text is blank
     */
    fun processSearchQuery(searchText: String): String?
}

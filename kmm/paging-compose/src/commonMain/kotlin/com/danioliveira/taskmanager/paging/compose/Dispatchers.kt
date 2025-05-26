package com.danioliveira.taskmanager.paging.compose

import kotlin.coroutines.CoroutineContext

/**
 * A dispatcher that dispatches execution onto the UI thread.
 */
expect val UiDispatcher: CoroutineContext
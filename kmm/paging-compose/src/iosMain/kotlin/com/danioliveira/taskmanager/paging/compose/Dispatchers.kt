package com.danioliveira.taskmanager.paging.compose

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * iOS implementation of UiDispatcher using Dispatchers.Main
 */
actual val UiDispatcher: CoroutineContext = Dispatchers.Main
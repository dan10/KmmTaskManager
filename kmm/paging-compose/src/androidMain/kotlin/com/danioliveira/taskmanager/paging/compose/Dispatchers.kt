package com.danioliveira.taskmanager.paging.compose

import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Android implementation of UiDispatcher using AndroidUiDispatcher.Main
 */
actual val UiDispatcher: CoroutineContext = AndroidUiDispatcher.Main
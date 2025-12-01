package com.danioliveira.taskmanager.testing

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Android implementation that enables testTagsAsResourceId semantic property.
 * This allows testTag() modifiers to be exposed as resource-id attributes in the UI hierarchy,
 * making them discoverable by Appium/UiAutomator.
 */
actual fun Modifier.enableTestTagsAsResourceId(): Modifier = 
    this.semantics { testTagsAsResourceId = true }



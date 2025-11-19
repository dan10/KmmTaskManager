package com.danioliveira.taskmanager.testing

import androidx.compose.ui.Modifier

/**
 * iOS implementation - no-op as test tags are automatically exposed as accessibility identifiers.
 * On iOS, testTag() modifiers are automatically mapped to accessibilityIdentifier without
 * additional configuration.
 */
actual fun Modifier.enableTestTagsAsResourceId(): Modifier = this



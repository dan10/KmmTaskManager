package com.danioliveira.taskmanager.testing

import androidx.compose.ui.Modifier

/**
 * Platform-specific modifier that enables test tags to be exposed as resource IDs on Android.
 * On iOS, this is a no-op as test tags are automatically exposed as accessibility identifiers.
 * 
 * This is particularly useful for bottom sheets, dialogs, and other composables that create
 * separate windows and don't inherit the testTagsAsResourceId semantic from the root app.
 * 
 * Usage:
 * ```
 * ModalBottomSheet(
 *     modifier = Modifier.enableTestTagsAsResourceId(),
 *     // ...
 * ) {
 *     // Bottom sheet content with testTag modifiers
 * }
 * ```
 */
expect fun Modifier.enableTestTagsAsResourceId(): Modifier



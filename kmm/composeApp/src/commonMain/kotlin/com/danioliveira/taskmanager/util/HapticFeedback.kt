package com.danioliveira.taskmanager.util

import androidx.compose.runtime.Composable

/**
 * Haptic feedback utility for providing tactile feedback across platforms.
 * 
 * Usage:
 * ```
 * val haptic = rememberHapticFeedback()
 * Button(onClick = { 
 *     haptic.performHapticFeedback(HapticFeedbackType.Click)
 * })
 * ```
 */
interface HapticFeedback {
    fun performHapticFeedback(type: HapticFeedbackType)
}

enum class HapticFeedbackType {
    /** Light tap - for simple interactions like button presses */
    Click,
    
    /** Medium impact - for selections or toggles */
    LongPress,
    
    /** Strong feedback - for important actions or confirmations */
    Success,
    
    /** Warning feedback - for errors or warnings */
    Error
}

/**
 * Remember a platform-specific haptic feedback implementation.
 */
@Composable
expect fun rememberHapticFeedback(): HapticFeedback


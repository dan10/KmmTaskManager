package com.danioliveira.taskmanager.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class AndroidHapticFeedback(private val view: View) : HapticFeedback {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun performHapticFeedback(type: HapticFeedbackType) {
        val feedbackConstant = when (type) {
            HapticFeedbackType.Click -> HapticFeedbackConstants.KEYBOARD_TAP
            HapticFeedbackType.LongPress -> HapticFeedbackConstants.LONG_PRESS
            HapticFeedbackType.Success -> HapticFeedbackConstants.CONFIRM
            HapticFeedbackType.Error -> HapticFeedbackConstants.REJECT
        }
        view.performHapticFeedback(feedbackConstant)
    }
}

@Composable
actual fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { AndroidHapticFeedback(view) }
}


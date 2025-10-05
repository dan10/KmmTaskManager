package com.danioliveira.taskmanager.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

class IOSHapticFeedback : HapticFeedback {
    private val impactGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    private val notificationGenerator = UINotificationFeedbackGenerator()
    
    init {
        impactGenerator.prepare()
        notificationGenerator.prepare()
    }
    
    override fun performHapticFeedback(type: HapticFeedbackType) {
        when (type) {
            HapticFeedbackType.Click -> {
                val lightGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
                lightGenerator.prepare()
                lightGenerator.impactOccurred()
            }
            HapticFeedbackType.LongPress -> {
                impactGenerator.impactOccurred()
            }
            HapticFeedbackType.Success -> {
                notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            }
            HapticFeedbackType.Error -> {
                notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
            }
        }
    }
}

@Composable
actual fun rememberHapticFeedback(): HapticFeedback {
    return remember { IOSHapticFeedback() }
}


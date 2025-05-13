package com.danioliveira.taskmanager

import androidx.compose.ui.window.ComposeUIViewController
import com.danioliveira.taskmanager.di.KoinInitializer

fun MainViewController() = ComposeUIViewController {
    KoinInitializer.initialize()
    TaskItApp()
}
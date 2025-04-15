package com.danioliveira.taskmanager

import androidx.compose.material.BottomNavigation
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danioliveira.taskmanager.ui.tasks.TasksScreen
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    TaskItTheme {
        Scaffold(
            bottomBar = {
                BottomNavigation() {

                }
            }
        ) {
            TaskItNavHost()
        }
    }
}

@Composable
fun TaskItNavHost() {
    NavHost(
        navController = rememberNavController(),
        startDestination = "tasks"
    ) {
        composable("tasks") {
            TasksScreen()
        }
        composable("task/{taskId}") {
            TaskScreen()
        }
    }
}


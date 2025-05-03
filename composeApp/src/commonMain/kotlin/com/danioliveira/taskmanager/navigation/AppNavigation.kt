package com.danioliveira.taskmanager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.ic_folder
import kmmtaskmanager.composeapp.generated.resources.nav_profile
import kmmtaskmanager.composeapp.generated.resources.nav_projects
import kmmtaskmanager.composeapp.generated.resources.nav_tasks
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class NavIcon {
    data class ImageVectorIcon(val imageVector: ImageVector) : NavIcon()
    data class DrawableResourceIcon(val drawableResource: DrawableResource) : NavIcon()
}

sealed class BottomNavItem(val destination: Screen, val title: StringResource, val icon: NavIcon) {

    object Tasks : BottomNavItem(Screen.Tasks, Res.string.nav_tasks, NavIcon.ImageVectorIcon(Icons.Default.Check))
    object Projects :
        BottomNavItem(Screen.Projects, Res.string.nav_projects, NavIcon.DrawableResourceIcon(Res.drawable.ic_folder))

    object Profile :
        BottomNavItem(Screen.Profile, Res.string.nav_profile, NavIcon.ImageVectorIcon(Icons.Default.Person))
}

sealed interface Screen {
    // Authentication
    object Login : Screen
    object Register : Screen

    // Top level destinations
    object Tasks : Screen
    object Projects : Screen
    object Profile : Screen

    // Task-related screens
    data class TasksDetails(val taskId: String) : Screen

    data class TasksFiles(val taskId: String) : Screen

    data class TasksComments(val taskId: String) : Screen

    // Project-related screens
    data class ProjectDetails(val projectId: String) : Screen

    object CreateTask : Screen
    data class Task(val taskId: String) : Screen
}
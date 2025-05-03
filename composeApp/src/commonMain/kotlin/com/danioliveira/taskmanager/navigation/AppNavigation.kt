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
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class NavIcon {
    data class ImageVectorIcon(val imageVector: ImageVector) : NavIcon()
    data class DrawableResourceIcon(val drawableResource: DrawableResource) : NavIcon()
}

sealed class BottomNavItem(val route: String, val title: StringResource, val icon: NavIcon) : Screen {

    object Tasks : BottomNavItem("tasks", Res.string.nav_tasks, NavIcon.ImageVectorIcon(Icons.Default.Check))
    object Projects :
        BottomNavItem("projects", Res.string.nav_projects, NavIcon.DrawableResourceIcon(Res.drawable.ic_folder))

    object Profile :
        BottomNavItem("profile", Res.string.nav_profile, NavIcon.ImageVectorIcon(Icons.Default.Person))
}

sealed interface Screen {
    // Authentication
    @Serializable
    object Login : Screen

    @Serializable
    object Register : Screen

    // Task-related screens
    @Serializable
    data class TasksDetails(val taskId: String) : Screen

    @Serializable
    data class TasksFiles(val taskId: String) : Screen

    @Serializable
    data class TasksComments(val taskId: String) : Screen

    // Project-related screens
    @Serializable
    data class ProjectDetails(val projectId: String) : Screen

    @Serializable
    data class CreateEditTask(val taskId: String?) : Screen
}
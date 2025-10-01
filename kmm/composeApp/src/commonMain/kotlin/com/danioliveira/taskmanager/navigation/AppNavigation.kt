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

data class TopLevelRoute<T : Any>(val name: StringResource, val route: T, val icon: NavIcon)

val topLevelRoutes = listOf(
    TopLevelRoute(Res.string.nav_tasks, Screen.Tasks, NavIcon.ImageVectorIcon(Icons.Default.Check)),
    TopLevelRoute(
        Res.string.nav_projects,
        Screen.Projects,
        NavIcon.DrawableResourceIcon(Res.drawable.ic_folder)
    ),
    TopLevelRoute(
        Res.string.nav_profile,
        Screen.Profile,
        NavIcon.ImageVectorIcon(Icons.Default.Person)
    )
)

sealed interface Screen {
    // Authentication
    @Serializable
    object Login : Screen

    @Serializable
    object Register : Screen

    @Serializable
    data object Tasks : Screen

    @Serializable
    data object Projects : Screen

    @Serializable
    data object Profile : Screen

    // Task-related screens
    @Serializable
    data class TasksDetails(val taskId: String) : Screen

    // Project-related screens
    @Serializable
    data class ProjectDetails(val projectId: String) : Screen

    @Serializable
    data class CreateEditProject(val projectId: String?) : Screen

    @Serializable
    data class CreateEditTask(val taskId: String?, val projectId: String? = null) : Screen
}

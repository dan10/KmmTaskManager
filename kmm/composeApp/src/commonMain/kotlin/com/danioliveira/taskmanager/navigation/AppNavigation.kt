package com.danioliveira.taskmanager.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import com.danioliveira.taskmanager.LocalNavAnimatedVisibilityScope
import com.danioliveira.taskmanager.feature.projects.navigation.ProjectsBaseRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.TasksBaseRoute
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.ic_folder
import kmmtaskmanager.composeapp.generated.resources.nav_calendar
import kmmtaskmanager.composeapp.generated.resources.nav_projects
import kmmtaskmanager.composeapp.generated.resources.nav_tasks
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

sealed class NavIcon {
    data class ImageVectorIcon(val imageVector: ImageVector) : NavIcon()
    data class DrawableResourceIcon(val drawableResource: DrawableResource) : NavIcon()
}

data class TopLevelRoute<T : Any>(val name: StringResource, val route: T, val icon: NavIcon)

val topLevelRoutes = listOf(
    TopLevelRoute(Res.string.nav_tasks, TasksBaseRoute, NavIcon.ImageVectorIcon(Icons.Default.Check)),
    TopLevelRoute(Res.string.nav_calendar, Screen.Calendar, NavIcon.ImageVectorIcon(Icons.Default.List)),
    TopLevelRoute(
        Res.string.nav_projects,
        ProjectsBaseRoute,
        NavIcon.DrawableResourceIcon(Res.drawable.ic_folder)
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
    data object Calendar : Screen

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
    
    @Serializable
    data class EditTask(val taskId: String) : Screen
}

public inline fun <reified T : Any> NavGraphBuilder.composableWithCompositionLocal(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? =
        null,
    noinline exitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? =
        null,
    noinline popEnterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? =
        enterTransition,
    noinline popExitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? =
        exitTransition,
    noinline sizeTransform:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    SizeTransform?)? =
        null,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform
    ) {
        CompositionLocalProvider(
            LocalNavAnimatedVisibilityScope provides this@composable
        ) {
            content(it)
        }
    }
}

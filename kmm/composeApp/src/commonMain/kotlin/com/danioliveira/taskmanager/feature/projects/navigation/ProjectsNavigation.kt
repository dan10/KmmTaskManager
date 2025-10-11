package com.danioliveira.taskmanager.feature.projects.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.danioliveira.taskmanager.LocalSharedTransitionScope
import com.danioliveira.taskmanager.feature.projects.ui.details.ProjectDetailsScreen
import com.danioliveira.taskmanager.feature.projects.ui.list.ProjectsScreen
import com.danioliveira.taskmanager.navigation.composableWithCompositionLocal
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable data object ProjectsRoute

@Serializable data class ProjectDetailRoute(val projectId: String)

@Serializable data object ProjectsBaseRoute

fun NavController.navigateToProjects(navOptions: NavOptions) = navigate(ProjectsRoute, navOptions)

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.projectsSection(
    onProjectClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onTaskClick: (Uuid) -> Unit,
    taskDetailsDestination: NavGraphBuilder.() -> Unit
) {
    navigation<ProjectsBaseRoute>(startDestination = ProjectsRoute) {
        composableWithCompositionLocal<ProjectsRoute> {
            val sharedTransitionScope = LocalSharedTransitionScope.current
                ?: throw IllegalStateException("No sharedTransitionScope found")
            with(sharedTransitionScope) {
                ProjectsScreen(
                    navigateToProjectDetail = onProjectClick
                )
            }
        }

        composableWithCompositionLocal<ProjectDetailRoute> {
            val sharedTransitionScope = LocalSharedTransitionScope.current
                ?: throw IllegalStateException("No sharedTransitionScope found")
            with(sharedTransitionScope) {
                ProjectDetailsScreen(
                    onBack = onBackClick,
                    navigateToTaskDetail = onTaskClick
                )
            }
        }

        // Include task details screen in this section
        taskDetailsDestination()
    }
}



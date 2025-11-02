package com.danioliveira.taskmanager.feature.projects.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.danioliveira.taskmanager.core.domain.model.Project
import com.danioliveira.taskmanager.core.ui.components.PrincipalTaskItTopAppBar
import com.danioliveira.taskmanager.core.ui.components.ProjectItemSkeleton
import com.danioliveira.taskmanager.core.ui.components.TaskItEmptyState
import com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
import com.danioliveira.taskmanager.core.ui.theme.TaskItThemeExt
import com.danioliveira.taskmanager.feature.projects.ui.create.CreateEditProjectBottomSheet
import com.danioliveira.taskmanager.ui.projects.ProjectsAction
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.ic_folder
import kmmtaskmanager.composeapp.generated.resources.projects_add
import kmmtaskmanager.composeapp.generated.resources.projects_empty_subtitle
import kmmtaskmanager.composeapp.generated.resources.projects_empty_title
import kmmtaskmanager.composeapp.generated.resources.projects_title
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

context(_: SharedTransitionScope, _: AnimatedVisibilityScope)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = koinViewModel(),
    navigateToProjectDetail: (String) -> Unit,
    onGlobalSearch: (String) -> Unit = {}
) {
    var showCreateProjectBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.checkAndRefresh()
    }
        val onAction: (ProjectsAction) -> Unit = { action ->
            when (action) {
                is ProjectsAction.OpenProjectDetails -> {
                    navigateToProjectDetail(action.projectId)
                }

                is ProjectsAction.OpenCreateProject -> {
                    // Show the BottomSheet instead of navigating
                    showCreateProjectBottomSheet = true
                }

                else -> {
                    viewModel.handleActions(action)
                }
            }
        }

        ProjectsScreen(
            pagingItems = viewModel.projectFlow.collectAsLazyPagingItems(),
            onAction = onAction,
            onGlobalSearch = onGlobalSearch
        )
        
        // Project Create BottomSheet
        if (showCreateProjectBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showCreateProjectBottomSheet = false
                },
                sheetState = sheetState
            ) {
                CreateEditProjectBottomSheet(
                    projectId = null,
                    onDismiss = {
                        showCreateProjectBottomSheet = false
                    }
                )
            }
        }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun ProjectsTopBar(onGlobalSearch: (String) -> Unit) {
    with(sts) {
        PrincipalTaskItTopAppBar(
            title = stringResource(Res.string.projects_title),
            onSearch = onGlobalSearch,
            modifier = Modifier.sharedBounds(
                sts.rememberSharedContentState(key = "main_top_bar"),
                avs
            )
        )
    }
}

context(_: SharedTransitionScope, _: AnimatedVisibilityScope)
@Composable
private fun ProjectsScreen(
    pagingItems: LazyPagingItems<Project>,
    onAction: (ProjectsAction) -> Unit,
    onGlobalSearch: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            ProjectsTopBar(onGlobalSearch = onGlobalSearch)
        },
        floatingActionButton = {
            ProjectsFloatingActionButton(onAction)
        }
    ) { paddingValues ->
        ProjectsList(
            modifier = Modifier.padding(paddingValues),
            pagingItems = pagingItems,
            onAction = onAction
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun ProjectsFloatingActionButton(onAction: (ProjectsAction) -> Unit) {
    with(sts) {
        FloatingActionButton(
            modifier = Modifier
                .sharedElement(
                    sts.rememberSharedContentState(key = "add_fab"),
                avs
            ),
            onClick = { onAction(ProjectsAction.OpenCreateProject) },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.projects_add),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Composable
private fun ProjectsList(
    pagingItems: LazyPagingItems<Project>,
    onAction: (ProjectsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = pagingItems.itemContentType { "project" }
        ) { index ->
            val project = pagingItems[index]
            if (project != null) {
                // Real project item
                ProjectCard(
                    project = project,
                    onClick = { onAction(ProjectsAction.OpenProjectDetails(project.id)) }
                )
            } else {
                // Placeholder (null item) - show shimmer skeleton
                ProjectItemSkeleton()
            }
        }

        if (pagingItems.itemCount == 0) {
            item {
                TaskItEmptyState(
                    title = stringResource(Res.string.projects_empty_title),
                    message = stringResource(Res.string.projects_empty_subtitle),
                    content = {
                        Image(
                            painter = painterResource(Res.drawable.ic_folder),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    val extendedColors = TaskItThemeExt.colors
    val progressPercentage = calculateProgressPercentage(project.completed, project.total)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.surfaceCard
        )
    ) {
        ProjectCardContent(
            project = project,
            progressPercentage = progressPercentage,
            extendedColors = extendedColors
        )
    }
}

private fun calculateProgressPercentage(completed: Int, total: Int): Int {
    return if (total > 0) {
        ((completed.toFloat() / total) * 100).toInt()
    } else {
        0
    }
}

@Composable
private fun ProjectCardContent(
    project: Project,
    progressPercentage: Int,
    extendedColors: TaskItExtendedColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        ProjectHeader(project, extendedColors)
        
        if (!project.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            ProjectDescription(project.description, extendedColors)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ProjectProgress(
            completed = project.completed,
            total = project.total,
            progressPercentage = progressPercentage,
            extendedColors = extendedColors
        )
    }
}

@Composable
private fun ProjectHeader(project: Project, extendedColors: TaskItExtendedColors) {
    Text(
        text = project.name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = extendedColors.textPrimary
    )
}

@Composable
private fun ProjectDescription(description: String, extendedColors: TaskItExtendedColors) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = extendedColors.textSecondary
    )
}

@Composable
private fun ProjectProgress(
    completed: Int,
    total: Int,
    progressPercentage: Int,
    extendedColors: TaskItExtendedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$completed of $total tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.textSecondary
        )
        Text(
            text = "$progressPercentage%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = extendedColors.textPrimary
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    LinearProgressIndicator(
        progress = { if (total > 0) completed.toFloat() / total else 0f },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = extendedColors.trackNeutral,
        strokeCap = StrokeCap.Round,
    )
}

@Preview
@Composable
private fun ProjectsScreenPreview() {
    val fakeData = List(5) { index ->
        Project(
            id = "project-$index",
            name = "Project $index",
            completed = index,
            inProgress = 2,
            total = 10,
            description = "Description for project $index"
        )
    }
    val pagingData = PagingData.from(fakeData)
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        SharedTransitionScope {
            AnimatedVisibility(true) {
                ProjectsScreen(
                    pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
                    onAction = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun PojectsEmptyScreenPreview() {
    val pagingData = PagingData.from(emptyList<Project>())
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        SharedTransitionScope {
            AnimatedVisibility(true) {
                ProjectsScreen(
                    pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
                    onAction = {}
                )

            }
        }
    }
}

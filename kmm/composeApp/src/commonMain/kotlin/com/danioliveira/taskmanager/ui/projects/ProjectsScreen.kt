package com.danioliveira.taskmanager.ui.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.danioliveira.taskmanager.domain.Project
import com.danioliveira.taskmanager.paging.compose.LazyPagingItems
import com.danioliveira.taskmanager.paging.compose.collectAsLazyPagingItems
import com.danioliveira.taskmanager.paging.compose.itemContentType
import com.danioliveira.taskmanager.paging.compose.itemKey
import com.danioliveira.taskmanager.ui.components.TaskItEmptyState
import com.danioliveira.taskmanager.ui.components.TaskItLoadingState
import com.danioliveira.taskmanager.ui.components.TaskItSmallLoadingIndicator
import com.danioliveira.taskmanager.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_search
import kmmtaskmanager.composeapp.generated.resources.ic_folder
import kmmtaskmanager.composeapp.generated.resources.project_completed
import kmmtaskmanager.composeapp.generated.resources.project_icon
import kmmtaskmanager.composeapp.generated.resources.project_in_progress
import kmmtaskmanager.composeapp.generated.resources.project_total
import kmmtaskmanager.composeapp.generated.resources.projects_add
import kmmtaskmanager.composeapp.generated.resources.projects_all
import kmmtaskmanager.composeapp.generated.resources.projects_empty_subtitle
import kmmtaskmanager.composeapp.generated.resources.projects_empty_title
import kmmtaskmanager.composeapp.generated.resources.projects_search_placeholder
import kmmtaskmanager.composeapp.generated.resources.projects_title
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = koinViewModel(),
    navigateToProjectDetail: (String) -> Unit,
    navigateToCreateProject: () -> Unit
) {
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.checkAndRefresh()
    }

    Surface(color = Color(0XFFF1F5F9)) {
        val onAction: (ProjectsAction) -> Unit = { action ->
            when (action) {
                is ProjectsAction.OpenProjectDetails -> {
                    navigateToProjectDetail(action.projectId)
                }

                is ProjectsAction.OpenCreateProject -> {
                    navigateToCreateProject()
                }

                else -> {
                    viewModel.handleActions(action)
                }
            }
        }

        ProjectsScreen(
            state = viewModel.state,
            pagingItems = viewModel.projectFlow.collectAsLazyPagingItems(),
            onAction = onAction
        )
    }
}

@Composable
private fun ProjectsScreen(
    state: ProjectsState,
    pagingItems: LazyPagingItems<Project>,
    onAction: (ProjectsAction) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        floatingActionButton = {
            ProjectsFloatingActionButton(onAction)
        }
    ) { paddingValues ->
        ProjectsContent(
            paddingValues = paddingValues,
            state = state,
            pagingItems = pagingItems,
            onAction = onAction
        )
    }
}

@Composable
private fun ProjectsFloatingActionButton(onAction: (ProjectsAction) -> Unit) {
    FloatingActionButton(
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

@Composable
private fun ProjectsContent(
    paddingValues: PaddingValues,
    state: ProjectsState,
    pagingItems: LazyPagingItems<Project>,
    onAction: (ProjectsAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        ProjectsHeader()

        ProjectsSearchField(
            searchFieldState = state.searchFieldState
        )

        ProjectsSubheader()

        if (pagingItems.loadState.append == LoadState.Loading && pagingItems.itemCount == 0) {
            TaskItLoadingState()
        } else {
            ProjectsList(
                pagingItems = pagingItems,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun ProjectsHeader() {
    Text(
        text = stringResource(Res.string.projects_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun ProjectsSearchField(
    searchFieldState: TextFieldState
) {
    TrackItInputField(
        state = searchFieldState,
        label = stringResource(Res.string.projects_search_placeholder),
        isError = false,
        errorMessage = "",
        enabled = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        trailingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(Res.string.content_description_search)
            )
        }
    )
}

@Composable
private fun ProjectsSubheader() {
    Text(
        text = stringResource(Res.string.projects_all),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ProjectsList(
    pagingItems: LazyPagingItems<Project>,
    onAction: (ProjectsAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = pagingItems.itemContentType { "project" }
        ) { index ->
            val project = pagingItems[index]
            if (project != null) {
                ProjectCard(
                    project = project,
                    onClick = { onAction(ProjectsAction.OpenProjectDetails(project.id)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (pagingItems.loadState.append == LoadState.Loading) {
            item {
                TaskItSmallLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
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
    val randomColor = remember {
        Color(
            red = Random.nextFloat(),
            green = Random.nextFloat(),
            blue = Random.nextFloat()
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(randomColor.copy(0.15f))
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_folder),
                        contentDescription = stringResource(Res.string.project_icon),
                        tint = randomColor,
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
            progress = { if (project.total > 0) project.completed.toFloat() / project.total else 0f },
            modifier = Modifier.fillMaxWidth(),
            color = randomColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = StrokeCap.Round,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.project_completed, project.completed),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(Res.string.project_in_progress, project.inProgress),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(Res.string.project_total, project.total),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
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
        ProjectsScreen(
            state = ProjectsState(isLoading = false),
            pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun PojectsEmptyScreenPreview() {
    val pagingData = PagingData.from(emptyList<Project>())
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        ProjectsScreen(
            state = ProjectsState(isLoading = false),
            pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
            onAction = {}
        )
    }
}

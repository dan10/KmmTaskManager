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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        // Create a wrapper for the onAction function that handles navigation
        val onAction: (ProjectsAction) -> Unit = { action ->
            when (action) {
                is ProjectsAction.OpenProjectDetails -> {
                    // Handle navigation directly
                    navigateToProjectDetail(action.projectId)
                }

                is ProjectsAction.OpenCreateProject -> {
                    // Handle navigation to create project
                    navigateToCreateProject()
                }

                else -> {
                    // Pass other actions to the ViewModel
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
        backgroundColor = Color(0xFFF1F5F9),
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
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(Res.string.projects_add),
            tint = MaterialTheme.colors.onPrimary
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

        // Show loading indicator when initial loading
        if (pagingItems.loadState.append == LoadState.Loading && pagingItems.itemCount == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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
        style = MaterialTheme.typography.h4,
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
        style = MaterialTheme.typography.h6,
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
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        if (pagingItems.itemCount == 0) {
            item {
                EmptyProjectsList()
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
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
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
                        tint = randomColor, // Apply the random color
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (project.total > 0) project.completed.toFloat() / project.total else 0f,
                color = randomColor,
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.project_completed, project.completed),
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = stringResource(Res.string.project_in_progress, project.inProgress),
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = stringResource(Res.string.project_total, project.total),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
fun EmptyProjectsList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Illustration1
        Image(
            painter = painterResource(Res.drawable.ic_folder),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),

            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 24.dp)
        )

        // Title
        Text(
            text = stringResource(Res.string.projects_empty_title),
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Message
        Text(
            text = stringResource(Res.string.projects_empty_subtitle),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun ProjectsScreenPreview() {
    // Create fake data for preview
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
    // Create pagingData from a list of fake data
    val pagingData = PagingData.from(fakeData)
    // Pass pagingData containing fake data to a MutableStateFlow
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
    // Pass pagingData containing fake data to a MutableStateFlow
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        ProjectsScreen(
            state = ProjectsState(isLoading = false),
            pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
            onAction = {}
        )
    }
}

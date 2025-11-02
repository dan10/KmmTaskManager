package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.app_name
import kmmtaskmanager.composeapp.generated.resources.content_description_close
import kmmtaskmanager.composeapp.generated.resources.content_description_close_search
import kmmtaskmanager.composeapp.generated.resources.content_description_navigate_back
import kmmtaskmanager.composeapp.generated.resources.content_description_search
import kmmtaskmanager.composeapp.generated.resources.global_search_placeholder
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

data class TaskItSearchState(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val isActive: Boolean,
    val onActiveChange: (Boolean) -> Unit,
    val placeholder: String,
    val enableClearOnClose: Boolean = true
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskItTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showNavigationIcon: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
    searchState: TaskItSearchState? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isSearchActive = searchState?.isActive == true

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    TopAppBar(
        modifier = modifier,
        colors = topAppBarColors(),
        navigationIcon = { NavigationIcon(showNavigationIcon, onNavigateBack) },
        title = { 
            TopAppBarTitle(
                title = title,
                isSearchActive = isSearchActive,
                searchState = searchState,
                focusRequester = focusRequester,
                focusManager = focusManager
            )
        },
        actions = {
            actions()
            if (searchState != null) {
                SearchActionIcon(isSearchActive, searchState)
            }
        }
    )
}

@Composable
private fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun NavigationIcon(
    showNavigationIcon: Boolean,
    onNavigateBack: (() -> Unit)?
) {
    if (showNavigationIcon && onNavigateBack != null) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.content_description_navigate_back)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TopAppBarTitle(
    title: String,
    isSearchActive: Boolean,
    searchState: TaskItSearchState?,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    AnimatedContent(
        targetState = isSearchActive,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "TaskItTopAppBarTitle"
    ) { active ->
        if (active && searchState != null) {
            SearchTextField(searchState, focusRequester, focusManager)
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SearchTextField(
    searchState: TaskItSearchState,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = searchState.query,
        onValueChange = searchState.onQueryChange,
        placeholder = { Text(text = searchState.placeholder) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(Res.string.content_description_search)
            )
        },
        trailingIcon = {
            if (searchState.query.isNotEmpty()) {
                IconButton(onClick = { searchState.onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.content_description_close)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SearchActionIcon(
    isSearchActive: Boolean,
    searchState: TaskItSearchState
) {
    AnimatedContent(
        targetState = isSearchActive,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TaskItTopAppBarSearchIcon"
    ) { active ->
        if (active) {
            IconButton(onClick = {
                if (searchState.enableClearOnClose) {
                    searchState.onQueryChange("")
                }
                searchState.onActiveChange(false)
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.content_description_close_search)
                )
            }
        } else {
            IconButton(onClick = { searchState.onActiveChange(true) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.content_description_search)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalTaskItTopAppBar(
    title: String,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    Column(modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(Res.string.content_description_search)
                    )
                }
            }
        )

        AnimatedVisibility(
            visible = showSearch
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                state = textFieldState,
                placeholder = { Text(text = stringResource(Res.string.global_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(Res.string.content_description_search)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showSearch = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.content_description_close)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onKeyboardAction = object : KeyboardActionHandler {
                    override fun onKeyboardAction(performDefaultAction: () -> Unit) {
                        onSearch(textFieldState.text.toString())
                        performDefaultAction()
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun PrincipalTaskItTopAppBarPreview() {
    TaskItTheme {
        PrincipalTaskItTopAppBar(
            title = "Tasks",
            modifier = Modifier
                .padding(8.dp)
        )
    }
}


@Preview
@Composable
private fun TaskItTopAppBarPreview() {
    TaskItTheme {
        var active by rememberSaveable { mutableStateOf(false) }
        var query by rememberSaveable { mutableStateOf("") }

        TaskItTopAppBar(
            title = stringResource(Res.string.app_name),
            showNavigationIcon = true,
            onNavigateBack = {},
            searchState = TaskItSearchState(
                query = query,
                onQueryChange = { query = it },
                isActive = active,
                onActiveChange = { active = it },
                placeholder = "placeholder", //stringResource(Res.string.global_search_placeholder),
                enableClearOnClose = true
            ),
            actions = {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                }
            }
        )
    }
}


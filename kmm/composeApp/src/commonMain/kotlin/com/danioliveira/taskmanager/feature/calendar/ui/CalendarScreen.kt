package com.danioliveira.taskmanager.feature.calendar.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.ui.components.PrincipalTaskItTopAppBar
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.ic_calendar_month
import org.jetbrains.compose.resources.painterResource

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun CalendarScreen(
    onGlobalSearch: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CalendarTopBar(onGlobalSearch = onGlobalSearch)
        }
    ) { paddingValues ->
        CalendarContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun CalendarTopBar(onGlobalSearch: (String) -> Unit) {
    with(sts) {
        PrincipalTaskItTopAppBar(
            title = "Calendar",
            onSearch = onGlobalSearch,
            modifier = Modifier.sharedBounds(
                sts.rememberSharedContentState(key = "main_top_bar"),
                avs
            )
        )
    }
}

@Composable
private fun CalendarContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_calendar_month),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Calendar Screen",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Coming Soon",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


package com.danioliveira.taskmanager.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.danioliveira.taskmanager.core.domain.manager.AuthManager
import com.danioliveira.taskmanager.core.ui.components.TaskItPrimaryActionButton
import com.danioliveira.taskmanager.core.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.domain.User
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.nav_profile
import kmmtaskmanager.composeapp.generated.resources.profile_email_label
import kmmtaskmanager.composeapp.generated.resources.profile_joined_label
import kmmtaskmanager.composeapp.generated.resources.profile_logout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val authManager = koinInject<AuthManager>()
    val coroutineScope = rememberCoroutineScope()
    
    val user by produceState<User?>(initialValue = null) {
        value = authManager.getCurrentUser()
    }

    Scaffold(
        topBar = {
            TaskItTopAppBar(
                title = stringResource(Res.string.nav_profile),
                showNavigationIcon = true,
                onNavigateBack = onBack
            )
        }
    ) { paddingValues ->
        ProfileContent(
            user = user,
            onLogout = {
                coroutineScope.launch {
                    authManager.logout()
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ProfileContent(
    user: User?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (user != null) {
            ProfileHeader(
                displayName = user.displayName,
                initials = computeInitials(user.displayName, user.email)
            )
            
            ProfileInfoCard(
                email = user.email,
                joinedDate = user.createdAt
            )
            
            Spacer(modifier = Modifier.padding(8.dp))
            
            TaskItPrimaryActionButton(
                text = stringResource(Res.string.profile_logout),
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String,
    initials: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileAvatar(initials = initials)
        
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(120.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    email: String,
    joinedDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileInfoField(
                label = stringResource(Res.string.profile_email_label),
                value = email,
                icon = Icons.Default.Email
            )
            
            ProfileInfoField(
                label = stringResource(Res.string.profile_joined_label),
                value = formatJoinedDate(joinedDate)
            )
        }
    }
}

@Composable
private fun ProfileInfoField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = icon?.let {
            {
                androidx.compose.material3.Icon(
                    imageVector = it,
                    contentDescription = null
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

private fun computeInitials(displayName: String, email: String): String {
    val trimmed = displayName.trim()
    if (trimmed.isNotEmpty()) {
        val parts = trimmed.split(" ").filter { it.isNotBlank() }
        val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar()
        val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
        return buildString {
            first?.let { append(it) }
            second?.let { append(it) }
        }.ifEmpty { trimmed.first().uppercaseChar().toString() }
    }
    return email.takeIf { it.isNotBlank() }
        ?.let { computeInitials(it, "") }
        ?: ""
}

private fun formatJoinedDate(dateString: String): String {
    // Try to parse and format the date, or return as-is if parsing fails
    return try {
        // Assuming ISO format like "2023-01-01T00:00:00" or similar
        // For now, just return a formatted version or the original
        dateString.take(10) // Take first 10 chars (YYYY-MM-DD)
    } catch (e: Exception) {
        dateString
    }
}


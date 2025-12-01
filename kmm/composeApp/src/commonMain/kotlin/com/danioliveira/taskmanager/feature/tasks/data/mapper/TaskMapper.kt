package com.danioliveira.taskmanager.feature.tasks.data.mapper

import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Extension function to convert TaskResponse to Task domain model.
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun TaskResponse.toTask(): Task {
    return Task(
        id = Uuid.parse(id),
        title = title,
        description = description,
        projectName = projectName,
        status = TaskStatus.valueOf(status.name),
        priority = Priority.valueOf(priority.name),
        dueDate = dueDate,
        createdAt = createdAt,
    )
}

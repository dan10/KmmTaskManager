package com.danioliveira.taskmanager.data.mapper

import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Extension function to convert TaskResponse to Task domain model.
 */
@OptIn(ExperimentalUuidApi::class)
fun TaskResponse.toTask(): Task {
    return Task(
        id = Uuid.parse(id),
        title = title,
        description = description,
        projectName = projectId, // Note: This is just the ID, not the name
        status = status,
        priority = priority,
        dueDate = dueDate
    )
}

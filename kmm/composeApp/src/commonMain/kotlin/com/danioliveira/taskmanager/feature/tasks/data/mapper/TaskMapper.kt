package com.danioliveira.taskmanager.feature.tasks.data.mapper

import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.core.domain.model.Task
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
        projectName = projectName,
        status = status,
        priority = priority,
        dueDate = dueDate
    )
}

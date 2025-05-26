package com.danioliveira.taskmanager.data.mapper

import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.domain.Project

/**
 * Extension function to convert ProjectResponse to Project domain model.
 */
fun ProjectResponse.toProject(): Project {
    return Project(
        id = id,
        name = name,
        completed = completed,
        inProgress = inProgress,
        total = total,
        description = description
    )
}
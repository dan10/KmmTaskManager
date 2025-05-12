package com.danioliveira.taskmanager.data.mapper

import com.danioliveira.taskmanager.api.response.FileResponse
import com.danioliveira.taskmanager.domain.File

/**
 * Extension function to convert FileResponse to File domain model.
 */
fun FileResponse.toFile(): File {
    return File(
        name = name,
        size = size,
        uploadedDate = uploadedDate
    )
}

/**
 * Extension function to convert a list of FileResponse to a list of File domain models.
 */
fun List<FileResponse>.toFiles(): List<File> {
    return map { it.toFile() }
}
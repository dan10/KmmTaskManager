package com.danioliveira.taskmanager.domain

/**
 * Domain model for a file.
 *
 * @property name The name of the file
 * @property size The size of the file (e.g., "2.4 MB")
 * @property uploadedDate The date the file was uploaded (e.g., "2024-11-20")
 */
data class File(
    val name: String,
    val size: String,
    val uploadedDate: String
)
package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object FileUploadsTable : UUIDTable() {
    val filename = varchar("filename", 255)
    val uploader = reference("uploader", UsersTable)
    val project = reference("project", ProjectsTable).nullable()
    val task = reference("task", TasksTable).nullable()
    val url = varchar("url", 512)
    val uploadedAt = datetime("uploaded_at")
}
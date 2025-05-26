package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.FileUploadsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class FileUploadDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FileUploadDAOEntity>(FileUploadsTable)

    var filename by FileUploadsTable.filename
    var uploader by UserDAOEntity referencedOn FileUploadsTable.uploader
    var project by ProjectDAOEntity optionalReferencedOn FileUploadsTable.project
    var task by TaskDAOEntity optionalReferencedOn FileUploadsTable.task
    var url by FileUploadsTable.url
    var uploadedAt by FileUploadsTable.uploadedAt
}
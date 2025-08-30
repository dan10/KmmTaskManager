package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.ProjectsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

class ProjectDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectDAOEntity>(ProjectsTable)

    var name by ProjectsTable.name
    var description by ProjectsTable.description
    var owner by ProjectsTable.owner
    var createdAt by ProjectsTable.createdAt
}

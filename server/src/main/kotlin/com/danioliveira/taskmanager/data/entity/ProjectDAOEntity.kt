package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.ProjectsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ProjectDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectDAOEntity>(ProjectsTable)

    var name by ProjectsTable.name
    var description by ProjectsTable.description
    var owner by ProjectsTable.owner
    var createdAt by ProjectsTable.createdAt
}

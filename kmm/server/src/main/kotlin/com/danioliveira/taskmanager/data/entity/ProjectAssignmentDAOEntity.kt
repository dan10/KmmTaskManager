package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

class ProjectAssignmentDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectAssignmentDAOEntity>(ProjectAssignmentsTable)

    var project by ProjectDAOEntity referencedOn ProjectAssignmentsTable.project
    var user by UserDAOEntity referencedOn ProjectAssignmentsTable.user
    var assignedAt by ProjectAssignmentsTable.assignedAt
}
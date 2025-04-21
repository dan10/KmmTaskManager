package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ProjectAssignmentDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectAssignmentDAOEntity>(ProjectAssignmentsTable)

    var project by ProjectDAOEntity referencedOn ProjectAssignmentsTable.project
    var user by UserDAOEntity referencedOn ProjectAssignmentsTable.user
    var assignedAt by ProjectAssignmentsTable.assignedAt
}
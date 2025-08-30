package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.TasksTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

class TaskDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskDAOEntity>(TasksTable)

    var title by TasksTable.title
    var description by TasksTable.description
    var project by ProjectDAOEntity optionalReferencedOn TasksTable.project
    var assignee by UserDAOEntity optionalReferencedOn TasksTable.assignee
    var creator by UserDAOEntity referencedOn TasksTable.creator
    var status by TasksTable.status
    var dueDate by TasksTable.dueDate

    var priority by TasksTable.priority

    var createdAt by TasksTable.createdAt
}

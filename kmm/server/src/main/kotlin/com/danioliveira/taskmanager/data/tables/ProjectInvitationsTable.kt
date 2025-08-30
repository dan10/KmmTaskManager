package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.datetime

object ProjectInvitationsTable : UUIDTable() {
    val project = reference("project", ProjectsTable)
    val invitedUser = reference("invited_user", UsersTable)
    val inviter = reference("inviter", UsersTable)
    val status = varchar("status", 50) // PENDING, ACCEPTED, DECLINED
    val invitedAt = datetime("invited_at")
}

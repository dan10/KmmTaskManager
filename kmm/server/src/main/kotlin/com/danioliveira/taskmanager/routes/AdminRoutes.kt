package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.data.tables.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Admin routes for database management and testing support.
 * These endpoints should only be enabled in development/testing environments.
 */
fun Route.adminRoutes() {
    route("/admin") {
        // Health check endpoint
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "healthy", "timestamp" to System.currentTimeMillis()))
        }

        // Database cleanup endpoint for testing
        delete("/cleanup") {
            try {
                transaction {
                    // Delete in order to respect foreign key constraints
                    ProjectAssignmentsTable.deleteAll()
                    ProjectInvitationsTable.deleteAll()
                    FileUploadsTable.deleteAll()
                    TasksTable.deleteAll()
                    ProjectsTable.deleteAll()
                    UsersTable.deleteAll()
                }

                call.respond(
                    HttpStatusCode.OK, mapOf(
                        "message" to "Database cleanup completed successfully",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Database cleanup failed",
                        "message" to e.message,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
        }

        // Database status endpoint
        get("/status") {
            try {
                val counts = transaction {
                    mapOf(
                        "users" to UsersTable.selectAll().count(),
                        "projects" to ProjectsTable.selectAll().count(),
                        "tasks" to TasksTable.selectAll().count(),
                        "project_assignments" to ProjectAssignmentsTable.selectAll().count(),
                        "project_invitations" to ProjectInvitationsTable.selectAll().count(),
                        "file_uploads" to FileUploadsTable.selectAll().count()
                    )
                }

                call.respond(
                    HttpStatusCode.OK, mapOf(
                        "status" to "connected",
                        "table_counts" to counts,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Database status check failed",
                        "message" to e.message,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
        }
    }
} 
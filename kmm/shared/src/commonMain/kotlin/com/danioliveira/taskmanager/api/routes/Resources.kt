package com.danioliveira.taskmanager.api.routes

import io.ktor.resources.Resource

/** Resource for type-safe requests responding on the `v1/users` path. */
@Resource(Routes.ALL_USERS)
class Users {

    /** Resource for type-safe requests responding on the `v1/users/{userId}` path. */
    @Resource(Routes.BY_USER_ID)
    class Id(val parent: Users = Users(), val userId: String)
}

/** Resource for type-safe requests responding on the `v1/projects` path. */
@Resource(Routes.ALL_PROJECTS)
class Projects {
    /** Resource for type-safe requests responding on the `v1/projects/{projectId}` path. */
    @Resource(Routes.BY_PROJECT_ID)
    class Id(val parent: Projects = Projects(), val projectId: String) {

        @Resource(Routes.PROJECT_TASKS)
        class Tasks(
            val parent: Id,
            val page: Int = 0,
            val size: Int = 10,
            val searchText: String = ""
        )

        // Type-safe child resource for `v1/projects/{projectId}/assign`
        @Resource("assign")
        class Assign(val parent: Id)

        // Type-safe child resource for `v1/projects/{projectId}/assign/{userId}`
        @Resource("assign/{userId}")
        class AssignUser(val parent: Id, val userId: String)

        // Type-safe child resource for `v1/projects/{projectId}/users`
        @Resource("users")
        class Users(val parent: Id)
    }
}

// Resource para obter projetos de um usuário com paginação/search/sort
@Resource(Routes.ALL_PROJECTS)
class UserProjects(val size: Int = 10, val page: Int = 0, val searchText: String? = null, val sort: String? = null)

@Resource(Routes.ALL_TASKS)
class Tasks() {

    @Resource(Routes.BY_TASK_ID)
    class Id(val parent: Tasks = Tasks(), val taskId: String) {
        
        // POST /v1/tasks/{taskId}/assign - Assign task to user
        @Resource(Routes.TASK_ASSIGN)
        class Assign(val parent: Id)
        
        // POST /v1/tasks/{taskId}/status - Update task status
        @Resource(Routes.TASK_STATUS)
        class Status(val parent: Id)
    }
    
    // GET /v1/tasks/owned - Get tasks owned by user
    @Resource(Routes.TASKS_OWNED)
    class Owned(val parent: Tasks = Tasks(), val page: Int = 0, val size: Int = 10)
    
    // GET /v1/tasks/assigned - Get tasks assigned to user  
    @Resource(Routes.TASKS_ASSIGNED)
    class Assigned(val parent: Tasks = Tasks(), val page: Int = 1, val size: Int = 10, val query: String? = null)
    
    // GET /v1/tasks/stats - Get task statistics (counts by status)
    @Resource(Routes.TASKS_STATS)
    class Stats(val parent: Tasks = Tasks())
}

@Resource(Routes.AUTH)
class Auth {
    // recursos aninhados relativos a v1/auth
    @Resource(Routes.AUTH_CHILD_LOGIN)
    class Login(val parent: Auth = Auth())

    @Resource(Routes.AUTH_CHILD_GOOGLE)
    class Google(val parent: Auth = Auth())

    @Resource(Routes.AUTH_CHILD_REGISTER)
    class Register(val parent: Auth = Auth())

    @Resource(Routes.AUTH_CHILD_REGISTER_GOOGLE)
    class RegisterGoogle(val parent: Auth = Auth())
}

// Removidas classes top-level AuthLogin/AuthGoogleLogin/AuthRegister/AuthRegisterGoogle

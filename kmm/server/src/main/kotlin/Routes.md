# Productivity App API Routes

A comprehensive REST API design for a productivity management application that handles tasks, projects, user management, and social authentication.

## Authentication Routes

### Basic Authentication
```http
POST /auth/register          # User registration
POST /auth/login             # User login
POST /auth/logout            # User logout
POST /auth/refresh-token     # Refresh JWT token
POST /auth/forgot-password   # Request password reset
POST /auth/reset-password    # Reset password with token
```

### Social Authentication
```http
GET  /auth/google            # Initiate Google OAuth
GET  /auth/google/callback   # Google OAuth callback
GET  /auth/github            # Initiate GitHub OAuth
GET  /auth/github/callback   # GitHub OAuth callback
GET  /auth/facebook          # Initiate Facebook OAuth
GET  /auth/facebook/callback # Facebook OAuth callback
```

## User Profile Routes

```http
GET    /users/me             # Get current user profile
PUT    /users/me             # Update current user profile
DELETE /users/me             # Delete current user account
GET    /users/me/dashboard   # Get dashboard overview with task/project counts
```

## Task Management Routes

### Basic Task Operations
```http
GET    /tasks                # Get all tasks for authenticated user
POST   /tasks                # Create new task
GET    /tasks/:id            # Get specific task details
PUT    /tasks/:id            # Update task
DELETE /tasks/:id            # Delete task
PATCH  /tasks/:id/status     # Update task status (todo/doing/completed)
```

### Task Filtering and Stats
```http
GET    /tasks/owned          # Get tasks owned by user
GET    /tasks/assigned       # Get tasks assigned to user
GET    /tasks/stats          # Get task statistics (counts by status)
```

### Common Query Parameters for Tasks
- `?status=todo|doing|completed` - Filter by task status
- `?project=123` - Filter by project ID
- `?assignee=456` - Filter by assignee user ID
- `?sort=dueDate&order=desc` - Sort tasks
- `?limit=10&offset=0` - Pagination

## Project Management Routes

### Basic Project Operations
```http
GET    /projects             # Get all projects for authenticated user
POST   /projects             # Create new project
GET    /projects/:id         # Get specific project details
PUT    /projects/:id         # Update project
DELETE /projects/:id         # Delete project
```

### Project Filtering
```http
GET    /projects/owned       # Get projects owned by user
GET    /projects/participating # Get projects user is participating in
```

## Project-Task Relationship Routes

### Project Tasks
```http
GET    /projects/:id/tasks   # Get all tasks in a specific project
POST   /projects/:id/tasks   # Create task in specific project
```

### Project Members
```http
GET    /projects/:id/members # Get all users participating in project
POST   /projects/:id/members # Add member to project
DELETE /projects/:id/members/:userId # Remove member from project
GET    /projects/:id/members/:userId # Get specific member details in project
PUT    /projects/:id/members/:userId # Update member role/permissions
```

## Dashboard and Analytics Routes

```http
GET    /dashboard            # Get complete dashboard data
GET    /dashboard/tasks      # Get task overview and counts
GET    /dashboard/projects   # Get project overview and statistics
```

## API Design Principles

### RESTful Conventions
- **GET**: Retrieve data
- **POST**: Create new resources
- **PUT**: Update entire resource
- **PATCH**: Partial resource updates
- **DELETE**: Remove resources

### URL Structure
- Use plural nouns for collections (`/tasks`, `/projects`)
- Use descriptive nested routes (`/projects/:id/tasks`)
- Maintain consistent naming patterns
- Use hyphens for multi-word endpoints (`/refresh-token`)

### Response Format Standards
All endpoints should return consistent JSON responses:

```json
{
  "success": true,
  "data": {
    // Response data here
  },
  "message": "Optional success message",
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 100,
    "pages": 10
  }
}
```

### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": [
      {
        "field": "email",
        "message": "Valid email is required"
      }
    ]
  }
}
```

## Common Query Parameters

### Filtering
- `status` - Filter by status
- `assignee` - Filter by assigned user
- `owner` - Filter by owner
- `project` - Filter by project ID

### Sorting
- `sort` - Field to sort by
- `order` - Sort direction (asc/desc)

### Pagination
- `page` - Page number (starts from 1)
- `size` - Number of items per page

## Authentication

All routes except authentication endpoints require a valid JWT token in the Authorization header:

```http
Authorization: Bearer <jwt_token>
```

## HTTP Status Codes

- `200` - OK (successful GET, PUT, PATCH)
- `201` - Created (successful POST)
- `204` - No Content (successful DELETE)
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource doesn't exist)
- `409` - Conflict (resource already exists)
- `500` - Internal Server Error
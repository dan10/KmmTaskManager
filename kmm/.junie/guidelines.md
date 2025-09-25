# Project Guidelines - KMM LLM Application

## Project Overview

This is a Kotlin Multiplatform Mobile (KMM) application that integrates with Language Learning Models (LLM). The project
follows a modular architecture split into three main components:

- ComposeApp (Frontend)
- Server (Backend)
- Shared (Common Code)

## Project Structure

### ComposeApp (Frontend)

The frontend implementation using Compose Multiplatform.

### Server (Backend)

The backend implementation using Ktor framework with the following structure:

```
server/
├── data/
│   ├── entity/         # Data entities
│   ├── tables/         # Database tables definitions
│   └── repository/     # Repository implementations
├── domain/
│   ├── repository/     # Repository interfaces
│   └── service/        # Business logic services
├── routes/             # API route definitions
├── plugins/            # Server configuration and plugins
└── security/          # JWT configuration and security utilities
```

#### Backend Technical Requirements:

1. Framework: Ktor
2. Database ORM: Exposed
3. Authentication: JWT (JSON Web Tokens)
    - JWT configuration in security package
    - Token validation middleware
    - Secured routes using JWT authentication

4. Repository Pattern:
    - All repository interfaces must be defined in `domain/repository`
    - Implementations must be in `data/repository`
    - Repository functions must:
        - Be extension functions of `Transaction`
        - Be suspended functions
        - Use shared request/response models from the shared module
   ```kotlin
   // Example:
   suspend fun Transaction.getUserById(id: Int): UserResponse? {
       // Implementation returning shared.api.response.UserResponse
   }
   ```

5. Service Layer:
    - Services should use shared request/response models
    - Handle business logic and data transformation
   ```kotlin
   class UserService(private val userRepository: UserRepository) {
       suspend fun getUser(request: UserRequest): UserResponse {
           // Implementation using shared.api.request.UserRequest
           // Returns shared.api.response.UserResponse
       }
   }
   ```

### Shared (Common Code)

Common code shared between frontend and backend:

```
shared/
├── api/
│   ├── request/        # Request models
│   └── response/       # Response models
```

## Development Guidelines

### Code Style

1. Follow Kotlin official coding conventions
2. Use meaningful names for classes, functions, and variables
3. Document public APIs using KDoc
4. Maximum line length: 120 characters
5. Repository functions must follow the specified pattern:
    - Must be Transaction extensions
    - Must be suspend functions
    - Must be properly documented

### Testing Requirements

1. Unit Tests
    - All repository implementations must be tested
    - All service logic must be tested
    - Minimum test coverage: 80%

2. Integration Tests
    - API endpoints must be tested
    - Database operations must be tested
    - Test must cover error cases and edge scenarios

3. Test Running Instructions
   ```bash
   # Run all tests
   ./gradlew test

   # Run backend tests only
   ./gradlew server:test

   # Run frontend tests only
   ./gradlew composeApp:test
   ```

### Build Process

1. Required Steps Before Submission:
    - Run all tests: `./gradlew test`
    - Build the project: `./gradlew build`
    - Verify ktlint: `./gradlew ktlintCheck`

2. Build Commands:
   ```bash
   # Full project build
   ./gradlew build

   # Backend only
   ./gradlew server:build

   # Frontend only
   ./gradlew composeApp:build
   ```

### Error Handling

1. Backend:
    - Use proper HTTP status codes
    - Return structured error responses
    - Log errors appropriately
    - Handle transaction rollbacks properly

2. Frontend:
    - Implement proper error handling for API calls
    - Show user-friendly error messages
    - Implement retry mechanisms where appropriate

### Version Control Guidelines

1. Branch Naming:
    - feature/feature-name
    - bugfix/bug-description
    - hotfix/issue-description

2. Commit Messages:
    - Use descriptive commit messages
    - Follow conventional commits format
    - Reference issue numbers when applicable

3. Pull Requests:
    - Include description of changes
    - Reference related issues
    - Include test coverage information
    - Must pass CI/CD checks

## Security Considerations

1. JWT Authentication:
    - Use secure JWT implementation with appropriate algorithms (e.g., RS256)
    - Implement token expiration and refresh mechanisms
    - Store JWT secrets/keys securely using environment variables
    - Example JWT configuration:
   ```kotlin
   install(Authentication) {
       jwt {
           realm = environment.config.property("jwt.realm").getString()
           verifier(jwkProvider)
           validate { credential ->
               // Validation logic
           }
       }
   }
   ```

2. Input Validation:
    - Validate all user inputs using shared request models
    - Sanitize data before database operations
    - Implement rate limiting
    - Example validation:
   ```kotlin
   fun Route.userRoutes() {
       authenticate {
           post("/user") {
               val request = call.receive<UserRequest>() // Using shared.api.request.UserRequest
               // Validation and processing
           }
       }
   }
   ```

3. Data Protection:
    - Encrypt sensitive data
    - Follow data protection regulations
    - Implement proper access controls
    - Use HTTPS for all communications
    - Secure token storage on client side

## Documentation Requirements

1. API Documentation:
    - Document all endpoints
    - Include request/response examples
    - Document error responses

2. Code Documentation:
    - Document complex algorithms
    - Include KDoc for public APIs
    - Document configuration requirements

## Deployment

1. Prerequisites:
    - JDK 17 or higher
    - Docker (for containerization)
    - PostgreSQL database

2. Environment Configuration:
    - Use environment variables for configuration
    - Document required environment variables
    - Provide example configuration files

3. Deployment Steps:
   ```bash
   # Build Docker image
   docker build -t kmm-llm-app .

   # Run container
   docker run -p 8080:8080 kmm-llm-app
   ```

## Support and Maintenance

1. Logging:
    - Implement comprehensive logging
    - Use appropriate log levels
    - Include relevant context in logs

2. Monitoring:
    - Implement health checks
    - Monitor application metrics
    - Set up alerting for critical issues
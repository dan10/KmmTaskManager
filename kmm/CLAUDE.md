# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Development Build & Run
```bash
# Compile Kotlin metadata (fast type checking)
./gradlew compileKotlinMetadata

# Run Android app (requires Android setup)
./gradlew :composeApp:installDebug

# Run server in JVM mode
./gradlew :server:runJvm

# Run server in GraalVM native mode (slower compilation, faster runtime)
./gradlew :server:nativeCompile
./gradlew :server:runNative
```

### Testing & Quality
```bash
# Run standard load tests
./gradlew :server:gatlingRun

# Run extended 30-minute load tests
./gradlew :server:gatlingRunLong

# Run quick load tests
./gradlew :server:gatlingRunQuick

# Run stress tests
./gradlew :server:gatlingRunStress
```

### Docker & Metrics
```bash
# Start complete environment (app, database, prometheus, grafana)
docker-compose up -d

# Access services:
# - Application: http://localhost:8080
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000 (admin/admin)
```

## Architecture Overview

### Project Structure
This is a Kotlin Multiplatform project with these main modules:
- `/composeApp` - Shared UI code using Compose Multiplatform (Android/iOS/Desktop)
- `/server` - Ktor server application with REST API
- `/shared` - Shared business logic between client and server
- `/paging-compose` - Custom paging library for Compose
- `/iosApp` - iOS-specific entry point

### UI Architecture Pattern

The app follows a consistent **State-Action-Effect** pattern:

#### State Management
Each screen has:
- **State data class**: Immutable state (e.g., `TasksState`, `TaskCreateState`)
- **Action sealed interface**: User actions (e.g., `TasksAction`, `TaskCreateAction`)
- **Effect sealed class**: Side effects like navigation, snackbars (e.g., `TasksEffect`)

#### ViewModel Pattern
```kotlin
class FeatureViewModel : ViewModel() {
    var state by mutableStateOf(FeatureState())
        private set

    private val _effects = MutableSharedFlow<FeatureEffect>()
    val effects: SharedFlow<FeatureEffect> = _effects.asSharedFlow()

    fun handleActions(action: FeatureAction) { /* ... */ }
}
```

#### Effect Handler Pattern
Each feature has an `EffectHandler` composable that handles side effects:
```kotlin
@Composable
fun FeatureEffectHandler(
    viewModel: FeatureViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigate: (Route) -> Unit
)
```

### Feature Structure
Each feature follows this structure:
```
feature/
├── data/
│   ├── network/      # API services
│   └── repository/   # Repository implementations
├── domain/
│   ├── model/        # Domain models
│   ├── repository/   # Repository interfaces
│   └── usecase/      # Business logic use cases
├── ui/
│   ├── feature/      # Screen composables, ViewModels, State
│   └── components/   # Reusable UI components
└── di/              # Dependency injection modules
```

### Dependency Injection
Uses Koin with feature-based modules:
- Each feature has its own DI module (e.g., `tasksModule`, `authModule`)
- Modules are combined in `appModule`
- ViewModels are injected using `koinViewModel()`

### Navigation
- Type-safe navigation with kotlinx.serialization
- Screen routes defined in `navigation/AppNavigation.kt`
- Uses sealed classes for route definitions

### Custom Paging
- Custom paging implementation in `/paging-compose` module
- Integration with Compose for pagination handling
- Works with the State-Action-Effect pattern

## Key Patterns to Follow

### When Creating New Features
1. Follow the feature folder structure above
2. Implement State-Action-Effect pattern
3. Create dedicated EffectHandler for side effects
4. Add feature module to DI system
5. Use existing UI components from `core/ui/components/`

### ViewModel Implementation
- Use `mutableStateOf` for UI state
- Emit effects through `SharedFlow` for side effects
- Handle all actions in `handleActions(action: FeatureAction)`
- Keep ViewModels focused on orchestration, not business logic

### Effect Handling
- Snackbars: Use different durations (Short/Long) based on effect type
- Navigation: Emit navigation effects, handle in EffectHandler
- Confirmation dialogs: Use snackbar with action buttons

### Testing Strategy
- Load testing with Gatling for server performance
- Multiple test modes: quick, standard, long (30min), stress
- Metrics collection with Prometheus/Grafana
- JVM vs GraalVM native performance comparison

## Common Development Patterns

### Adding a New Screen
1. Create State/Action/Effect classes
2. Implement ViewModel with proper effect handling
3. Create EffectHandler composable
4. Build main screen composable using existing components
5. Add route to navigation system
6. Register ViewModel in DI module

### Task/Error Handling
- Use `Result<T>` for operations that can fail
- Emit error effects for user feedback
- Provide retry mechanisms in error snackbars
- Handle loading states in UI

### Component Usage
- Prefer existing components from `core/ui/components/`
- Follow naming convention: `TaskIt*` for custom components
- Use proper error handling with `isError` and `errorMessage` parameters
- Disable UI during loading states with `enabled` parameter

The codebase emphasizes clean architecture, type safety, and consistent patterns across all features.
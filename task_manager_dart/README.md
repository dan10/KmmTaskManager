# Task Manager

A cross-platform task management application built with Flutter and Dart.

## Project Structure

The project is organized into three main modules:

- `app/`: Flutter application for iOS and Android
- `server/`: Dart server implementation
- `shared/`: Shared code between app and server

## Prerequisites

- Flutter SDK (latest stable version)
- Dart SDK (latest stable version)
- Docker and Docker Compose
- PostgreSQL (if running locally)

## Quick Setup

We provide a setup script that automates the installation process. To use it:

1. Make the script executable:
```bash
chmod +x setup.sh
```

2. Run the setup script:
```bash
./setup.sh
```

The script will:
- Check if all required tools are installed
- Install dependencies for all modules
- Generate necessary code
- Create the environment file

## Manual Setup Instructions

If you prefer to set up manually, follow these steps:

1. Clone the repository:
```bash
git clone <repository-url>
cd task_manager
```

2. Install dependencies for each module:

```bash
# Install app dependencies
cd app
flutter pub get

# Install server dependencies
cd ../server
dart pub get

# Install shared dependencies
cd ../shared
dart pub get
```

3. Generate code:

```bash
# Generate Freezed and JSON serialization code in shared module
cd shared
dart run build_runner build

# Generate Mockito mocks in server module
cd ../server
dart run build_runner build
```

4. Set up environment variables:

Create a `.env` file in the server directory with the following content:
```
DATABASE_URL=postgres://postgres:postgres@localhost:5432/task_manager
JWT_SECRET=your-secret-key-here
PORT=8080
HOST=0.0.0.0
```

## Running the Application

### Using Docker Compose (Recommended)

1. Start the application:
```bash
docker-compose up
```

This will start:
- PostgreSQL database
- Dart server
- The server will be available at http://localhost:8080

### Manual Setup

1. Start PostgreSQL:
```bash
# If using Docker
docker run --name task-manager-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=task_manager \
  -p 5432:5432 \
  -d postgres:15-alpine

# Or start your local PostgreSQL instance
```

2. Start the server:
```bash
cd server
dart run bin/server.dart
```

3. Run the Flutter app:
```bash
cd app
flutter run
```

## Development

### Running Tests

```bash
# Run server tests
cd server
dart test

# Run app tests
cd app
flutter test
```

### Code Generation

When making changes to models or adding new mocks, regenerate the code:

```bash
# In shared module
dart run build_runner build

# In server module
dart run build_runner build

# In app module
flutter pub run build_runner build
```

## Architecture

The application follows a clean architecture pattern:

- **Presentation Layer**: Flutter UI components and screens
- **Domain Layer**: Business logic and use cases
- **Data Layer**: Repositories and data sources

### Server Architecture

- **Routes**: HTTP request handlers
- **Services**: Business logic implementation
- **Repositories**: Data access layer
- **Models**: Shared data models

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Run tests
4. Submit a pull request

## License

[Add your license here] 
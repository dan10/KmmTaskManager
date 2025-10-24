class ApiConstants {
  // Base URL - Android emulator uses 10.0.2.2 to access host machine's localhost
  // Use 'http://localhost:8081' for iOS simulator or web
  static const String baseUrl = 'http://192.168.68.51:8081';

  // Auth endpoints (matching KMM: v1/auth/...)
  static const String loginEndpoint = '/v1/auth/login';
  static const String registerEndpoint = '/v1/auth/register';
  static const String googleLoginEndpoint = '/v1/auth/google-login';
  static const String logoutEndpoint = '/v1/auth/logout';
  static const String refreshTokenEndpoint = '/v1/auth/refresh';

  // Task endpoints (matching KMM: v1/tasks/...)
  static const String tasksEndpoint = '/v1/tasks';
  static const String assignedTasksEndpoint = '/v1/tasks/assigned';

  // Project endpoints (matching KMM: v1/projects/...)
  static const String projectsEndpoint = '/v1/projects';
} 
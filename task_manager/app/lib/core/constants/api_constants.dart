class ApiConstants {
  // Base URL - this should be configurable based on environment
  static const String baseUrl = 'http://localhost:8080';

  // Auth endpoints
  static const String loginEndpoint = '/api/auth/login';
  static const String registerEndpoint = '/api/auth/register';
  static const String googleLoginEndpoint = '/api/auth/google-login';
  static const String logoutEndpoint = '/api/auth/logout';
  static const String refreshTokenEndpoint = '/api/auth/refresh';

  // Task endpoints
  static const String tasksEndpoint = '/api/tasks';

  // Project endpoints
  static const String projectsEndpoint = '/api/projects';
} 
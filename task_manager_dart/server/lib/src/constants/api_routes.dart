/// API route constants for the Task Manager server
class ApiRoutes {
  // Auth routes
  static const String authRegister = '/register';
  static const String authLogin = '/login';
  static const String authGoogleLogin = '/google-login';
  
  // Task routes
  static const String tasksStats = '/stats';
  static const String tasksCreatedByMe = '/created-by-me';
  static const String tasksByProject = '/project/<projectId>';
  static const String taskById = '/<id>';
  static const String taskAssign = '/<id>/assign';
  static const String taskStatus = '/<id>/status';
}

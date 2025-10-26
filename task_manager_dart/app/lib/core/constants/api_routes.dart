/// API Routes constants
/// Similar to KMM's Resources.kt for type-safe API routing
class ApiRoutes {
  ApiRoutes._(); // Private constructor to prevent instantiation

  // Base paths
  static const String v1 = '/v1';
  
  // Auth routes
  static const String auth = '$v1/auth';
  static const String authLogin = '$auth/login';
  static const String authRegister = '$auth/register';
  static const String authGoogle = '$auth/google';
  static const String authGoogleLogin = '$auth/google-login';
  static const String authRegisterGoogle = '$auth/register-google';
  static const String authLogout = '$auth/logout';
  static const String authRefresh = '$auth/refresh';
  
  // User routes
  static const String users = '$v1/users';
  static String userById(String userId) => '$users/$userId';
  
  // Project routes
  static const String projects = '$v1/projects';
  static String projectById(String projectId) => '$projects/$projectId';
  static String projectTasks(String projectId) => '$projects/$projectId/tasks';
  static String projectMembers(String projectId) => '$projects/$projectId/members';
  static String projectMemberById(String projectId, String userId) => '$projects/$projectId/members/$userId';
  static String projectStats(String projectId) => '$projects/$projectId/stats';
  static String projectAssign(String projectId) => '$projects/$projectId/assign';
  static String projectAssignUser(String projectId, String userId) => '$projects/$projectId/assign/$userId';
  static String projectUsers(String projectId) => '$projects/$projectId/users';
  
  // Task routes
  static const String tasks = '$v1/tasks';
  static String taskById(String taskId) => '$tasks/$taskId';
  static String taskAssign(String taskId) => '$tasks/$taskId/assign';
  static String taskStatus(String taskId) => '$tasks/$taskId/status';
  
  // Task query routes
  static const String tasksOwned = '$tasks/owned';
  static const String tasksAssigned = '$tasks/assigned';
  static const String tasksStats = '$tasks/stats';
}


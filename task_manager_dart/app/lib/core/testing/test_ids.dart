/// Test identifiers for Appium automation.
/// These identifiers are exposed as resource-id (Android) and accessibilityIdentifier (iOS)
/// when using Semantics widget with identifier property.
class TestIds {
  // Auth
  static const String btnLogin = 'btn_login';
  static const String btnRegister = 'btn_register';
  static const String btnLogout = 'btn_logout';
  static const String txtEmail = 'txt_email';
  static const String txtPassword = 'txt_password';
  static const String linkRegister = 'link_register';
  
  // Navigation
  static const String navTasks = 'nav_tasks';
  static const String navProjects = 'nav_projects';
  static const String navCalendar = 'nav_calendar';
  
  // Tasks
  static const String btnAddTask = 'btn_add_task';
  static const String listTasks = 'list_tasks';
  static const String txtSearch = 'txt_search';
  static const String cardTask = 'card_task_';
  
  // Projects
  static const String btnAddProject = 'btn_add_project';
  static const String listProjects = 'list_projects';
  static const String cardProject = 'card_project_';
  
  // Calendar
  static const String listCalendarTasks = 'list_calendar_tasks';
  
  // Profile
  static const String btnProfile = 'btn_profile';
  
  // Common
  static const String btnBack = 'btn_back';
  static const String btnSave = 'btn_save';
  static const String btnCancel = 'btn_cancel';
  
  /// Generate a task card identifier with task ID
  static String taskCard(String id) => '$cardTask$id';
  
  /// Generate a project card identifier with project ID
  static String projectCard(String id) => '$cardProject$id';
}



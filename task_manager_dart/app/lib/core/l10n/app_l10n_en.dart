// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_l10n.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get authAppName => 'Task Manager';

  @override
  String get authLoginTitle => 'Welcome Back';

  @override
  String get authRegisterTitle => 'Create Account';

  @override
  String get authEmail => 'Email';

  @override
  String get authEmailHint => 'Enter your email';

  @override
  String get authPassword => 'Password';

  @override
  String get authPasswordHint => 'Enter your password';

  @override
  String get authConfirmPassword => 'Confirm Password';

  @override
  String get authName => 'Name';

  @override
  String get authNameHint => 'Enter your full name';

  @override
  String get authLoginButton => 'Login';

  @override
  String get authRegisterButton => 'Create Account';

  @override
  String get authSignUp => 'Sign Up';

  @override
  String get authSignIn => 'Sign In';

  @override
  String get authWithoutAccount => 'Don\'t have an account?';

  @override
  String get authAlreadyHaveAccount => 'Already have an account?';

  @override
  String get authEmailError => 'Please enter a valid email address';

  @override
  String get authPasswordError => 'Password must be at least 8 characters';

  @override
  String authPasswordTooShort(int minLength) {
    return 'Password must be at least $minLength characters';
  }

  @override
  String get authNameError => 'Name should not be empty';

  @override
  String get authNameTooShort => 'Name must be at least 2 characters';

  @override
  String get authConfirmPasswordError => 'Passwords do not match';

  @override
  String authLoginError(String error) {
    return 'Login failed: $error';
  }

  @override
  String authRegisterError(String error) {
    return 'Registration failed: $error';
  }

  @override
  String get authForgotPassword => 'Forgot Password?';

  @override
  String get authResetPassword => 'Reset Password';

  @override
  String get authLogout => 'Logout';

  @override
  String get authLoggingIn => 'Logging in...';

  @override
  String get authCreatingAccount => 'Creating account...';

  @override
  String get taskDetailsTitle => 'Task Details';

  @override
  String get taskEditTitle => 'Edit Task';

  @override
  String get taskListTitle => 'Tasks';

  @override
  String get taskSearchPlaceholder => 'Search tasks...';

  @override
  String get taskNotFound => 'Task not found';

  @override
  String get taskLoadError => 'Failed to load task details';

  @override
  String get taskDeletedSuccess => 'Task deleted successfully';

  @override
  String get taskDeletedError => 'Failed to delete task';

  @override
  String get taskUpdatedSuccess => 'Task updated successfully';

  @override
  String get taskUpdatedError => 'Failed to update task';

  @override
  String get taskCreatedSuccess => 'Task created successfully';

  @override
  String taskCreatedError(String error) {
    return 'Failed to create task: $error';
  }

  @override
  String get taskDeleteDialogTitle => 'Delete Task';

  @override
  String get taskDeleteDialogMessage =>
      'Are you sure you want to delete this task? This action cannot be undone.';

  @override
  String get taskDescriptionLabel => 'Description';

  @override
  String get taskInformationLabel => 'Task Information';

  @override
  String get taskDatesLabel => 'Dates';

  @override
  String get taskDueDateLabel => 'Due Date';

  @override
  String get taskNoDueDate => 'No due date';

  @override
  String get taskSetDueDate => 'Set due date';

  @override
  String get taskStatusLabel => 'Status';

  @override
  String get taskPriorityLabel => 'Priority';

  @override
  String taskPriorityText(String priority) {
    return '$priority Priority';
  }

  @override
  String get taskCreatedAtLabel => 'Created';

  @override
  String get taskUpdatedAtLabel => 'Last Updated';

  @override
  String get taskTitleLabel => 'Title';

  @override
  String get taskTitleHint => 'Enter task title';

  @override
  String get taskDescriptionHint => 'Enter task description';

  @override
  String get taskDescriptionOptionalHint => 'Enter task description (optional)';

  @override
  String get taskDueDateOptionalHint => 'Select due date (optional)';

  @override
  String get taskTitleRequired => 'Title *';

  @override
  String get taskStatusTodo => 'To Do';

  @override
  String get taskStatusInProgress => 'In Progress';

  @override
  String get taskStatusDone => 'Done';

  @override
  String get taskPriorityHigh => 'High';

  @override
  String get taskPriorityMedium => 'Medium';

  @override
  String get taskPriorityLow => 'Low';

  @override
  String get taskCreateTitle => 'Create Task';

  @override
  String get taskEmptyTitle => 'No Tasks Yet';

  @override
  String get taskEmptySubtitle => 'Create your first task to get started';

  @override
  String get taskEmptyTip1 => '• Tap the + button to create a task';

  @override
  String get taskEmptyTip2 => '• Add a title and description';

  @override
  String get taskEmptyTip3 => '• Set priority and due date';

  @override
  String get taskProgressTitle => 'Your Progress';

  @override
  String get taskProgressNoTasks => 'No tasks yet';

  @override
  String taskProgressCompleted(int completed, int total) {
    return '$completed of $total completed';
  }

  @override
  String taskProgressCount(int completed, int total) {
    return '$completed/$total';
  }

  @override
  String get taskProgressWelcome => 'Welcome! Let\'s add your first task.';

  @override
  String get taskProgressEncouragement => 'You\'re making steady progress';

  @override
  String get commonCancel => 'Cancel';

  @override
  String get commonDelete => 'Delete';

  @override
  String get commonUpdate => 'Update';

  @override
  String get commonRetry => 'Retry';

  @override
  String get commonNA => 'N/A';

  @override
  String get projectsTitle => 'Projects';

  @override
  String get projectsSearchPlaceholder => 'Search projects...';

  @override
  String get projectsEmptyTitle => 'No Projects Yet';

  @override
  String get projectsEmptySubtitle =>
      'Create your first project to organize your tasks';

  @override
  String get projectsLoadError => 'Failed to load projects';

  @override
  String get projectsLoadMoreError => 'Failed to load more projects';

  @override
  String get projectDeletedSuccess => 'Project deleted successfully';

  @override
  String get projectDeletedError => 'Failed to delete project';

  @override
  String get projectCreatedSuccess => 'Project created successfully';

  @override
  String get projectCreatedError => 'Failed to create project';

  @override
  String get projectUpdatedSuccess => 'Project updated successfully';

  @override
  String get projectUpdatedError => 'Failed to update project';

  @override
  String get projectDetailsTitle => 'Project Details';

  @override
  String get projectCreateTitle => 'Create Project';

  @override
  String get projectEditTitle => 'Edit Project';

  @override
  String get projectNameLabel => 'Project Name';

  @override
  String get projectNameHint => 'Enter project name';

  @override
  String get projectDescriptionLabel => 'Description';

  @override
  String get projectDescriptionHint => 'Enter project description (optional)';

  @override
  String get projectNameRequired => 'Project name is required';
}

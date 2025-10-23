// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get contentDescriptionShowPassword => 'Show password';

  @override
  String get contentDescriptionHidePassword => 'Hide password';

  @override
  String get contentDescriptionBack => 'Back';

  @override
  String get contentDescriptionDelete => 'Delete';

  @override
  String get contentDescriptionAddTask => 'Add Task';

  @override
  String get contentDescriptionSearch => 'Search';

  @override
  String get titleWithoutAccount => 'Don\'t have an account?';

  @override
  String get buttonSignUp => 'Sign Up';

  @override
  String get titleEmail => 'Email';

  @override
  String get titlePassword => 'Password';

  @override
  String get titleLoginButton => 'Login';

  @override
  String get titleEmailError => 'Please enter a valid email address';

  @override
  String get titlePasswordError => 'Password must be at least 8 characters';

  @override
  String get titleName => 'Name';

  @override
  String get titleNameError => 'Name should not be empty';

  @override
  String get titleRegisterButton => 'Register';

  @override
  String get titleConfirmPassword => 'Confirm Password';

  @override
  String get titleConfirmPasswordError => 'Passwords do not match';

  @override
  String get titleAlreadyHaveAccount => 'Already have an account?';

  @override
  String get buttonSignIn => 'Sign in';

  @override
  String get navTasks => 'Tasks';

  @override
  String get navProjects => 'Projects';

  @override
  String get navProfile => 'Profile';

  @override
  String get navCalendar => 'Calendar';

  @override
  String get tasksTitle => 'My Tasks';

  @override
  String get tasksSearchPlaceholder => 'Search tasks...';

  @override
  String get tasksProgressTitle => 'Your Progress';

  @override
  String tasksProgressPercentage(int percentage) {
    return '$percentage%';
  }

  @override
  String tasksProgressCompleted(int completed, int total) {
    return '$completed of $total tasks completed';
  }

  @override
  String get tasksEmptyTitle => 'Ready to Get Started?';

  @override
  String get tasksEmptySubtitle => 'Here are some ideas to help you begin:';

  @override
  String get taskDetailsTitle => 'Task Details';

  @override
  String get taskDueDate => 'Due:';

  @override
  String get taskNoDueDate => 'No due date';

  @override
  String get taskProject => 'Project:';

  @override
  String get taskAssignedTo => 'Assigned to:';

  @override
  String get projectsTitle => 'Projects';

  @override
  String get projectsSearchPlaceholder => 'Search projects...';

  @override
  String get projectsAll => 'All Projects';

  @override
  String get projectsAdd => 'Add Project';

  @override
  String get projectsEmptyTitle => 'No Projects Found';

  @override
  String get projectsEmptySubtitle =>
      'Create your first project to get started';

  @override
  String projectCompleted(int count) {
    return '$count completed';
  }

  @override
  String projectInProgress(int count) {
    return '$count in progress';
  }

  @override
  String projectTotal(int count) {
    return '$count total';
  }

  @override
  String get profileComingSoon => 'Profile Screen - Coming Soon';

  @override
  String get createTask => 'Create Task';

  @override
  String get editTask => 'Edit Task';

  @override
  String get taskTitleLabel => 'Title';

  @override
  String get taskTitleError => 'Title is required';

  @override
  String get taskDescriptionLabel => 'Description';

  @override
  String get taskPriorityLabel => 'Priority';

  @override
  String get taskDueDateLabel => 'Due Date';

  @override
  String get taskDueDatePlaceholder => 'DD/MM/YYYY';

  @override
  String get taskTitlePlaceholder => 'Enter task title';

  @override
  String get taskDescriptionPlaceholder => 'Enter task description (optional)';

  @override
  String get taskProjectLabel => 'Project';

  @override
  String get taskProjectPlaceholder => 'Select a project';

  @override
  String get taskProjectRequired => 'Please select a project';

  @override
  String taskLoadError(String error) {
    return 'Failed to load task: $error';
  }

  @override
  String taskCreateError(String error) {
    return 'Failed to create task: $error';
  }

  @override
  String taskUpdateError(String error) {
    return 'Failed to update task: $error';
  }

  @override
  String get taskDeleteTitle => 'Delete Task';

  @override
  String get taskDeleteMessage =>
      'Are you sure you want to delete this task? This action cannot be undone.';

  @override
  String get taskDeleteButton => 'Delete';

  @override
  String taskDeleteError(String error) {
    return 'Failed to delete task: $error';
  }

  @override
  String get createProject => 'Create Project';

  @override
  String get editProject => 'Edit Project';

  @override
  String get projectNameLabel => 'Project Name';

  @override
  String get projectNameError => 'Project name cannot be empty';

  @override
  String get projectDescriptionLabel => 'Description (optional)';

  @override
  String get projectCancelButton => 'Cancel';

  @override
  String get projectCreateButton => 'Create';

  @override
  String get projectUpdateButton => 'Update';

  @override
  String projectLoadError(String error) {
    return 'Failed to load project: $error';
  }

  @override
  String projectCreateError(String error) {
    return 'Failed to create project: $error';
  }

  @override
  String projectUpdateError(String error) {
    return 'Failed to update project: $error';
  }

  @override
  String get projectNamePlaceholder => 'Enter project name';

  @override
  String get projectDescriptionPlaceholder =>
      'Enter project description (optional)';

  @override
  String get authAppName => 'Task Manager';

  @override
  String get authWithoutAccount => 'Don\'t have an account?';

  @override
  String get authSignUp => 'Sign Up';

  @override
  String get authEmail => 'Email';

  @override
  String get authPassword => 'Password';

  @override
  String get authLoginButton => 'Login';

  @override
  String get authEmailError => 'Please enter a valid email address';

  @override
  String get authPasswordError => 'Password must be at least 8 characters';

  @override
  String get authName => 'Name';

  @override
  String get authNameError => 'Name should not be empty';

  @override
  String get authRegisterButton => 'Register';

  @override
  String get authConfirmPassword => 'Confirm Password';

  @override
  String get authConfirmPasswordError => 'Passwords do not match';

  @override
  String get authAlreadyHaveAccount => 'Already have an account?';

  @override
  String get authSignIn => 'Sign in';

  @override
  String get authDemoCredentials => 'Demo Credentials';

  @override
  String get authDemoCredentialsText =>
      'Email: test@example.com\nPassword: password';

  @override
  String get accessibilityShowPassword => 'Show password';

  @override
  String get accessibilityHidePassword => 'Hide password';

  @override
  String get taskCreateButton => 'Create';

  @override
  String get taskUpdateButton => 'Save Changes';

  @override
  String get taskCancelButton => 'Cancel';

  @override
  String get validationEmailRequired => 'Email is required';

  @override
  String get validationEmailInvalid => 'Please enter a valid email address';

  @override
  String get validationPasswordRequired => 'Password is required';

  @override
  String get validationPasswordTooShort =>
      'Password must be at least 8 characters long';

  @override
  String get validationPasswordTooLong =>
      'Password must be less than 128 characters long';

  @override
  String get validationPasswordNeedsLowercase =>
      'Password must contain at least one lowercase letter';

  @override
  String get validationPasswordNeedsUppercase =>
      'Password must contain at least one uppercase letter';

  @override
  String get validationPasswordNeedsNumber =>
      'Password must contain at least one number';

  @override
  String get validationPasswordNeedsSpecialChar =>
      'Password must contain at least one special character';

  @override
  String get validationConfirmPasswordRequired =>
      'Please confirm your password';

  @override
  String get validationPasswordsDoNotMatch => 'Passwords do not match';

  @override
  String get validationNameRequired => 'Name is required';

  @override
  String get validationNameTooShort =>
      'Name must be at least 2 characters long';

  @override
  String get validationNameTooLong =>
      'Name must be less than 50 characters long';

  @override
  String get validationNameInvalidCharacters =>
      'Name can only contain letters, spaces, hyphens, and apostrophes';

  @override
  String validationFieldRequired(String fieldName) {
    return '$fieldName is required';
  }

  @override
  String get passwordStrengthVeryWeak => 'Very Weak';

  @override
  String get passwordStrengthWeak => 'Weak';

  @override
  String get passwordStrengthMedium => 'Medium';

  @override
  String get passwordStrengthStrong => 'Strong';

  @override
  String get passwordStrengthVeryStrong => 'Very Strong';

  @override
  String get taskProjectHint => 'Select a project';

  @override
  String get saveChanges => 'Save Changes';

  @override
  String get taskCreated => 'Task created successfully';

  @override
  String get taskUpdated => 'Task updated successfully';

  @override
  String get cancel => 'Cancel';

  @override
  String get delete => 'Delete';

  @override
  String get deleteTask => 'Delete Task';
}

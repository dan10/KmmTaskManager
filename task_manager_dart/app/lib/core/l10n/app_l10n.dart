import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_l10n_en.dart';
import 'app_l10n_es.dart';
import 'app_l10n_pt.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_l10n.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('es'),
    Locale('pt'),
    Locale('pt', 'BR'),
  ];

  /// No description provided for @appName.
  ///
  /// In en, this message translates to:
  /// **'TaskIt'**
  String get appName;

  /// App name displayed on auth screens
  ///
  /// In en, this message translates to:
  /// **'Task Manager'**
  String get authAppName;

  /// Login screen title
  ///
  /// In en, this message translates to:
  /// **'Welcome Back'**
  String get authLoginTitle;

  /// Register screen title
  ///
  /// In en, this message translates to:
  /// **'Create Account'**
  String get authRegisterTitle;

  /// Email field label
  ///
  /// In en, this message translates to:
  /// **'Email'**
  String get authEmail;

  /// Email field hint text
  ///
  /// In en, this message translates to:
  /// **'Enter your email'**
  String get authEmailHint;

  /// Password field label
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get authPassword;

  /// Password field hint text
  ///
  /// In en, this message translates to:
  /// **'Enter your password'**
  String get authPasswordHint;

  /// Confirm password field label
  ///
  /// In en, this message translates to:
  /// **'Confirm Password'**
  String get authConfirmPassword;

  /// Name field label for registration
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get authName;

  /// Name field hint text
  ///
  /// In en, this message translates to:
  /// **'Enter your full name'**
  String get authNameHint;

  /// Login button text
  ///
  /// In en, this message translates to:
  /// **'Login'**
  String get authLoginButton;

  /// Register button text
  ///
  /// In en, this message translates to:
  /// **'Create Account'**
  String get authRegisterButton;

  /// Sign up link text
  ///
  /// In en, this message translates to:
  /// **'Sign Up'**
  String get authSignUp;

  /// Sign in link text
  ///
  /// In en, this message translates to:
  /// **'Sign In'**
  String get authSignIn;

  /// Text asking if user doesn't have an account
  ///
  /// In en, this message translates to:
  /// **'Don\'t have an account?'**
  String get authWithoutAccount;

  /// Text asking if user already has an account
  ///
  /// In en, this message translates to:
  /// **'Already have an account?'**
  String get authAlreadyHaveAccount;

  /// Error message for invalid email
  ///
  /// In en, this message translates to:
  /// **'Please enter a valid email address'**
  String get authEmailError;

  /// Error message for invalid password
  ///
  /// In en, this message translates to:
  /// **'Password must be at least 8 characters'**
  String get authPasswordError;

  /// Error message when password is too short
  ///
  /// In en, this message translates to:
  /// **'Password must be at least {minLength} characters'**
  String authPasswordTooShort(int minLength);

  /// Error message for empty name field
  ///
  /// In en, this message translates to:
  /// **'Name should not be empty'**
  String get authNameError;

  /// Error message when name is too short
  ///
  /// In en, this message translates to:
  /// **'Name must be at least 2 characters'**
  String get authNameTooShort;

  /// Error message when passwords don't match
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match'**
  String get authConfirmPasswordError;

  /// Generic login error message
  ///
  /// In en, this message translates to:
  /// **'Login failed: {error}'**
  String authLoginError(String error);

  /// Generic registration error message
  ///
  /// In en, this message translates to:
  /// **'Registration failed: {error}'**
  String authRegisterError(String error);

  /// Forgot password link text
  ///
  /// In en, this message translates to:
  /// **'Forgot Password?'**
  String get authForgotPassword;

  /// Reset password button text
  ///
  /// In en, this message translates to:
  /// **'Reset Password'**
  String get authResetPassword;

  /// Logout button text
  ///
  /// In en, this message translates to:
  /// **'Logout'**
  String get authLogout;

  /// Loading text while logging in
  ///
  /// In en, this message translates to:
  /// **'Logging in...'**
  String get authLoggingIn;

  /// Loading text while creating account
  ///
  /// In en, this message translates to:
  /// **'Creating account...'**
  String get authCreatingAccount;

  /// Title for task details screen
  ///
  /// In en, this message translates to:
  /// **'Task Details'**
  String get taskDetailsTitle;

  /// Title for task edit screen
  ///
  /// In en, this message translates to:
  /// **'Edit Task'**
  String get taskEditTitle;

  /// Title for task list screen
  ///
  /// In en, this message translates to:
  /// **'Tasks'**
  String get taskListTitle;

  /// Placeholder text for task search field
  ///
  /// In en, this message translates to:
  /// **'Search tasks...'**
  String get taskSearchPlaceholder;

  /// Message when task cannot be found
  ///
  /// In en, this message translates to:
  /// **'Task not found'**
  String get taskNotFound;

  /// Error message when task details fail to load
  ///
  /// In en, this message translates to:
  /// **'Failed to load task details'**
  String get taskLoadError;

  /// Success message after deleting a task
  ///
  /// In en, this message translates to:
  /// **'Task deleted successfully'**
  String get taskDeletedSuccess;

  /// Error message when deleting a task fails
  ///
  /// In en, this message translates to:
  /// **'Failed to delete task'**
  String get taskDeletedError;

  /// Success message after updating a task
  ///
  /// In en, this message translates to:
  /// **'Task updated successfully'**
  String get taskUpdatedSuccess;

  /// Error message when updating a task fails
  ///
  /// In en, this message translates to:
  /// **'Failed to update task'**
  String get taskUpdatedError;

  /// Success message after creating a task
  ///
  /// In en, this message translates to:
  /// **'Task created successfully'**
  String get taskCreatedSuccess;

  /// Error message when creating a task fails
  ///
  /// In en, this message translates to:
  /// **'Failed to create task: {error}'**
  String taskCreatedError(String error);

  /// Title for delete task confirmation dialog
  ///
  /// In en, this message translates to:
  /// **'Delete Task'**
  String get taskDeleteDialogTitle;

  /// Message for delete task confirmation dialog
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this task? This action cannot be undone.'**
  String get taskDeleteDialogMessage;

  /// Label for task description section
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get taskDescriptionLabel;

  /// Label for task information section
  ///
  /// In en, this message translates to:
  /// **'Task Information'**
  String get taskInformationLabel;

  /// Label for task dates section
  ///
  /// In en, this message translates to:
  /// **'Dates'**
  String get taskDatesLabel;

  /// Label for task due date field
  ///
  /// In en, this message translates to:
  /// **'Due Date'**
  String get taskDueDateLabel;

  /// Text shown when task has no due date
  ///
  /// In en, this message translates to:
  /// **'No due date'**
  String get taskNoDueDate;

  /// Placeholder text for setting due date
  ///
  /// In en, this message translates to:
  /// **'Set due date'**
  String get taskSetDueDate;

  /// Label for task status field
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get taskStatusLabel;

  /// Label for task priority field
  ///
  /// In en, this message translates to:
  /// **'Priority'**
  String get taskPriorityLabel;

  /// Text showing task priority level
  ///
  /// In en, this message translates to:
  /// **'{priority} Priority'**
  String taskPriorityText(String priority);

  /// Label for task created date
  ///
  /// In en, this message translates to:
  /// **'Created'**
  String get taskCreatedAtLabel;

  /// Label for task last updated date
  ///
  /// In en, this message translates to:
  /// **'Last Updated'**
  String get taskUpdatedAtLabel;

  /// Label for task title field
  ///
  /// In en, this message translates to:
  /// **'Title'**
  String get taskTitleLabel;

  /// Hint text for task title field
  ///
  /// In en, this message translates to:
  /// **'Enter task title'**
  String get taskTitleHint;

  /// Hint text for task description field
  ///
  /// In en, this message translates to:
  /// **'Enter task description'**
  String get taskDescriptionHint;

  /// Hint text for optional task description field
  ///
  /// In en, this message translates to:
  /// **'Enter task description (optional)'**
  String get taskDescriptionOptionalHint;

  /// Hint text for optional due date field
  ///
  /// In en, this message translates to:
  /// **'Select due date (optional)'**
  String get taskDueDateOptionalHint;

  /// Label indicating title is required
  ///
  /// In en, this message translates to:
  /// **'Title *'**
  String get taskTitleRequired;

  /// Label for todo task status
  ///
  /// In en, this message translates to:
  /// **'To Do'**
  String get taskStatusTodo;

  /// Label for in progress task status
  ///
  /// In en, this message translates to:
  /// **'In Progress'**
  String get taskStatusInProgress;

  /// Label for done task status
  ///
  /// In en, this message translates to:
  /// **'Done'**
  String get taskStatusDone;

  /// Label for high priority
  ///
  /// In en, this message translates to:
  /// **'High'**
  String get taskPriorityHigh;

  /// Label for medium priority
  ///
  /// In en, this message translates to:
  /// **'Medium'**
  String get taskPriorityMedium;

  /// Label for low priority
  ///
  /// In en, this message translates to:
  /// **'Low'**
  String get taskPriorityLow;

  /// Label for no priority
  ///
  /// In en, this message translates to:
  /// **'None'**
  String get taskPriorityNone;

  /// Title for create task bottom sheet
  ///
  /// In en, this message translates to:
  /// **'Create Task'**
  String get taskCreateTitle;

  /// Title shown when task list is empty
  ///
  /// In en, this message translates to:
  /// **'No Tasks Yet'**
  String get taskEmptyTitle;

  /// Subtitle shown when task list is empty
  ///
  /// In en, this message translates to:
  /// **'Create your first task to get started'**
  String get taskEmptySubtitle;

  /// First tip in empty task list
  ///
  /// In en, this message translates to:
  /// **'• Tap the + button to create a task'**
  String get taskEmptyTip1;

  /// Second tip in empty task list
  ///
  /// In en, this message translates to:
  /// **'• Add a title and description'**
  String get taskEmptyTip2;

  /// Third tip in empty task list
  ///
  /// In en, this message translates to:
  /// **'• Set priority and due date'**
  String get taskEmptyTip3;

  /// Title for progress summary section
  ///
  /// In en, this message translates to:
  /// **'Your Progress'**
  String get taskProgressTitle;

  /// Text shown when there are no tasks
  ///
  /// In en, this message translates to:
  /// **'No tasks yet'**
  String get taskProgressNoTasks;

  /// Text showing how many tasks are completed
  ///
  /// In en, this message translates to:
  /// **'{completed} of {total} completed'**
  String taskProgressCompleted(int completed, int total);

  /// Short format showing completed tasks count
  ///
  /// In en, this message translates to:
  /// **'{completed}/{total}'**
  String taskProgressCount(int completed, int total);

  /// Welcome message when there are no tasks
  ///
  /// In en, this message translates to:
  /// **'Welcome! Let\'s add your first task.'**
  String get taskProgressWelcome;

  /// Encouragement message when making progress
  ///
  /// In en, this message translates to:
  /// **'You\'re making steady progress'**
  String get taskProgressEncouragement;

  /// Cancel button text
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get commonCancel;

  /// Delete button text
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get commonDelete;

  /// Update button text
  ///
  /// In en, this message translates to:
  /// **'Update'**
  String get commonUpdate;

  /// Retry button text
  ///
  /// In en, this message translates to:
  /// **'Retry'**
  String get commonRetry;

  /// Not available abbreviation
  ///
  /// In en, this message translates to:
  /// **'N/A'**
  String get commonNA;

  /// Title for projects screen
  ///
  /// In en, this message translates to:
  /// **'Projects'**
  String get projectsTitle;

  /// Placeholder text for project search field
  ///
  /// In en, this message translates to:
  /// **'Search projects...'**
  String get projectsSearchPlaceholder;

  /// Title shown when project list is empty
  ///
  /// In en, this message translates to:
  /// **'No Projects Yet'**
  String get projectsEmptyTitle;

  /// Subtitle shown when project list is empty
  ///
  /// In en, this message translates to:
  /// **'Create your first project to organize your tasks'**
  String get projectsEmptySubtitle;

  /// Error message when projects fail to load
  ///
  /// In en, this message translates to:
  /// **'Failed to load projects'**
  String get projectsLoadError;

  /// Error message when loading more projects fails
  ///
  /// In en, this message translates to:
  /// **'Failed to load more projects'**
  String get projectsLoadMoreError;

  /// Success message after deleting a project
  ///
  /// In en, this message translates to:
  /// **'Project deleted successfully'**
  String get projectDeletedSuccess;

  /// Error message when deleting a project fails
  ///
  /// In en, this message translates to:
  /// **'Failed to delete project'**
  String get projectDeletedError;

  /// Success message after creating a project
  ///
  /// In en, this message translates to:
  /// **'Project created successfully'**
  String get projectCreatedSuccess;

  /// Error message when creating a project fails
  ///
  /// In en, this message translates to:
  /// **'Failed to create project'**
  String get projectCreatedError;

  /// Success message after updating a project
  ///
  /// In en, this message translates to:
  /// **'Project updated successfully'**
  String get projectUpdatedSuccess;

  /// Error message when updating a project fails
  ///
  /// In en, this message translates to:
  /// **'Failed to update project'**
  String get projectUpdatedError;

  /// Title for project details screen
  ///
  /// In en, this message translates to:
  /// **'Project Details'**
  String get projectDetailsTitle;

  /// Title for create project screen
  ///
  /// In en, this message translates to:
  /// **'Create Project'**
  String get projectCreateTitle;

  /// Title for edit project screen
  ///
  /// In en, this message translates to:
  /// **'Edit Project'**
  String get projectEditTitle;

  /// Label for project name field
  ///
  /// In en, this message translates to:
  /// **'Project Name'**
  String get projectNameLabel;

  /// Hint text for project name field
  ///
  /// In en, this message translates to:
  /// **'Enter project name'**
  String get projectNameHint;

  /// Label for project description field
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get projectDescriptionLabel;

  /// Hint text for project description field
  ///
  /// In en, this message translates to:
  /// **'Enter project description (optional)'**
  String get projectDescriptionHint;

  /// Error message when project name is empty
  ///
  /// In en, this message translates to:
  /// **'Project name is required'**
  String get projectNameRequired;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'es', 'pt'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when language+country codes are specified.
  switch (locale.languageCode) {
    case 'pt':
      {
        switch (locale.countryCode) {
          case 'BR':
            return AppLocalizationsPtBr();
        }
        break;
      }
  }

  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
    case 'pt':
      return AppLocalizationsPt();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}

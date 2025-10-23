import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_es.dart';
import 'app_localizations_pt.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
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

  /// The application name
  ///
  /// In en, this message translates to:
  /// **'TaskIt'**
  String get appName;

  /// Accessibility description for show password icon
  ///
  /// In en, this message translates to:
  /// **'Show password'**
  String get contentDescriptionShowPassword;

  /// Accessibility description for hide password icon
  ///
  /// In en, this message translates to:
  /// **'Hide password'**
  String get contentDescriptionHidePassword;

  /// Accessibility description for back button
  ///
  /// In en, this message translates to:
  /// **'Back'**
  String get contentDescriptionBack;

  /// Accessibility description for delete button
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get contentDescriptionDelete;

  /// Accessibility description for add task button
  ///
  /// In en, this message translates to:
  /// **'Add Task'**
  String get contentDescriptionAddTask;

  /// Accessibility description for search icon
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get contentDescriptionSearch;

  /// Text asking if user doesn't have an account
  ///
  /// In en, this message translates to:
  /// **'Don\'t have an account?'**
  String get titleWithoutAccount;

  /// Sign up button text
  ///
  /// In en, this message translates to:
  /// **'Sign Up'**
  String get buttonSignUp;

  /// Email field label
  ///
  /// In en, this message translates to:
  /// **'Email'**
  String get titleEmail;

  /// Password field label
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get titlePassword;

  /// Login button text
  ///
  /// In en, this message translates to:
  /// **'Login'**
  String get titleLoginButton;

  /// Email validation error message
  ///
  /// In en, this message translates to:
  /// **'Please enter a valid email address'**
  String get titleEmailError;

  /// Password validation error message
  ///
  /// In en, this message translates to:
  /// **'Password must be at least 8 characters'**
  String get titlePasswordError;

  /// Name field label
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get titleName;

  /// Name validation error message
  ///
  /// In en, this message translates to:
  /// **'Name should not be empty'**
  String get titleNameError;

  /// Register button text
  ///
  /// In en, this message translates to:
  /// **'Register'**
  String get titleRegisterButton;

  /// Confirm password field label
  ///
  /// In en, this message translates to:
  /// **'Confirm Password'**
  String get titleConfirmPassword;

  /// Confirm password validation error message
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match'**
  String get titleConfirmPasswordError;

  /// Text asking if user already has an account
  ///
  /// In en, this message translates to:
  /// **'Already have an account?'**
  String get titleAlreadyHaveAccount;

  /// Sign in button text
  ///
  /// In en, this message translates to:
  /// **'Sign in'**
  String get buttonSignIn;

  /// Bottom navigation tasks tab
  ///
  /// In en, this message translates to:
  /// **'Tasks'**
  String get navTasks;

  /// Bottom navigation projects tab
  ///
  /// In en, this message translates to:
  /// **'Projects'**
  String get navProjects;

  /// Bottom navigation profile tab
  ///
  /// In en, this message translates to:
  /// **'Profile'**
  String get navProfile;

  /// Bottom navigation calendar tab
  ///
  /// In en, this message translates to:
  /// **'Calendar'**
  String get navCalendar;

  /// Tasks screen title
  ///
  /// In en, this message translates to:
  /// **'My Tasks'**
  String get tasksTitle;

  /// Placeholder text for tasks search field
  ///
  /// In en, this message translates to:
  /// **'Search tasks...'**
  String get tasksSearchPlaceholder;

  /// Progress section title
  ///
  /// In en, this message translates to:
  /// **'Your Progress'**
  String get tasksProgressTitle;

  /// Progress percentage display
  ///
  /// In en, this message translates to:
  /// **'{percentage}%'**
  String tasksProgressPercentage(int percentage);

  /// Progress completion text
  ///
  /// In en, this message translates to:
  /// **'{completed} of {total} tasks completed'**
  String tasksProgressCompleted(int completed, int total);

  /// Empty tasks list title
  ///
  /// In en, this message translates to:
  /// **'Ready to Get Started?'**
  String get tasksEmptyTitle;

  /// Empty tasks list subtitle
  ///
  /// In en, this message translates to:
  /// **'Here are some ideas to help you begin:'**
  String get tasksEmptySubtitle;

  /// Task details screen title
  ///
  /// In en, this message translates to:
  /// **'Task Details'**
  String get taskDetailsTitle;

  /// Task due date label
  ///
  /// In en, this message translates to:
  /// **'Due:'**
  String get taskDueDate;

  /// Text when task has no due date
  ///
  /// In en, this message translates to:
  /// **'No due date'**
  String get taskNoDueDate;

  /// Task project label
  ///
  /// In en, this message translates to:
  /// **'Project:'**
  String get taskProject;

  /// Task assigned to label
  ///
  /// In en, this message translates to:
  /// **'Assigned to:'**
  String get taskAssignedTo;

  /// Projects screen title
  ///
  /// In en, this message translates to:
  /// **'Projects'**
  String get projectsTitle;

  /// Placeholder text for projects search field
  ///
  /// In en, this message translates to:
  /// **'Search projects...'**
  String get projectsSearchPlaceholder;

  /// All projects section title
  ///
  /// In en, this message translates to:
  /// **'All Projects'**
  String get projectsAll;

  /// Add project button text
  ///
  /// In en, this message translates to:
  /// **'Add Project'**
  String get projectsAdd;

  /// Empty projects list title
  ///
  /// In en, this message translates to:
  /// **'No Projects Found'**
  String get projectsEmptyTitle;

  /// Empty projects list subtitle
  ///
  /// In en, this message translates to:
  /// **'Create your first project to get started'**
  String get projectsEmptySubtitle;

  /// Project completed tasks count
  ///
  /// In en, this message translates to:
  /// **'{count} completed'**
  String projectCompleted(int count);

  /// Project in progress tasks count
  ///
  /// In en, this message translates to:
  /// **'{count} in progress'**
  String projectInProgress(int count);

  /// Project total tasks count
  ///
  /// In en, this message translates to:
  /// **'{count} total'**
  String projectTotal(int count);

  /// Profile screen placeholder text
  ///
  /// In en, this message translates to:
  /// **'Profile Screen - Coming Soon'**
  String get profileComingSoon;

  /// Create task screen title
  ///
  /// In en, this message translates to:
  /// **'Create Task'**
  String get createTask;

  /// Edit task screen title
  ///
  /// In en, this message translates to:
  /// **'Edit Task'**
  String get editTask;

  /// Task title field label
  ///
  /// In en, this message translates to:
  /// **'Title'**
  String get taskTitleLabel;

  /// Task title validation error
  ///
  /// In en, this message translates to:
  /// **'Title is required'**
  String get taskTitleError;

  /// Task description field label
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get taskDescriptionLabel;

  /// Task priority field label
  ///
  /// In en, this message translates to:
  /// **'Priority'**
  String get taskPriorityLabel;

  /// Task due date field label
  ///
  /// In en, this message translates to:
  /// **'Due Date'**
  String get taskDueDateLabel;

  /// Task due date field placeholder
  ///
  /// In en, this message translates to:
  /// **'DD/MM/YYYY'**
  String get taskDueDatePlaceholder;

  /// Task title field placeholder
  ///
  /// In en, this message translates to:
  /// **'Enter task title'**
  String get taskTitlePlaceholder;

  /// Task description field placeholder
  ///
  /// In en, this message translates to:
  /// **'Enter task description (optional)'**
  String get taskDescriptionPlaceholder;

  /// Task project field label
  ///
  /// In en, this message translates to:
  /// **'Project'**
  String get taskProjectLabel;

  /// Task project field placeholder
  ///
  /// In en, this message translates to:
  /// **'Select a project'**
  String get taskProjectPlaceholder;

  /// Task project required error message
  ///
  /// In en, this message translates to:
  /// **'Please select a project'**
  String get taskProjectRequired;

  /// Task load error message
  ///
  /// In en, this message translates to:
  /// **'Failed to load task: {error}'**
  String taskLoadError(String error);

  /// Task creation error message
  ///
  /// In en, this message translates to:
  /// **'Failed to create task: {error}'**
  String taskCreateError(String error);

  /// Task update error message
  ///
  /// In en, this message translates to:
  /// **'Failed to update task: {error}'**
  String taskUpdateError(String error);

  /// Delete task dialog title
  ///
  /// In en, this message translates to:
  /// **'Delete Task'**
  String get taskDeleteTitle;

  /// Delete task confirmation message
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this task? This action cannot be undone.'**
  String get taskDeleteMessage;

  /// Delete task button text
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get taskDeleteButton;

  /// Task deletion error message
  ///
  /// In en, this message translates to:
  /// **'Failed to delete task: {error}'**
  String taskDeleteError(String error);

  /// Create project screen title
  ///
  /// In en, this message translates to:
  /// **'Create Project'**
  String get createProject;

  /// Edit project screen title
  ///
  /// In en, this message translates to:
  /// **'Edit Project'**
  String get editProject;

  /// Project name field label
  ///
  /// In en, this message translates to:
  /// **'Project Name'**
  String get projectNameLabel;

  /// Project name validation error
  ///
  /// In en, this message translates to:
  /// **'Project name cannot be empty'**
  String get projectNameError;

  /// Project description field label
  ///
  /// In en, this message translates to:
  /// **'Description (optional)'**
  String get projectDescriptionLabel;

  /// Cancel project button text
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get projectCancelButton;

  /// Create project button text
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get projectCreateButton;

  /// Update project button text
  ///
  /// In en, this message translates to:
  /// **'Update'**
  String get projectUpdateButton;

  /// Project load error message
  ///
  /// In en, this message translates to:
  /// **'Failed to load project: {error}'**
  String projectLoadError(String error);

  /// Project creation error message
  ///
  /// In en, this message translates to:
  /// **'Failed to create project: {error}'**
  String projectCreateError(String error);

  /// Project update error message
  ///
  /// In en, this message translates to:
  /// **'Failed to update project: {error}'**
  String projectUpdateError(String error);

  /// Project name field placeholder
  ///
  /// In en, this message translates to:
  /// **'Enter project name'**
  String get projectNamePlaceholder;

  /// Project description field placeholder
  ///
  /// In en, this message translates to:
  /// **'Enter project description (optional)'**
  String get projectDescriptionPlaceholder;

  /// App name displayed on auth screens
  ///
  /// In en, this message translates to:
  /// **'Task Manager'**
  String get authAppName;

  /// Text asking if user doesn't have an account
  ///
  /// In en, this message translates to:
  /// **'Don\'t have an account?'**
  String get authWithoutAccount;

  /// Sign up button text
  ///
  /// In en, this message translates to:
  /// **'Sign Up'**
  String get authSignUp;

  /// Email field label
  ///
  /// In en, this message translates to:
  /// **'Email'**
  String get authEmail;

  /// Password field label
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get authPassword;

  /// Login button text
  ///
  /// In en, this message translates to:
  /// **'Login'**
  String get authLoginButton;

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

  /// Name field label for registration
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get authName;

  /// Error message for empty name field
  ///
  /// In en, this message translates to:
  /// **'Name should not be empty'**
  String get authNameError;

  /// Register button text
  ///
  /// In en, this message translates to:
  /// **'Register'**
  String get authRegisterButton;

  /// Confirm password field label
  ///
  /// In en, this message translates to:
  /// **'Confirm Password'**
  String get authConfirmPassword;

  /// Error message when passwords don't match
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match'**
  String get authConfirmPasswordError;

  /// Text asking if user already has an account
  ///
  /// In en, this message translates to:
  /// **'Already have an account?'**
  String get authAlreadyHaveAccount;

  /// Sign in button text
  ///
  /// In en, this message translates to:
  /// **'Sign in'**
  String get authSignIn;

  /// Demo credentials section title
  ///
  /// In en, this message translates to:
  /// **'Demo Credentials'**
  String get authDemoCredentials;

  /// Demo credentials text
  ///
  /// In en, this message translates to:
  /// **'Email: test@example.com\nPassword: password'**
  String get authDemoCredentialsText;

  /// Accessibility description for show password icon
  ///
  /// In en, this message translates to:
  /// **'Show password'**
  String get accessibilityShowPassword;

  /// Accessibility description for hide password icon
  ///
  /// In en, this message translates to:
  /// **'Hide password'**
  String get accessibilityHidePassword;

  /// Create task button text
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get taskCreateButton;

  /// Update task button text
  ///
  /// In en, this message translates to:
  /// **'Save Changes'**
  String get taskUpdateButton;

  /// Cancel task button text
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get taskCancelButton;

  /// Error message when email field is empty
  ///
  /// In en, this message translates to:
  /// **'Email is required'**
  String get validationEmailRequired;

  /// Error message when email format is invalid
  ///
  /// In en, this message translates to:
  /// **'Please enter a valid email address'**
  String get validationEmailInvalid;

  /// Error message when password field is empty
  ///
  /// In en, this message translates to:
  /// **'Password is required'**
  String get validationPasswordRequired;

  /// Error message when password is too short
  ///
  /// In en, this message translates to:
  /// **'Password must be at least 8 characters long'**
  String get validationPasswordTooShort;

  /// Error message when password is too long
  ///
  /// In en, this message translates to:
  /// **'Password must be less than 128 characters long'**
  String get validationPasswordTooLong;

  /// Error message when password lacks lowercase letters
  ///
  /// In en, this message translates to:
  /// **'Password must contain at least one lowercase letter'**
  String get validationPasswordNeedsLowercase;

  /// Error message when password lacks uppercase letters
  ///
  /// In en, this message translates to:
  /// **'Password must contain at least one uppercase letter'**
  String get validationPasswordNeedsUppercase;

  /// Error message when password lacks numbers
  ///
  /// In en, this message translates to:
  /// **'Password must contain at least one number'**
  String get validationPasswordNeedsNumber;

  /// Error message when password lacks special characters
  ///
  /// In en, this message translates to:
  /// **'Password must contain at least one special character'**
  String get validationPasswordNeedsSpecialChar;

  /// Error message when confirm password field is empty
  ///
  /// In en, this message translates to:
  /// **'Please confirm your password'**
  String get validationConfirmPasswordRequired;

  /// Error message when passwords don't match
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match'**
  String get validationPasswordsDoNotMatch;

  /// Error message when name field is empty
  ///
  /// In en, this message translates to:
  /// **'Name is required'**
  String get validationNameRequired;

  /// Error message when name is too short
  ///
  /// In en, this message translates to:
  /// **'Name must be at least 2 characters long'**
  String get validationNameTooShort;

  /// Error message when name is too long
  ///
  /// In en, this message translates to:
  /// **'Name must be less than 50 characters long'**
  String get validationNameTooLong;

  /// Error message when name contains invalid characters
  ///
  /// In en, this message translates to:
  /// **'Name can only contain letters, spaces, hyphens, and apostrophes'**
  String get validationNameInvalidCharacters;

  /// Generic error message for required fields
  ///
  /// In en, this message translates to:
  /// **'{fieldName} is required'**
  String validationFieldRequired(String fieldName);

  /// Password strength indicator for very weak passwords
  ///
  /// In en, this message translates to:
  /// **'Very Weak'**
  String get passwordStrengthVeryWeak;

  /// Password strength indicator for weak passwords
  ///
  /// In en, this message translates to:
  /// **'Weak'**
  String get passwordStrengthWeak;

  /// Password strength indicator for medium passwords
  ///
  /// In en, this message translates to:
  /// **'Medium'**
  String get passwordStrengthMedium;

  /// Password strength indicator for strong passwords
  ///
  /// In en, this message translates to:
  /// **'Strong'**
  String get passwordStrengthStrong;

  /// Password strength indicator for very strong passwords
  ///
  /// In en, this message translates to:
  /// **'Very Strong'**
  String get passwordStrengthVeryStrong;

  /// Hint text for task project dropdown
  ///
  /// In en, this message translates to:
  /// **'Select a project'**
  String get taskProjectHint;

  /// Save changes button text
  ///
  /// In en, this message translates to:
  /// **'Save Changes'**
  String get saveChanges;

  /// Success message when task is created
  ///
  /// In en, this message translates to:
  /// **'Task created successfully'**
  String get taskCreated;

  /// Success message when task is updated
  ///
  /// In en, this message translates to:
  /// **'Task updated successfully'**
  String get taskUpdated;

  /// Cancel button text
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// Delete button text
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get delete;

  /// Delete task dialog title
  ///
  /// In en, this message translates to:
  /// **'Delete Task'**
  String get deleteTask;
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

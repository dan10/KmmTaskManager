// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_l10n.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get authAppName => 'Gestor de Tareas';

  @override
  String get authLoginTitle => 'Bienvenido de Vuelta';

  @override
  String get authRegisterTitle => 'Crear Cuenta';

  @override
  String get authEmail => 'Correo Electrónico';

  @override
  String get authEmailHint => 'Ingresa tu correo electrónico';

  @override
  String get authPassword => 'Contraseña';

  @override
  String get authPasswordHint => 'Ingresa tu contraseña';

  @override
  String get authConfirmPassword => 'Confirmar Contraseña';

  @override
  String get authName => 'Nombre';

  @override
  String get authNameHint => 'Ingresa tu nombre completo';

  @override
  String get authLoginButton => 'Iniciar Sesión';

  @override
  String get authRegisterButton => 'Crear Cuenta';

  @override
  String get authSignUp => 'Registrarse';

  @override
  String get authSignIn => 'Iniciar Sesión';

  @override
  String get authWithoutAccount => '¿No tienes una cuenta?';

  @override
  String get authAlreadyHaveAccount => '¿Ya tienes una cuenta?';

  @override
  String get authEmailError =>
      'Por favor, ingresa una dirección de correo electrónico válida';

  @override
  String get authPasswordError =>
      'La contraseña debe tener al menos 8 caracteres';

  @override
  String authPasswordTooShort(int minLength) {
    return 'La contraseña debe tener al menos $minLength caracteres';
  }

  @override
  String get authNameError => 'El nombre no debe estar vacío';

  @override
  String get authNameTooShort => 'El nombre debe tener al menos 2 caracteres';

  @override
  String get authConfirmPasswordError => 'Las contraseñas no coinciden';

  @override
  String authLoginError(String error) {
    return 'Error al iniciar sesión: $error';
  }

  @override
  String authRegisterError(String error) {
    return 'Error al registrarse: $error';
  }

  @override
  String get authForgotPassword => '¿Olvidaste tu contraseña?';

  @override
  String get authResetPassword => 'Restablecer Contraseña';

  @override
  String get authLogout => 'Cerrar Sesión';

  @override
  String get authLoggingIn => 'Iniciando sesión...';

  @override
  String get authCreatingAccount => 'Creando cuenta...';

  @override
  String get taskDetailsTitle => 'Detalles de la Tarea';

  @override
  String get taskEditTitle => 'Editar Tarea';

  @override
  String get taskListTitle => 'Tareas';

  @override
  String get taskSearchPlaceholder => 'Buscar tareas...';

  @override
  String get taskNotFound => 'Tarea no encontrada';

  @override
  String get taskLoadError => 'Error al cargar los detalles de la tarea';

  @override
  String get taskDeletedSuccess => 'Tarea eliminada con éxito';

  @override
  String get taskDeletedError => 'Error al eliminar la tarea';

  @override
  String get taskUpdatedSuccess => 'Tarea actualizada con éxito';

  @override
  String get taskUpdatedError => 'Error al actualizar la tarea';

  @override
  String get taskCreatedSuccess => 'Tarea creada con éxito';

  @override
  String taskCreatedError(String error) {
    return 'Error al crear la tarea: $error';
  }

  @override
  String get taskDeleteDialogTitle => 'Eliminar Tarea';

  @override
  String get taskDeleteDialogMessage =>
      '¿Estás seguro de que quieres eliminar esta tarea? Esta acción no se puede deshacer.';

  @override
  String get taskDescriptionLabel => 'Descripción';

  @override
  String get taskInformationLabel => 'Información de la Tarea';

  @override
  String get taskDatesLabel => 'Fechas';

  @override
  String get taskDueDateLabel => 'Fecha de Vencimiento';

  @override
  String get taskNoDueDate => 'Sin fecha de vencimiento';

  @override
  String get taskSetDueDate => 'Establecer fecha de vencimiento';

  @override
  String get taskStatusLabel => 'Estado';

  @override
  String get taskPriorityLabel => 'Prioridad';

  @override
  String taskPriorityText(String priority) {
    return 'Prioridad $priority';
  }

  @override
  String get taskCreatedAtLabel => 'Creada';

  @override
  String get taskUpdatedAtLabel => 'Última Actualización';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleHint => 'Ingresa el título de la tarea';

  @override
  String get taskDescriptionHint => 'Ingresa la descripción de la tarea';

  @override
  String get taskDescriptionOptionalHint =>
      'Ingresa la descripción de la tarea (opcional)';

  @override
  String get taskDueDateOptionalHint =>
      'Selecciona la fecha de vencimiento (opcional)';

  @override
  String get taskTitleRequired => 'Título *';

  @override
  String get taskStatusTodo => 'Por Hacer';

  @override
  String get taskStatusInProgress => 'En Progreso';

  @override
  String get taskStatusDone => 'Completada';

  @override
  String get taskPriorityHigh => 'Alta';

  @override
  String get taskPriorityMedium => 'Media';

  @override
  String get taskPriorityLow => 'Baja';

  @override
  String get taskPriorityNone => 'Ninguna';

  @override
  String get taskCreateTitle => 'Crear Tarea';

  @override
  String get taskEmptyTitle => 'Aún No Hay Tareas';

  @override
  String get taskEmptySubtitle => 'Crea tu primera tarea para comenzar';

  @override
  String get taskEmptyTip1 => '• Toca el botón + para crear una tarea';

  @override
  String get taskEmptyTip2 => '• Agrega un título y descripción';

  @override
  String get taskEmptyTip3 => '• Establece la prioridad y fecha de vencimiento';

  @override
  String get taskProgressTitle => 'Tu Progreso';

  @override
  String get taskProgressNoTasks => 'Aún no hay tareas';

  @override
  String taskProgressCompleted(int completed, int total) {
    return '$completed de $total completadas';
  }

  @override
  String taskProgressCount(int completed, int total) {
    return '$completed/$total';
  }

  @override
  String get taskProgressWelcome => '¡Bienvenido! Agreguemos tu primera tarea.';

  @override
  String get taskProgressEncouragement =>
      'Estás haciendo un progreso constante';

  @override
  String get commonCancel => 'Cancelar';

  @override
  String get commonDelete => 'Eliminar';

  @override
  String get commonUpdate => 'Actualizar';

  @override
  String get commonRetry => 'Reintentar';

  @override
  String get commonNA => 'N/D';

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

  @override
  String get calendarTitle => 'Calendar';

  @override
  String calendarTasksFor(String date) {
    return 'Tasks for $date';
  }

  @override
  String calendarTaskCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count tasks',
      one: '1 task',
      zero: 'No tasks',
    );
    return '$_temp0';
  }

  @override
  String get calendarNoTasksForDate => 'No tasks scheduled for this date';

  @override
  String get profileTitle => 'Profile';

  @override
  String get profileDisplayName => 'Display Name';

  @override
  String get profileEmail => 'Email';

  @override
  String get profileLogout => 'Logout';

  @override
  String get profileLogoutConfirmTitle => 'Logout';

  @override
  String get profileLogoutConfirmMessage => 'Are you sure you want to logout?';

  @override
  String get profileErrorLoading => 'Unable to load user information';

  @override
  String get cancel => 'Cancel';
}

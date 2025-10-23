// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get contentDescriptionShowPassword => 'Mostrar contraseña';

  @override
  String get contentDescriptionHidePassword => 'Ocultar contraseña';

  @override
  String get contentDescriptionBack => 'Volver';

  @override
  String get contentDescriptionDelete => 'Eliminar';

  @override
  String get contentDescriptionAddTask => 'Añadir Tarea';

  @override
  String get contentDescriptionSearch => 'Buscar';

  @override
  String get titleWithoutAccount => '¿No tienes una cuenta?';

  @override
  String get buttonSignUp => 'Registrarse';

  @override
  String get titleEmail => 'Email';

  @override
  String get titlePassword => 'Contraseña';

  @override
  String get titleLoginButton => 'Iniciar Sesión';

  @override
  String get titleEmailError =>
      'Por favor, introduce una dirección de email válida';

  @override
  String get titlePasswordError =>
      'La contraseña debe tener al menos 8 caracteres';

  @override
  String get titleName => 'Nombre';

  @override
  String get titleNameError => 'Por favor, introduce un nombre';

  @override
  String get titleRegisterButton => 'Registrar';

  @override
  String get titleConfirmPassword => 'Confirmar Contraseña';

  @override
  String get titleConfirmPasswordError => 'Las contraseñas no coinciden';

  @override
  String get titleAlreadyHaveAccount => '¿Ya tienes una cuenta?';

  @override
  String get buttonSignIn => 'Iniciar Sesión';

  @override
  String get navTasks => 'Tareas';

  @override
  String get navProjects => 'Proyectos';

  @override
  String get navProfile => 'Perfil';

  @override
  String get navCalendar => 'Calendario';

  @override
  String get tasksTitle => 'Mis Tareas';

  @override
  String get tasksSearchPlaceholder => 'Buscar tareas...';

  @override
  String get tasksProgressTitle => 'Tu Progreso';

  @override
  String tasksProgressPercentage(int percentage) {
    return '$percentage%';
  }

  @override
  String tasksProgressCompleted(int completed, int total) {
    return '$completed de $total tareas completadas';
  }

  @override
  String get tasksEmptyTitle => '¿Listo para Empezar?';

  @override
  String get tasksEmptySubtitle =>
      'Aquí hay algunas ideas para ayudarte a comenzar:';

  @override
  String get taskDetailsTitle => 'Detalles de la Tarea';

  @override
  String get taskDueDate => 'Vencimiento:';

  @override
  String get taskNoDueDate => 'Sin fecha de vencimiento';

  @override
  String get taskProject => 'Proyecto:';

  @override
  String get taskAssignedTo => 'Asignado a:';

  @override
  String get projectsTitle => 'Proyectos';

  @override
  String get projectsSearchPlaceholder => 'Buscar proyectos...';

  @override
  String get projectsAll => 'Todos los Proyectos';

  @override
  String get projectsAdd => 'Añadir Proyecto';

  @override
  String get projectsEmptyTitle => 'No se encontraron Proyectos';

  @override
  String get projectsEmptySubtitle => 'Crea tu primer proyecto para comenzar';

  @override
  String projectCompleted(int count) {
    return '$count completadas';
  }

  @override
  String projectInProgress(int count) {
    return '$count en progreso';
  }

  @override
  String projectTotal(int count) {
    return '$count total';
  }

  @override
  String get profileComingSoon => 'Pantalla de Perfil - Próximamente';

  @override
  String get createTask => 'Crear Tarea';

  @override
  String get editTask => 'Editar Tarea';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleError => 'El título es obligatorio';

  @override
  String get taskDescriptionLabel => 'Descripción';

  @override
  String get taskPriorityLabel => 'Prioridad';

  @override
  String get taskDueDateLabel => 'Fecha de Vencimiento';

  @override
  String get taskDueDatePlaceholder => 'DD/MM/AAAA';

  @override
  String get taskTitlePlaceholder => 'Introduce el título de la tarea';

  @override
  String get taskDescriptionPlaceholder =>
      'Introduce la descripción de la tarea (opcional)';

  @override
  String get taskProjectLabel => 'Proyecto';

  @override
  String get taskProjectPlaceholder => 'Selecciona un proyecto';

  @override
  String get taskProjectRequired => 'Por favor, selecciona un proyecto';

  @override
  String taskLoadError(String error) {
    return 'Error al cargar tarea: $error';
  }

  @override
  String taskCreateError(String error) {
    return 'Error al crear tarea: $error';
  }

  @override
  String taskUpdateError(String error) {
    return 'Error al actualizar tarea: $error';
  }

  @override
  String get taskDeleteTitle => 'Eliminar Tarea';

  @override
  String get taskDeleteMessage =>
      '¿Estás seguro de que quieres eliminar esta tarea? Esta acción no se puede deshacer.';

  @override
  String get taskDeleteButton => 'Eliminar';

  @override
  String taskDeleteError(String error) {
    return 'Error al eliminar tarea: $error';
  }

  @override
  String get createProject => 'Crear Proyecto';

  @override
  String get editProject => 'Editar Proyecto';

  @override
  String get projectNameLabel => 'Nombre del Proyecto';

  @override
  String get projectNameError => 'El nombre del proyecto no puede estar vacío';

  @override
  String get projectDescriptionLabel => 'Descripción (opcional)';

  @override
  String get projectCancelButton => 'Cancelar';

  @override
  String get projectCreateButton => 'Crear';

  @override
  String get projectUpdateButton => 'Actualizar';

  @override
  String projectLoadError(String error) {
    return 'Error al cargar proyecto: $error';
  }

  @override
  String projectCreateError(String error) {
    return 'Error al crear proyecto: $error';
  }

  @override
  String projectUpdateError(String error) {
    return 'Error al actualizar proyecto: $error';
  }

  @override
  String get projectNamePlaceholder => 'Introduce el nombre del proyecto';

  @override
  String get projectDescriptionPlaceholder =>
      'Introduce la descripción del proyecto (opcional)';

  @override
  String get authAppName => 'Task Manager';

  @override
  String get authWithoutAccount => '¿No tienes una cuenta?';

  @override
  String get authSignUp => 'Registrarse';

  @override
  String get authEmail => 'Email';

  @override
  String get authPassword => 'Contraseña';

  @override
  String get authLoginButton => 'Iniciar Sesión';

  @override
  String get authEmailError =>
      'Por favor, introduce una dirección de email válida';

  @override
  String get authPasswordError =>
      'La contraseña debe tener al menos 8 caracteres';

  @override
  String get authName => 'Nombre';

  @override
  String get authNameError => 'Por favor, introduce un nombre';

  @override
  String get authRegisterButton => 'Registrar';

  @override
  String get authConfirmPassword => 'Confirmar Contraseña';

  @override
  String get authConfirmPasswordError => 'Las contraseñas no coinciden';

  @override
  String get authAlreadyHaveAccount => '¿Ya tienes una cuenta?';

  @override
  String get authSignIn => 'Iniciar Sesión';

  @override
  String get authDemoCredentials => 'Credenciales Demo';

  @override
  String get authDemoCredentialsText =>
      'Email: test@example.com\nContraseña: password';

  @override
  String get accessibilityShowPassword => 'Mostrar contraseña';

  @override
  String get accessibilityHidePassword => 'Ocultar contraseña';

  @override
  String get taskCreateButton => 'Crear';

  @override
  String get taskUpdateButton => 'Guardar Cambios';

  @override
  String get taskCancelButton => 'Cancelar';

  @override
  String get validationEmailRequired => 'El correo electrónico es obligatorio';

  @override
  String get validationEmailInvalid =>
      'Por favor ingrese una dirección de correo electrónico válida';

  @override
  String get validationPasswordRequired => 'La contraseña es obligatoria';

  @override
  String get validationPasswordTooShort =>
      'La contraseña debe tener al menos 8 caracteres';

  @override
  String get validationPasswordTooLong =>
      'La contraseña debe tener menos de 128 caracteres';

  @override
  String get validationPasswordNeedsLowercase =>
      'La contraseña debe contener al menos una letra minúscula';

  @override
  String get validationPasswordNeedsUppercase =>
      'La contraseña debe contener al menos una letra mayúscula';

  @override
  String get validationPasswordNeedsNumber =>
      'La contraseña debe contener al menos un número';

  @override
  String get validationPasswordNeedsSpecialChar =>
      'La contraseña debe contener al menos un carácter especial';

  @override
  String get validationConfirmPasswordRequired =>
      'Por favor confirme su contraseña';

  @override
  String get validationPasswordsDoNotMatch => 'Las contraseñas no coinciden';

  @override
  String get validationNameRequired => 'El nombre es obligatorio';

  @override
  String get validationNameTooShort =>
      'El nombre debe tener al menos 2 caracteres';

  @override
  String get validationNameTooLong =>
      'El nombre debe tener menos de 50 caracteres';

  @override
  String get validationNameInvalidCharacters =>
      'El nombre solo puede contener letras, espacios, guiones y apostrofes';

  @override
  String validationFieldRequired(String fieldName) {
    return '$fieldName es obligatorio';
  }

  @override
  String get passwordStrengthVeryWeak => 'Muy Débil';

  @override
  String get passwordStrengthWeak => 'Débil';

  @override
  String get passwordStrengthMedium => 'Medio';

  @override
  String get passwordStrengthStrong => 'Fuerte';

  @override
  String get passwordStrengthVeryStrong => 'Muy Fuerte';

  @override
  String get taskProjectHint => 'Seleccionar un proyecto';

  @override
  String get saveChanges => 'Guardar Cambios';

  @override
  String get taskCreated => 'Tarea creada con éxito';

  @override
  String get taskUpdated => 'Tarea actualizada con éxito';

  @override
  String get cancel => 'Cancelar';

  @override
  String get delete => 'Eliminar';

  @override
  String get deleteTask => 'Eliminar Tarea';
}

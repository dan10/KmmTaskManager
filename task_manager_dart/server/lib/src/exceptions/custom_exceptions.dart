import './app_exception.dart';

// General Purpose Exceptions
class NotFoundException extends AppException {
  @override
  final int statusCode;
  @override
  final String errorType;
  @override
  final String message;
  @override
  final Map<String, dynamic>? details;

  NotFoundException({
    this.statusCode = 404,
    this.errorType = 'Not Found',
    required this.message,
    this.details,
  });
}

class ValidationException extends AppException {
  @override
  final int statusCode;
  @override
  final String errorType;
  @override
  final String message;
  @override
  final Map<String, dynamic>? details;

  ValidationException({
    this.statusCode = 400,
    this.errorType = 'Bad Request',
    required this.message,
    this.details,
  });
}

class AuthenticationException extends AppException {
  @override
  final int statusCode;
  @override
  final String errorType;
  @override
  final String message;
  @override
  final Map<String, dynamic>? details;
  
  AuthenticationException({
    this.statusCode = 401,
    this.errorType = 'Unauthorized',
    required this.message,
    this.details,
  });
}

class ConflictException extends AppException {
  @override
  final int statusCode;
  @override
  final String errorType;
  @override
  final String message;
  @override
  final Map<String, dynamic>? details;

  ConflictException({
    this.statusCode = 409,
    this.errorType = 'Conflict',
    required this.message,
    this.details,
  });
}


// Specific NotFound Exceptions
class ProjectNotFoundException extends NotFoundException {
  ProjectNotFoundException({String id = ''}) 
      : super(message: 'Project ${id.isNotEmpty ? "with ID $id" : ""} not found.');
}

class UserNotFoundException extends NotFoundException {
  UserNotFoundException({String id = ''}) 
      : super(message: 'User ${id.isNotEmpty ? "with ID $id" : ""} not found.');
}

class TaskNotFoundException extends NotFoundException {
  TaskNotFoundException({String id = ''}) 
      : super(message: 'Task ${id.isNotEmpty ? "with ID $id" : ""} not found.');
}


// Specific Conflict Exceptions
class AlreadyAssignedException extends ConflictException {
  AlreadyAssignedException({required String message}) 
      : super(message: message);
}

// Generic Forbidden Exception
class ForbiddenException extends AppException {
  @override
  final int statusCode;
  @override
  final String errorType;
  @override
  final String message;
  @override
  final Map<String, dynamic>? details;

  ForbiddenException({
    this.statusCode = 403,
    this.errorType = 'Forbidden',
    required this.message,
    this.details,
  });
}

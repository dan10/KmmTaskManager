import 'package:shelf/shelf.dart';
import 'package:task_manager_shared/models.dart';
import '../exceptions/app_exception.dart';
// custom_exceptions.dart is not directly used here but good to keep in mind its types extend AppException
// import '../exceptions/custom_exceptions.dart'; 
import '../util/shelf_helpers.dart'; // For jsonResponse

Middleware errorHandlingMiddleware() {
  return (Handler innerHandler) {
    return (Request request) async {
      try {
        return await innerHandler(request);
      } catch (e, stackTrace) {
        print('Error caught by middleware: $e');
        print(stackTrace); // Log stack trace for debugging

        if (e is AppException) {
          final errorDto = ErrorResponseDto(
            statusCode: e.statusCode,
            error: e.errorType,
            message: e.message,
            details: e.details,
          );
          return jsonResponse(errorDto.toJson(), statusCode: e.statusCode);
        } else if (e is FormatException) { // Catch FormatExceptions from readJsonBody or elsewhere
          final errorDto = ErrorResponseDto(
            statusCode: 400,
            error: 'Bad Request',
            message: 'Invalid request format: ${e.message}',
          );
          return jsonResponse(errorDto.toJson(), statusCode: 400);
        }
        // Add other specific non-AppException checks if needed, e.g. for JWT errors from a library
        /* else if (e is SomeLibrarySecurityException) {
           // ... handle specific library exception ...
           // final errorDto = ErrorResponseDto(statusCode: 401, error: 'Unauthorized', message: 'Authentication error.');
           // return jsonResponse(errorDto.toJson(), statusCode: 401);
        } */
          else {
          // Generic fallback for unknown errors
          final errorDto = ErrorResponseDto(
            statusCode: 500,
            error: 'Internal Server Error',
            message: 'An unexpected error occurred. Please try again later.',
            // Optionally include e.toString() in development for 'message', but not production
          );
          return jsonResponse(errorDto.toJson(), statusCode: 500);
        }
      }
    };
  };
}

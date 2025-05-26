abstract class AppException implements Exception {
  int get statusCode;
  String get errorType; // e.g., "NotFound", "Validation", "Authentication"
  String get message;
  Map<String, dynamic>? get details;

  @override
  String toString() {
    return 'AppException ($errorType - $statusCode): $message ${details != null ? details : ""}';
  }
}

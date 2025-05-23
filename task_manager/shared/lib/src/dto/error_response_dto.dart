class ErrorResponseDto {
  final int statusCode;
  final String error; // e.g., "Not Found", "Bad Request"
  final String? message;
  final Map<String, dynamic>? details;

  const ErrorResponseDto({
    required this.statusCode,
    required this.error,
    this.message,
    this.details,
  });

  factory ErrorResponseDto.fromJson(Map<String, dynamic> json) {
    return ErrorResponseDto(
      statusCode: json['statusCode'] as int,
      error: json['error'] as String,
      message: json['message'] as String?,
      details: json['details'] as Map<String, dynamic>?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'statusCode': statusCode,
      'error': error,
      'message': message,
      'details': details,
    };
  }
} 
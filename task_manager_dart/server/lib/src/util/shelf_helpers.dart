import 'dart:convert';
import 'package:shelf/shelf.dart';

// Extension for parsing JSON request bodies
extension RequestHelpers on Request {
  Future<Map<String, dynamic>> readJsonBody() async {
    try {
      final bodyString = await readAsString();
      if (bodyString.isEmpty) {
        // Or throw a specific exception if empty body is not allowed
        return <String, dynamic>{};
      }
      final json = jsonDecode(bodyString) as Map<String, dynamic>;
      return json;
    } catch (e) {
      // Consider throwing a specific BadRequestException or similar
      // to be caught by error handling middleware later.
      // For now, rethrow or throw a generic FormatException.
      print('Error reading or parsing JSON body: $e');
      throw FormatException('Invalid JSON format in request body: ${e.toString()}');
    }
  }
}

// Helper function for creating JSON responses
Response jsonResponse(dynamic data, {int statusCode = 200}) {
  // data is assumed to be a Map or List (or any object that jsonEncode can handle)
  // that has already been processed by .toJson() if it was a custom object.
  return Response(
    statusCode,
    body: jsonEncode(data),
    headers: {'Content-Type': 'application/json'},
  );
}

// Convenience helper for 200 OK JSON responses
Response okJsonResponse(dynamic data) {
  return jsonResponse(data, statusCode: 200);
}

// Example of other status code helpers if needed in the future
// Response createdJsonResponse(dynamic data) {
//   return jsonResponse(data, statusCode: 201);
// }

// Response badRequestJsonResponse({String? message, Map<String, dynamic>? details}) {
//   final body = <String, dynamic>{};
//   if (message != null) body['message'] = message;
//   if (details != null) body['details'] = details;
//   return jsonResponse(body, statusCode: 400);
// }

// Response notFoundJsonResponse({String? message}) {
//   final body = <String, dynamic>{'message': message ?? 'Resource not found'};
//   return jsonResponse(body, statusCode: 404);
// }

// Response internalServerErrorJsonResponse({String? message}) {
//    final body = <String, dynamic>{'message': message ?? 'Internal server error'};
//    return jsonResponse(body, statusCode: 500);
// }

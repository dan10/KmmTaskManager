// Unauthorized interceptor middleware for handling 401 responses

import 'package:http/http.dart' as http;
import 'package:pretty_http_logger/pretty_http_logger.dart';

/// Middleware that handles unauthorized (401) responses
class UnauthorizedInterceptor extends MiddlewareContract {
  UnauthorizedInterceptor({
    required this.onUnauthorized,
  });

  final void Function()? onUnauthorized;

  @override
  void interceptRequest(RequestData data) {
    // No request interception needed
  }

  @override
  void interceptResponse(ResponseData data) {
    // Check for 401 Unauthorized
    if (data.statusCode == 401) {
      // Trigger the unauthorized handler
      onUnauthorized?.call();
    }
  }

  @override
  void interceptError(dynamic error) {
    // Check if error is a Response with 401 status
    if (error is http.Response && error.statusCode == 401) {
      onUnauthorized?.call();
    }
  }
}


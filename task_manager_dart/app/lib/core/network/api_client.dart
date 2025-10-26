// Copyright 2024 Task Manager. All rights reserved.
// Core API client with middleware support for authentication, logging, and error handling.

import 'dart:convert';
import 'dart:io';

import 'package:pretty_http_logger/pretty_http_logger.dart';

import '../utils/result.dart';
import 'middleware/unauthorized_interceptor.dart';

/// Function type for providing authentication token
typedef AuthTokenProvider = Future<String?> Function();

/// Function type for handling unauthorized access (401 errors)
typedef UnauthorizedHandler = void Function();

/// API Client with middleware support for authentication, logging, and error handling
class ApiClient {
  ApiClient({
    String? host,
    int? port,
    String? scheme,
    AuthTokenProvider? authTokenProvider,
    UnauthorizedHandler? unauthorizedHandler,
    bool enableLogging = true,
  })  : _host = host ?? 'localhost',
        _port = port ?? 8081,
        _scheme = scheme ?? 'http',
        _authTokenProvider = authTokenProvider,
        _unauthorizedHandler = unauthorizedHandler {
    _initializeClient(enableLogging);
  }

  final String _host;
  final int _port;
  final String _scheme;
  final AuthTokenProvider? _authTokenProvider;
  final UnauthorizedHandler? _unauthorizedHandler;

  late HttpWithMiddleware _client;

  /// Initialize HTTP client with middleware
  void _initializeClient(bool enableLogging) {
    final middlewares = <MiddlewareContract>[
      // Logger should be first to log the full request
      if (enableLogging)
        HttpLogger(
          logLevel: LogLevel.BODY,
        ),
      
      // Unauthorized interceptor should be last to handle errors
      UnauthorizedInterceptor(onUnauthorized: _unauthorizedHandler),
    ];

    _client = HttpWithMiddleware.build(
      middlewares: middlewares,
      requestTimeout: const Duration(seconds: 30),
    );
  }

  /// Add authorization header if token is available
  Future<Map<String, String>> _getHeadersWithAuth() async {
    final headers = _getHeaders();
    
    if (_authTokenProvider != null) {
      final token = await _authTokenProvider!();
      if (token != null && token.isNotEmpty) {
        headers[HttpHeaders.authorizationHeader] = 'Bearer $token';
      }
    }

    return headers;
  }

  /// Build URI for API endpoint
  /// Query parameters must have String or Iterable<String> values
  Uri _buildUri(String path, {Map<String, String>? queryParameters}) {
    return Uri(
      scheme: _scheme,
      host: _host,
      port: _port,
      path: path,
      queryParameters: queryParameters,
    );
  }

  /// Common headers for all requests
  Map<String, String> _getHeaders() {
    return {
      HttpHeaders.contentTypeHeader: 'application/json',
      HttpHeaders.acceptHeader: 'application/json',
    };
  }

  /// Generic GET request
  Future<Result<T>> get<T>(
    String path, {
    Map<String, String>? queryParameters,
    required T Function(dynamic json) fromJson,
  }) async {
    try {
      final uri = _buildUri(path, queryParameters: queryParameters);
      final headers = await _getHeadersWithAuth();
      final response = await _client.get(
        uri,
        headers: headers,
      );

      if (response.statusCode >= 200 && response.statusCode < 300) {
        final json = jsonDecode(response.body);
        return Result.ok(fromJson(json));
      } else {
        return Result.error(
          HttpException(
            'Request failed with status: ${response.statusCode}',
          ),
        );
      }
    } on Exception catch (error) {
      return Result.error(error);
    }
  }

  /// Generic GET request for lists
  Future<Result<List<T>>> getList<T>(
    String path, {
    Map<String, String>? queryParameters,
    required T Function(dynamic json) fromJson,
  }) async {
    try {
      final uri = _buildUri(path, queryParameters: queryParameters);
      final headers = await _getHeadersWithAuth();
      final response = await _client.get(
        uri,
        headers: headers,
      );

      if (response.statusCode >= 200 && response.statusCode < 300) {
        final json = jsonDecode(response.body) as List<dynamic>;
        return Result.ok(json.map((item) => fromJson(item)).toList());
      } else {
        return Result.error(
          HttpException(
            'Request failed with status: ${response.statusCode}',
          ),
        );
      }
    } on Exception catch (error) {
      return Result.error(error);
    }
  }

  /// Generic POST request
  Future<Result<T>> post<T>(
    String path, {
    dynamic body,
    Map<String, String>? queryParameters,
    required T Function(dynamic json) fromJson,
  }) async {
    try {
      final uri = _buildUri(path, queryParameters: queryParameters);
      final headers = await _getHeadersWithAuth();
      final response = await _client.post(
        uri,
        headers: headers,
        body: body != null ? jsonEncode(body) : null,
      );

      if (response.statusCode >= 200 && response.statusCode < 300) {
        if (response.body.isEmpty) {
          // For empty responses, return null or handle appropriately
          return Result.error(
            const HttpException('Empty response body'),
          );
        }
        final json = jsonDecode(response.body);
        return Result.ok(fromJson(json));
      } else {
        return Result.error(
          HttpException(
            'Request failed with status: ${response.statusCode}',
          ),
        );
      }
    } on Exception catch (error) {
      return Result.error(error);
    }
  }

  /// Generic PUT request
  Future<Result<T>> put<T>(
    String path, {
    dynamic body,
    Map<String, String>? queryParameters,
    required T Function(dynamic json) fromJson,
  }) async {
    try {
      final uri = _buildUri(path, queryParameters: queryParameters);
      final headers = await _getHeadersWithAuth();
      final response = await _client.put(
        uri,
        headers: headers,
        body: body != null ? jsonEncode(body) : null,
      );

      if (response.statusCode >= 200 && response.statusCode < 300) {
        final json = jsonDecode(response.body);
        return Result.ok(fromJson(json));
      } else {
        return Result.error(
          HttpException(
            'Request failed with status: ${response.statusCode}',
          ),
        );
      }
    } on Exception catch (error) {
      return Result.error(error);
    }
  }

  /// Generic DELETE request
  Future<Result<void>> delete(
    String path, {
    Map<String, String>? queryParameters,
  }) async {
    try {
      final uri = _buildUri(path, queryParameters: queryParameters);
      final headers = await _getHeadersWithAuth();
      final response = await _client.delete(
        uri,
        headers: headers,
      );

      // 204 No Content or 200 OK
      if (response.statusCode == 204 || response.statusCode == 200) {
        return const Result.ok(null);
      } else {
        return Result.error(
          HttpException(
            'Request failed with status: ${response.statusCode}',
          ),
        );
      }
    } on Exception catch (error) {
      return Result.error(error);
    }
  }

  /// PATCH request (if needed)
  Future<Result<T>> patch<T>(
    String path, {
    dynamic body,
    Map<String, String>? queryParameters,
    required T Function(dynamic json) fromJson,
  }) async {
    try {
      final uri = _buildUri(path, queryParameters: queryParameters);
      final headers = await _getHeadersWithAuth();
      final response = await _client.patch(
        uri,
        headers: headers,
        body: body != null ? jsonEncode(body) : null,
      );

      if (response.statusCode >= 200 && response.statusCode < 300) {
        final json = jsonDecode(response.body);
        return Result.ok(fromJson(json));
      } else {
        return Result.error(
          HttpException(
            'Request failed with status: ${response.statusCode}',
          ),
        );
      }
    } on Exception catch (error) {
      return Result.error(error);
    }
  }

  /// Health check endpoint
  Future<bool> checkHealth() async {
    try {
      final uri = _buildUri('/health');
      final response = await _client.get(uri);
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  /// Update auth token provider at runtime
  void updateAuthTokenProvider(AuthTokenProvider? provider) {
    _initializeClient(true);
  }

  /// Update unauthorized handler at runtime
  void updateUnauthorizedHandler(UnauthorizedHandler? handler) {
    _initializeClient(true);
  }
}


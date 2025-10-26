// Provider for API Client with automatic token injection and 401 handling

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../routing/app_router.dart';
import '../services/storage_service.dart';
import 'api_client.dart';

/// Creates and configures an API client instance for the app
class ApiClientProvider {
  ApiClientProvider._();

  static ApiClient? _instance;

  /// Get or create the API client instance
  static ApiClient getInstance(BuildContext context) {
    if (_instance != null) return _instance!;

    // Get the base URL from environment or use default
    const host = String.fromEnvironment('API_HOST', defaultValue: 'localhost');
    const port = int.fromEnvironment('API_PORT', defaultValue: 8081);
    const scheme = String.fromEnvironment('API_SCHEME', defaultValue: 'http');

    _instance = ApiClient(
      host: host,
      port: port,
      scheme: scheme,
      authTokenProvider: () async {
        // Get token from secure storage
        final token = await StorageService.instance.getAuthToken();
        return token;
      },
      unauthorizedHandler: () {
        // Handle 401 by clearing token and redirecting to login
        _handleUnauthorized(context);
      },
      enableLogging: true,
    );

    return _instance!;
  }

  /// Handle unauthorized access (401 error)
  static void _handleUnauthorized(BuildContext context) {
    // Clear the stored token
    StorageService.instance.removeAuthToken();

    // Navigate to login screen
    // Use WidgetsBinding to ensure we're not in the middle of a build
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (context.mounted) {
        context.go(AppRoutes.login);
        
        // Show a snackbar to inform the user
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Session expired. Please login again.'),
            duration: Duration(seconds: 3),
          ),
        );
      }
    });
  }

  /// Reset the instance (useful for testing or logout)
  static void reset() {
    _instance = null;
  }
}


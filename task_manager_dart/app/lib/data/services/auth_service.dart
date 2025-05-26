import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:task_manager_shared/models.dart';
import '../../core/constants/api_constants.dart';

/// Low-level API service for authentication HTTP calls
abstract class AuthApiService {
  Future<LoginResponseDto> login(LoginRequestDto request);

  Future<LoginResponseDto> register(RegisterRequestDto request);

  Future<LoginResponseDto> googleLogin(GoogleLoginRequestDto request);

  Future<void> logout(String token);
}

class AuthApiServiceImpl implements AuthApiService {
  final String _baseUrl;
  final http.Client _httpClient;

  AuthApiServiceImpl({
    required String baseUrl,
    http.Client? httpClient,
  })
      : _baseUrl = baseUrl,
        _httpClient = httpClient ?? http.Client();

  @override
  Future<LoginResponseDto> login(LoginRequestDto request) async {
    try {
      final response = await _httpClient.post(
        Uri.parse('$_baseUrl${ApiConstants.loginEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        return LoginResponseDto.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        final errorResponse = ErrorResponseDto.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
        throw Exception(errorResponse.message);
      }
    } catch (e) {
      throw Exception('Login API call failed: ${e.toString()}');
    }
  }

  @override
  Future<LoginResponseDto> register(RegisterRequestDto request) async {
    try {
      final response = await _httpClient.post(
        Uri.parse('$_baseUrl${ApiConstants.registerEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 201) {
        return LoginResponseDto.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        final errorResponse = ErrorResponseDto.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
        throw Exception(errorResponse.message);
      }
    } catch (e) {
      throw Exception('Registration API call failed: ${e.toString()}');
    }
  }

  @override
  Future<LoginResponseDto> googleLogin(GoogleLoginRequestDto request) async {
    try {
      final response = await _httpClient.post(
        Uri.parse('$_baseUrl${ApiConstants.googleLoginEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        return LoginResponseDto.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        final errorResponse = ErrorResponseDto.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
        throw Exception(errorResponse.message);
      }
    } catch (e) {
      throw Exception('Google login API call failed: ${e.toString()}');
    }
  }

  @override
  Future<void> logout(String token) async {
    try {
      // Optional server logout endpoint call
      await _httpClient.post(
        Uri.parse('$_baseUrl${ApiConstants.logoutEndpoint}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );
    } catch (e) {
      // Don't throw on logout API failure - local logout should still work
      debugPrint('Logout API call failed: ${e.toString()}');
    }
  }

  void dispose() {
    _httpClient.close();
  }
} 
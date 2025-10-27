import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:task_manager_shared/models.dart';

class SecureStorage {
  static const _storage = FlutterSecureStorage();

  // Keys for secure storage
  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'current_user';
  static const String _refreshTokenKey = 'refresh_token';

  // Token management
  Future<void> storeToken(String token) async {
    await _storage.write(key: _tokenKey, value: token);
  }

  Future<String?> getToken() async {
    return await _storage.read(key: _tokenKey);
  }

  Future<void> deleteToken() async {
    await _storage.delete(key: _tokenKey);
  }

  // Refresh token management
  Future<void> storeRefreshToken(String refreshToken) async {
    await _storage.write(key: _refreshTokenKey, value: refreshToken);
  }

  Future<String?> getRefreshToken() async {
    return await _storage.read(key: _refreshTokenKey);
  }

  Future<void> deleteRefreshToken() async {
    await _storage.delete(key: _refreshTokenKey);
  }

  // User data management
  Future<void> storeUser(UserPublicResponseDto user) async {
    final userJson = jsonEncode(user.toJson());
    await _storage.write(key: _userKey, value: userJson);
  }

  Future<UserPublicResponseDto?> getUser() async {
    final userJson = await _storage.read(key: _userKey);
    if (userJson != null) {
      final userMap = jsonDecode(userJson) as Map<String, dynamic>;
      return UserPublicResponseDto.fromJson(userMap);
    }
    return null;
  }

  Future<void> deleteUser() async {
    await _storage.delete(key: _userKey);
  }

  // Clear all stored data
  Future<void> clearAll() async {
    await _storage.deleteAll();
  }

  // Check if user is authenticated
  Future<bool> hasValidToken() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }
} 
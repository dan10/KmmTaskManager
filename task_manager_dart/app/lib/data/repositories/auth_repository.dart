import 'package:task_manager_shared/models.dart';
import '../services/auth_service.dart';
import '../sources/local/secure_storage.dart';

/// Repository that orchestrates authentication business logic
/// Uses API service for network calls and secure storage for local data
abstract class AuthRepository {
  Future<LoginResponseDto> login(String email, String password);

  Future<LoginResponseDto> register(String name, String email, String password);

  Future<LoginResponseDto> googleLogin(String idToken);

  Future<void> logout();

  Future<String?> getStoredToken();

  Future<UserPublicResponseDto?> getCurrentUser();

  Future<bool> isLoggedIn();
}

class AuthRepositoryImpl implements AuthRepository {
  final AuthApiService _apiService;
  final SecureStorage _secureStorage;

  AuthRepositoryImpl({
    required AuthApiService apiService,
    required SecureStorage secureStorage,
  })
      : _apiService = apiService,
        _secureStorage = secureStorage;

  @override
  Future<LoginResponseDto> login(String email, String password) async {
    try {
      // Create and validate request
      final request = LoginRequestDto(
        email: email,
        password: password,
      );

      // Validate request
      if (!request.isValid) {
        final validationErrors = request.validate();
        final errorMessage = validationErrors.values.first;
        throw Exception(errorMessage);
      }

      // Make API call
      final loginResponse = await _apiService.login(request);

      // Store credentials locally
      await _secureStorage.storeToken(loginResponse.token);
      await _secureStorage.storeUser(loginResponse.user);

      return loginResponse;
    } catch (e) {
      throw Exception('Login failed: ${e.toString()}');
    }
  }

  @override
  Future<LoginResponseDto> register(String name, String email,
      String password) async {
    try {
      // Create and validate request
      final request = RegisterRequestDto(
        displayName: name,
        email: email,
        password: password,
      );

      // Validate request
      if (!request.isValid) {
        final validationErrors = request.validate();
        final errorMessage = validationErrors.values.first;
        throw Exception(errorMessage);
      }

      // Make API call
      final loginResponse = await _apiService.register(request);

      // Store credentials locally
      await _secureStorage.storeToken(loginResponse.token);
      await _secureStorage.storeUser(loginResponse.user);

      return loginResponse;
    } catch (e) {
      throw Exception('Registration failed: ${e.toString()}');
    }
  }

  @override
  Future<LoginResponseDto> googleLogin(String idToken) async {
    try {
      // Create request
      final request = GoogleLoginRequestDto(idToken: idToken);

      // Make API call
      final loginResponse = await _apiService.googleLogin(request);

      // Store credentials locally
      await _secureStorage.storeToken(loginResponse.token);
      await _secureStorage.storeUser(loginResponse.user);

      return loginResponse;
    } catch (e) {
      throw Exception('Google login failed: ${e.toString()}');
    }
  }

  @override
  Future<void> logout() async {
    try {
      // Get current token for server logout
      final token = await _secureStorage.getToken();

      // Call server logout if we have a token
      if (token != null && token.isNotEmpty) {
        await _apiService.logout(token);
      }

      // Clear local storage regardless of server response
      await _secureStorage.clearAll();
    } catch (e) {
      // Always clear local storage even if server logout fails
      await _secureStorage.clearAll();
      throw Exception('Logout failed: ${e.toString()}');
    }
  }

  @override
  Future<String?> getStoredToken() async {
    return await _secureStorage.getToken();
  }

  @override
  Future<UserPublicResponseDto?> getCurrentUser() async {
    return await _secureStorage.getUser();
  }

  @override
  Future<bool> isLoggedIn() async {
    final token = await _secureStorage.getToken();
    return token != null && token.isNotEmpty;
  }
} 
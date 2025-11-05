import 'package:task_manager_shared/models.dart';
import 'package:task_manager_app/features/auth/data/repositories/auth_repository.dart';
import 'package:task_manager_app/core/utils/result.dart';

/// Manual mock implementation of AuthRepository for testing
class MockAuthRepository implements AuthRepository {
  // Mock data storage
  String? _storedToken;
  String? _storedRefreshToken;
  UserPublicResponseDto? _currentUser;
  bool _isLoggedIn = false;

  // Mock responses to be set by tests
  LoginResponseDto? mockLoginResponse;
  Exception? mockException;
  bool shouldThrowException = false;

  @override
  Future<Result<void>> login({required String email, required String password}) async {
    if (shouldThrowException && mockException != null) {
      return Result.error(mockException!);
    }

    if (mockLoginResponse != null) {
      _storedToken = mockLoginResponse!.token;
      _currentUser = mockLoginResponse!.user;
      _isLoggedIn = true;
      return Result.ok(null);
    }

    return Result.error(Exception('No mock response set'));
  }

  @override
  Future<Result<void>> register({required String name, required String email, required String password}) async {
    if (shouldThrowException && mockException != null) {
      return Result.error(mockException!);
    }

    if (mockLoginResponse != null) {
      _storedToken = mockLoginResponse!.token;
      _currentUser = mockLoginResponse!.user;
      _isLoggedIn = true;
      return Result.ok(null);
    }

    return Result.error(Exception('No mock response set'));
  }

  @override
  Future<LoginResponseDto> googleLogin(String idToken) async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }

    if (mockLoginResponse != null) {
      _storedToken = mockLoginResponse!.token;
      _currentUser = mockLoginResponse!.user;
      _isLoggedIn = true;
      return mockLoginResponse!;
    }

    throw Exception('No mock response set');
  }

  @override
  Future<void> logout() async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }

    _storedToken = null;
    _storedRefreshToken = null;
    _currentUser = null;
    _isLoggedIn = false;
  }

  @override
  Future<bool> isLoggedIn() async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    return _isLoggedIn;
  }

  @override
  Future<String?> getStoredToken() async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    return _storedToken;
  }

  Future<String?> getStoredRefreshToken() async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    return _storedRefreshToken;
  }

  @override
  Future<UserPublicResponseDto?> getCurrentUser() async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    return _currentUser;
  }

  Future<void> storeTokens(String token, String refreshToken) async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    _storedToken = token;
    _storedRefreshToken = refreshToken;
  }

  Future<void> storeUser(UserPublicResponseDto user) async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    _currentUser = user;
  }

  Future<void> clearStorage() async {
    if (shouldThrowException && mockException != null) {
      throw mockException!;
    }
    _storedToken = null;
    _storedRefreshToken = null;
    _currentUser = null;
    _isLoggedIn = false;
  }

  // Helper methods for testing
  void reset() {
    _storedToken = null;
    _storedRefreshToken = null;
    _currentUser = null;
    _isLoggedIn = false;
    mockLoginResponse = null;
    mockException = null;
    shouldThrowException = false;
  }

  void setMockResponse(LoginResponseDto response) {
    mockLoginResponse = response;
    shouldThrowException = false;
  }

  void setMockException(Exception exception) {
    mockException = exception;
    shouldThrowException = true;
  }

  void setLoggedIn(bool loggedIn) {
    _isLoggedIn = loggedIn;
  }

  void setCurrentUser(UserPublicResponseDto? user) {
    _currentUser = user;
  }

  void setStoredToken(String? token) {
    _storedToken = token;
  }
} 
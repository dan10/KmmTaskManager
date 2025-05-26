import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';
import '../../data/repositories/auth_repository.dart';

enum AuthState {
  initial,
  loading,
  authenticated,
  unauthenticated,
  error,
}

/// Main authentication ViewModel for session management
/// Handles overall auth state, logout, and session initialization
class AuthViewModel extends ChangeNotifier {
  AuthRepository? _authRepository;

  AuthViewModel(this._authRepository);

  // Update repository for Provider dependency injection
  void updateRepository(AuthRepository authRepository) {
    _authRepository = authRepository;
  }

  // State
  AuthState _state = AuthState.initial;
  UserPublicResponseDto? _currentUser;
  String? _errorMessage;
  bool _isLoading = false;

  // Getters
  AuthState get state => _state;

  UserPublicResponseDto? get currentUser => _currentUser;
  String? get errorMessage => _errorMessage;

  bool get isLoading => _isLoading;

  bool get isAuthenticated => _state == AuthState.authenticated;

  // Initialize auth state (check if user is already logged in)
  Future<void> initialize() async {
    if (_authRepository == null) return;

    try {
      _setLoading(true);

      final isLoggedIn = await _authRepository!.isLoggedIn();
      if (isLoggedIn) {
        _currentUser = await _authRepository!.getCurrentUser();
        _setState(AuthState.authenticated);
      } else {
        _setState(AuthState.unauthenticated);
      }
    } catch (e) {
      _setError('Failed to initialize authentication: ${e.toString()}');
    } finally {
      _setLoading(false);
    }
  }

  // Set authenticated state (called after successful login/register)
  void setAuthenticated(UserPublicResponseDto user) {
    _currentUser = user;
    _setState(AuthState.authenticated);
  }

  // Logout
  Future<void> logout() async {
    if (_authRepository == null) return;

    try {
      _setLoading(true);

      await _authRepository!.logout();
      _currentUser = null;
      _setState(AuthState.unauthenticated);
    } catch (e) {
      _setError('Logout failed: ${e.toString()}');
    } finally {
      _setLoading(false);
    }
  }

  // Get current token
  Future<String?> getToken() async {
    if (_authRepository == null) return null;

    try {
      return await _authRepository!.getStoredToken();
    } catch (e) {
      // Log error but don't throw - return null instead
      return null;
    }
  }

  // Refresh current user data
  Future<void> refreshUser() async {
    if (_authRepository == null) return;
    
    try {
      _currentUser = await _authRepository!.getCurrentUser();
      notifyListeners();
    } catch (e) {
      _setError('Failed to refresh user data: ${e.toString()}');
    }
  }

  // Clear error
  void clearError() {
    _clearError();
  }

  // Private helper methods
  void _setState(AuthState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _state = AuthState.error;
    notifyListeners();
  }

  void _clearError() {
    _errorMessage = null;
    notifyListeners();
  }
} 
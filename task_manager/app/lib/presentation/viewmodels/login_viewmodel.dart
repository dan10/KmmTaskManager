import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';
import '../../data/repositories/auth_repository.dart';

enum LoginState {
  initial,
  loading,
  success,
  error,
}

class LoginViewModel extends ChangeNotifier {
  AuthRepository? _authRepository;

  LoginViewModel(this._authRepository);

  // Update repository for Provider dependency injection
  void updateRepository(AuthRepository authRepository) {
    _authRepository = authRepository;
  }

  // State
  LoginState _state = LoginState.initial;
  UserPublicResponseDto? _user;
  String? _errorMessage;
  bool _isLoading = false;

  // Getters
  LoginState get state => _state;

  UserPublicResponseDto? get user => _user;

  String? get errorMessage => _errorMessage;

  bool get isLoading => _isLoading;

  bool get isSuccess => _state == LoginState.success;

  // Login
  Future<void> login(String email, String password) async {
    if (_authRepository == null) return;

    try {
      _setLoading(true);
      _clearError();

      final response = await _authRepository!.login(email, password);
      _user = response.user;
      _setState(LoginState.success);
    } catch (e) {
      _setError('Login failed: ${e.toString()}');
      _setState(LoginState.error);
    } finally {
      _setLoading(false);
    }
  }

  // Google Login
  Future<void> googleLogin(String idToken) async {
    if (_authRepository == null) return;

    try {
      _setLoading(true);
      _clearError();

      final response = await _authRepository!.googleLogin(idToken);
      _user = response.user;
      _setState(LoginState.success);
    } catch (e) {
      _setError('Google login failed: ${e.toString()}');
      _setState(LoginState.error);
    } finally {
      _setLoading(false);
    }
  }

  // Clear error
  void clearError() {
    _clearError();
  }

  // Reset state
  void reset() {
    _state = LoginState.initial;
    _user = null;
    _errorMessage = null;
    _isLoading = false;
    notifyListeners();
  }

  // Private helper methods
  void _setState(LoginState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _state = LoginState.error;
    notifyListeners();
  }

  void _clearError() {
    _errorMessage = null;
    notifyListeners();
  }
} 
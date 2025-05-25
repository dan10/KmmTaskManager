import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';
import '../../data/repositories/auth_repository.dart';

enum RegisterState {
  initial,
  loading,
  success,
  error,
}

class RegisterViewModel extends ChangeNotifier {
  AuthRepository? _authRepository;

  RegisterViewModel(this._authRepository);

  // Update repository for Provider dependency injection
  void updateRepository(AuthRepository authRepository) {
    _authRepository = authRepository;
  }

  // State
  RegisterState _state = RegisterState.initial;
  UserPublicResponseDto? _user;
  String? _errorMessage;
  bool _isLoading = false;

  // Getters
  RegisterState get state => _state;

  UserPublicResponseDto? get user => _user;

  String? get errorMessage => _errorMessage;

  bool get isLoading => _isLoading;

  bool get isSuccess => _state == RegisterState.success;

  // Register
  Future<void> register(String name, String email, String password) async {
    if (_authRepository == null) return;

    try {
      _setLoading(true);
      _clearError();

      final response = await _authRepository!.register(name, email, password);
      _user = response.user;
      _setState(RegisterState.success);
    } catch (e) {
      _setError('Registration failed: ${e.toString()}');
      _setState(RegisterState.error);
    } finally {
      _setLoading(false);
    }
  }

  // Google Register (same as Google Login)
  Future<void> googleRegister(String idToken) async {
    if (_authRepository == null) return;

    try {
      _setLoading(true);
      _clearError();

      final response = await _authRepository!.googleLogin(idToken);
      _user = response.user;
      _setState(RegisterState.success);
    } catch (e) {
      _setError('Google registration failed: ${e.toString()}');
      _setState(RegisterState.error);
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
    _state = RegisterState.initial;
    _user = null;
    _errorMessage = null;
    _isLoading = false;
    notifyListeners();
  }

  // Private helper methods
  void _setState(RegisterState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _state = RegisterState.error;
    notifyListeners();
  }

  void _clearError() {
    _errorMessage = null;
    notifyListeners();
  }
} 
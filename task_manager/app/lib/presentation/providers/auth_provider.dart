import 'package:flutter/foundation.dart';
import '../../domain/entities/user.dart';

enum AuthState {
  initial,
  loading,
  authenticated,
  unauthenticated,
  error,
}

class AuthProvider extends ChangeNotifier {
  AuthState _state = AuthState.initial;
  User? _user;
  String? _errorMessage;

  AuthState get state => _state;
  User? get user => _user;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => _state == AuthState.authenticated && _user != null;

  Future<void> login(String email, String password) async {
    _setState(AuthState.loading);
    
    try {
      // TODO: Implement actual login with repository
      await Future.delayed(const Duration(seconds: 1)); // Simulate API call
      
      if (email == 'test@example.com' && password == 'password') {
        _user = const User(
          id: '1',
          email: 'test@example.com',
          name: 'Test User',
        );
        _setState(AuthState.authenticated);
      } else {
        _setError('Invalid credentials');
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> register(String email, String password, String name) async {
    _setState(AuthState.loading);
    
    try {
      // TODO: Implement actual registration with repository
      await Future.delayed(const Duration(seconds: 1)); // Simulate API call
      
      _user = User(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        email: email,
        name: name,
      );
      _setState(AuthState.authenticated);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> logout() async {
    _setState(AuthState.loading);
    
    try {
      // TODO: Implement actual logout with repository
      await Future.delayed(const Duration(milliseconds: 500));
      
      _user = null;
      _setState(AuthState.unauthenticated);
    } catch (e) {
      _setError(e.toString());
    }
  }

  void _setState(AuthState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = AuthState.error;
    _errorMessage = error;
    notifyListeners();
  }

  void clearError() {
    if (_state == AuthState.error) {
      _setState(_user != null ? AuthState.authenticated : AuthState.unauthenticated);
    }
  }
} 
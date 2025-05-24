import 'package:flutter/foundation.dart';
import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';

enum AuthViewState {
  initial,
  loading,
  authenticated,
  unauthenticated,
  error,
}

class AuthViewModel extends ChangeNotifier {
  // TODO: Inject repository when data layer is implemented
  // final AuthRepository _authRepository;
  
  AuthViewState _state = AuthViewState.initial;
  User? _user;
  String? _errorMessage;

  AuthViewState get state => _state;
  User? get user => _user;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => _state == AuthViewState.authenticated && _user != null;
  bool get isLoading => _state == AuthViewState.loading;

  // TODO: Constructor will accept repository when implemented
  // AuthViewModel(this._authRepository);

  Future<void> login(String email, String password) async {
    if (_isFormValid(email, password)) {
      _setState(AuthViewState.loading);
      
      try {
        // TODO: Replace with actual repository call
        await _simulateLogin(email, password);
      } catch (e) {
        _setError(e.toString());
      }
    } else {
      _setError('Please fill in all fields');
    }
  }

  Future<void> register(String email, String password, String name) async {
    if (_isRegistrationFormValid(email, password, name)) {
      _setState(AuthViewState.loading);
      
      try {
        // TODO: Replace with actual repository call
        await _simulateRegister(email, password, name);
      } catch (e) {
        _setError(e.toString());
      }
    } else {
      _setError('Please fill in all fields');
    }
  }

  Future<void> logout() async {
    _setState(AuthViewState.loading);
    
    try {
      // TODO: Replace with actual repository call
      await Future.delayed(const Duration(milliseconds: 500));
      
      _user = null;
      _setState(AuthViewState.unauthenticated);
    } catch (e) {
      _setError(e.toString());
    }
  }

  // TODO: Remove simulation methods when repository is implemented
  Future<void> _simulateLogin(String email, String password) async {
    await Future.delayed(const Duration(seconds: 1));
    
    if (email == 'test@example.com' && password == 'password') {
      _user = const User(
        id: '1',
        email: 'test@example.com',
        name: 'Test User',
      );
      _setState(AuthViewState.authenticated);
    } else {
      throw Exception('Invalid credentials');
    }
  }

  Future<void> _simulateRegister(String email, String password, String name) async {
    await Future.delayed(const Duration(seconds: 1));
    
    _user = User(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      email: email,
      name: name,
    );
    _setState(AuthViewState.authenticated);
  }

  bool _isFormValid(String email, String password) {
    return email.isNotEmpty && password.isNotEmpty && _isValidEmail(email);
  }

  bool _isRegistrationFormValid(String email, String password, String name) {
    return email.isNotEmpty && 
           password.isNotEmpty && 
           name.isNotEmpty && 
           _isValidEmail(email) &&
           password.length >= 6;
  }

  bool _isValidEmail(String email) {
    return RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(email);
  }

  void _setState(AuthViewState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = AuthViewState.error;
    _errorMessage = error;
    notifyListeners();
  }

  void clearError() {
    if (_state == AuthViewState.error) {
      _setState(_user != null ? AuthViewState.authenticated : AuthViewState.unauthenticated);
    }
  }
} 
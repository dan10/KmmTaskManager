import 'package:flutter/material.dart';
import 'package:task_manager_shared/models.dart';
import '../viewmodels/auth_viewmodel.dart';

// Re-export the AuthState from the view model
export '../viewmodels/auth_viewmodel.dart' show AuthState;

class AuthProvider extends ChangeNotifier {
  final AuthViewModel _authViewModel;

  AuthProvider(this._authViewModel) {
    _authViewModel.addListener(_onAuthStateChanged);
    _initialize();
  }

  Future<void> _initialize() async {
    await _authViewModel.initialize();
    notifyListeners();
  }

  AuthState get state => _authViewModel.state;
  UserPublicResponseDto? get user => _authViewModel.currentUser;
  String? get errorMessage => _authViewModel.errorMessage;
  bool get isAuthenticated => _authViewModel.isAuthenticated;
  bool get isLoading => _authViewModel.isLoading;

  Future<void> logout() async {
    await _authViewModel.logout();
  }

  Future<String?> getToken() async {
    return await _authViewModel.getToken();
  }

  void clearError() {
    _authViewModel.clearError();
  }

  void setAuthenticated(UserPublicResponseDto user) {
    _authViewModel.setAuthenticated(user);
  }

  void _onAuthStateChanged() {
    notifyListeners();
  }

  @override
  void dispose() {
    _authViewModel.removeListener(_onAuthStateChanged);
    super.dispose();
  }
}
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';
import '../viewmodels/auth_viewmodel.dart';

// Re-export the AuthState from the view model
export '../viewmodels/auth_viewmodel.dart' show AuthState;

/// Legacy AuthProvider for backward compatibility
/// Wraps the AuthViewModel for session management only
/// Login/Register functionality is now handled by separate ViewModels
class AuthProvider extends ChangeNotifier {
  AuthViewModel? _authViewModel;

  // Initialize with context to get AuthViewModel from Provider
  void initialize(BuildContext context) {
    if (_authViewModel != null) return;

    _authViewModel = Provider.of<AuthViewModel>(context, listen: false);
    // Listen to changes in the auth view model
    _authViewModel!.addListener(_onAuthStateChanged);
    // Initialize the authentication state
    _authViewModel!.initialize();
  }

  // Delegate properties to the view model
  AuthState get state => _authViewModel?.state ?? AuthState.initial;

  UserPublicResponseDto? get user => _authViewModel?.currentUser;

  String? get errorMessage => _authViewModel?.errorMessage;

  bool get isAuthenticated => _authViewModel?.isAuthenticated ?? false;

  bool get isLoading => _authViewModel?.isLoading ?? false;

  // Session management methods only
  Future<void> logout() async {
    await _authViewModel?.logout();
  }

  Future<String?> getToken() async {
    return await _authViewModel?.getToken();
  }

  void clearError() {
    _authViewModel?.clearError();
  }

  // Set authenticated state (called by login/register ViewModels)
  void setAuthenticated(UserPublicResponseDto user) {
    _authViewModel?.setAuthenticated(user);
  }

  // Listen to auth view model changes and notify listeners
  void _onAuthStateChanged() {
    notifyListeners();
  }

  @override
  void dispose() {
    _authViewModel?.removeListener(_onAuthStateChanged);
    super.dispose();
  }
} 
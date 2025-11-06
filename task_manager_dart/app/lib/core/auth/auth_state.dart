import 'package:flutter/foundation.dart';

import '../data/local/secure_storage.dart';

/// Simple auth state notifier used to trigger router refreshes on auth changes.
class AuthState extends ChangeNotifier {
  AuthState(this._secureStorage);

  final SecureStorage _secureStorage;

  bool _isLoggedIn = false;
  bool get isLoggedIn => _isLoggedIn;

  /// Load current auth state from storage (optional).
  Future<void> load() async {
    final token = await _secureStorage.getToken();
    final newValue = token != null && token.isNotEmpty;
    if (_isLoggedIn != newValue) {
      _isLoggedIn = newValue;
      notifyListeners();
    }
  }

  /// Set token and notify listeners.
  Future<void> setToken(String token) async {
    await _secureStorage.storeToken(token);
    if (!_isLoggedIn) {
      _isLoggedIn = true;
      notifyListeners();
    }
  }

  /// Clear token and notify listeners.
  Future<void> clearToken() async {
    await _secureStorage.deleteToken();
    if (_isLoggedIn) {
      _isLoggedIn = false;
      notifyListeners();
    }
  }

  /// Call this on 401s to force logout + navigation via router redirect.
  Future<void> logout() async {
    await clearToken();
  }
}

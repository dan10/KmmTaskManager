import 'package:crypto/crypto.dart';
import 'dart:convert';
import 'package:shared/src/models/user.dart';
import '../repositories/auth_repository.dart';
import '../services/jwt_service.dart';

abstract class AuthService {
  Future<User> register(String name, String email, String password);
  Future<User> login(String email, String password);
  Future<User?> getCurrentUser(String id);
}

class AuthServiceImpl implements AuthService {
  final AuthRepository _authRepository;
  final JwtService _jwtService;

  AuthServiceImpl(this._authRepository, this._jwtService);

  @override
  Future<User> register(String name, String email, String password) async {
    // Check if user already exists
    final existingUser = await _authRepository.findByEmail(email);
    if (existingUser != null) {
      throw Exception('User already exists');
    }

    // Create new user
    final user = User(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      name: name,
      email: email,
      passwordHash: _hashPassword(password),
    );

    return _authRepository.create(user);
  }

  @override
  Future<User> login(String email, String password) async {
    // Find user
    final user = await _authRepository.findByEmail(email);
    if (user == null) {
      throw Exception('User not found');
    }

    // Verify password
    if (user.passwordHash != _hashPassword(password)) {
      throw Exception('Invalid password');
    }

    return user;
  }

  @override
  Future<User?> getCurrentUser(String id) async {
    return _authRepository.findById(id);
  }

  String _hashPassword(String password) {
    // TODO: Implement proper password hashing
    return password;
  }
}

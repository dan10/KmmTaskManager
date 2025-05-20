import 'package:shared/src/models/user.dart';
import '../data/in_memory_database.dart';

abstract class AuthRepository {
  Future<User> create(User user);
  Future<User?> findByEmail(String email);
  Future<User?> findById(String id);
  Future<void> delete(String id);
}

class AuthRepositoryImpl implements AuthRepository {
  final InMemoryDatabase _db;

  AuthRepositoryImpl(this._db);

  @override
  Future<User> create(User user) async {
    return _db.createUser(user);
  }

  @override
  Future<User?> findByEmail(String email) async {
    return _db.findUserByEmail(email);
  }

  @override
  Future<User?> findById(String id) async {
    return _db.findUserById(id);
  }

  @override
  Future<void> delete(String id) async {
    await _db.deleteUser(id);
  }
}

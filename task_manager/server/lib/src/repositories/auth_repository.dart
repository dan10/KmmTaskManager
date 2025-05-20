import 'package:postgres/postgres.dart';
import 'package:shared/src/models/user.dart' as shared;

class AuthRepository {
  final PostgreSQLConnection _db;

  AuthRepository(this._db);

  Future<shared.User> createUser(shared.User user) async {
    final result = await _db.query(
      '''
      INSERT INTO users (id, name, email, password_hash)
      VALUES (@id, @name, @email, @passwordHash)
      RETURNING *
      ''',
      substitutionValues: {
        'id': user.id,
        'name': user.name,
        'email': user.email,
        'passwordHash': user.passwordHash,
      },
    );

    return _mapUserFromRow(result.first);
  }

  Future<shared.User?> findUserById(String id) async {
    final result = await _db.query(
      'SELECT * FROM users WHERE id = @id',
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapUserFromRow(result.first);
  }

  Future<shared.User?> findUserByEmail(String email) async {
    final result = await _db.query(
      'SELECT * FROM users WHERE email = @email',
      substitutionValues: {'email': email},
    );

    if (result.isEmpty) return null;
    return _mapUserFromRow(result.first);
  }

  Future<void> updateUser(shared.User user) async {
    await _db.execute(
      '''
      UPDATE users
      SET name = @name,
          email = @email,
          password_hash = @passwordHash
      WHERE id = @id
      ''',
      substitutionValues: {
        'id': user.id,
        'name': user.name,
        'email': user.email,
        'passwordHash': user.passwordHash,
      },
    );
  }

  Future<void> deleteUser(String id) async {
    await _db.execute(
      'DELETE FROM users WHERE id = @id',
      substitutionValues: {'id': id},
    );
  }

  shared.User _mapUserFromRow(List<dynamic> row) {
    return shared.User(
      id: row[0] as String,
      name: row[1] as String,
      email: row[2] as String,
      passwordHash: row[3] as String,
    );
  }
}

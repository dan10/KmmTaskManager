import 'package:postgres/postgres.dart';
import 'package:shared/models.dart' as shared;

class AuthRepository {
  final PostgreSQLConnection _db;

  AuthRepository(this._db);

  Future<shared.User> createUser(shared.User user) async {
    final result = await _db.query(
      '''
      INSERT INTO users (id, display_name, email, password_hash, google_id, created_at)
      VALUES (@id, @displayName, @email, @passwordHash, @googleId, @createdAt)
      RETURNING *
      ''',
      substitutionValues: {
        'id': user.id,
        'displayName': user.displayName,
        'email': user.email,
        'passwordHash': user.passwordHash,
        'googleId': user.googleId,
        'createdAt': user.createdAt,
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
      SET display_name = @displayName,
          email = @email,
          password_hash = @passwordHash,
          google_id = @googleId
      WHERE id = @id
      ''',
      substitutionValues: {
        'id': user.id,
        'displayName': user.displayName,
        'email': user.email,
        'passwordHash': user.passwordHash,
        'googleId': user.googleId,
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
      displayName: row[1] as String,
      email: row[2] as String,
      passwordHash: row[3] as String?,
      googleId: row[4] as String?,
      createdAt: row[5] as String,
    );
  }
}

import 'package:postgres/postgres.dart';
import 'package:task_manager_shared/models.dart' as shared;

class AuthRepository {
  final Connection _db;

  AuthRepository(this._db);

  Future<shared.User> createUser(shared.User user) async {
    final result = await _db.execute(
      Sql.named('''
      INSERT INTO users (id, display_name, email, password_hash, google_id, created_at)
      VALUES (@id, @displayName, @email, @passwordHash, @googleId, @createdAt)
      RETURNING *
      '''),
      parameters: {
        'id': user.id,
        'displayName': user.displayName,
        'email': user.email,
        'passwordHash': user.passwordHash,
        'googleId': user.googleId,
        'createdAt': user.createdAt,
      },
    );

    return _mapUserFromRow(result.first.toColumnMap());
  }

  Future<shared.User?> findUserById(String id) async {
    final result = await _db.execute(
      Sql.named('SELECT * FROM users WHERE id = @id'),
      parameters: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapUserFromRow(result.first.toColumnMap());
  }

  Future<shared.User?> findUserByEmail(String email) async {
    final result = await _db.execute(
      Sql.named('SELECT * FROM users WHERE email = @email'),
      parameters: {'email': email},
    );

    if (result.isEmpty) return null;
    return _mapUserFromRow(result.first.toColumnMap());
  }

  Future<void> updateUser(shared.User user) async {
    await _db.execute(
      Sql.named('''
      UPDATE users
      SET display_name = @displayName,
          email = @email,
          password_hash = @passwordHash,
          google_id = @googleId,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = @id
      '''),
      parameters: {
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
      Sql.named('DELETE FROM users WHERE id = @id'),
      parameters: {'id': id},
    );
  }

  shared.User _mapUserFromRow(Map<String, dynamic> row) {
    // Handle created_at which is now TIMESTAMP (can be DateTime or String)
    final createdAtValue = row['created_at'];
    final createdAtString = createdAtValue is DateTime
        ? createdAtValue.toIso8601String()
        : createdAtValue as String;

    return shared.User(
      id: row['id'] as String,
      displayName: row['display_name'] as String,
      email: row['email'] as String,
      passwordHash: row['password_hash'] as String?,
      googleId: row['google_id'] as String?,
      createdAt: createdAtString,
    );
  }
}

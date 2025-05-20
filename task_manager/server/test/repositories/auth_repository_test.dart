import 'package:test/test.dart';
import 'package:shared/src/models/user.dart';
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/auth_repository.dart';

void main() {
  late InMemoryDatabase db;
  late AuthRepository repository;

  setUp(() {
    db = InMemoryDatabase();
    repository = AuthRepositoryImpl(db);
  });

  group('AuthRepository', () {
    test('should create and find user by email', () async {
      final user = User(
        id: '1',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );

      await repository.create(user);
      final foundUser = await repository.findByEmail('test@example.com');

      expect(foundUser, isNotNull);
      expect(foundUser?.id, equals(user.id));
      expect(foundUser?.name, equals(user.name));
      expect(foundUser?.email, equals(user.email));
    });

    test('should return null when user not found', () async {
      final user = await repository.findByEmail('nonexistent@example.com');
      expect(user, isNull);
    });
  });
}

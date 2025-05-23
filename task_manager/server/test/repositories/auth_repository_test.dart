import 'package:test/test.dart';
import 'package:shared/models.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../helpers/test_base.dart';

void main() {
  late TestBase testBase;
  late AuthRepository repository;

  setUp(() async {
    testBase = TestBase();
    await testBase.setUp();
    repository = AuthRepository(testBase.connection);
  });

  tearDown(() async {
    await testBase.tearDown();
  });

  group('AuthRepository Integration Tests', () {
    test('should create a user', () async {
      final user = User(
        id: '1',
        displayName: 'Test User',
        email: 'test@example.com',
        googleId: 'google-123',
        createdAt: DateTime.now().toIso8601String(),
      );

      final createdUser = await repository.createUser(user);
      expect(createdUser.id, equals(user.id));
      expect(createdUser.displayName, equals(user.displayName));
      expect(createdUser.email, equals(user.email));
      expect(createdUser.googleId, equals(user.googleId));
      expect(createdUser.createdAt, equals(user.createdAt));
    });

    test('should find a user by id', () async {
      final user = User(
        id: '1',
        displayName: 'Test User',
        email: 'test@example.com',
        googleId: 'google-123',
        createdAt: DateTime.now().toIso8601String(),
      );

      await repository.createUser(user);
      final foundUser = await repository.findUserById(user.id);
      expect(foundUser, isNotNull);
      expect(foundUser!.id, equals(user.id));
      expect(foundUser.displayName, equals(user.displayName));
      expect(foundUser.email, equals(user.email));
      expect(foundUser.googleId, equals(user.googleId));
    });

    test('should find a user by email', () async {
      final user = User(
        id: '1',
        displayName: 'Test User',
        email: 'test@example.com',
        googleId: 'google-123',
        createdAt: DateTime.now().toIso8601String(),
      );

      await repository.createUser(user);
      final foundUser = await repository.findUserByEmail(user.email);
      expect(foundUser, isNotNull);
      expect(foundUser!.id, equals(user.id));
      expect(foundUser.displayName, equals(user.displayName));
      expect(foundUser.email, equals(user.email));
      expect(foundUser.googleId, equals(user.googleId));
    });

    test('should return null when user not found by id', () async {
      final foundUser = await repository.findUserById('non-existent');
      expect(foundUser, isNull);
    });

    test('should return null when user not found by email', () async {
      final foundUser = await repository.findUserByEmail('non@existent.com');
      expect(foundUser, isNull);
    });

    test('should handle users with password hash (for regular login)', () async {
      final user = User(
        id: '2',
        displayName: 'Password User',
        email: 'password@example.com',
        passwordHash: 'hashed_password_123',
        createdAt: DateTime.now().toIso8601String(),
      );

      final createdUser = await repository.createUser(user);
      expect(createdUser.passwordHash, equals(user.passwordHash));
      expect(createdUser.googleId, isNull);

      final foundUser = await repository.findUserById(user.id);
      expect(foundUser!.passwordHash, equals(user.passwordHash));
    });
  });
} 
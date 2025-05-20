import 'package:test/test.dart';
import 'package:shared/src/models/user.dart' as shared;
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
    await testBase.clearTables();
    await testBase.tearDown();
  });

  group('AuthRepository', () {
    test('should create a user', () async {
      final user = shared.User(
        id: '1',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );

      final createdUser = await repository.createUser(user);
      expect(createdUser.id, equals(user.id));
      expect(createdUser.name, equals(user.name));
      expect(createdUser.email, equals(user.email));
      expect(createdUser.passwordHash, equals(user.passwordHash));
    });

    test('should find a user by id', () async {
      final user = shared.User(
        id: '1',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );

      await repository.createUser(user);
      final foundUser = await repository.findUserById(user.id);
      expect(foundUser, isNotNull);
      expect(foundUser!.id, equals(user.id));
      expect(foundUser.name, equals(user.name));
      expect(foundUser.email, equals(user.email));
      expect(foundUser.passwordHash, equals(user.passwordHash));
    });

    test('should find a user by email', () async {
      final user = shared.User(
        id: '1',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );

      await repository.createUser(user);
      final foundUser = await repository.findUserByEmail(user.email);
      expect(foundUser, isNotNull);
      expect(foundUser!.id, equals(user.id));
      expect(foundUser.name, equals(user.name));
      expect(foundUser.email, equals(user.email));
      expect(foundUser.passwordHash, equals(user.passwordHash));
    });

    test('should update a user', () async {
      final user = shared.User(
        id: '1',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );

      await repository.createUser(user);

      final updatedUser = shared.User(
        id: user.id,
        name: 'Updated User',
        email: 'updated@example.com',
        passwordHash: 'new_hashed_password',
      );

      await repository.updateUser(updatedUser);
      final foundUser = await repository.findUserById(user.id);
      expect(foundUser, isNotNull);
      expect(foundUser!.name, equals(updatedUser.name));
      expect(foundUser.email, equals(updatedUser.email));
      expect(foundUser.passwordHash, equals(updatedUser.passwordHash));
    });

    test('should delete a user', () async {
      final user = shared.User(
        id: '1',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );

      await repository.createUser(user);
      await repository.deleteUser(user.id);
      final foundUser = await repository.findUserById(user.id);
      expect(foundUser, isNull);
    });
  });
}

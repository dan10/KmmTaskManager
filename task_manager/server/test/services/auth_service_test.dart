import 'package:test/test.dart';
import 'package:shared/src/models/user.dart';
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/services/auth_service.dart';
import '../../lib/src/services/jwt_service.dart';

void main() {
  late InMemoryDatabase db;
  late AuthRepository repository;
  late AuthService service;
  late JwtService jwtService;

  setUp(() {
    db = InMemoryDatabase();
    repository = AuthRepositoryImpl(db);
    jwtService = JwtService();
    service = AuthServiceImpl(repository, jwtService);
  });

  group('AuthService', () {
    test('should register a new user', () async {
      final user = await service.register(
        'Test User',
        'test@example.com',
        'password123',
      );

      expect(user.name, equals('Test User'));
      expect(user.email, equals('test@example.com'));
    });

    test('should not register user with existing email', () async {
      await service.register(
        'Test User',
        'test@example.com',
        'password123',
      );

      expect(
        () => service.register(
          'Another User',
          'test@example.com',
          'password123',
        ),
        throwsException,
      );
    });

    test('should login with valid credentials', () async {
      await service.register(
        'Test User',
        'test@example.com',
        'password123',
      );

      final user = await service.login(
        'test@example.com',
        'password123',
      );

      expect(user.name, equals('Test User'));
      expect(user.email, equals('test@example.com'));
    });

    test('should not login with invalid password', () async {
      await service.register(
        'Test User',
        'test@example.com',
        'password123',
      );

      expect(
        () => service.login(
          'test@example.com',
          'wrongpassword',
        ),
        throwsException,
      );
    });
  });
}

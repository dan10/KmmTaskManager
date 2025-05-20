import 'package:test/test.dart';
import 'package:shared/src/models/user.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/services/auth_service.dart';
import '../../lib/src/services/jwt_service.dart';
import '../helpers/test_base.dart';

void main() {
  late AuthServiceImpl authService;
  late AuthRepository authRepository;
  late JwtService jwtService;
  late TestBase testBase;

  setUp(() async {
    testBase = TestBase();
    await testBase.setUp();
    authRepository = AuthRepository(testBase.connection);
    jwtService = JwtService();
    authService = AuthServiceImpl(authRepository, jwtService);
  });

  tearDown(() async {
    await testBase.clearTables();
    await testBase.tearDown();
  });

  group('AuthService', () {
    final testUser = User(
      id: '1',
      name: 'Test User',
      email: 'test@example.com',
      passwordHash: 'hashed_password',
    );

    group('register', () {
      test('should register a new user successfully', () async {
        final result = await authService.register(
          'Test User',
          'test@example.com',
          'password123',
        );

        expect(result.name, equals('Test User'));
        expect(result.email, equals('test@example.com'));
      });

      test('should throw exception when user already exists', () async {
        // First register
        await authService.register(
          'Test User',
          'test@example.com',
          'password123',
        );

        // Try to register again with same email
        expect(
          () => authService.register(
            'Test User',
            'test@example.com',
            'password123',
          ),
          throwsException,
        );
      });
    });

    group('login', () {
      test('should login successfully with correct credentials', () async {
        // First register
        await authService.register(
          'Test User',
          'test@example.com',
          'password123',
        );

        final result = await authService.login(
          'test@example.com',
          'password123',
        );

        expect(result.name, equals('Test User'));
        expect(result.email, equals('test@example.com'));
      });

      test('should throw exception when user not found', () async {
        expect(
          () => authService.login(
            'nonexistent@example.com',
            'password123',
          ),
          throwsException,
        );
      });

      test('should throw exception with incorrect password', () async {
        // First register
        await authService.register(
          'Test User',
          'test@example.com',
          'password123',
        );

        expect(
          () => authService.login(
            'test@example.com',
            'wrong_password',
          ),
          throwsException,
        );
      });
    });

    group('getCurrentUser', () {
      test('should return user when found', () async {
        // First register
        final registeredUser = await authService.register(
          'Test User',
          'test@example.com',
          'password123',
        );

        final result = await authService.getCurrentUser(registeredUser.id);

        expect(result?.name, equals('Test User'));
        expect(result?.email, equals('test@example.com'));
      });

      test('should return null when user not found', () async {
        final result = await authService.getCurrentUser('nonexistent_id');

        expect(result, isNull);
      });
    });
  });
}

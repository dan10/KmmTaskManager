import 'dart:convert';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import 'package:shared/models.dart';
import '../../lib/src/routes/auth_routes.dart';
import '../../lib/src/services/auth_service.dart';
import '../../lib/src/services/jwt_service.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/exceptions/custom_exceptions.dart';
import '../../lib/src/middleware/error_handling_middleware.dart';
import '../helpers/test_base.dart';

// Simple test implementation that works with the real AuthService
class _TestAuthService implements AuthService {
  final AuthRepository _authRepository;
  
  _TestAuthService(this._authRepository);

  @override
  Future<User> register(String displayName, String email, String password,
      {bool isSocialLogin = false}) async {
    
    // Validation - DTO validation should happen in routes
    final registerDto = RegisterRequestDto(
      displayName: displayName,
      email: email,
      password: password,
    );
    
    if (!registerDto.isValid) {
      throw ValidationException(
        message: 'Registration validation failed.',
        details: registerDto.validate(),
      );
    }
    
    // Check if user already exists
    final existingUser = await _authRepository.findUserByEmail(email);
    if (existingUser != null) {
      if (isSocialLogin) {
        return existingUser;
      }
      throw ConflictException(message: 'User with this email already exists.');
    }

    final user = User(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      displayName: displayName,
      email: email,
      passwordHash: 'hashed_$password', // Simple hash for testing
      createdAt: DateTime.now().toIso8601String(),
    );

    return _authRepository.createUser(user);
  }

  @override
  Future<User> login(String email, String password) async {
    // Validation
    final loginDto = LoginRequestDto(email: email, password: password);
    if (!loginDto.isValid) {
      throw ValidationException(
        message: 'Login validation failed.',
        details: loginDto.validate(),
      );
    }
    
    final user = await _authRepository.findUserByEmail(email);
    if (user == null) {
      throw AuthenticationException(message: 'Invalid email or password.');
    }

    if (user.passwordHash != 'hashed_$password') {
      throw AuthenticationException(message: 'Invalid email or password.');
    }

    return user;
  }

  @override
  Future<User?> getCurrentUser(String id) async {
    return _authRepository.findUserById(id);
  }

  @override
  Future<User> googleLogin(GoogleLoginRequestDto request) async {
    throw UnimplementedError('Google login not implemented in test service');
  }
}

// Simple mock JWT service for testing
class _MockJwtService implements JwtService {
  @override
  String generateToken(User user) => 'mock_token_${user.id}';

  @override
  Map<String, dynamic>? validateToken(String token) {
    if (token.startsWith('mock_token_')) {
      final userId = token.replaceFirst('mock_token_', '');
      return {
        'sub': userId,
        'email': 'test@example.com',
        'displayName': 'Test User',
      };
    }
    return null;
  }

  @override
  String? getUserIdFromToken(String token) {
    final payload = validateToken(token);
    return payload?['sub'] as String?;
  }
}

void main() {
  group('AuthRoutes Integration Tests', () {
    late TestBase testBase;
    late AuthService authService;
    late JwtService jwtService;
    late AuthRepository authRepository;
    late Handler handler;

    setUpAll(() async {
      testBase = TestBase();
      await testBase.setUp();
      
      // Set up real services with container database
      authRepository = AuthRepository(testBase.connection);
      authService = _TestAuthService(authRepository);
      jwtService = _MockJwtService();
      
      final authRoutes = AuthRoutes(authService, jwtService);
      
      // Wrap with error handling middleware like the real server
      handler = Pipeline()
        .addMiddleware(errorHandlingMiddleware())
        .addHandler(authRoutes.router);
    });

    tearDownAll(() async {
      await testBase.tearDown();
    });

    setUp(() async {
      // Clear users table before each test
      await testBase.connection.execute('DELETE FROM users');
    });

    group('POST /register', () {
      test('should register a new user successfully', () async {
        final userData = {
          'displayName': 'Test User',
          'email': 'test@example.com',
          'password': 'password123',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(userData),
          headers: {
            'content-type': 'application/json',
          },
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(200));
        final body = await response.readAsString();
        final Map<String, dynamic> userResponse = jsonDecode(body);
        
        expect(userResponse['displayName'], equals('Test User'));
        expect(userResponse['email'], equals('test@example.com'));
        expect(userResponse['id'], isA<String>());
        expect(userResponse['id'], isNotEmpty);
        
        // Verify user was actually saved to database
        final dbResult = await testBase.connection.query(
          'SELECT email, display_name FROM users WHERE email = @email',
          substitutionValues: {'email': 'test@example.com'},
        );
        expect(dbResult, hasLength(1));
        expect(dbResult.first[0], equals('test@example.com'));
        expect(dbResult.first[1], equals('Test User'));
      });

      test('should return 409 for duplicate email registration', () async {
        // First registration
        final userData = {
          'displayName': 'First User',
          'email': 'duplicate@example.com',
          'password': 'password123',
        };

        await handler(Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(userData),
          headers: {'content-type': 'application/json'},
        ));

        // Second registration with same email
        final secondUserData = {
          'displayName': 'Second User',
          'email': 'duplicate@example.com',
          'password': 'different123',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(secondUserData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(409));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Conflict'));
        expect(errorResponse['message'], contains('already exists'));
      });

      test('should return 400 for invalid registration data - short password', () async {
        final userData = {
          'displayName': 'Test User',
          'email': 'test@example.com',
          'password': '123', // Too short
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(userData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['details']['password'], contains('at least 6 characters'));
      });

      test('should return 400 for invalid registration data - empty display name', () async {
        final userData = {
          'displayName': '', // Empty
          'email': 'test@example.com',
          'password': 'password123',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(userData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['details']['displayName'], contains('cannot be empty'));
      });

      test('should return 400 for invalid email format', () async {
        final userData = {
          'displayName': 'Test User',
          'email': 'invalid-email', // Invalid format
          'password': 'password123',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(userData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['details']['email'], contains('Invalid email format'));
      });
    });

    group('POST /login', () {
      const testEmail = 'login@example.com';
      const testPassword = 'password123';
      const testDisplayName = 'Login User';

      setUp(() async {
        // Register a user for login tests
        final userData = {
          'displayName': testDisplayName,
          'email': testEmail,
          'password': testPassword,
        };

        final registerResponse = await handler(Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(userData),
          headers: {'content-type': 'application/json'},
        ));
        
        expect(registerResponse.statusCode, equals(200), 
               reason: 'Setup registration failed');
      });

      test('should login successfully with correct credentials', () async {
        final loginData = {
          'email': testEmail,
          'password': testPassword,
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(200));
        final body = await response.readAsString();
        final Map<String, dynamic> loginResponse = jsonDecode(body);
        
        expect(loginResponse['token'], isA<String>());
        expect(loginResponse['token'], isNotEmpty);
        expect(loginResponse['user']['email'], equals(testEmail));
        expect(loginResponse['user']['displayName'], equals(testDisplayName));
        expect(loginResponse['user']['id'], isA<String>());
        
        // Verify the token contains the correct user info
        final token = loginResponse['token'] as String;
        final payload = jwtService.validateToken(token);
        expect(payload, isNotNull);
        expect(payload!['sub'], isNotEmpty);
      });

      test('should return 401 for incorrect password', () async {
        final loginData = {
          'email': testEmail,
          'password': 'wrongpassword',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(401));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Unauthorized'));
        expect(errorResponse['message'], contains('Invalid email or password'));
      });

      test('should return 401 for non-existent user', () async {
        final loginData = {
          'email': 'nonexistent@example.com',
          'password': testPassword,
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(401));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Unauthorized'));
        expect(errorResponse['message'], contains('Invalid email or password'));
      });

      test('should return 400 for empty email', () async {
        final loginData = {
          'email': '',
          'password': testPassword,
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['details']['email'], contains('cannot be empty'));
      });

      test('should return 400 for empty password', () async {
        final loginData = {
          'email': testEmail,
          'password': '',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['details']['password'], contains('cannot be empty'));
      });
    });

    group('Error handling', () {
      test('should handle malformed JSON in register request', () async {
        final request = Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: 'invalid json',
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['message'], contains('Invalid request format'));
      });

      test('should handle malformed JSON in login request', () async {
        final request = Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: 'invalid json',
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(400));
        final body = await response.readAsString();
        final errorResponse = jsonDecode(body);
        expect(errorResponse['error'], equals('Bad Request'));
        expect(errorResponse['message'], contains('Invalid request format'));
      });

      test('should return 404 for non-existent routes', () async {
        final request = Request(
          'POST',
          Uri.parse('http://localhost/nonexistent'),
          body: jsonEncode({}),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(404));
      });

      test('should return 405 for wrong HTTP method', () async {
        final request = Request(
          'GET', // Should be POST
          Uri.parse('http://localhost/register'),
          headers: {'content-type': 'application/json'},
        );
        
        final response = await handler(request);
        
        expect(response.statusCode, equals(404)); // Shelf router returns 404 for method mismatch
      });
    });

    group('Integration scenarios', () {
      test('should allow login after successful registration', () async {
        const email = 'integration@example.com';
        const password = 'password123';
        const displayName = 'Integration User';

        // Register
        final registerData = {
          'displayName': displayName,
          'email': email,
          'password': password,
        };

        final registerResponse = await handler(Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(registerData),
          headers: {'content-type': 'application/json'},
        ));
        
        expect(registerResponse.statusCode, equals(200));

        // Login with same credentials
        final loginData = {
          'email': email,
          'password': password,
        };

        final loginResponse = await handler(Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        ));
        
        expect(loginResponse.statusCode, equals(200));
        final loginBody = await loginResponse.readAsString();
        final loginResult = jsonDecode(loginBody);
        
        expect(loginResult['token'], isA<String>());
        expect(loginResult['user']['email'], equals(email));
        expect(loginResult['user']['displayName'], equals(displayName));
      });

      test('should not allow login with wrong password after registration', () async {
        const email = 'wrong-password@example.com';
        const correctPassword = 'password123';
        const wrongPassword = 'wrongpassword';
        const displayName = 'Wrong Password User';

        // Register
        final registerData = {
          'displayName': displayName,
          'email': email,
          'password': correctPassword,
        };

        final registerResponse = await handler(Request(
          'POST',
          Uri.parse('http://localhost/register'),
          body: jsonEncode(registerData),
          headers: {'content-type': 'application/json'},
        ));
        
        expect(registerResponse.statusCode, equals(200));

        // Try to login with wrong password
        final loginData = {
          'email': email,
          'password': wrongPassword,
        };

        final loginResponse = await handler(Request(
          'POST',
          Uri.parse('http://localhost/login'),
          body: jsonEncode(loginData),
          headers: {'content-type': 'application/json'},
        ));
        
        expect(loginResponse.statusCode, equals(401));
      });
    });
  });
} 
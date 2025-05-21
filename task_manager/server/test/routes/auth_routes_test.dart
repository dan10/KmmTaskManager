import 'dart:convert';
import 'package:shelf_test_handler/shelf_test_handler.dart';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart' as shelf;
import 'package:mockito/mockito.dart';

import '../../../lib/src/config/app_config.dart';
import '../../../lib/src/data/database.dart'; // Assuming InMemoryDatabase is a form of Database
import '../../../lib/src/repositories/auth_repository.dart';
import '../../../lib/src/services/auth_service.dart';
import '../../../lib/src/services/jwt_service.dart';
import '../../../lib/src/routes/auth_routes.dart';
import '../../../lib/src/middleware/error_handling_middleware.dart';
import '../../../lib/src/dto/auth/register_request_dto.dart';
import '../../../lib/src/dto/auth/login_request_dto.dart';
import '../../../lib/src/dto/auth/google_login_request_dto.dart';
import '../../../lib/src/dto/error_response_dto.dart';
import '../../../lib/src/exceptions/custom_exceptions.dart';

import '../services/auth_service_test.mocks.dart'; // Use AppConfig mock from auth_service_test


void main() {
  // Using InMemoryDatabase means we don't need TestBase for DB setup here.
  // Instead, we'll use a real AuthRepository with InMemoryDatabase.
  late Database db; // This will be InMemoryDatabase
  late AuthRepository authRepository;
  late AuthService authService;
  late JwtService jwtService;
  late shelf.Pipeline pipeline;
  late MockAppConfig mockAppConfig;

  setUp(() {
    db = InMemoryDatabase(); // Use InMemoryDatabase
    authRepository = AuthRepository(db); // Pass the InMemoryDatabase
    
    mockAppConfig = MockAppConfig();
    when(mockAppConfig.jwtSecret).thenReturn('test-super-secret-key-for-jwt-longer-than-32-bytes');
    when(mockAppConfig.googleClientId).thenReturn('YOUR_TEST_GOOGLE_CLIENT_ID.apps.googleusercontent.com');

    jwtService = JwtService(mockAppConfig);
    authService = AuthServiceImpl(authRepository, jwtService, mockAppConfig);
    
    final authRoutes = AuthRoutes(authService, jwtService).router;
    pipeline = const shelf.Pipeline()
        .addMiddleware(errorHandlingMiddleware())
        .addHandler(authRoutes);
  });

  group('AuthRoutes', () {
    group('/register', () {
      test('should register a new user successfully', () async {
        final handler = ShelfTestHandler(pipeline);
        final requestDto = RegisterRequestDto(
            name: 'Test User', email: 'test@example.com', password: 'password123');
        
        final response = await handler.post(
          '/register',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );

        expect(response.statusCode, 200);
        final body = jsonDecode(await response.readAsString());
        expect(body['name'], 'Test User');
        expect(body['email'], 'test@example.com');
        expect(body['id'], isA<String>());

        // Verify login with new credentials (indirectly tests hashing)
        final loginDto = LoginRequestDto(email: 'test@example.com', password: 'password123');
        final loginResponse = await handler.post(
          '/login',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(loginDto.toJson()),
        );
        expect(loginResponse.statusCode, 200);
      });

      test('should return 409 Conflict for existing email', () async {
        final handler = ShelfTestHandler(pipeline);
        final requestDto = RegisterRequestDto(
            name: 'Test User', email: 'test@example.com', password: 'password123');
        await handler.post( // First registration
          '/register',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );

        final response = await handler.post( // Second registration with same email
          '/register',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.copyWith(name: 'Another User').toJson()),
        );
        
        expect(response.statusCode, 409);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.error, 'Conflict');
        expect(body.message, contains('User with this email already exists.'));
      });

      test('should return 400 for invalid registration data (short password)', () async {
        final handler = ShelfTestHandler(pipeline);
        final requestDto = RegisterRequestDto(
            name: 'Test User', email: 'test@example.com', password: '123'); // Short password
        
        final response = await handler.post(
          '/register',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );
        expect(response.statusCode, 400);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.error, 'Bad Request');
        expect(body.message, 'Registration validation failed.');
        expect(body.details!['password'], 'Password must be at least 6 characters long.');
      });
       test('should return 400 for invalid registration data (empty name)', () async {
        final handler = ShelfTestHandler(pipeline);
        final requestDto = RegisterRequestDto(
            name: '', email: 'test@example.com', password: 'password123');
        
        final response = await handler.post(
          '/register',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );
        expect(response.statusCode, 400);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.details!['name'], 'Name cannot be empty.');
      });
    });

    group('/login', () {
      setUp(() async {
        // Register a user first
        final handler = ShelfTestHandler(pipeline);
        final requestDto = RegisterRequestDto(
            name: 'Login User', email: 'login@example.com', password: 'password123');
        final regResponse = await handler.post(
          '/register',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );
        expect(regResponse.statusCode, 200, reason: "Setup registration failed");
      });

      test('should login successfully with correct credentials', () async {
        final handler = ShelfTestHandler(pipeline);
        final loginDto = LoginRequestDto(email: 'login@example.com', password: 'password123');
        
        final response = await handler.post(
          '/login',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(loginDto.toJson()),
        );
        expect(response.statusCode, 200);
        final body = jsonDecode(await response.readAsString());
        expect(body['token'], isA<String>());
        expect(body['user']['email'], 'login@example.com');
      });

      test('should return 401 for incorrect password', () async {
        final handler = ShelfTestHandler(pipeline);
        final loginDto = LoginRequestDto(email: 'login@example.com', password: 'wrongpassword');
        
        final response = await handler.post(
          '/login',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(loginDto.toJson()),
        );
        expect(response.statusCode, 401);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.error, 'Unauthorized');
        expect(body.message, 'Invalid email or password.');
      });

      test('should return 401 for non-existent user', () async {
        final handler = ShelfTestHandler(pipeline);
        final loginDto = LoginRequestDto(email: 'nosuchuser@example.com', password: 'password123');
        
        final response = await handler.post(
          '/login',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(loginDto.toJson()),
        );
        expect(response.statusCode, 401);
         final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.error, 'Unauthorized');
        expect(body.message, 'Invalid email or password.');
      });

      test('should return 400 for empty email in login', () async {
         final handler = ShelfTestHandler(pipeline);
        final loginDto = LoginRequestDto(email: '', password: 'password123');
        
        final response = await handler.post(
          '/login',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(loginDto.toJson()),
        );
        expect(response.statusCode, 400);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.details!['email'], 'Email cannot be empty.');
      });
    });
    
    group('/google', () {
      test('should return 400 if idToken is empty', () async {
        final handler = ShelfTestHandler(pipeline);
        final requestDto = GoogleLoginRequestDto(idToken: '');
        
        final response = await handler.post(
          '/google',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );
        expect(response.statusCode, 400);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.error, 'Bad Request');
        expect(body.message, 'Google login validation failed.');
        expect(body.details!['idToken'], 'ID token cannot be empty.');
      });

      test('should return 401 for invalid (placeholder) Google token', () async {
        // This test assumes the current AuthService.googleLogin will fail
        // because the tokeninfo endpoint will reject a dummy token.
        final handler = ShelfTestHandler(pipeline);
        final requestDto = GoogleLoginRequestDto(idToken: 'this-is-not-a-real-google-id-token');
        
        final response = await handler.post(
          '/google',
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestDto.toJson()),
        );
        expect(response.statusCode, 401);
        final body = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(body.error, 'Unauthorized');
        expect(body.message, contains('Failed to verify Google ID token'));
      });
      
      // More comprehensive tests for /google would require mocking the HTTP client
      // used by AuthService to call the tokeninfo endpoint, or using a test double for Google's API.
      // These are beyond typical unit tests for route handlers if http call is direct.
    });
  });
}

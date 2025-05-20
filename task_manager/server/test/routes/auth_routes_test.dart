import 'dart:convert';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf.dart' as shelf;
import 'package:shelf_router/shelf_router.dart';
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/services/auth_service.dart';
import '../../lib/src/services/jwt_service.dart';
import '../../lib/src/routes/auth_routes.dart';

void main() {
  late InMemoryDatabase db;
  late AuthRepository repository;
  late AuthService service;
  late JwtService jwtService;
  late Handler handler;

  setUp(() {
    db = InMemoryDatabase();
    repository = AuthRepositoryImpl(db);
    jwtService = JwtService();
    service = AuthServiceImpl(repository, jwtService);
    handler = AuthRoutes(service, jwtService).router;
  });

  group('AuthRoutes', () {
    test('registers a new user', () async {
      final request = shelf.Request(
        'POST',
        Uri.parse('http://localhost/register'),
        body: jsonEncode({
          'name': 'Test User',
          'email': 'test@example.com',
          'password': 'password123',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      final response = await handler(request);
      expect(response.statusCode, 200);
      final body = await response.readAsString();
      final data = jsonDecode(body);
      expect(data['name'], 'Test User');
      expect(data['email'], 'test@example.com');
    });

    test('does not register user with existing email', () async {
      // Register first
      final request1 = shelf.Request(
        'POST',
        Uri.parse('http://localhost/register'),
        body: jsonEncode({
          'name': 'Test User',
          'email': 'test@example.com',
          'password': 'password123',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      await handler(request1);
      // Try to register again
      final request2 = shelf.Request(
        'POST',
        Uri.parse('http://localhost/register'),
        body: jsonEncode({
          'name': 'Another User',
          'email': 'test@example.com',
          'password': 'password123',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      final response = await handler(request2);
      expect(response.statusCode, 400);
      final body = await response.readAsString();
      final data = jsonDecode(body);
      expect(data['error'], contains('User already exists'));
    });

    test('logs in with valid credentials', () async {
      // Register first
      final registerRequest = shelf.Request(
        'POST',
        Uri.parse('http://localhost/register'),
        body: jsonEncode({
          'name': 'Test User',
          'email': 'test@example.com',
          'password': 'password123',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      await handler(registerRequest);
      // Login
      final loginRequest = shelf.Request(
        'POST',
        Uri.parse('http://localhost/login'),
        body: jsonEncode({
          'email': 'test@example.com',
          'password': 'password123',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      final response = await handler(loginRequest);
      expect(response.statusCode, 200);
      final body = await response.readAsString();
      final data = jsonDecode(body);
      expect(data['token'], isNotNull);
      expect(data['user']['email'], 'test@example.com');
    });

    test('does not log in with invalid password', () async {
      // Register first
      final registerRequest = shelf.Request(
        'POST',
        Uri.parse('http://localhost/register'),
        body: jsonEncode({
          'name': 'Test User',
          'email': 'test@example.com',
          'password': 'password123',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      await handler(registerRequest);
      // Login with wrong password
      final loginRequest = shelf.Request(
        'POST',
        Uri.parse('http://localhost/login'),
        body: jsonEncode({
          'email': 'test@example.com',
          'password': 'wrongpassword',
        }),
        headers: {'Content-Type': 'application/json'},
      );
      final response = await handler(loginRequest);
      expect(response.statusCode, 401);
      final body = await response.readAsString();
      final data = jsonDecode(body);
      expect(data['error'], contains('Invalid password'));
    });
  });
}

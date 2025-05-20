import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import 'package:shared/src/models/user.dart';
import '../../lib/src/middleware/auth_middleware.dart';
import '../../lib/src/services/jwt_service.dart';

void main() {
  late JwtService jwtService;
  late AuthMiddleware authMiddleware;
  late Handler innerHandler;

  setUp(() {
    jwtService = JwtService();
    authMiddleware = AuthMiddleware(jwtService);
    innerHandler = (Request request) async {
      return Response.ok('Success', context: request.context);
    };
  });

  group('AuthMiddleware', () {
    test('allows access to login and register endpoints without token',
        () async {
      final handler = authMiddleware.middleware(innerHandler);

      final loginRequest =
          Request('GET', Uri.parse('http://localhost/auth/login'));
      final registerRequest =
          Request('GET', Uri.parse('http://localhost/auth/register'));

      final loginResponse = await handler(loginRequest);
      final registerResponse = await handler(registerRequest);

      expect(loginResponse.statusCode, equals(200));
      expect(registerResponse.statusCode, equals(200));
    });

    test('rejects request without authorization header', () async {
      final handler = authMiddleware.middleware(innerHandler);
      final request = Request('GET', Uri.parse('http://localhost/tasks'));

      final response = await handler(request);

      expect(response.statusCode, equals(401));
      expect(await response.readAsString(),
          equals('Missing or invalid authorization header'));
    });

    test('rejects request with invalid token format', () async {
      final handler = authMiddleware.middleware(innerHandler);
      final request = Request(
        'GET',
        Uri.parse('http://localhost/tasks'),
        headers: {'authorization': 'InvalidToken'},
      );

      final response = await handler(request);

      expect(response.statusCode, equals(401));
      expect(await response.readAsString(),
          equals('Missing or invalid authorization header'));
    });

    test('rejects request with invalid token', () async {
      final handler = authMiddleware.middleware(innerHandler);
      final request = Request(
        'GET',
        Uri.parse('http://localhost/tasks'),
        headers: {'authorization': 'Bearer invalid.token.here'},
      );

      final response = await handler(request);

      expect(response.statusCode, equals(401));
      expect(await response.readAsString(), equals('Invalid or expired token'));
    });

    test('adds user context to request with valid token', () async {
      final user = User(
        id: '123',
        name: 'Test User',
        email: 'test@example.com',
        passwordHash: 'hashed_password',
      );
      final token = jwtService.generateToken(user);

      final handler = authMiddleware.middleware(innerHandler);
      final request = Request(
        'GET',
        Uri.parse('http://localhost/tasks'),
        headers: {'authorization': 'Bearer $token'},
      );

      final response = await handler(request);

      expect(response.statusCode, equals(200));
      expect(response.context?['userId'], equals(user.id));
      expect(response.context?['userEmail'], equals(user.email));
    });
  });
}

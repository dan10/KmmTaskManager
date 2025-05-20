import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../services/auth_service.dart';
import '../services/jwt_service.dart';

class AuthRoutes {
  final AuthService authService;
  final JwtService jwtService;

  AuthRoutes(this.authService, this.jwtService);

  Router get router {
    final router = Router();

    // Register endpoint
    router.post('/register', (Request request) async {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      try {
        final user = await authService.register(
          data['name'] as String,
          data['email'] as String,
          data['password'] as String,
        );
        return Response.ok(
            jsonEncode({
              'id': user.id,
              'name': user.name,
              'email': user.email,
            }),
            headers: {'Content-Type': 'application/json'});
      } catch (e) {
        return Response(400,
            body: jsonEncode({'error': e.toString()}),
            headers: {'Content-Type': 'application/json'});
      }
    });

    // Login endpoint
    router.post('/login', (Request request) async {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      try {
        final user = await authService.login(
          data['email'] as String,
          data['password'] as String,
        );
        final token = jwtService.generateToken(user);
        return Response.ok(
            jsonEncode({
              'token': token,
              'user': {
                'id': user.id,
                'name': user.name,
                'email': user.email,
              }
            }),
            headers: {'Content-Type': 'application/json'});
      } catch (e) {
        return Response(401,
            body: jsonEncode({'error': e.toString()}),
            headers: {'Content-Type': 'application/json'});
      }
    });

    return router;
  }
}

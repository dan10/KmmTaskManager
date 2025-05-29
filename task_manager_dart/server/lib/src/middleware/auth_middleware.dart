import 'package:shelf/shelf.dart';
import '../services/jwt_service.dart';

class AuthMiddleware {
  final JwtService _jwtService;

  AuthMiddleware(this._jwtService);

  Middleware middleware() {
    return (Handler handler) {
      return (Request request) async {
        print(
            'Auth middleware called for ${request.method} ${request.requestedUri
                .path}');
        final authHeader = request.headers['Authorization'];
        if (authHeader == null || !authHeader.startsWith('Bearer ')) {
          print('Missing or invalid authorization header');
          return Response.unauthorized('Missing or invalid authorization header');
        }

        final token = authHeader.substring(7);
        try {
          final payload = _jwtService.validateToken(token);
          if (payload == null) {
            print('JWT validation returned null');
            return Response.unauthorized('Invalid token');
          }

          final userId = payload['sub'] as String?;
          if (userId == null) {
            print('Missing user ID in token payload');
            return Response.unauthorized('Invalid token: missing user ID');
          }

          print('Auth successful for user: $userId');
          // Add user info to the request context and pass to next handler
          final modifiedRequest = request.change(context: {
            ...request.context,
            'user': payload,
            'userId': userId,
          });

          return await handler(modifiedRequest);
        } catch (e) {
          print('Auth middleware exception: $e');
          return Response.unauthorized('Invalid token');
        }
      };
    };
  }
}

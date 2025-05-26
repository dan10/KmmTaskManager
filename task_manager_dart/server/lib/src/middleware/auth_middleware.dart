import 'package:shelf/shelf.dart';
import '../services/jwt_service.dart';

class AuthMiddleware {
  final JwtService _jwtService;

  AuthMiddleware(this._jwtService);

  Middleware middleware() {
    return (Handler handler) {
      return (Request request) async {
        final authHeader = request.headers['Authorization'];
        if (authHeader == null || !authHeader.startsWith('Bearer ')) {
          return Response.unauthorized('Missing or invalid authorization header');
        }

        final token = authHeader.substring(7);
        try {
          final payload = _jwtService.validateToken(token);
          if (payload == null) {
            return Response.unauthorized('Invalid token');
          }

          final userId = payload['sub'] as String?;
          if (userId == null) {
            return Response.unauthorized('Invalid token: missing user ID');
          }

          // Add user info to the request context and pass to next handler
          final modifiedRequest = request.change(context: {
            ...request.context,
            'user': payload,
            'userId': userId,
          });

          return await handler(modifiedRequest);
        } catch (e) {
          return Response.unauthorized('Invalid token');
        }
      };
    };
  }
}

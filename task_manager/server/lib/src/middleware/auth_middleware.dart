import 'package:shelf/shelf.dart';
import '../services/jwt_service.dart';

class AuthMiddleware {
  final JwtService _jwtService;

  AuthMiddleware(this._jwtService);

  Middleware get middleware => (Handler innerHandler) {
        return (Request request) async {
          // Skip auth for login and register endpoints
          if (request.url.path == 'auth/login' ||
              request.url.path == 'auth/register') {
            return innerHandler(request);
          }

          final authHeader = request.headers['authorization'];
          if (authHeader == null || !authHeader.startsWith('Bearer ')) {
            return Response.unauthorized(
                'Missing or invalid authorization header');
          }

          final token = authHeader.substring(7);
          final payload = _jwtService.validateToken(token);
          if (payload == null) {
            return Response.unauthorized('Invalid or expired token');
          }

          // Add user context to the request
          final modifiedRequest = request.change(context: {
            'userId': payload['sub'],
            'userEmail': payload['email'],
          });

          return innerHandler(modifiedRequest);
        };
      };
}

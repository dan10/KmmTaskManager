import 'package:shelf/shelf.dart';
import '../services/jwt_service.dart';

Middleware authMiddleware() {
  return (Handler innerHandler) {
    return (Request request) async {
      final authHeader = request.headers['authorization'];
      if (authHeader == null || !authHeader.startsWith('Bearer ')) {
        return Response.unauthorized('Missing or invalid authorization header');
      }

      final token = authHeader.substring(7);
      final jwtService = JwtService();
      final payload = jwtService.validateToken(token);

      if (payload == null) {
        return Response.unauthorized('Invalid or expired token');
      }

      // Add user info to request context
      final updatedRequest = request.change(context: {
        'userId': payload['sub'],
        'userName': payload['name'],
        'userEmail': payload['email'],
      });

      return innerHandler(updatedRequest);
    };
  };
}

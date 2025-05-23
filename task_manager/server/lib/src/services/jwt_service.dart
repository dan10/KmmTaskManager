import 'package:dart_jsonwebtoken/dart_jsonwebtoken.dart';
import 'package:shared/src/models/user.dart';
import '../config/app_config.dart';



class JwtService {
  final AppConfig _appConfig;
  // _secret is not directly used if _appConfig.jwtSecret is used in methods.
  // Keeping it if it's part of a desired pattern, otherwise can remove.
  // late final String _secret; 

  JwtService(this._appConfig) {
    // _secret = _appConfig.jwtSecret; // Initialize _secret from AppConfig
    if (_appConfig.jwtSecret.isEmpty) {
      // This check is crucial. A server should not run with an empty secret.
      throw StateError('JWT_SECRET is not configured. Cannot initialize JwtService.');
    }
  }

  String generateToken(User user) {
    final payload = {
      'sub': user.id,
      'name': user.displayName,
      'email': user.email,
      'iat': DateTime.now().millisecondsSinceEpoch ~/ 1000,
      // 'exp' is automatically set by dart_jsonwebtoken if expiresIn is provided
    };

    final jwt = JWT(payload, issuer: 'task_manager_server');
    final token = jwt.sign(
      SecretKey(_appConfig.jwtSecret),
      expiresIn: const Duration(days: 7), // Standard 'exp' claim
    );
    return token;
  }

  Map<String, dynamic>? validateToken(String token) {
    try {
      final jwt = JWT.verify(token, SecretKey(_appConfig.jwtSecret));
      return jwt.payload as Map<String, dynamic>;
    } on JWTExpiredException {
      print('JWT Expired');
      // Consider throwing an AuthenticationException if middleware should handle it
      // throw AuthenticationException(message: 'Token expired.');
      return null; 
    } on JWTException catch (err) { // Catches other JWT errors like invalid signature
      print('JWT Error: ${err.message}');
      // Consider throwing an AuthenticationException
      // throw AuthenticationException(message: 'Invalid token: ${err.message}');
      return null;
    } catch (e) { // Catch any other unexpected errors during validation
      print('Unexpected error during token validation: $e');
      return null;
    }
  }

  String? getUserIdFromToken(String token) {
    // For safety, this should also verify the token before extracting payload.
    // If validateToken is always called first in the request lifecycle,
    // then a simple decode might be considered, but it's a risk.
    try {
      final jwt = JWT.verify(token, SecretKey(_appConfig.jwtSecret));
      final payload = jwt.payload as Map<String, dynamic>;
      return payload['sub'] as String?;
    } on JWTException { // Covers expired, invalid format, signature, etc.
      return null;
    } catch (e) {
      print('Unexpected error in getUserIdFromToken: $e');
      return null;
    }
  }
}

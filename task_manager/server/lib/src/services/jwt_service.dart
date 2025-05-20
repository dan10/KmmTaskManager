import 'dart:convert';
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:shared/src/models/user.dart';

class JwtService {
  final String _secret;

  JwtService() : _secret = 'your-secret-key-here'; // For testing purposes

  String generateToken(User user) {
    final payload = {
      'sub': user.id,
      'name': user.name,
      'email': user.email,
      'iat': DateTime.now().millisecondsSinceEpoch ~/ 1000,
      'exp': (DateTime.now().add(const Duration(days: 7)))
              .millisecondsSinceEpoch ~/
          1000,
    };

    // TODO: Implement proper JWT signing
    return base64Encode(utf8.encode(json.encode(payload)));
  }

  Map<String, dynamic>? validateToken(String token) {
    try {
      final decoded = JwtDecoder.decode(token);
      final exp = decoded['exp'] as int;
      if (!DateTime.fromMillisecondsSinceEpoch(exp * 1000)
          .isAfter(DateTime.now())) {
        return null;
      }
      return decoded;
    } catch (_) {
      return null;
    }
  }

  String? getUserIdFromToken(String token) {
    try {
      final decoded = JwtDecoder.decode(token);
      return decoded['sub'] as String;
    } catch (_) {
      return null;
    }
  }
}

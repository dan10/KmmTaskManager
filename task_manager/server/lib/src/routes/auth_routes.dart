import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:shared/models.dart';

import '../services/auth_service.dart';
import '../services/jwt_service.dart';
import '../util/shelf_helpers.dart';

class AuthRoutes {
  final AuthService _authService;
  final JwtService _jwtService;

  AuthRoutes(this._authService, this._jwtService);

  Router get router {
    final router = Router();

    router.post('/register', (Request request) async {
      final requestBody = await request.readJsonBody();
      final registerDto = RegisterRequestDto.fromJson(requestBody);
      
      final user = await _authService.register(
        registerDto.displayName,
        registerDto.email,
        registerDto.password,
      );
      
      final userDto = UserPublicResponseDto.fromUser(user);
      return okJsonResponse(userDto.toJson());
    });

    router.post('/login', (Request request) async {
      final requestBody = await request.readJsonBody();
      final loginDto = LoginRequestDto.fromJson(requestBody);
      
      final user = await _authService.login(loginDto.email, loginDto.password);
      final token = _jwtService.generateToken(user);

      final response = LoginResponseDto(
        token: token,
        user: UserPublicResponseDto.fromUser(user),
      );

      return okJsonResponse(response.toJson());
    });

    router.post('/google-login', (Request request) async {
      final requestBody = await request.readJsonBody();
      final googleLoginDto = GoogleLoginRequestDto.fromJson(requestBody);
      
      final user = await _authService.googleLogin(googleLoginDto);
      final token = _jwtService.generateToken(user);

      final response = LoginResponseDto(
        token: token,
        user: UserPublicResponseDto.fromUser(user),
      );

      return okJsonResponse(response.toJson());
    });

    return router;
  }
}

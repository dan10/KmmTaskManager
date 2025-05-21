import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../services/auth_service.dart';
import '../services/jwt_service.dart';
import '../dto/user_public_response_dto.dart';
import '../dto/auth/login_request_dto.dart';
import '../dto/auth/register_request_dto.dart';
import '../dto/auth/google_login_request_dto.dart';
import '../util/shelf_helpers.dart';
import '../exceptions/custom_exceptions.dart'; // Import new exceptions
import '../dto/error_response_dto.dart';      // Import ErrorResponseDto

class AuthRoutes {
  final AuthService authService;
  final JwtService jwtService;

  AuthRoutes(this.authService, this.jwtService);

  Router get router {
    final router = Router();

    // Register endpoint
    router.post('/register', (Request request) async {
      // Middleware will handle FormatException from readJsonBody
      // Middleware will handle ConflictException from authService.register
      // Middleware will handle any other AppException or generic Exception
      final requestBody = await request.readJsonBody();
      final registerDto = RegisterRequestDto.fromJson(requestBody);
      registerDto.validateOrThrow(); // Call validation method
      final user = await authService.register(
        registerDto.name,
        registerDto.email,
        registerDto.password,
      );
      final userDto = UserPublicResponseDto(
        id: user.id,
        name: user.name,
        email: user.email,
      );
      return okJsonResponse(userDto.toJson());
    });

    // Login endpoint
    router.post('/login', (Request request) async {
      // Middleware will handle FormatException from readJsonBody
      // Middleware will handle AuthenticationException from authService.login
      final requestBody = await request.readJsonBody();
      final loginDto = LoginRequestDto.fromJson(requestBody);
      loginDto.validateOrThrow(); // Call validation method
      final user = await authService.login(
        loginDto.email,
        loginDto.password,
      );
      final token = jwtService.generateToken(user);
      final userDto = UserPublicResponseDto(
        id: user.id,
        name: user.name,
        email: user.email,
      );
      return okJsonResponse({
        'token': token,
        'user': userDto.toJson(),
      });
    });

    // Google Sign-In endpoint
    router.post('/google', (Request request) async {
      // Middleware will handle FormatException from readJsonBody
      // Middleware will handle AuthenticationException from authService.googleLogin
      final requestBody = await request.readJsonBody();
      final googleLoginDto = GoogleLoginRequestDto.fromJson(requestBody);
      googleLoginDto.validateOrThrow(); // Call validation method
      final user = await authService.googleLogin(googleLoginDto);
      final token = jwtService.generateToken(user); // Your app's JWT
      final userDto = UserPublicResponseDto(
        id: user.id,
        name: user.name,
        email: user.email,
      );
      return okJsonResponse({
        'token': token,
        'user': userDto.toJson(),
      });
      }
    });

    return router;
  }
}

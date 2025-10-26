import 'package:task_manager_shared/models.dart';
import '../../../../core/constants/api_routes.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/utils/result.dart';

/// Low-level API service for authentication HTTP calls
abstract class AuthApiService {
  Future<Result<LoginResponseDto>> login(LoginRequestDto request);

  Future<Result<LoginResponseDto>> register(RegisterRequestDto request);

  Future<Result<LoginResponseDto>> googleLogin(GoogleLoginRequestDto request);

  Future<Result<void>> logout(String token);
}

class AuthApiServiceImpl implements AuthApiService {
  final ApiClient _client;

  AuthApiServiceImpl({
    String? host,
    int? port,
    String? scheme,
  }) : _client = ApiClient(
          host: host ?? const String.fromEnvironment('API_HOST', defaultValue: 'localhost'),
          port: port ?? const int.fromEnvironment('API_PORT', defaultValue: 8081),
          scheme: scheme ?? const String.fromEnvironment('API_SCHEME', defaultValue: 'http'),
          // No auth token provider needed for auth endpoints
          authTokenProvider: null,
          // No unauthorized handler needed for auth endpoints
          unauthorizedHandler: null,
          enableLogging: true,
        );

  @override
  Future<Result<LoginResponseDto>> login(LoginRequestDto request) async {
    return _client.post<LoginResponseDto>(
      ApiRoutes.authLogin,
      body: request.toJson(),
      fromJson: (json) => LoginResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<LoginResponseDto>> register(RegisterRequestDto request) async {
    return _client.post<LoginResponseDto>(
      ApiRoutes.authRegister,
      body: request.toJson(),
      fromJson: (json) => LoginResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<LoginResponseDto>> googleLogin(GoogleLoginRequestDto request) async {
    return _client.post<LoginResponseDto>(
      ApiRoutes.authGoogleLogin,
      body: request.toJson(),
      fromJson: (json) => LoginResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<void>> logout(String token) async {
    // Create a temporary client with token for logout
    final clientWithToken = ApiClient(
      host: const String.fromEnvironment('API_HOST', defaultValue: 'localhost'),
      port: const int.fromEnvironment('API_PORT', defaultValue: 8081),
      scheme: const String.fromEnvironment('API_SCHEME', defaultValue: 'http'),
      authTokenProvider: () async => token,
      unauthorizedHandler: null,
      enableLogging: true,
    );

    // Call logout endpoint - don't fail if it doesn't work
    final result = await clientWithToken.post<void>(
      ApiRoutes.authLogout,
      body: null,
      fromJson: (_) => null,
    );

    // Always return success - local logout should work even if server call fails
    return const Result.ok(null);
  }
}


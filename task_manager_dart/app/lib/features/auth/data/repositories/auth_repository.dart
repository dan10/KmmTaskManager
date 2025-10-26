import 'package:task_manager_shared/models.dart';

import '../services/auth_service.dart';
import '../../../../data/sources/local/secure_storage.dart';
import '../../../../core/utils/result.dart';

abstract class AuthRepository {
  Future<Result<void>> login({required String email, required String password});
  Future<Result<void>> register({required String name, required String email, required String password});
}

class AuthRepositoryImpl implements AuthRepository {
  AuthRepositoryImpl({required AuthApiService apiService, required SecureStorage secureStorage})
      : _apiService = apiService,
        _secureStorage = secureStorage;

  final AuthApiService _apiService;
  final SecureStorage _secureStorage;

  @override
  Future<Result<void>> login({required String email, required String password}) async {
    final request = LoginRequestDto(email: email, password: password);
    if (!request.isValid) {
      final validationErrors = request.validate();
      return Result.error(Exception(validationErrors.values.first));
    }

    final result = await _apiService.login(request);

    switch (result) {
      case Ok<LoginResponseDto>():
        final loginResponse = result.value;
        await _secureStorage.storeToken(loginResponse.token);
        await _secureStorage.storeUser(loginResponse.user);
        return const Result.ok(null);
      case Error<LoginResponseDto>():
        return Result.error(result.error);
    }
  }

  @override
  Future<Result<void>> register({required String name, required String email, required String password}) async {
    final request = RegisterRequestDto(displayName: name, email: email, password: password);
    if (!request.isValid) {
      final validationErrors = request.validate();
      return Result.error(Exception(validationErrors.values.first));
    }

    final result = await _apiService.register(request);

    switch (result) {
      case Ok<LoginResponseDto>():
        final loginResponse = result.value;
        await _secureStorage.storeToken(loginResponse.token);
        await _secureStorage.storeUser(loginResponse.user);
        return const Result.ok(null);
      case Error<LoginResponseDto>():
        return Result.error(result.error);
    }
  }
}

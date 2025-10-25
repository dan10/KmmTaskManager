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
    try {
      final request = LoginRequestDto(email: email, password: password);
      if (!request.isValid) {
        final validationErrors = request.validate();
        return Result.error(Exception(validationErrors.values.first));
      }
      final loginResponse = await _apiService.login(request);
      await _secureStorage.storeToken(loginResponse.token);
      await _secureStorage.storeUser(loginResponse.user);
      return Result.ok(null);
    } catch (e) {
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }

  @override
  Future<Result<void>> register({required String name, required String email, required String password}) async {
    try {
      final request = RegisterRequestDto(displayName: name, email: email, password: password);
      if (!request.isValid) {
        final validationErrors = request.validate();
        return Result.error(Exception(validationErrors.values.first));
      }
      final loginResponse = await _apiService.register(request);
      await _secureStorage.storeToken(loginResponse.token);
      await _secureStorage.storeUser(loginResponse.user);
      return Result.ok(null);
    } catch (e) {
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }
}

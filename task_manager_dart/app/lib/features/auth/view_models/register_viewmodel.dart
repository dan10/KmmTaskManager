import 'package:logging/logging.dart';

import '../data/repositories/auth_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

class RegisterViewModel {
  RegisterViewModel({required AuthRepository authRepository})
      : _authRepository = authRepository {
    register = Command1<void, (String name, String email, String password)>(_register);
  }

  final AuthRepository _authRepository;
  final _log = Logger('RegisterViewModel');

  late Command1 register;

  Future<Result<void>> _register((String, String, String) data) async {
    final (name, email, password) = data;
    final result = await _authRepository.register(name: name, email: email, password: password);
    if (result is Error<void>) {
      _log.warning('Register failed! ${result.error}');
    }
    return result;
  }
}



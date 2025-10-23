import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_app/features/auth/view_models/register_viewmodel.dart';
import 'package:task_manager_app/features/auth/data/repositories/auth_repository.dart' as auth;
import 'package:task_manager_app/utils/result.dart';

class _FakeAuthRepo implements auth.AuthRepository {
  _FakeAuthRepo(this._loginResult, this._registerResult);
  final Result<void> _loginResult;
  final Result<void> _registerResult;

  @override
  Future<Result<void>> login({required String email, required String password}) async => _loginResult;

  @override
  Future<Result<void>> register({required String name, required String email, required String password}) async => _registerResult;
}

void main() {
  test('RegisterViewModel completes on ok', () async {
    final repo = _FakeAuthRepo(Ok<void>(null), Ok<void>(null));
    final vm = RegisterViewModel(authRepository: repo);

    await vm.register.execute(('John', 'a@b.com', 'pw'));

    expect(vm.register.completed, isTrue);
    expect(vm.register.error, isFalse);
  });

  test('RegisterViewModel errors on error', () async {
    final repo = _FakeAuthRepo(Ok<void>(null), Error<void>(Exception('fail')));
    final vm = RegisterViewModel(authRepository: repo);

    await vm.register.execute(('John', 'a@b.com', 'pw'));

    expect(vm.register.completed, isFalse);
    expect(vm.register.error, isTrue);
  });
}



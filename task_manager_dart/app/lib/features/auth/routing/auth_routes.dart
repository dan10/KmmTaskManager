import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_app/core/routing/app_router.dart';

import '../data/repositories/auth_repository.dart' as feature_auth;
import '../view_models/login_viewmodel.dart';
import '../pages/login_screen.dart';
import '../view_models/register_viewmodel.dart';
import '../pages/register_screen.dart';

final List<GoRoute> authRoutes = [
  GoRoute(
    path: AppRoutes.login,
    builder: (context, state) {
      final repo = Provider.of<feature_auth.AuthRepository>(context, listen: false);
      final vm = LoginViewModel(authRepository: repo);
      return LoginScreen(viewModel: vm);
    },
  ),
  GoRoute(
    path: AppRoutes.register,
    builder: (context, state) {
      final repo = Provider.of<feature_auth.AuthRepository>(context, listen: false);
      final vm = RegisterViewModel(authRepository: repo);
      return RegisterScreen(viewModel: vm);
    },
  ),
];
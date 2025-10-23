import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../../features/auth/routing/auth_routes.dart';
import '../../features/projects/routing/project_routes.dart';
import '../../features/tasks/routing/task_routes.dart';
import '../../presentation/views/main/main_view.dart';
import '../../data/sources/local/secure_storage.dart';

// It's good practice to have your route paths as constants
class AppRoutes {
  static const String login = '/login';
  static const String register = '/register';
  static const String home = '/';
  // Projects
  static const String projects = '/projects';
  static const String projectDetail = '/project/:projectId';
  static const String projectCreate = '/project/create';
  static const String projectEdit = '/project/:projectId/edit';
  // Tasks
  static const String tasks = '/tasks';
  static const String taskDetail = '/task/:taskId';
  static const String taskCreate = '/task/create';
  static const String taskEdit = '/task/:taskId/edit';
  // Calendar
  static const String calendar = '/calendar';
}

final GlobalKey<NavigatorState> _rootNavigatorKey = GlobalKey<NavigatorState>();

final GoRouter appRouter = GoRouter(
  navigatorKey: _rootNavigatorKey,
  initialLocation: AppRoutes.login, // The first route to show
  redirect: _authRedirect,
  routes: [
    GoRoute(
      path: AppRoutes.home,
      builder: (context, state) {
        final tabParam = state.uri.queryParameters['tab'];
        final initialTab = tabParam != null ? int.tryParse(tabParam) : null;
        return MainView(initialTab: initialTab);
      },
    ),
    ...authRoutes,
    ...projectRoutes,
    ...taskRoutes,
  ],
  // Optional: Add error handling
  errorBuilder: (context, state) => Scaffold(
    body: Center(child: Text('Page not found: ${state.error}')),
  ),
);

Future<String?> _authRedirect(BuildContext context, GoRouterState state) async {
  final secureStorage = Provider.of<SecureStorage>(context, listen: false);
  final token = await secureStorage.getToken();
  final loggedIn = token != null && token.isNotEmpty;

  final isAuthRoute = state.matchedLocation == AppRoutes.login ||
      state.matchedLocation == AppRoutes.register;

  if (!loggedIn && !isAuthRoute) {
    return AppRoutes.login;
  }

  if (loggedIn && isAuthRoute) {
    return AppRoutes.home;
  }

  return null;
}
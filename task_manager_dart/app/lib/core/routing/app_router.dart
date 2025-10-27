import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/routing/auth_routes.dart';
import '../../features/projects/routing/project_routes.dart';
import '../../features/tasks/routing/task_routes.dart';
import '../data/local/secure_storage.dart';

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

/// Create GoRouter with SecureStorage for auth redirect
GoRouter createAppRouter(SecureStorage secureStorage) {
  return GoRouter(
    navigatorKey: _rootNavigatorKey,
    initialLocation: AppRoutes.login,
    redirect: (context, state) => _authRedirect(secureStorage, state),
    routes: [
    // Auth routes
    ...authRoutes,

    // Redirect '/' to tasks branch
    GoRoute(
      path: AppRoutes.home,
      redirect: (context, state) => AppRoutes.tasks,
    ),

    // Bottom-navigation shell
    StatefulShellRoute(
      builder: (context, state, navigationShell) {
        return navigationShell;
      },
      navigatorContainerBuilder: (context, navigationShell, children) {
            return ScaffoldWithNavBar(
              navigationShell: navigationShell,
              children: children,
            );
          },
      branches: [
        // Branch 0: Tasks
        StatefulShellBranch(routes: taskRoutes, observers: [HeroController()]),
        // Branch 1: Calendar (middle)
        StatefulShellBranch(
          routes: [
            GoRoute(
              path: AppRoutes.calendar,
              builder: (context, state) =>
                  const Center(child: Text('Calendar')),
            ),
          ],
        ),
        // Branch 2: Projects
        StatefulShellBranch(routes: projectRoutes),
      ],
    ),
    ],
    errorBuilder: (context, state) =>
        Scaffold(body: Center(child: Text('Page not found: ${state.error}'))),
  );
}

Future<String?> _authRedirect(SecureStorage secureStorage, GoRouterState state) async {
  final token = await secureStorage.getToken();
  final loggedIn = token != null && token.isNotEmpty;

  final isAuthRoute =
      state.matchedLocation == AppRoutes.login ||
      state.matchedLocation == AppRoutes.register;

  if (!loggedIn && !isAuthRoute) {
    return AppRoutes.login;
  }

  if (loggedIn && isAuthRoute) {
    return AppRoutes.home;
  }

  return null;
}

/// A StatefulWidget that combines a Scaffold with a PageView for smooth transitions.
class ScaffoldWithNavBar extends StatefulWidget {
  const ScaffoldWithNavBar({
    required this.navigationShell,
    required this.children,
    super.key,
  });

  final StatefulNavigationShell navigationShell;
  final List<Widget> children;

  @override
  State<ScaffoldWithNavBar> createState() => _ScaffoldWithNavBarState();
}

class _ScaffoldWithNavBarState extends State<ScaffoldWithNavBar> {
  late final PageController _pageController = PageController(
    initialPage: widget.navigationShell.currentIndex,
  );

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      body: SafeArea(
        child: PageView(
          controller: _pageController,
          onPageChanged: (int i) => widget.navigationShell.goBranch(i),
          children: widget.children,
        ),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(icon: Icon(Icons.checklist), label: 'Tasks'),
          BottomNavigationBarItem(
            icon: Icon(Icons.calendar_today),
            label: 'Calendar',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.folder), label: 'Projects'),
        ],
        currentIndex: widget.navigationShell.currentIndex,
        onTap: _onTap,
      ),
    );
  }

  void _onTap(int index) {
    // Animate the PageView to the new page.
    // The router's state will be updated automatically by the PageView's onPageChanged callback.
    if (_pageController.hasClients && _pageController.page?.round() != index) {
      _pageController.jumpToPage(index);
    }
  }
}

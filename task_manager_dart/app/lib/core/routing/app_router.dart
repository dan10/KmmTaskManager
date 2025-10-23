import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../../features/auth/routing/auth_routes.dart';
import '../../features/projects/routing/project_routes.dart';
import '../../features/tasks/routing/task_routes.dart';
import '../../features/tasks/pages/task_list_screen.dart';
import '../../features/projects/pages/project_list_screen.dart';
import '../../data/sources/local/secure_storage.dart';
import 'package:collection/collection.dart';

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
  initialLocation: AppRoutes.login,
  redirect: _authRedirect,
  routes: [
    // Auth routes
    ...authRoutes,

    // Redirect '/' to tasks branch
    GoRoute(
      path: AppRoutes.home,
      redirect: (context, state) => AppRoutes.tasks,
    ),

    // Bottom-navigation shell with horizontal slide transitions
    StatefulShellRoute(
      parentNavigatorKey: _rootNavigatorKey,
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
        StatefulShellBranch(routes: taskRoutes),
        // Branch 1: Calendar (middle)
        StatefulShellBranch(
          routes: [
            GoRoute(
              path: AppRoutes.calendar,
              builder: (context, state) => const Center(child: Text('Calendar')),
            ),
          ],
        ),
        // Branch 2: Projects
        StatefulShellBranch(routes: projectRoutes),
      ],
    ),
  ],
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

class _ShellScaffold extends StatefulWidget {
  const _ShellScaffold({required this.child});
  final StatefulNavigationShell child;

  @override
  State<_ShellScaffold> createState() => _ShellScaffoldState();
}

class _ShellScaffoldState extends State<_ShellScaffold> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<Offset> _animation;
  int _previousIndex = 0;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _animation = Tween<Offset>(
      begin: Offset.zero,
      end: Offset.zero,
    ).animate(CurveTween(curve: Curves.easeInOut).animate(_controller));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _onTabTapped(int index) {
    if (index == widget.child.currentIndex) {
      widget.child.goBranch(index, initialLocation: true);
      return;
    }

    final direction = index > _previousIndex ? 1.0 : -1.0;

    setState(() {
      _animation = Tween<Offset>(
        begin: Offset(direction, 0),
        end: Offset.zero,
      ).animate(CurveTween(curve: Curves.easeInOut).animate(_controller));
      _previousIndex = widget.child.currentIndex;
    });

    _controller.forward(from: 0.0);
    widget.child.goBranch(index);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('TaskIt')),
      body: SafeArea(
        child: SlideTransition(
          position: _animation,
          child: widget.child,
        ),
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: widget.child.currentIndex,
        onTap: _onTabTapped,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.checklist), label: 'Tasks'),
          BottomNavigationBarItem(icon: Icon(Icons.calendar_today), label: 'Calendar'),
          BottomNavigationBarItem(icon: Icon(Icons.folder), label: 'Projects'),
        ],
      ),
    );
  }
}

class ScaffoldWithNavBar extends StatelessWidget {
  /// Constructs an [ScaffoldWithNavBar].
  const ScaffoldWithNavBar({
    required this.navigationShell,
    required this.children,
    Key? key,
  }) : super(key: key ?? const ValueKey<String>('ScaffoldWithNavBar'));

  /// The navigation shell and container for the branch Navigators.
  final StatefulNavigationShell navigationShell;

  /// The children (branch Navigators) to display in a custom container
  /// ([AnimatedBranchContainer]).
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: AnimatedBranchContainer(
        currentIndex: navigationShell.currentIndex,
        children: children,
      ),
      bottomNavigationBar: BottomNavigationBar(
        // Here, the items of BottomNavigationBar are hard coded. In a real
        // world scenario, the items would most likely be generated from the
        // branches of the shell route, which can be fetched using
        // `navigationShell.route.branches`.
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(icon: Icon(Icons.checklist), label: 'Tasks'),
          BottomNavigationBarItem(icon: Icon(Icons.calendar_today), label: 'Calendar'),
          BottomNavigationBarItem(icon: Icon(Icons.folder), label: 'Projects'),
        ],
        currentIndex: navigationShell.currentIndex,
        onTap: (int index) => _onTap(context, index),
      ),
    );
  }

  /// Navigate to the current location of the branch at the provided index when
  /// tapping an item in the BottomNavigationBar.
  void _onTap(BuildContext context, int index) {
    // When navigating to a new branch, it's recommended to use the goBranch
    // method, as doing so makes sure the last navigation state of the
    // Navigator for the branch is restored.
    navigationShell.goBranch(
      index,
      // A common pattern when using bottom navigation bars is to support
      // navigating to the initial location when tapping the item that is
      // already active. This example demonstrates how to support this behavior,
      // using the initialLocation parameter of goBranch.
      initialLocation: index == navigationShell.currentIndex,
    );
  }
}

class AnimatedBranchContainer extends StatelessWidget {
  /// Creates a AnimatedBranchContainer
  const AnimatedBranchContainer({
    super.key,
    required this.currentIndex,
    required this.children,
  });

  /// The index (in [children]) of the branch Navigator to display.
  final int currentIndex;

  /// The children (branch Navigators) to display in this container.
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children:
      children.mapIndexed((int index, Widget navigator) {
        return AnimatedScale(
          scale: index == currentIndex ? 1 : 1.5,
          duration: const Duration(milliseconds: 400),
          child: AnimatedOpacity(
            opacity: index == currentIndex ? 1 : 0,
            duration: const Duration(milliseconds: 400),
            child: _branchNavigatorWrapper(index, navigator),
          ),
        );
      }).toList(),
    );
  }

  Widget _branchNavigatorWrapper(int index, Widget navigator) => IgnorePointer(
    ignoring: index != currentIndex,
    child: TickerMode(enabled: index == currentIndex, child: navigator),
  );
}
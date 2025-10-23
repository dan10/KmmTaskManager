import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../core/routing/app_router.dart';
import '../features/auth/routing/auth_routes.dart';

// Legacy presentation imports removed; feature routes used instead
import '../presentation/views/main/main_view.dart';
import '../presentation/providers/auth_provider.dart';

class AppRouter {
  static GoRouter get router => _router;

  static final _router = GoRouter(
    initialLocation: AppRoutes.login,
    redirect: (context, state) {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final isAuthRoute = state.matchedLocation == '/login' || 
                         state.matchedLocation == '/register';

      // If not authenticated and not on auth route, redirect to login
      if (!authProvider.isAuthenticated && !isAuthRoute) {
        return '/login';
      }

      // If authenticated and on auth route, redirect to home
      if (authProvider.isAuthenticated && isAuthRoute) {
        return '/';
      }

      return null; // No redirect needed
    },
    routes: [
      ...authRoutes,
      GoRoute(
        path: '/',
        name: 'home',
        builder: (context, state) {
          final tabParam = state.uri.queryParameters['tab'];
          int? initialTab;
          if (tabParam != null) {
            initialTab = int.tryParse(tabParam);
          }
          return MainView(initialTab: initialTab);
        },
      ),
      // Auth routes are provided via core/routing/app_router.dart now
      // Projects routes are provided via feature router
      // Tasks routes provided via feature router
      // Project and Task create/edit provided via feature routers
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 16),
            Text(
              'Page not found: ${state.matchedLocation}',
              style: const TextStyle(fontSize: 18),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => context.go('/'),
              child: const Text('Go Home'),
            ),
          ],
        ),
      ),
    ),
  );
}

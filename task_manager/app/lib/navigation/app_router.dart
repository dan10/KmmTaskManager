import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../presentation/views/auth/login_view.dart';
import '../presentation/views/auth/register_view.dart';
import '../presentation/views/main/main_view.dart';
import '../presentation/views/projects/project_list_view.dart';
import '../presentation/views/projects/project_detail_view.dart';
import '../presentation/views/tasks/task_list_view.dart';
import '../presentation/views/tasks/task_detail_view.dart';
import '../presentation/views/tasks/task_create_edit_view.dart';
import '../presentation/viewmodels/auth_viewmodel.dart';

class AppRouter {
  static GoRouter get router => _router;

  static final _router = GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      final authViewModel = Provider.of<AuthViewModel>(context, listen: false);
      final isAuthRoute = state.matchedLocation == '/login' || 
                         state.matchedLocation == '/register';

      // If not authenticated and not on auth route, redirect to login
      if (!authViewModel.isAuthenticated && !isAuthRoute) {
        return '/login';
      }

      // If authenticated and on auth route, redirect to home
      if (authViewModel.isAuthenticated && isAuthRoute) {
        return '/';
      }

      return null; // No redirect needed
    },
    routes: [
      GoRoute(
        path: '/',
        name: 'home',
        builder: (context, state) => const MainView(),
      ),
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginView(),
      ),
      GoRoute(
        path: '/register',
        name: 'register',
        builder: (context, state) => const RegisterView(),
      ),
      GoRoute(
        path: '/projects',
        name: 'projects',
        builder: (context, state) => const ProjectListView(),
        routes: [
          GoRoute(
            path: ':projectId',
            name: 'project-detail',
            builder: (context, state) {
              final projectId = state.pathParameters['projectId']!;
              return ProjectDetailView(projectId: projectId);
            },
          ),
        ],
      ),
      GoRoute(
        path: '/tasks',
        name: 'tasks',
        builder: (context, state) {
          final projectId = state.uri.queryParameters['projectId'];
          return TaskListView(projectId: projectId);
        },
        routes: [
          GoRoute(
            path: 'create',
            name: 'task-create',
            builder: (context, state) {
              final projectId = state.uri.queryParameters['projectId'];
              return TaskCreateEditView(projectId: projectId);
            },
          ),
          GoRoute(
            path: ':taskId',
            name: 'task-detail',
            builder: (context, state) {
              final taskId = state.pathParameters['taskId']!;
              return TaskDetailView(taskId: taskId);
            },
            routes: [
              GoRoute(
                path: 'edit',
                name: 'task-edit',
                builder: (context, state) {
                  final taskId = state.pathParameters['taskId']!;
                  return TaskCreateEditView(taskId: taskId);
                },
              ),
            ],
          ),
        ],
      ),
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

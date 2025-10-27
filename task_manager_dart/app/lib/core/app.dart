import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_app/core/routing/app_router.dart';

import '../core/data/local/secure_storage.dart';
import 'theme/app_theme.dart';

class TaskManagerApp extends StatelessWidget {
  const TaskManagerApp({super.key});

  @override
  Widget build(BuildContext context) {
    final secureStorage = Provider.of<SecureStorage>(context, listen: false);
    
    return MaterialApp.router(
      title: 'Task Manager',
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.system,
      routerConfig: createAppRouter(secureStorage),
      debugShowCheckedModeBanner: false,
    );
  }
} 
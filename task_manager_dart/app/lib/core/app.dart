import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_app/core/routing/app_router.dart';
import 'package:task_manager_app/core/theme/theme.dart';

import '../core/data/local/secure_storage.dart';

class TaskManagerApp extends StatelessWidget {
  const TaskManagerApp({super.key});

  @override
  Widget build(BuildContext context) {
    final secureStorage = Provider.of<SecureStorage>(context, listen: false);
    
    return MaterialApp.router(
      title: 'Task Manager',
      theme: TaskItTheme.light(),
      darkTheme: TaskItTheme.dark(),
      themeMode: ThemeMode.system,
      routerConfig: createAppRouter(secureStorage),
      debugShowCheckedModeBanner: false,
    );
  }
} 
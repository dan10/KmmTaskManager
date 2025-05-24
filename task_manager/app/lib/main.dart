import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/app.dart';
import 'presentation/viewmodels/auth_viewmodel.dart';
import 'presentation/viewmodels/project_viewmodel.dart';
import 'presentation/viewmodels/task_viewmodel.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // TODO: Initialize services when dependencies are available
  // await StorageService.instance.init();
  // await ApiService.instance.init();
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthViewModel()),
        ChangeNotifierProvider(create: (_) => ProjectViewModel()),
        ChangeNotifierProvider(create: (_) => TaskViewModel()),
      ],
      child: const TaskManagerApp(),
    ),
  );
} 
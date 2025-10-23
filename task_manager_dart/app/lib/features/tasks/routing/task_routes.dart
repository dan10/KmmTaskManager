import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../view_models/task_detail_viewmodel.dart';
import '../view_models/task_create_edit_viewmodel.dart';
import '../pages/task_detail_screen.dart';
import '../pages/task_create_edit_screen.dart';
import '../../../core/routing/app_router.dart';

final List<GoRoute> taskRoutes = [
  GoRoute(
    path: AppRoutes.tasks,
    redirect: (context, state) => '${AppRoutes.home}?tab=0',
  ),
  GoRoute(
    path: AppRoutes.taskDetail,
    builder: (context, state) {
      final vm = Provider.of<TaskDetailViewModel>(context, listen: false);
      final taskId = state.pathParameters['taskId']!;
      if (vm.state.task == null && !vm.load.running) {
        vm.load.execute(taskId);
      }
      return TaskDetailScreen(taskId: taskId);
    },
  ),
  GoRoute(
    path: AppRoutes.taskCreate,
    builder: (context, state) {
      Provider.of<TaskCreateEditViewModel>(context, listen: false);
      final projectId = state.uri.queryParameters['projectId'];
      return TaskCreateEditScreen(projectId: projectId);
    },
  ),
  GoRoute(
    path: AppRoutes.taskEdit,
    builder: (context, state) {
      Provider.of<TaskCreateEditViewModel>(context, listen: false);
      final taskId = state.pathParameters['taskId']!;
      return TaskCreateEditScreen(taskId: taskId);
    },
  ),
];



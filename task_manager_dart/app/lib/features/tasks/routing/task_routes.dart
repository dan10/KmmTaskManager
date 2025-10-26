import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../core/routing/app_router.dart';
import '../di/providers.dart';
import '../pages/task_create_edit_screen.dart';
import '../presentation/views/task_details_screen.dart';
import '../presentation/views/task_list_screen.dart';
import '../presentation/viewmodels/tasks_viewmodel.dart';
import '../view_models/task_create_edit_viewmodel.dart';

final List<GoRoute> taskRoutes = [
  GoRoute(
    path: AppRoutes.tasks,
    builder: (context, state) {
      // Ensure TasksViewModel is available
      Provider.of<TasksViewModel>(context, listen: false);
      return const TaskListScreen();
    }
  ),
  GoRoute(
    path: AppRoutes.taskDetail,
    builder: (context, state) {
      final taskId = state.pathParameters['taskId']!;
      final initialTask = state.extra as TaskDto?; // Get passed task
      
      final viewModel = createTaskDetailsViewModel(
        context: context,
        taskId: taskId,
        initialTask: initialTask,
      );
      
      // Set navigation callbacks
      viewModel.onBack = () => context.pop();
      viewModel.onEditTask = (id) => context.push(
        AppRoutes.taskEdit.replaceFirst(':taskId', id),
      );

      return ChangeNotifierProvider.value(
        value: viewModel,
        child: TaskDetailsScreen(
          taskId: taskId,
          initialTask: initialTask,
        ),
      );
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



import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../core/routing/app_router.dart';
import '../di/providers.dart';
import '../presentation/views/task_details_screen.dart';
import '../presentation/views/task_edit_screen.dart';
import '../presentation/views/task_list_screen.dart';
import '../presentation/viewmodels/tasks_viewmodel.dart';

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
    path: AppRoutes.taskEdit,
    builder: (context, state) {
      final taskId = state.pathParameters['taskId']!;
      
      final viewModel = createTaskEditViewModel(
        context: context,
        taskId: taskId,
      );
      
      // Set navigation callbacks
      viewModel.onBack = () => context.pop();
      viewModel.onTaskUpdated = () => context.pop();
      viewModel.onTaskDeleted = () {
        // Pop twice: once from edit screen, once from details screen
        context.pop();
        context.pop();
      };

      return ChangeNotifierProvider.value(
        value: viewModel,
        child: TaskEditScreen(taskId: taskId),
      );
    },
  ),
];



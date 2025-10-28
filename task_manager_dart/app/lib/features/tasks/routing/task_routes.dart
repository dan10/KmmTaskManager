import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../core/routing/app_router.dart';
import '../di/providers.dart';
import '../presentation/views/task_details_screen.dart';
import '../presentation/views/task_edit_screen.dart';
import '../presentation/views/task_list_screen.dart';
import '../presentation/viewmodels/tasks_viewmodel.dart';

/// Main task routes for the tasks tab
final List<GoRoute> taskRoutes = [
  GoRoute(
    path: AppRoutes.tasks,
    builder: (context, state) {
      // Ensure TasksViewModel is available
      Provider.of<TasksViewModel>(context, listen: false);
      return const TaskListScreen();
    }
  ),
  // Include task detail and edit routes
  ...taskDetailsRoutes(),
];

/// Task details routes that can be injected into other sections
/// Similar to taskDetailsDestination in ProjectsNavigation.kt
/// 
/// Returns the task detail and edit routes that can be reused
/// in different navigation contexts (tasks tab, projects tab, etc.)
List<GoRoute> taskDetailsRoutes() {
  return [
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
}

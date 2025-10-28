import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../presentation/views/project_list_screen.dart';
import '../presentation/views/project_detail_screen.dart';
import '../presentation/viewmodels/project_tasks_viewmodel.dart';
import '../../../core/routing/app_router.dart';
import '../data/repositories/project_repository.dart';
import '../../tasks/domain/usecases/update_task_status_usecase.dart';

/// Projects section with nested task details
/// Similar to ProjectsNavigation.kt in KMM version
/// 
/// [taskDetailsRoutes] - Function that provides task detail/edit routes to be included in this section
List<GoRoute> projectsSection({
  required List<GoRoute> Function() taskDetailsRoutes,
}) {
  return [
    GoRoute(
      path: AppRoutes.projects,
      builder: (context, state) => const ProjectListScreen(),
    ),
    GoRoute(
      path: AppRoutes.projectDetail,
      name: 'project-detail',
      builder: (context, state) {
        final projectId = state.pathParameters['projectId']!;
        return ChangeNotifierProvider(
          create: (context) => ProjectTasksViewModel(
            projectRepository: Provider.of<ProjectRepository>(context, listen: false),
            updateTaskStatusUseCase: Provider.of<UpdateTaskStatusUseCase>(context, listen: false),
            projectId: projectId,
          ),
          child: ProjectDetailScreen(projectId: projectId),
        );
      },
    ),
    // Include task details routes in the projects section
    // This allows navigating to task details from project detail screen
    ...taskDetailsRoutes(),
  ];
}

import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../view_models/project_detail_viewmodel.dart';
import '../view_models/project_create_edit_viewmodel.dart';
import '../presentation/views/project_list_screen.dart';
import '../pages/project_detail_screen.dart';
import '../pages/project_create_edit_screen.dart';
import '../../../core/routing/app_router.dart';

final List<GoRoute> projectRoutes = [
  GoRoute(
    path: AppRoutes.projects,
    builder: (context, state) => const ProjectListScreen(),
  ),
  GoRoute(
    path: AppRoutes.projectDetail,
    name: 'project-detail',
    builder: (context, state) {
      final vm = Provider.of<ProjectDetailViewModel>(context, listen: false);
      final projectId = state.pathParameters['projectId']!;
      if (vm.state.project == null && !vm.load.running) {
        vm.load.execute(projectId);
      }
      return ProjectDetailScreen(projectId: projectId);
    },
  ),
  GoRoute(
    path: AppRoutes.projectCreate,
    name: 'project-create',
    builder: (context, state) {
      Provider.of<ProjectCreateEditViewModel>(context, listen: false);
      return const ProjectCreateEditScreen();
    },
  ),
  GoRoute(
    path: AppRoutes.projectEdit,
    name: 'project-edit',
    builder: (context, state) {
      Provider.of<ProjectCreateEditViewModel>(context, listen: false);
      final projectId = state.pathParameters['projectId']!;
      return ProjectCreateEditScreen(projectId: projectId);
    },
  ),
];



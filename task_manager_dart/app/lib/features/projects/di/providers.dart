import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../../../core/network/api_client.dart';
import '../data/services/project_api_service.dart';
import '../data/repositories/project_repository.dart';
import '../view_models/project_list_viewmodel.dart';
import '../view_models/project_detail_viewmodel.dart';
import '../view_models/project_create_edit_viewmodel.dart';
import '../presentation/viewmodels/projects_viewmodel.dart';

List<SingleChildWidget> get providers => [
  // Project API Service - injects the singleton ApiClient from core providers
  ProxyProvider<ApiClient, ProjectApiService>(
    update: (_, apiClient, __) => ProjectApiServiceImpl(apiClient),
  ),

  // Project Repository
  ProxyProvider<ProjectApiService, ProjectRepository>(
    update: (_, api, __) => ProjectRepositoryImpl(api),
  ),
  // New ProjectsViewModel with pagination support
  ChangeNotifierProxyProvider<ProjectRepository, ProjectsViewModel>(
    create: (context) => ProjectsViewModel(
      repository: Provider.of<ProjectRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? ProjectsViewModel(repository: repo),
  ),
  // Legacy ProjectListViewModel (can be removed later)
  ChangeNotifierProxyProvider<ProjectRepository, ProjectListViewModel>(
    create: (context) => ProjectListViewModel(
      repository: Provider.of<ProjectRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? ProjectListViewModel(repository: repo),
  ),
  ChangeNotifierProxyProvider<ProjectRepository, ProjectDetailViewModel>(
    create: (context) => ProjectDetailViewModel(
      repository: Provider.of<ProjectRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? ProjectDetailViewModel(repository: repo),
  ),
  ChangeNotifierProxyProvider<ProjectRepository, ProjectCreateEditViewModel>(
    create: (context) => ProjectCreateEditViewModel(
      repository: Provider.of<ProjectRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? ProjectCreateEditViewModel(repository: repo),
  ),
];



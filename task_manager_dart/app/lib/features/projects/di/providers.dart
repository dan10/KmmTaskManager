import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../../../data/services/project_api_service.dart';
import '../data/repositories/project_repository.dart';
import '../view_models/project_list_viewmodel.dart';
import '../view_models/project_detail_viewmodel.dart';
import '../view_models/project_create_edit_viewmodel.dart';

List<SingleChildWidget> get providers => [
  ProxyProvider<ProjectApiService, ProjectRepository>(
    update: (_, api, __) => ProjectRepositoryImpl(api),
  ),
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



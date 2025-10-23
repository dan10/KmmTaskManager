import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../../../data/services/task_api_service.dart';
import '../data/repositories/task_repository.dart';
import '../view_models/task_list_viewmodel.dart';
import '../view_models/task_detail_viewmodel.dart';
import '../view_models/task_create_edit_viewmodel.dart';

List<SingleChildWidget> get providers => [
  ProxyProvider<TaskApiService, TaskRepository>(
    update: (_, api, __) => TaskRepositoryImpl(api),
  ),
  ChangeNotifierProxyProvider<TaskRepository, TaskListViewModel>(
    create: (context) => TaskListViewModel(
      repository: Provider.of<TaskRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? TaskListViewModel(repository: repo),
  ),
  ChangeNotifierProxyProvider<TaskRepository, TaskDetailViewModel>(
    create: (context) => TaskDetailViewModel(
      repository: Provider.of<TaskRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? TaskDetailViewModel(repository: repo),
  ),
  ChangeNotifierProxyProvider<TaskRepository, TaskCreateEditViewModel>(
    create: (context) => TaskCreateEditViewModel(
      repository: Provider.of<TaskRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? TaskCreateEditViewModel(repository: repo),
  ),
];



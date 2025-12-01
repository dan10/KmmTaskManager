import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../../../core/network/api_client.dart';
import '../../tasks/domain/usecases/delete_task_usecase.dart';
import '../../tasks/domain/usecases/update_task_status_usecase.dart';
import '../data/repositories/calendar_repository_impl.dart';
import '../data/services/calendar_api_service.dart';
import '../domain/repositories/calendar_repository.dart';
import '../domain/usecases/get_tasks_for_date_usecase.dart';
import '../presentation/viewmodels/calendar_viewmodel.dart';

List<SingleChildWidget> get providers => [
      // Calendar API Service
      ProxyProvider<ApiClient, CalendarApiService>(
        update: (_, apiClient, __) => CalendarApiServiceImpl(apiClient),
      ),

      // Calendar Repository
      ProxyProvider<CalendarApiService, CalendarRepository>(
        update: (_, api, __) => CalendarRepositoryImpl(api),
      ),

      // Use Cases
      ProxyProvider<CalendarRepository, GetTasksForDateUseCase>(
        update: (_, repo, __) => GetTasksForDateUseCase(repo),
      ),

      // Calendar ViewModel
      ChangeNotifierProxyProvider3<GetTasksForDateUseCase,
          UpdateTaskStatusUseCase, DeleteTaskUseCase, CalendarViewModel>(
        create: (context) => CalendarViewModel(
          getTasksForDateUseCase:
              Provider.of<GetTasksForDateUseCase>(context, listen: false),
          updateTaskStatusUseCase:
              Provider.of<UpdateTaskStatusUseCase>(context, listen: false),
          deleteTaskUseCase:
              Provider.of<DeleteTaskUseCase>(context, listen: false),
        ),
        update: (_, getTasks, updateStatus, deleteTask, previous) =>
            previous ??
            CalendarViewModel(
              getTasksForDateUseCase: getTasks,
              updateTaskStatusUseCase: updateStatus,
              deleteTaskUseCase: deleteTask,
            ),
      ),
    ];


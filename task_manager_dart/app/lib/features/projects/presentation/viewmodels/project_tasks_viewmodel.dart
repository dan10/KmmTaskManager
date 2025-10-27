import 'package:flutter/foundation.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../../../tasks/domain/usecases/update_task_status_usecase.dart';
import '../state/project_detail_state.dart';
import '../../../../core/utils/result.dart';
import '../../../../core/utils/command.dart';
import '../../data/repositories/project_repository.dart';

/// ViewModel for Project Details screen with tasks list
class ProjectTasksViewModel extends ChangeNotifier {
  final ProjectRepository _projectRepository;
  final UpdateTaskStatusUseCase _updateTaskStatusUseCase;
  final String projectId;
  final _log = Logger('ProjectTasksViewModel');

  ProjectTasksViewModel({
    required ProjectRepository projectRepository,
    required UpdateTaskStatusUseCase updateTaskStatusUseCase,
    required this.projectId,
  })  : _projectRepository = projectRepository,
        _updateTaskStatusUseCase = updateTaskStatusUseCase {
    // Initialize commands
    updateTaskStatus = Command1<void, (String, TaskStatus)>(_updateTaskStatus);
    loadProject = Command0<void>(_loadProject);
    
    // Initialize pagination controller for project tasks
    _pagingController = PagingController<int, TaskDto>(
      getNextPageKey: (state) {
        if (state.lastPageIsEmpty) return null;
        return state.nextIntPageKey;
      },
      fetchPage: _fetchPage,
    );
    
    // Load project details
    loadProject.execute();
  }

  // Commands
  late Command1<void, (String, TaskStatus)> updateTaskStatus;
  late Command0<void> loadProject;

  // Pagination controller for tasks
  late final PagingController<int, TaskDto> _pagingController;
  PagingController<int, TaskDto> get pagingController => _pagingController;

  // State
  ProjectDetailState _state = const ProjectDetailState();
  ProjectDetailState get state => _state;

  static const int _pageSize = 20;

  // Load project details
  Future<Result<void>> _loadProject() async {
    _state = _state.copyWith(isLoading: true);
    notifyListeners();

    _log.info('Loading project: $projectId');
    final result = await _projectRepository.getProject(projectId);

    if (result is Ok<Project>) {
      _log.info('Project loaded: ${result.value.name}');
      _state = _state.copyWith(
        isLoading: false,
        project: result.value,
        errorMessage: null,
      );
    } else {
      final error = (result as Error).error;
      _log.severe('Failed to load project: $error');
      _state = _state.copyWith(
        isLoading: false,
        errorMessage: error.toString(),
      );
    }
    notifyListeners();
    return result is Ok ? Result.ok(null) : Result.error((result as Error).error);
  }

  // Fetch page for pagination (tasks for this project)
  Future<List<TaskDto>> _fetchPage(int pageKey) async {
    try {
      final result = await _projectRepository.getProjectTasks(
        projectId: projectId,
        page: pageKey - 1,
        size: _pageSize,
      );

      if (result is Ok<PaginatedResponse<TaskDto>>) {
        final response = result.value;
        return response.items;
      } else {
        final error = (result as Error).error;
        throw error;
      }
    } catch (error) {
      rethrow;
    }
  }

  // Command: Update task status
  Future<Result<void>> _updateTaskStatus((String, TaskStatus) params) async {
    final (taskId, status) = params;
    final result = await _updateTaskStatusUseCase(taskId, status);
    
    // Refresh task list and project (to update progress)
    _pagingController.refresh();
    await loadProject.execute();
    
    return result is Ok
        ? Result.ok(null)
        : Result.error((result as Error).error);
  }

  // Refresh everything
  Future<void> refresh() async {
    _pagingController.refresh();
    await loadProject.execute();
  }

  @override
  void dispose() {
    updateTaskStatus.dispose();
    loadProject.dispose();
    _pagingController.dispose();
    super.dispose();
  }
}


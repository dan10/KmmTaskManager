import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/task_repository.dart';

enum TaskListState {
  initial,
  loading,
  loaded,
  loadingMore,
  error,
  refreshing,
}

class TaskListViewModel extends ChangeNotifier {
  final TaskRepository _taskRepository;

  TaskListState _state = TaskListState.initial;
  List<TaskDto> _tasks = [];
  String? _errorMessage;
  String _searchQuery = '';
  String? _projectId;
  
  // Pagination
  int _currentPage = 0;
  int _pageSize = 20;
  int _totalPages = 0;
  int _totalItems = 0;
  bool _hasMorePages = false;

  TaskListState get state => _state;
  List<TaskDto> get tasks => List.unmodifiable(_tasks);
  String? get errorMessage => _errorMessage;
  String get searchQuery => _searchQuery;
  String? get projectId => _projectId;
  
  bool get isLoading => _state == TaskListState.loading;
  bool get isLoadingMore => _state == TaskListState.loadingMore;
  bool get isRefreshing => _state == TaskListState.refreshing;
  bool get hasTasks => _tasks.isNotEmpty;
  bool get hasMorePages => _hasMorePages;
  
  // Pagination info
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;
  int get totalItems => _totalItems;
  int get pageSize => _pageSize;

  // Filtered tasks by status
  List<TaskDto> get todoTasks => _tasks.where((task) => task.status == TaskStatus.todo).toList();
  List<TaskDto> get inProgressTasks => _tasks.where((task) => task.status == TaskStatus.inProgress).toList();
  List<TaskDto> get doneTasks => _tasks.where((task) => task.status == TaskStatus.done).toList();

  // Tasks by priority
  List<TaskDto> get highPriorityTasks => _tasks.where((task) => task.priority == Priority.high).toList();
  List<TaskDto> get mediumPriorityTasks => _tasks.where((task) => task.priority == Priority.medium).toList();
  List<TaskDto> get lowPriorityTasks => _tasks.where((task) => task.priority == Priority.low).toList();

  // Overdue tasks (tasks with due date in the past and not done)
  List<TaskDto> get overdueTasks => _tasks.where((task) => 
    task.dueDate != null && 
    task.dueDate!.isBefore(DateTime.now()) && 
    task.status != TaskStatus.done
  ).toList();

  TaskListViewModel(this._taskRepository);

  Future<void> loadTasks({
    String? projectId,
    bool refresh = false,
  }) async {
    if (refresh) {
      _setState(TaskListState.refreshing);
      _currentPage = 0;
      _tasks.clear();
    } else {
      _setState(TaskListState.loading);
    }

    _projectId = projectId;
    
    try {
      final response = await _taskRepository.getTasks(
        page: _currentPage,
        size: _pageSize,
        query: _searchQuery.isNotEmpty ? _searchQuery : null,
        projectId: _projectId,
      );
      
      if (refresh) {
        _tasks = response.items;
      } else {
        _tasks.addAll(response.items);
      }
      
      _totalPages = response.totalPages;
      _totalItems = response.total;
      _hasMorePages = _currentPage < _totalPages - 1;
      
      _setState(TaskListState.loaded);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> loadMoreTasks() async {
    if (!_hasMorePages || _state == TaskListState.loadingMore) {
      return;
    }

    _setState(TaskListState.loadingMore);
    _currentPage++;

    try {
      final response = await _taskRepository.getTasks(
        page: _currentPage,
        size: _pageSize,
        query: _searchQuery.isNotEmpty ? _searchQuery : null,
        projectId: _projectId,
      );
      
      _tasks.addAll(response.items);
      _hasMorePages = _currentPage < _totalPages - 1;
      
      _setState(TaskListState.loaded);
    } catch (e) {
      _currentPage--; // Revert page increment on error
      _setError(e.toString());
    }
  }

  Future<void> searchTasks(String query) async {
    _searchQuery = query;
    _currentPage = 0;
    _tasks.clear();
    
    await loadTasks(projectId: _projectId);
  }

  Future<void> refreshTasks() async {
    await loadTasks(projectId: _projectId, refresh: true);
  }

  Future<void> createTask(TaskCreateRequestDto request) async {
    try {
      final newTask = await _taskRepository.createTask(request);
      
      // Add to the beginning of the list if it matches current filters
      if (_shouldIncludeTask(newTask)) {
        _tasks.insert(0, newTask);
        _totalItems++;
        notifyListeners();
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> updateTask(String id, TaskUpdateRequestDto request) async {
    try {
      final updatedTask = await _taskRepository.updateTask(id, request);
      
      final index = _tasks.indexWhere((task) => task.id == id);
      if (index != -1) {
        if (_shouldIncludeTask(updatedTask)) {
          _tasks[index] = updatedTask;
        } else {
          _tasks.removeAt(index);
          _totalItems--;
        }
        notifyListeners();
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> deleteTask(String id) async {
    try {
      await _taskRepository.deleteTask(id);
      
      _tasks.removeWhere((task) => task.id == id);
      _totalItems--;
      notifyListeners();
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> changeTaskStatus(String id, TaskStatus status) async {
    try {
      final updatedTask = await _taskRepository.changeTaskStatus(id, status);
      
      final index = _tasks.indexWhere((task) => task.id == id);
      if (index != -1) {
        _tasks[index] = updatedTask;
        notifyListeners();
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> assignTask(String id, String assigneeId) async {
    try {
      final updatedTask = await _taskRepository.assignTask(id, assigneeId);
      
      final index = _tasks.indexWhere((task) => task.id == id);
      if (index != -1) {
        _tasks[index] = updatedTask;
        notifyListeners();
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  TaskDto? getTask(String id) {
    try {
      return _tasks.firstWhere((task) => task.id == id);
    } catch (e) {
      return null;
    }
  }

  void clearSearch() {
    if (_searchQuery.isNotEmpty) {
      _searchQuery = '';
      _currentPage = 0;
      _tasks.clear();
      loadTasks(projectId: _projectId);
    }
  }

  void clearError() {
    if (_state == TaskListState.error) {
      _setState(_tasks.isNotEmpty ? TaskListState.loaded : TaskListState.initial);
    }
  }

  bool _shouldIncludeTask(TaskDto task) {
    // Check if task matches current project filter
    if (_projectId != null && task.projectId != _projectId) {
      return false;
    }
    
    // Check if task matches current search query
    if (_searchQuery.isNotEmpty) {
      final query = _searchQuery.toLowerCase();
      return task.title.toLowerCase().contains(query) ||
             task.description.toLowerCase().contains(query);
    }
    
    return true;
  }

  void _setState(TaskListState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = TaskListState.error;
    _errorMessage = error;
    notifyListeners();
  }

  // Task statistics
  int get totalTasksCount => _totalItems;
  int get todoTasksCount => todoTasks.length;
  int get inProgressTasksCount => inProgressTasks.length;
  int get doneTasksCount => doneTasks.length;
  int get overdueTasksCount => overdueTasks.length;
  
  double get completionRate => totalTasksCount == 0 ? 0.0 : doneTasksCount / totalTasksCount;

  void reset() {
    _state = TaskListState.initial;
    _tasks.clear();
    _errorMessage = null;
    _searchQuery = '';
    _projectId = null;
    _currentPage = 0;
    _totalPages = 0;
    _totalItems = 0;
    _hasMorePages = false;
    notifyListeners();
  }

  // Statistics
  Map<String, int> get taskStatistics {
    return {
      'total': _tasks.length,
      'todo': todoTasks.length,
      'inProgress': inProgressTasks.length,
      'done': doneTasks.length,
      'overdue': overdueTasks.length,
      'highPriority': highPriorityTasks.length,
      'mediumPriority': mediumPriorityTasks.length,
      'lowPriority': lowPriorityTasks.length,
    };
  }

  double get completionPercentage {
    if (_tasks.isEmpty) return 0.0;
    return (doneTasks.length / _tasks.length) * 100;
  }
} 
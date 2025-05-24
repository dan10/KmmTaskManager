import 'package:flutter/foundation.dart';
import '../../domain/entities/task.dart';
import '../../domain/repositories/task_repository.dart';

enum TaskViewState {
  initial,
  loading,
  loaded,
  error,
}

class TaskViewModel extends ChangeNotifier {
  // TODO: Inject repository when data layer is implemented
  // final TaskRepository _taskRepository;
  
  TaskViewState _state = TaskViewState.initial;
  List<Task> _tasks = [];
  Task? _selectedTask;
  String? _errorMessage;
  String? _currentProjectId;

  TaskViewState get state => _state;
  List<Task> get tasks => List.unmodifiable(_tasks);
  Task? get selectedTask => _selectedTask;
  String? get errorMessage => _errorMessage;
  bool get isLoading => _state == TaskViewState.loading;
  bool get hasTasks => _tasks.isNotEmpty;

  // Filtered tasks by status
  List<Task> get todoTasks => _tasks.where((task) => task.status == TaskStatus.todo).toList();
  List<Task> get inProgressTasks => _tasks.where((task) => task.status == TaskStatus.inProgress).toList();
  List<Task> get completedTasks => _tasks.where((task) => task.status == TaskStatus.completed).toList();

  // Tasks by priority
  List<Task> get highPriorityTasks => _tasks.where((task) => task.priority == TaskPriority.high).toList();
  List<Task> get overdueTasks => _tasks.where((task) => task.isOverdue).toList();

  // TODO: Constructor will accept repository when implemented
  // TaskViewModel(this._taskRepository);

  Future<void> loadTasks({String? projectId}) async {
    _currentProjectId = projectId;
    _setState(TaskViewState.loading);
    
    try {
      // TODO: Replace with actual repository call
      await _simulateLoadTasks(projectId);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> createTask({
    required String title,
    String? description,
    required TaskPriority priority,
    required String projectId,
    String? assigneeId,
    DateTime? dueDate,
  }) async {
    if (_isTaskFormValid(title, projectId)) {
      try {
        // TODO: Replace with actual repository call
        await _simulateCreateTask(
          title: title,
          description: description,
          priority: priority,
          projectId: projectId,
          assigneeId: assigneeId,
          dueDate: dueDate,
        );
      } catch (e) {
        _setError(e.toString());
      }
    } else {
      _setError('Task title and project are required');
    }
  }

  Future<void> updateTask(String id, {
    String? title,
    String? description,
    TaskStatus? status,
    TaskPriority? priority,
    String? assigneeId,
    DateTime? dueDate,
  }) async {
    try {
      // TODO: Replace with actual repository call
      await _simulateUpdateTask(
        id,
        title: title,
        description: description,
        status: status,
        priority: priority,
        assigneeId: assigneeId,
        dueDate: dueDate,
      );
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> deleteTask(String id) async {
    try {
      // TODO: Replace with actual repository call
      await _simulateDeleteTask(id);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> updateTaskStatus(String id, TaskStatus status) async {
    await updateTask(id, status: status);
  }

  void selectTask(String id) {
    _selectedTask = _tasks.where((t) => t.id == id).firstOrNull;
    notifyListeners();
  }

  void clearSelection() {
    _selectedTask = null;
    notifyListeners();
  }

  Task? getTask(String id) {
    try {
      return _tasks.where((task) => task.id == id).first;
    } catch (e) {
      return null;
    }
  }

  List<Task> getTasksForProject(String projectId) {
    return _tasks.where((task) => task.projectId == projectId).toList();
  }

  // Task statistics
  int get totalTasks => _tasks.length;
  int get completedTasksCount => completedTasks.length;
  int get pendingTasksCount => todoTasks.length + inProgressTasks.length;
  double get completionRate => totalTasks == 0 ? 0.0 : completedTasksCount / totalTasks;

  // TODO: Remove simulation methods when repository is implemented
  Future<void> _simulateLoadTasks(String? projectId) async {
    await Future.delayed(const Duration(seconds: 1));
    
    final allTasks = [
      Task(
        id: '1',
        title: 'Implement authentication',
        description: 'Create login and registration screens with validation',
        status: TaskStatus.completed,
        priority: TaskPriority.high,
        projectId: '1',
        assigneeId: '1',
        dueDate: DateTime.now().add(const Duration(days: 2)),
        createdAt: DateTime.now().subtract(const Duration(days: 5)),
      ),
      Task(
        id: '2',
        title: 'Setup database schema',
        description: 'Design and implement PostgreSQL database schema',
        status: TaskStatus.inProgress,
        priority: TaskPriority.medium,
        projectId: '1',
        assigneeId: '1',
        dueDate: DateTime.now().add(const Duration(days: 5)),
        createdAt: DateTime.now().subtract(const Duration(days: 3)),
      ),
      Task(
        id: '3',
        title: 'Write API documentation',
        description: 'Document all REST API endpoints with examples',
        status: TaskStatus.todo,
        priority: TaskPriority.low,
        projectId: '1',
        dueDate: DateTime.now().add(const Duration(days: 10)),
        createdAt: DateTime.now().subtract(const Duration(days: 1)),
      ),
      Task(
        id: '4',
        title: 'Design dashboard UI',
        description: 'Create responsive dashboard interface',
        status: TaskStatus.todo,
        priority: TaskPriority.high,
        projectId: '2',
        dueDate: DateTime.now().add(const Duration(days: 7)),
        createdAt: DateTime.now().subtract(const Duration(days: 2)),
      ),
      Task(
        id: '5',
        title: 'Implement real-time notifications',
        description: 'Add WebSocket support for live updates',
        status: TaskStatus.inProgress,
        priority: TaskPriority.medium,
        projectId: '2',
        dueDate: DateTime.now().subtract(const Duration(days: 1)), // Overdue
        createdAt: DateTime.now().subtract(const Duration(days: 4)),
      ),
      Task(
        id: '6',
        title: 'Setup CI/CD pipeline',
        description: 'Configure automated testing and deployment',
        status: TaskStatus.todo,
        priority: TaskPriority.high,
        projectId: '3',
        dueDate: DateTime.now().add(const Duration(days: 3)),
        createdAt: DateTime.now(),
      ),
    ];
    
    // Filter by project if specified
    if (projectId != null) {
      _tasks = allTasks.where((task) => task.projectId == projectId).toList();
    } else {
      _tasks = allTasks;
    }
    
    _setState(TaskViewState.loaded);
  }

  Future<void> _simulateCreateTask({
    required String title,
    String? description,
    required TaskPriority priority,
    required String projectId,
    String? assigneeId,
    DateTime? dueDate,
  }) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    final newTask = Task(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: title,
      description: description,
      status: TaskStatus.todo,
      priority: priority,
      projectId: projectId,
      assigneeId: assigneeId,
      dueDate: dueDate,
      createdAt: DateTime.now(),
    );
    
    _tasks.add(newTask);
    notifyListeners();
  }

  Future<void> _simulateUpdateTask(String id, {
    String? title,
    String? description,
    TaskStatus? status,
    TaskPriority? priority,
    String? assigneeId,
    DateTime? dueDate,
  }) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    final index = _tasks.indexWhere((task) => task.id == id);
    if (index != -1) {
      _tasks[index] = _tasks[index].copyWith(
        title: title,
        description: description,
        status: status,
        priority: priority,
        assigneeId: assigneeId,
        dueDate: dueDate,
        updatedAt: DateTime.now(),
      );
      
      // Update selected task if it's the one being updated
      if (_selectedTask?.id == id) {
        _selectedTask = _tasks[index];
      }
      
      notifyListeners();
    }
  }

  Future<void> _simulateDeleteTask(String id) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    _tasks.removeWhere((task) => task.id == id);
    
    // Clear selection if deleted task was selected
    if (_selectedTask?.id == id) {
      _selectedTask = null;
    }
    
    notifyListeners();
  }

  bool _isTaskFormValid(String title, String projectId) {
    return title.isNotEmpty && title.trim().length >= 3 && projectId.isNotEmpty;
  }

  void _setState(TaskViewState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = TaskViewState.error;
    _errorMessage = error;
    notifyListeners();
  }

  void clearError() {
    if (_state == TaskViewState.error) {
      _setState(_tasks.isNotEmpty ? TaskViewState.loaded : TaskViewState.initial);
    }
  }
} 
import 'package:flutter/foundation.dart';
import '../../domain/entities/task.dart';

enum TaskProviderState {
  initial,
  loading,
  loaded,
  error,
}

class TaskProvider extends ChangeNotifier {
  TaskProviderState _state = TaskProviderState.initial;
  List<Task> _tasks = [];
  String? _errorMessage;

  TaskProviderState get state => _state;
  List<Task> get tasks => List.unmodifiable(_tasks);
  String? get errorMessage => _errorMessage;

  List<Task> getTasksForProject(String projectId) {
    return _tasks.where((task) => task.projectId == projectId).toList();
  }

  Future<void> loadTasks({String? projectId}) async {
    _setState(TaskProviderState.loading);
    
    try {
      // TODO: Implement actual API call with repository
      await Future.delayed(const Duration(seconds: 1)); // Simulate API call
      
      _tasks = [
        Task(
          id: '1',
          title: 'Implement login screen',
          description: 'Create a beautiful login screen with validation',
          status: TaskStatus.completed,
          priority: TaskPriority.high,
          projectId: '1',
          assigneeId: '1',
          dueDate: DateTime.now().add(const Duration(days: 2)),
          createdAt: DateTime.now().subtract(const Duration(days: 5)),
        ),
        Task(
          id: '2',
          title: 'Setup database',
          description: 'Configure PostgreSQL database',
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
          description: 'Document all API endpoints',
          status: TaskStatus.todo,
          priority: TaskPriority.low,
          projectId: '1',
          dueDate: DateTime.now().add(const Duration(days: 10)),
          createdAt: DateTime.now().subtract(const Duration(days: 1)),
        ),
        Task(
          id: '4',
          title: 'Design user interface',
          description: 'Create mockups for all screens',
          status: TaskStatus.todo,
          priority: TaskPriority.high,
          projectId: '2',
          dueDate: DateTime.now().add(const Duration(days: 7)),
          createdAt: DateTime.now().subtract(const Duration(days: 2)),
        ),
      ];
      
      _setState(TaskProviderState.loaded);
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
    try {
      // TODO: Implement actual API call with repository
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
    } catch (e) {
      _setError(e.toString());
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
      // TODO: Implement actual API call with repository
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
        notifyListeners();
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> deleteTask(String id) async {
    try {
      // TODO: Implement actual API call with repository
      await Future.delayed(const Duration(milliseconds: 500));
      
      _tasks.removeWhere((task) => task.id == id);
      notifyListeners();
    } catch (e) {
      _setError(e.toString());
    }
  }

  Task? getTask(String id) {
    try {
      return _tasks.firstWhere((task) => task.id == id);
    } catch (e) {
      return null;
    }
  }

  void _setState(TaskProviderState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = TaskProviderState.error;
    _errorMessage = error;
    notifyListeners();
  }

  void clearError() {
    if (_state == TaskProviderState.error) {
      _setState(_tasks.isNotEmpty ? TaskProviderState.loaded : TaskProviderState.initial);
    }
  }
} 
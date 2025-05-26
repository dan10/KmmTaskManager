import 'package:task_manager_shared/models.dart';

import '../../lib/data/repositories/task_repository.dart';

class MockTaskRepository implements TaskRepository {
  bool _shouldThrowError = false;
  bool _delayResponse = false;
  
  // Response configurations
  PaginatedResponse<TaskDto>? _getTasksResponse;
  TaskDto? _getTaskResponse;
  TaskDto? _createTaskResponse;
  TaskDto? _updateTaskResponse;
  bool _deleteTaskSuccess = false;
  TaskDto? _changeTaskStatusResponse;
  TaskDto? _assignTaskResponse;
  
  // Call tracking
  int getTasksCallCount = 0;
  int? lastGetTasksPage;
  int? lastGetTasksSize;
  String? lastGetTasksQuery;
  String? lastGetTasksProjectId;
  
  String? lastGetTaskId;
  TaskCreateRequestDto? lastCreateRequest;
  String? lastUpdateTaskId;
  TaskUpdateRequestDto? lastUpdateRequest;
  String? lastDeleteTaskId;
  String? lastStatusChangeTaskId;
  TaskStatus? lastStatusChangeStatus;
  String? lastAssignTaskId;
  String? lastAssigneeId;

  // Configuration methods
  void setShouldThrowError(bool shouldThrow) {
    _shouldThrowError = shouldThrow;
  }

  void setDelayResponse(bool delay) {
    _delayResponse = delay;
  }

  void setGetTasksResponse(PaginatedResponse<TaskDto> response) {
    _getTasksResponse = response;
  }

  void setGetTaskResponse(TaskDto response) {
    _getTaskResponse = response;
  }

  void setCreateTaskResponse(TaskDto response) {
    _createTaskResponse = response;
  }

  void setUpdateTaskResponse(TaskDto response) {
    _updateTaskResponse = response;
  }

  void setDeleteTaskSuccess(bool success) {
    _deleteTaskSuccess = success;
  }

  void setChangeTaskStatusResponse(TaskDto response) {
    _changeTaskStatusResponse = response;
  }

  void setAssignTaskResponse(TaskDto response) {
    _assignTaskResponse = response;
  }

  void reset() {
    _shouldThrowError = false;
    _delayResponse = false;
    _getTasksResponse = null;
    _getTaskResponse = null;
    _createTaskResponse = null;
    _updateTaskResponse = null;
    _deleteTaskSuccess = false;
    _changeTaskStatusResponse = null;
    _assignTaskResponse = null;
    
    getTasksCallCount = 0;
    lastGetTasksPage = null;
    lastGetTasksSize = null;
    lastGetTasksQuery = null;
    lastGetTasksProjectId = null;
    lastGetTaskId = null;
    lastCreateRequest = null;
    lastUpdateTaskId = null;
    lastUpdateRequest = null;
    lastDeleteTaskId = null;
    lastStatusChangeTaskId = null;
    lastStatusChangeStatus = null;
    lastAssignTaskId = null;
    lastAssigneeId = null;
  }

  Future<void> _simulateDelay() async {
    if (_delayResponse) {
      await Future.delayed(const Duration(milliseconds: 100));
    }
  }

  @override
  Future<PaginatedResponse<TaskDto>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  }) async {
    getTasksCallCount++;
    lastGetTasksPage = page;
    lastGetTasksSize = size;
    lastGetTasksQuery = query;
    lastGetTasksProjectId = projectId;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to load tasks: Mock error');
    }

    return _getTasksResponse ?? PaginatedResponse<TaskDto>(
      items: [],
      total: 0,
      page: page,
      size: size,
      totalPages: 0,
    );
  }

  @override
  Future<TaskDto> getTask(String id) async {
    lastGetTaskId = id;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to load task: Mock error');
    }

    return _getTaskResponse ?? TaskDto(
      id: id,
      title: 'Mock Task',
      description: 'Mock Description',
      status: TaskStatus.todo,
      priority: Priority.medium,
      creatorId: 'mock-user',
    );
  }

  @override
  Future<TaskDto> createTask(TaskCreateRequestDto request) async {
    lastCreateRequest = request;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to create task: Mock error');
    }

    return _createTaskResponse ?? TaskDto(
      id: 'mock-id',
      title: request.title,
      description: request.description,
      status: TaskStatus.todo,
      priority: request.priority,
      dueDate: request.dueDate,
      projectId: request.projectId,
      assigneeId: request.assigneeId,
      creatorId: 'mock-user',
    );
  }

  @override
  Future<TaskDto> updateTask(String id, TaskUpdateRequestDto request) async {
    lastUpdateTaskId = id;
    lastUpdateRequest = request;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to update task: Mock error');
    }

    return _updateTaskResponse ?? TaskDto(
      id: id,
      title: request.title ?? 'Mock Task',
      description: request.description ?? 'Mock Description',
      status: request.status ?? TaskStatus.todo,
      priority: request.priority ?? Priority.medium,
      dueDate: request.dueDate,
      projectId: request.projectId,
      assigneeId: request.assigneeId,
      creatorId: 'mock-user',
    );
  }

  @override
  Future<void> deleteTask(String id) async {
    lastDeleteTaskId = id;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to delete task: Mock error');
    }

    if (!_deleteTaskSuccess) {
      throw Exception('Delete failed');
    }
  }

  @override
  Future<TaskDto> changeTaskStatus(String id, TaskStatus status) async {
    lastStatusChangeTaskId = id;
    lastStatusChangeStatus = status;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to change task status: Mock error');
    }

    return _changeTaskStatusResponse ?? TaskDto(
      id: id,
      title: 'Mock Task',
      description: 'Mock Description',
      status: status,
      priority: Priority.medium,
      creatorId: 'mock-user',
    );
  }

  @override
  Future<TaskDto> assignTask(String id, String assigneeId) async {
    lastAssignTaskId = id;
    lastAssigneeId = assigneeId;

    await _simulateDelay();

    if (_shouldThrowError) {
      throw Exception('Failed to assign task: Mock error');
    }

    return _assignTaskResponse ?? TaskDto(
      id: id,
      title: 'Mock Task',
      description: 'Mock Description',
      status: TaskStatus.todo,
      priority: Priority.medium,
      assigneeId: assigneeId,
      creatorId: 'mock-user',
    );
  }
} 
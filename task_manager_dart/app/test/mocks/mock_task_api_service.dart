import 'package:task_manager_shared/models.dart';

import '../../lib/data/services/task_api_service.dart';

class MockTaskApiService implements TaskApiService {
  bool _shouldThrowError = false;
  
  // Response configurations
  PaginatedResponse<TaskDto>? _getTasksResponse;
  TaskDto? _getTaskResponse;
  TaskDto? _createTaskResponse;
  TaskDto? _updateTaskResponse;
  bool _deleteTaskSuccess = false;
  TaskDto? _changeTaskStatusResponse;
  TaskDto? _assignTaskResponse;
  
  // Last call parameters for verification
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
  TaskStatusChangeRequestDto? lastStatusChangeRequest;
  String? lastAssignTaskId;
  TaskAssignRequestDto? lastAssignRequest;

  // Configuration methods
  void setShouldThrowError(bool shouldThrow) {
    _shouldThrowError = shouldThrow;
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
    _getTasksResponse = null;
    _getTaskResponse = null;
    _createTaskResponse = null;
    _updateTaskResponse = null;
    _deleteTaskSuccess = false;
    _changeTaskStatusResponse = null;
    _assignTaskResponse = null;
    
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
    lastStatusChangeRequest = null;
    lastAssignTaskId = null;
    lastAssignRequest = null;
  }

  @override
  Future<PaginatedResponse<TaskDto>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  }) async {
    lastGetTasksPage = page;
    lastGetTasksSize = size;
    lastGetTasksQuery = query;
    lastGetTasksProjectId = projectId;

    if (_shouldThrowError) {
      throw Exception('Mock API error');
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

    if (_shouldThrowError) {
      throw Exception('Mock API error');
    }

    return _getTaskResponse ?? TaskDto(
      id: id,
      title: 'Mock Task',
      description: 'Mock Description',
      status: TaskStatus.todo,
      priority: Priority.medium,
      creatorId: 'mock-user',
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
  }

  @override
  Future<TaskDto> createTask(TaskCreateRequestDto request) async {
    lastCreateRequest = request;

    if (_shouldThrowError) {
      throw Exception('Mock API error');
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
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
  }

  @override
  Future<TaskDto> updateTask(String id, TaskUpdateRequestDto request) async {
    lastUpdateTaskId = id;
    lastUpdateRequest = request;

    if (_shouldThrowError) {
      throw Exception('Mock API error');
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
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
  }

  @override
  Future<void> deleteTask(String id) async {
    lastDeleteTaskId = id;

    if (_shouldThrowError) {
      throw Exception('Mock API error');
    }

    if (!_deleteTaskSuccess) {
      throw Exception('Delete failed');
    }
  }

  @override
  Future<TaskDto> changeTaskStatus(String id, TaskStatusChangeRequestDto request) async {
    lastStatusChangeTaskId = id;
    lastStatusChangeRequest = request;

    if (_shouldThrowError) {
      throw Exception('Mock API error');
    }

    return _changeTaskStatusResponse ?? TaskDto(
      id: id,
      title: 'Mock Task',
      description: 'Mock Description',
      status: request.status,
      priority: Priority.medium,
      creatorId: 'mock-user',
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
  }

  @override
  Future<TaskDto> assignTask(String id, TaskAssignRequestDto request) async {
    lastAssignTaskId = id;
    lastAssignRequest = request;

    if (_shouldThrowError) {
      throw Exception('Mock API error');
    }

    return _assignTaskResponse ?? TaskDto(
      id: id,
      title: 'Mock Task',
      description: 'Mock Description',
      status: TaskStatus.todo,
      priority: Priority.medium,
      assigneeId: request.assigneeId,
      creatorId: 'mock-user',
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
  }
} 
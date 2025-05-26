import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:task_manager_shared/models.dart';

import '../../core/constants/api_constants.dart';
import '../sources/local/secure_storage.dart';

abstract class TaskApiService {
  Future<PaginatedResponse<TaskDto>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  });
  
  Future<TaskDto> getTask(String id);
  Future<TaskDto> createTask(TaskCreateRequestDto request);
  Future<TaskDto> updateTask(String id, TaskUpdateRequestDto request);
  Future<void> deleteTask(String id);
  Future<TaskDto> changeTaskStatus(String id, TaskStatusChangeRequestDto request);
  Future<TaskDto> assignTask(String id, TaskAssignRequestDto request);
}

class TaskApiServiceImpl implements TaskApiService {
  final SecureStorage _secureStorage;

  TaskApiServiceImpl(this._secureStorage);

  Future<Map<String, String>> _getHeaders() async {
    final token = await _secureStorage.getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  @override
  Future<PaginatedResponse<TaskDto>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  }) async {
    final headers = await _getHeaders();
    
    final queryParams = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
      if (query != null && query.isNotEmpty) 'query': query,
      if (projectId != null) 'projectId': projectId,
    };
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}')
        .replace(queryParameters: queryParams);
    
    final response = await http.get(uri, headers: headers);
    
    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return PaginatedResponse<TaskDto>.fromJson(
        jsonData,
        (item) => TaskDto.fromJson(item as Map<String, dynamic>),
      );
    } else {
      throw Exception('Failed to load tasks: ${response.statusCode}');
    }
  }

  @override
  Future<TaskDto> getTask(String id) async {
    final headers = await _getHeaders();
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}/$id');
    
    final response = await http.get(uri, headers: headers);
    
    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return TaskDto.fromJson(jsonData);
    } else if (response.statusCode == 404) {
      throw Exception('Task not found');
    } else {
      throw Exception('Failed to load task: ${response.statusCode}');
    }
  }

  @override
  Future<TaskDto> createTask(TaskCreateRequestDto request) async {
    final headers = await _getHeaders();
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}');
    
    final response = await http.post(
      uri,
      headers: headers,
      body: json.encode(request.toJson()),
    );
    
    if (response.statusCode == 201) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return TaskDto.fromJson(jsonData);
    } else {
      throw Exception('Failed to create task: ${response.statusCode}');
    }
  }

  @override
  Future<TaskDto> updateTask(String id, TaskUpdateRequestDto request) async {
    final headers = await _getHeaders();
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}/$id');
    
    final response = await http.put(
      uri,
      headers: headers,
      body: json.encode(request.toJson()),
    );
    
    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return TaskDto.fromJson(jsonData);
    } else if (response.statusCode == 404) {
      throw Exception('Task not found');
    } else {
      throw Exception('Failed to update task: ${response.statusCode}');
    }
  }

  @override
  Future<void> deleteTask(String id) async {
    final headers = await _getHeaders();
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}/$id');
    
    final response = await http.delete(uri, headers: headers);
    
    if (response.statusCode != 204) {
      if (response.statusCode == 404) {
        throw Exception('Task not found');
      } else {
        throw Exception('Failed to delete task: ${response.statusCode}');
      }
    }
  }

  @override
  Future<TaskDto> changeTaskStatus(String id, TaskStatusChangeRequestDto request) async {
    final headers = await _getHeaders();
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}/$id/status');
    
    final response = await http.patch(
      uri,
      headers: headers,
      body: json.encode(request.toJson()),
    );
    
    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return TaskDto.fromJson(jsonData);
    } else if (response.statusCode == 404) {
      throw Exception('Task not found');
    } else {
      throw Exception('Failed to change task status: ${response.statusCode}');
    }
  }

  @override
  Future<TaskDto> assignTask(String id, TaskAssignRequestDto request) async {
    final headers = await _getHeaders();
    
    final uri = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.tasksEndpoint}/$id/assign');
    
    final response = await http.patch(
      uri,
      headers: headers,
      body: json.encode(request.toJson()),
    );
    
    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return TaskDto.fromJson(jsonData);
    } else if (response.statusCode == 404) {
      throw Exception('Task not found');
    } else {
      throw Exception('Failed to assign task: ${response.statusCode}');
    }
  }
} 
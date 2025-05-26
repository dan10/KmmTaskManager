import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:task_manager_shared/models.dart';

import '../sources/local/secure_storage.dart';
import '../../core/constants/api_constants.dart';

class ProjectApiService {
  final SecureStorage _secureStorage;
  final String _baseUrl = ApiConstants.baseUrl;

  ProjectApiService(this._secureStorage);

  Future<Map<String, String>> _getHeaders() async {
    final token = await _secureStorage.getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  // Get projects with pagination and search
  Future<PaginatedResponse<ProjectResponseDto>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    final headers = await _getHeaders();
    
    final queryParams = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
      if (query != null && query.isNotEmpty) 'query': query,
    };

    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}')
        .replace(queryParameters: queryParams);

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      
      return PaginatedResponse<ProjectResponseDto>(
        items: (jsonData['items'] as List)
            .map((item) => ProjectResponseDto.fromJson(item as Map<String, dynamic>))
            .toList(),
        page: jsonData['page'] as int,
        size: jsonData['size'] as int,
        total: jsonData['total'] as int,
        totalPages: jsonData['totalPages'] as int,
      );
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to fetch projects');
    }
  }

  // Get single project by ID
  Future<ProjectResponseDto> getProject(String projectId) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}/$projectId');

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return ProjectResponseDto.fromJson(jsonData);
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to fetch project');
    }
  }

  // Create new project
  Future<ProjectResponseDto> createProject(CreateProjectRequestDto request) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}');

    final response = await http.post(
      uri,
      headers: headers,
      body: json.encode(request.toJson()),
    );

    if (response.statusCode == 201) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return ProjectResponseDto.fromJson(jsonData);
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to create project');
    }
  }

  // Update existing project
  Future<ProjectResponseDto> updateProject(String projectId, ProjectUpdateRequestDto request) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}/$projectId');

    final response = await http.put(
      uri,
      headers: headers,
      body: json.encode(request.toJson()),
    );

    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return ProjectResponseDto.fromJson(jsonData);
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to update project');
    }
  }

  // Delete project
  Future<void> deleteProject(String projectId) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}/$projectId');

    final response = await http.delete(uri, headers: headers);

    if (response.statusCode != 204) {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to delete project');
    }
  }

  // Add member to project
  Future<ProjectResponseDto> addMember(String projectId, String userId) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}/$projectId/members');

    final response = await http.post(
      uri,
      headers: headers,
      body: json.encode({'userId': userId}),
    );

    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return ProjectResponseDto.fromJson(jsonData);
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to add member');
    }
  }

  // Remove member from project
  Future<ProjectResponseDto> removeMember(String projectId, String userId) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}/$projectId/members/$userId');

    final response = await http.delete(uri, headers: headers);

    if (response.statusCode == 200) {
      final jsonData = json.decode(response.body) as Map<String, dynamic>;
      return ProjectResponseDto.fromJson(jsonData);
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to remove member');
    }
  }

  // Get project statistics
  Future<Map<String, dynamic>> getProjectStats(String projectId) async {
    final headers = await _getHeaders();
    final uri = Uri.parse('$_baseUrl${ApiConstants.projectsEndpoint}/$projectId/stats');

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      return json.decode(response.body) as Map<String, dynamic>;
    } else {
      final errorData = json.decode(response.body) as Map<String, dynamic>;
      throw Exception(errorData['message'] ?? 'Failed to fetch project statistics');
    }
  }
} 
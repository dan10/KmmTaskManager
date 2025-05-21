import 'package:shared/models.dart' as shared_models;
import '../repositories/project_repository.dart';
import '../dto/user_public_response_dto.dart';
import '../exceptions/custom_exceptions.dart'; // Import new exceptions

abstract class ProjectService {
  Future<List<shared_models.Project>> getAllProjects(String userId, int page, int size, String? query);
  Future<List<shared_models.Project>> getAllSystemProjects(int page, int size, String? query);
  Future<shared_models.Project?> getProjectById(String id, String userId);
  Future<shared_models.Project> createProject(shared_models.Project project);
  Future<shared_models.Project> updateProject(
      String id, shared_models.Project project, String userId);
  Future<void> deleteProject(String id, String userId);
  Future<Map<String, String>> assignUserToProject(String projectId, String userId);
  Future<bool> removeUserFromProject(String projectId, String userId);
  Future<List<UserPublicResponseDto>> getUsersByProject(String projectId);
  Future<List<shared_models.Project>> getProjectsByUser(String userId);
}

// Old custom exceptions removed from here. They are now in custom_exceptions.dart


class ProjectServiceImpl implements ProjectService {
  final ProjectRepository _repository;

  ProjectServiceImpl(this._repository);

  @override
  Future<List<shared_models.Project>> getAllProjects(String userId, int page, int size, String? query) async {
    return _repository.getAllProjects(userId, page, size, query);
  }

  @override
  Future<List<shared_models.Project>> getAllSystemProjects(int page, int size, String? query) async {
    return _repository.getAllSystemProjects(page, size, query);
  }

  @override
  Future<shared_models.Project?> getProjectById(String id, String userId) async {
    final project = await _repository.findById(id);
    // TODO: Ensure this creatorId check is appropriate for all uses of getProjectById.
    // If a user who is only a member (not creator) should be able to get project by ID, this needs adjustment.
    // For now, assuming only creator or via specific member-related endpoints.
    if (project == null || project.creatorId != userId && !project.memberIds.contains(userId)) {
      // Allowing member to fetch the project as well
      return null;
    }
    return project;
  }

  @override
  Future<shared_models.Project> createProject(shared_models.Project project) async {
    return _repository.create(project);
  }

  @override
  Future<shared_models.Project> updateProject(
      String id, shared_models.Project project, String userId) async {
    final existingProject = await _repository.findById(id);
    if (existingProject == null) {
      throw ProjectNotFoundException(id: id);
    }
    if (existingProject.creatorId != userId) {
      // Consider if members should be able to update - likely not for general update.
      throw ForbiddenException(message: 'User not authorized to update project $id.');
    }
    return _repository.update(project);
  }

  @override
  Future<void> deleteProject(String id, String userId) async {
    final project = await _repository.findById(id);
    if (project == null) {
      throw ProjectNotFoundException(id: id);
    }
    if (project.creatorId != userId) {
      // Only creator can delete.
      throw ForbiddenException(message: 'User not authorized to delete project $id.');
    }
    await _repository.delete(id);
  }

  @override
  Future<Map<String, String>> assignUserToProject(String projectId, String userId) async {
    // Repository now throws ProjectNotFoundException, UserNotFoundException, AlreadyAssignedException
    return _repository.assignUserToProject(projectId, userId);
    // No need to check for null and throw generic Exception if repo handles it.
  }

  @override
  Future<bool> removeUserFromProject(String projectId, String userId) async {
    return _repository.removeUserFromProject(projectId, userId);
  }

  @override
  Future<List<UserPublicResponseDto>> getUsersByProject(String projectId) async {
    // Repository throws ProjectNotFoundException if project doesn't exist.
    final users = await _repository.getUsersByProject(projectId);
    return users.map((user) => UserPublicResponseDto(
      id: user.id,
      name: user.name,
      email: user.email,
    )).toList();
  }

  @override
  Future<List<shared_models.Project>> getProjectsByUser(String userId) async {
    // Repository throws UserNotFoundException if user doesn't exist.
    return _repository.getProjectsByUser(userId);
  }
}

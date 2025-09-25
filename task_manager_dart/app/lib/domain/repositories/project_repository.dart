import '../entities/project.dart';

abstract class ProjectRepository {
  Future<List<Project>> getProjects();
  Future<Project> getProject(String id);
  Future<Project> createProject(String name, String? description);
  Future<Project> updateProject(String id, String name, String? description);
  Future<void> deleteProject(String id);
} 
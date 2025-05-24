import 'package:flutter/foundation.dart';
import '../../domain/entities/project.dart';
import '../../domain/repositories/project_repository.dart';

enum ProjectViewState {
  initial,
  loading,
  loaded,
  error,
}

class ProjectViewModel extends ChangeNotifier {
  // TODO: Inject repository when data layer is implemented
  // final ProjectRepository _projectRepository;
  
  ProjectViewState _state = ProjectViewState.initial;
  List<Project> _projects = [];
  Project? _selectedProject;
  String? _errorMessage;

  ProjectViewState get state => _state;
  List<Project> get projects => List.unmodifiable(_projects);
  Project? get selectedProject => _selectedProject;
  String? get errorMessage => _errorMessage;
  bool get isLoading => _state == ProjectViewState.loading;
  bool get hasProjects => _projects.isNotEmpty;

  // TODO: Constructor will accept repository when implemented
  // ProjectViewModel(this._projectRepository);

  Future<void> loadProjects() async {
    _setState(ProjectViewState.loading);
    
    try {
      // TODO: Replace with actual repository call
      await _simulateLoadProjects();
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> createProject(String name, String? description) async {
    if (_isProjectFormValid(name)) {
      try {
        // TODO: Replace with actual repository call
        await _simulateCreateProject(name, description);
      } catch (e) {
        _setError(e.toString());
      }
    } else {
      _setError('Project name is required');
    }
  }

  Future<void> updateProject(String id, String name, String? description) async {
    if (_isProjectFormValid(name)) {
      try {
        // TODO: Replace with actual repository call
        await _simulateUpdateProject(id, name, description);
      } catch (e) {
        _setError(e.toString());
      }
    } else {
      _setError('Project name is required');
    }
  }

  Future<void> deleteProject(String id) async {
    try {
      // TODO: Replace with actual repository call
      await _simulateDeleteProject(id);
    } catch (e) {
      _setError(e.toString());
    }
  }

  void selectProject(String id) {
    _selectedProject = _projects.where((p) => p.id == id).firstOrNull;
    notifyListeners();
  }

  void clearSelection() {
    _selectedProject = null;
    notifyListeners();
  }

  Project? getProject(String id) {
    try {
      return _projects.where((project) => project.id == id).first;
    } catch (e) {
      return null;
    }
  }

  // TODO: Remove simulation methods when repository is implemented
  Future<void> _simulateLoadProjects() async {
    await Future.delayed(const Duration(seconds: 1));
    
    _projects = [
      Project(
        id: '1',
        name: 'Mobile App Development',
        description: 'Flutter task manager application',
        ownerId: '1',
        createdAt: DateTime.now().subtract(const Duration(days: 7)),
        taskCount: 5,
      ),
      Project(
        id: '2',
        name: 'Web Dashboard',
        description: 'Admin dashboard for task management',
        ownerId: '1',
        createdAt: DateTime.now().subtract(const Duration(days: 3)),
        taskCount: 2,
      ),
      Project(
        id: '3',
        name: 'API Development',
        description: 'REST API for task manager backend',
        ownerId: '1',
        createdAt: DateTime.now().subtract(const Duration(days: 1)),
        taskCount: 8,
      ),
    ];
    
    _setState(ProjectViewState.loaded);
  }

  Future<void> _simulateCreateProject(String name, String? description) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    final newProject = Project(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      name: name,
      description: description,
      ownerId: '1', // TODO: Get from auth context
      createdAt: DateTime.now(),
    );
    
    _projects.add(newProject);
    notifyListeners();
  }

  Future<void> _simulateUpdateProject(String id, String name, String? description) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    final index = _projects.indexWhere((project) => project.id == id);
    if (index != -1) {
      _projects[index] = _projects[index].copyWith(
        name: name,
        description: description,
        updatedAt: DateTime.now(),
      );
      
      // Update selected project if it's the one being updated
      if (_selectedProject?.id == id) {
        _selectedProject = _projects[index];
      }
      
      notifyListeners();
    }
  }

  Future<void> _simulateDeleteProject(String id) async {
    await Future.delayed(const Duration(milliseconds: 500));
    
    _projects.removeWhere((project) => project.id == id);
    
    // Clear selection if deleted project was selected
    if (_selectedProject?.id == id) {
      _selectedProject = null;
    }
    
    notifyListeners();
  }

  bool _isProjectFormValid(String name) {
    return name.isNotEmpty && name.trim().length >= 3;
  }

  void _setState(ProjectViewState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = ProjectViewState.error;
    _errorMessage = error;
    notifyListeners();
  }

  void clearError() {
    if (_state == ProjectViewState.error) {
      _setState(_projects.isNotEmpty ? ProjectViewState.loaded : ProjectViewState.initial);
    }
  }
} 
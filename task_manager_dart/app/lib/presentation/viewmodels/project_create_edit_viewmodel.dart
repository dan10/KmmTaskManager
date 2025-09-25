import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/project_repository.dart';

enum ProjectCreateEditState {
  initial,
  loading,
  loaded,
  saving,
  saved,
  deleting,
  deleted,
  error,
}

class ProjectCreateEditViewModel extends ChangeNotifier {
  final ProjectRepository _projectRepository;

  ProjectCreateEditState _state = ProjectCreateEditState.initial;
  Project? _project;
  String? _errorMessage;
  
  // Form fields
  String _name = '';
  String _description = '';
  List<String> _memberIds = [];

  // Getters
  ProjectCreateEditState get state => _state;
  Project? get project => _project;
  String? get errorMessage => _errorMessage;
  
  // Form field getters
  String get name => _name;
  String get description => _description;
  List<String> get memberIds => List.unmodifiable(_memberIds);
  
  // State checks
  bool get isLoading => _state == ProjectCreateEditState.loading;
  bool get isSaving => _state == ProjectCreateEditState.saving;
  bool get isDeleting => _state == ProjectCreateEditState.deleting;
  bool get hasProject => _project != null;
  bool get isEditing => _project != null;
  bool get isCreating => _project == null;
  
  // Validation
  bool get isValid => _name.trim().isNotEmpty;
  bool get hasChanges => _hasChanges();

  ProjectCreateEditViewModel(this._projectRepository);

  // Initialize for creating a new project
  void initializeForCreate() {
    _setState(ProjectCreateEditState.initial);
    _project = null;
    _clearForm();
    notifyListeners();
  }

  // Initialize for editing an existing project
  Future<void> initializeForEdit(String projectId) async {
    _setState(ProjectCreateEditState.loading);
    
    try {
      final project = await _projectRepository.getProject(projectId);
      _project = project;
      _populateFormFromProject(project);
      _setState(ProjectCreateEditState.loaded);
    } catch (e) {
      _setError('Failed to load project: ${e.toString()}');
    }
  }

  // Update form fields
  void updateName(String name) {
    _name = name;
    notifyListeners();
  }

  void updateDescription(String description) {
    _description = description;
    notifyListeners();
  }

  void updateMemberIds(List<String> memberIds) {
    _memberIds = List.from(memberIds);
    notifyListeners();
  }

  void addMember(String userId) {
    if (!_memberIds.contains(userId)) {
      _memberIds.add(userId);
      notifyListeners();
    }
  }

  void removeMember(String userId) {
    _memberIds.remove(userId);
    notifyListeners();
  }

  // Save project (create or update)
  Future<bool> saveProject() async {
    if (!isValid) {
      _setError('Please fill in all required fields');
      return false;
    }

    _setState(ProjectCreateEditState.saving);

    try {
      if (isCreating) {
        await _createProject();
      } else {
        await _updateProject();
      }
      
      _setState(ProjectCreateEditState.saved);
      return true;
    } catch (e) {
      _setError(isCreating 
          ? 'Failed to create project: ${e.toString()}'
          : 'Failed to update project: ${e.toString()}');
      return false;
    }
  }

  // Delete project
  Future<bool> deleteProject() async {
    if (_project == null) return false;

    _setState(ProjectCreateEditState.deleting);

    try {
      await _projectRepository.deleteProject(_project!.id);
      _setState(ProjectCreateEditState.deleted);
      return true;
    } catch (e) {
      _setError('Failed to delete project: ${e.toString()}');
      return false;
    }
  }

  // Clear error
  void clearError() {
    _errorMessage = null;
    if (_state == ProjectCreateEditState.error) {
      _setState(_project != null ? ProjectCreateEditState.loaded : ProjectCreateEditState.initial);
    }
  }

  // Reset form
  void reset() {
    _setState(ProjectCreateEditState.initial);
    _project = null;
    _clearForm();
    notifyListeners();
  }

  // Validation methods
  String? validateName(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Project name is required';
    }
    if (value.trim().length < 3) {
      return 'Project name must be at least 3 characters';
    }
    if (value.trim().length > 100) {
      return 'Project name cannot exceed 100 characters';
    }
    return null;
  }

  String? validateDescription(String? value) {
    if (value != null && value.trim().isNotEmpty) {
      if (value.trim().length > 500) {
        return 'Description cannot exceed 500 characters';
      }
    }
    return null;
  }

  // Get form validation errors
  Map<String, String> getValidationErrors() {
    final errors = <String, String>{};
    
    final nameError = validateName(_name);
    if (nameError != null) {
      errors['name'] = nameError;
    }
    
    final descriptionError = validateDescription(_description);
    if (descriptionError != null) {
      errors['description'] = descriptionError;
    }
    
    return errors;
  }

  // Private methods
  void _setState(ProjectCreateEditState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _setState(ProjectCreateEditState.error);
  }

  void _clearForm() {
    _name = '';
    _description = '';
    _memberIds = [];
    _errorMessage = null;
  }

  void _populateFormFromProject(Project project) {
    _name = project.name;
    _description = project.description ?? '';
    _memberIds = List.from(project.memberIds);
  }

  Future<void> _createProject() async {
    final request = CreateProjectRequestDto(
      name: _name.trim(),
      description: _description.trim().isEmpty ? null : _description.trim(),
    );

    final createdProject = await _projectRepository.createProject(request);
    _project = createdProject;
    
    // If members were added during creation, update the project
    if (_memberIds.isNotEmpty) {
      final updateRequest = ProjectUpdateRequestDto(
        memberIds: _memberIds,
      );
      _project = await _projectRepository.updateProject(_project!.id, updateRequest);
    }
  }

  Future<void> _updateProject() async {
    if (_project == null) return;

    final request = ProjectUpdateRequestDto(
      name: _name.trim() != _project!.name ? _name.trim() : null,
      description: _description.trim() != (_project!.description ?? '') 
          ? (_description.trim().isEmpty ? null : _description.trim()) 
          : null,
      memberIds: !_listEquals(_memberIds, _project!.memberIds) ? _memberIds : null,
    );

    // Only update if there are actual changes
    if (request.hasUpdates) {
      final updatedProject = await _projectRepository.updateProject(_project!.id, request);
      _project = updatedProject;
    }
  }

  bool _hasChanges() {
    if (_project == null) {
      return _name.trim().isNotEmpty || 
             _description.trim().isNotEmpty || 
             _memberIds.isNotEmpty;
    }

    return _name.trim() != _project!.name ||
           _description.trim() != (_project!.description ?? '') ||
           !_listEquals(_memberIds, _project!.memberIds);
  }

  bool _listEquals(List<String> a, List<String> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
} 
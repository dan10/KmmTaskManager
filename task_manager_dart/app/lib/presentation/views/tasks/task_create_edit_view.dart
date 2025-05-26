import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:task_manager_shared/models.dart';

import '../../viewmodels/task_create_edit_viewmodel.dart';
import '../../viewmodels/project_viewmodel.dart';

class TaskCreateEditView extends StatefulWidget {
  final String? taskId;
  final String? projectId;

  const TaskCreateEditView({
    super.key,
    this.taskId,
    this.projectId,
  });

  @override
  State<TaskCreateEditView> createState() => _TaskCreateEditViewState();
}

class _TaskCreateEditViewState extends State<TaskCreateEditView> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  
  Priority _selectedPriority = Priority.medium;
  DateTime? _selectedDueDate;
  String? _selectedProjectId;
  String? _selectedAssigneeId;

  bool get _isCreating => widget.taskId == null;

  @override
  void initState() {
    super.initState();
    _selectedProjectId = widget.projectId;
    
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final viewModel = Provider.of<TaskCreateEditViewModel>(context, listen: false);
      if (_isCreating) {
        viewModel.initializeForCreate(projectId: widget.projectId);
      } else {
        viewModel.initializeForEdit(widget.taskId!);
      }
    });
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Consumer<TaskCreateEditViewModel>(
      builder: (context, viewModel, child) {
        // Update form fields when task is loaded for editing
        if (viewModel.isEditing && viewModel.task != null) {
          _updateFormFromViewModel(viewModel);
        }

        return Scaffold(
          backgroundColor: const Color(0xFFF1F5F9),
          appBar: AppBar(
            title: Text(_isCreating ? l10n.createTask : l10n.editTask),
            backgroundColor: const Color(0xFFF1F5F9),
            elevation: 0,
            actions: [
              if (!_isCreating)
                IconButton(
                  icon: const Icon(Icons.delete, color: Colors.red),
                  onPressed: () => _showDeleteConfirmation(context, viewModel),
                ),
            ],
          ),
          body: _buildBody(context, viewModel, l10n),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, TaskCreateEditViewModel viewModel, AppLocalizations l10n) {
    if (viewModel.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (viewModel.state == TaskCreateEditState.error) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 16),
            Text(
              viewModel.errorMessage ?? 'An error occurred',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                viewModel.clearError();
                if (_isCreating) {
                  viewModel.initializeForCreate(projectId: widget.projectId);
                } else {
                  viewModel.initializeForEdit(widget.taskId!);
                }
              },
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Title Field
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.taskTitleLabel,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _titleController,
                      decoration: InputDecoration(
                        hintText: l10n.taskTitlePlaceholder,
                        errorText: viewModel.titleError,
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return l10n.taskTitleError;
                        }
                        if (value.trim().length < 3) {
                          return 'Title must be at least 3 characters';
                        }
                        if (value.trim().length > 100) {
                          return 'Title must be less than 100 characters';
                        }
                        return null;
                      },
                      onChanged: (value) => viewModel.updateTitle(value),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Description Field
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.taskDescriptionLabel,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _descriptionController,
                      decoration: InputDecoration(
                        hintText: l10n.taskDescriptionPlaceholder,
                        errorText: viewModel.descriptionError,
                      ),
                      maxLines: 4,
                      validator: (value) {
                        if (value != null && value.trim().isNotEmpty) {
                          if (value.trim().length < 10) {
                            return 'Description must be at least 10 characters';
                          }
                          if (value.trim().length > 500) {
                            return 'Description must be less than 500 characters';
                          }
                        }
                        return null;
                      },
                      onChanged: (value) => viewModel.updateDescription(value),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Priority and Due Date Row
            Row(
              children: [
                // Priority Field
                Expanded(
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            l10n.taskPriorityLabel,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 12),
                          DropdownButtonFormField<Priority>(
                            value: _selectedPriority,
                            decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                            ),
                            items: Priority.values.map((priority) {
                              return DropdownMenuItem(
                                value: priority,
                                child: Text(priority.name.toUpperCase()),
                              );
                            }).toList(),
                            onChanged: (value) {
                              if (value != null) {
                                setState(() {
                                  _selectedPriority = value;
                                });
                                viewModel.updatePriority(value);
                              }
                            },
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                
                const SizedBox(width: 16),
                
                // Due Date Field
                Expanded(
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            l10n.taskDueDateLabel,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 12),
                          InkWell(
                            onTap: () => _selectDueDate(context, viewModel),
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
                              decoration: BoxDecoration(
                                border: Border.all(color: Theme.of(context).colorScheme.outline),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    Icons.calendar_today,
                                    color: Theme.of(context).colorScheme.primary,
                                  ),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      _selectedDueDate != null
                                          ? _formatDate(_selectedDueDate!)
                                          : l10n.taskDueDatePlaceholder,
                                      style: TextStyle(
                                        color: _selectedDueDate != null
                                            ? Theme.of(context).colorScheme.onSurface
                                            : Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                                      ),
                                    ),
                                  ),
                                  if (_selectedDueDate != null)
                                    IconButton(
                                      icon: const Icon(Icons.clear),
                                      onPressed: () {
                                        setState(() {
                                          _selectedDueDate = null;
                                        });
                                        viewModel.updateDueDate(null);
                                      },
                                    ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // Project Field
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.taskProject,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Consumer<ProjectViewModel>(
                      builder: (context, projectViewModel, child) {
                        return DropdownButtonFormField<String>(
                          value: _selectedProjectId,
                          decoration: const InputDecoration(
                            border: OutlineInputBorder(),
                          ),
                          hint: Text(l10n.taskProjectHint),
                          items: projectViewModel.projects.map((project) {
                            return DropdownMenuItem(
                              value: project.id,
                              child: Text(project.name),
                            );
                          }).toList(),
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return l10n.taskProjectRequired;
                            }
                            return null;
                          },
                          onChanged: (value) {
                            setState(() {
                              _selectedProjectId = value;
                            });
                            viewModel.updateProjectId(value);
                          },
                        );
                      },
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 32),

            // Save Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: viewModel.isSaving ? null : () => _saveTask(context, viewModel, l10n),
                child: viewModel.isSaving
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Text(_isCreating ? l10n.createTask : l10n.saveChanges),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _updateFormFromViewModel(TaskCreateEditViewModel viewModel) {
    if (_titleController.text != viewModel.title) {
      _titleController.text = viewModel.title;
    }
    if (_descriptionController.text != viewModel.description) {
      _descriptionController.text = viewModel.description;
    }
    if (_selectedPriority != viewModel.priority) {
      _selectedPriority = viewModel.priority;
    }
    if (_selectedDueDate != viewModel.dueDate) {
      _selectedDueDate = viewModel.dueDate;
    }
    if (_selectedProjectId != viewModel.projectId) {
      _selectedProjectId = viewModel.projectId;
    }
    if (_selectedAssigneeId != viewModel.assigneeId) {
      _selectedAssigneeId = viewModel.assigneeId;
    }
  }

  Future<void> _selectDueDate(BuildContext context, TaskCreateEditViewModel viewModel) async {
    final date = await showDatePicker(
      context: context,
      initialDate: _selectedDueDate ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    
    if (date != null) {
      setState(() {
        _selectedDueDate = date;
      });
      viewModel.updateDueDate(date);
    }
  }

  Future<void> _saveTask(BuildContext context, TaskCreateEditViewModel viewModel, AppLocalizations l10n) async {
    if (_formKey.currentState?.validate() ?? false) {
      final success = await viewModel.saveTask();
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(_isCreating ? l10n.taskCreated : l10n.taskUpdated),
            backgroundColor: Colors.green,
          ),
        );
        context.pop();
      }
    }
  }

  void _showDeleteConfirmation(BuildContext context, TaskCreateEditViewModel viewModel) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Task'),
        content: const Text('Are you sure you want to delete this task? This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () async {
              Navigator.of(context).pop();
              final success = await viewModel.deleteTask();
              if (success && mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Task deleted successfully'),
                    backgroundColor: Colors.green,
                  ),
                );
                context.pop();
              }
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }
} 
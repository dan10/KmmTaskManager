import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../viewmodels/task_viewmodel.dart';
import '../../viewmodels/project_viewmodel.dart';
import '../../../domain/entities/task.dart';

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
  
  TaskPriority _selectedPriority = TaskPriority.medium;
  DateTime? _selectedDueDate;
  String? _selectedProjectId;
  bool _isLoading = false;
  String? _errorMessage;
  Task? _currentTask;

  bool get _isCreating => widget.taskId == null;
  bool get _isButtonEnabled => _titleController.text.isNotEmpty && !_isLoading && _selectedProjectId != null;

  @override
  void initState() {
    super.initState();
    _selectedProjectId = widget.projectId;
    
    if (!_isCreating) {
      _loadTask();
    }
    
    _titleController.addListener(_updateButtonState);
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _updateButtonState() {
    setState(() {});
  }

  void _loadTask() async {
    if (widget.taskId == null) return;

    final l10n = AppLocalizations.of(context)!;
    
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final taskViewModel = Provider.of<TaskViewModel>(context, listen: false);
      final task = taskViewModel.getTask(widget.taskId!);
      
      if (task != null) {
        setState(() {
          _currentTask = task;
          _titleController.text = task.title;
          _descriptionController.text = task.description ?? '';
          _selectedPriority = task.priority;
          _selectedDueDate = task.dueDate;
          _selectedProjectId = task.projectId;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = l10n.taskLoadError(e.toString());
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _buildAppBar(),
      body: _isLoading && _isCreating
          ? const Center(child: CircularProgressIndicator())
          : _buildBody(),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    final l10n = AppLocalizations.of(context)!;
    
    return AppBar(
      title: Text(_isCreating ? l10n.createTask : l10n.editTask),
      leading: IconButton(
        icon: const Icon(Icons.arrow_back),
        onPressed: () => context.go('/?tab=0'),
      ),
      actions: [
        if (!_isCreating)
          IconButton(
            icon: const Icon(Icons.delete),
            onPressed: _showDeleteConfirmation,
          ),
      ],
    );
  }

  Widget _buildBody() {
    return Column(
      children: [
        if (_errorMessage != null)
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: _buildErrorMessage(),
          ),
        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildTitleField(),
                  const SizedBox(height: 16),
                  _buildDescriptionField(),
                  const SizedBox(height: 16),
                  _buildPriorityField(),
                  const SizedBox(height: 16),
                  _buildDueDateField(),
                  if (_isCreating) ...[
                    const SizedBox(height: 16),
                    _buildProjectField(),
                  ],
                  const SizedBox(height: 100), // Extra space for keyboard
                ],
              ),
            ),
          ),
        ),
        _buildActionButtons(),
      ],
    );
  }

  Widget _buildErrorMessage() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.errorContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        _errorMessage!,
        style: TextStyle(
          color: Theme.of(context).colorScheme.onErrorContainer,
          fontSize: 14,
        ),
      ),
    );
  }

  Widget _buildTitleField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.taskTitleLabel,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        TextFormField(
          controller: _titleController,
          enabled: !_isLoading,
          decoration: InputDecoration(
            hintText: l10n.taskTitlePlaceholder,
            filled: true,
            fillColor: Theme.of(context).colorScheme.surface,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide.none,
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide(
                color: Theme.of(context).colorScheme.primary,
                width: 2,
              ),
            ),
            errorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide(
                color: Theme.of(context).colorScheme.error,
                width: 2,
              ),
            ),
          ),
          validator: (value) {
            if (value == null || value.trim().isEmpty) {
              return l10n.taskTitleError;
            }
            return null;
          },
          maxLines: 1,
        ),
      ],
    );
  }

  Widget _buildDescriptionField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.taskDescriptionLabel,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        TextFormField(
          controller: _descriptionController,
          enabled: !_isLoading,
          decoration: InputDecoration(
            hintText: l10n.taskDescriptionPlaceholder,
            filled: true,
            fillColor: Theme.of(context).colorScheme.surface,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide.none,
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide(
                color: Theme.of(context).colorScheme.primary,
                width: 2,
              ),
            ),
          ),
          maxLines: 3,
          minLines: 3,
        ),
      ],
    );
  }

  Widget _buildPriorityField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.taskPriorityLabel,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(
              color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
            ),
          ),
          child: DropdownButtonHideUnderline(
            child: DropdownButton<TaskPriority>(
              value: _selectedPriority,
              isExpanded: true,
              icon: const Icon(Icons.arrow_drop_down),
              onChanged: _isLoading ? null : (TaskPriority? newValue) {
                if (newValue != null) {
                  setState(() {
                    _selectedPriority = newValue;
                  });
                }
              },
              items: TaskPriority.values.map((TaskPriority priority) {
                return DropdownMenuItem<TaskPriority>(
                  value: priority,
                  child: Text(priority.name.toUpperCase()),
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildDueDateField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.taskDueDateLabel,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        InkWell(
          onTap: _isLoading ? null : _selectDueDate,
          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.surface,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
              ),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    _selectedDueDate != null
                        ? '${_selectedDueDate!.day}/${_selectedDueDate!.month}/${_selectedDueDate!.year}'
                        : l10n.taskDueDatePlaceholder,
                    style: TextStyle(
                      color: _selectedDueDate != null
                          ? Theme.of(context).colorScheme.onSurface
                          : Theme.of(context).colorScheme.onSurface.withOpacity(0.5),
                    ),
                  ),
                ),
                Icon(
                  Icons.calendar_today,
                  color: Theme.of(context).colorScheme.onSurface.withOpacity(0.5),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildProjectField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.taskProjectLabel,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        Consumer<ProjectViewModel>(
          builder: (context, projectViewModel, child) {
            return Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surface,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
                ),
              ),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  value: _selectedProjectId,
                  isExpanded: true,
                  hint: Text(l10n.taskProjectPlaceholder),
                  icon: const Icon(Icons.arrow_drop_down),
                  onChanged: _isLoading ? null : (String? newValue) {
                    setState(() {
                      _selectedProjectId = newValue;
                    });
                  },
                  items: projectViewModel.projects.map((project) {
                    return DropdownMenuItem<String>(
                      value: project.id,
                      child: Text(project.name),
                    );
                  }).toList(),
                ),
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _buildActionButtons() {
    final l10n = AppLocalizations.of(context)!;
    
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.only(bottom: 16),
        child: Row(
          children: [
            Expanded(
              child: OutlinedButton(
                onPressed: _isLoading ? null : () => context.go('/?tab=0'),
                style: OutlinedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                      vertical: 16, horizontal: 24),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: Text(l10n.taskCancelButton),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: ElevatedButton(
                onPressed: _isButtonEnabled ? _handleCreateOrUpdate : null,
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                      vertical: 16, horizontal: 24),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: _isLoading
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                        ),
                      )
                    : Text(
                  _isCreating ? l10n.taskCreateButton : l10n.taskUpdateButton,
                        style: const TextStyle(color: Colors.white),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _selectDueDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDueDate ?? DateTime.now().add(const Duration(days: 1)),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    
    if (picked != null && picked != _selectedDueDate) {
      setState(() {
        _selectedDueDate = picked;
      });
    }
  }

  void _handleCreateOrUpdate() async {
    final l10n = AppLocalizations.of(context)!;
    
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_selectedProjectId == null) {
      setState(() {
        _errorMessage = l10n.taskProjectRequired;
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final taskViewModel = Provider.of<TaskViewModel>(context, listen: false);
      
      if (_isCreating) {
        await taskViewModel.createTask(
          title: _titleController.text.trim(),
          description: _descriptionController.text.trim().isEmpty ? null : _descriptionController.text.trim(),
          priority: _selectedPriority,
          projectId: _selectedProjectId!,
          dueDate: _selectedDueDate,
        );
      } else {
        await taskViewModel.updateTask(
          widget.taskId!,
          title: _titleController.text.trim(),
          description: _descriptionController.text.trim().isEmpty ? null : _descriptionController.text.trim(),
          priority: _selectedPriority,
          dueDate: _selectedDueDate,
        );
      }
      
      if (mounted) {
        context.go('/?tab=0');
      }
    } catch (e) {
      setState(() {
        _errorMessage =
        _isCreating ? l10n.taskCreateError(e.toString()) : l10n.taskUpdateError(
            e.toString());
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  void _showDeleteConfirmation() {
    final l10n = AppLocalizations.of(context)!;
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.taskDeleteTitle),
        content: Text(l10n.taskDeleteMessage),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: Text(l10n.taskCancelButton),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () {
              Navigator.of(context).pop();
              _deleteTask();
            },
            child: Text(
              l10n.taskDeleteButton,
              style: TextStyle(
                color: Theme.of(context).colorScheme.onError,
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _deleteTask() async {
    final l10n = AppLocalizations.of(context)!;
    
    if (widget.taskId == null) return;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final taskViewModel = Provider.of<TaskViewModel>(context, listen: false);
      await taskViewModel.deleteTask(widget.taskId!);
      
      if (mounted) {
        context.go('/?tab=0');
      }
    } catch (e) {
      setState(() {
        _errorMessage = l10n.taskDeleteError(e.toString());
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }
} 
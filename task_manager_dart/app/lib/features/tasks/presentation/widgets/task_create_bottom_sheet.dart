import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/l10n/app_l10n.dart';
import '../../../../core/testing/test_ids.dart';
import '../../data/repositories/task_repository.dart';
import '../viewmodels/task_create_viewmodel.dart';

/// Bottom sheet for creating a new task
/// 
/// Matches KMM's TaskCreateBottomSheet behavior:
/// - Draggable sheet that opens to 75% height
/// - Form fields for title, description, priority, due date
/// - Dismisses and optionally triggers a refresh callback
/// - Uses TaskCreateViewModel for state management and validation
class TaskCreateBottomSheet extends StatefulWidget {
  const TaskCreateBottomSheet({
    super.key,
    required this.onDismiss,
    this.projectId,
  });

  /// Called when the sheet is dismissed
  /// 
  /// [shouldRefresh] is true if a task was created and the list should refresh
  final void Function(bool shouldRefresh) onDismiss;
  
  /// Optional project ID to associate the task with
  final String? projectId;

  @override
  State<TaskCreateBottomSheet> createState() => _TaskCreateBottomSheetState();
}

class _TaskCreateBottomSheetState extends State<TaskCreateBottomSheet> {
  late TaskCreateViewModel _viewModel;
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();

  @override
  void initState() {
    super.initState();
    
    // Get repository from context and create ViewModel
    final repository = context.read<TaskRepository>();
    _viewModel = TaskCreateViewModel(
      repository: repository,
      projectId: widget.projectId,
    );
    
    // Add listener for command completion
    _viewModel.createTask.addListener(_onCreateTaskResult);
    
    // Sync controllers with ViewModel
    _titleController.addListener(() {
      _viewModel.title.value = _titleController.text;
      _viewModel.validateForm();
    });
    
    _descriptionController.addListener(() {
      _viewModel.description.value = _descriptionController.text;
      _viewModel.validateForm();
    });
  }

  @override
  void dispose() {
    _viewModel.createTask.removeListener(_onCreateTaskResult);
    _titleController.dispose();
    _descriptionController.dispose();
    _viewModel.dispose();
    super.dispose();
  }

  void _onCreateTaskResult() {
    if (_viewModel.createTask.completed) {
      // Success - dismiss and refresh
      if (mounted) {
        // final l10n = AppLocalizations.of(context)!;
        Navigator.of(context).pop();
        widget.onDismiss(true);
        
        // Show success message
        // ScaffoldMessenger.of(context).showSnackBar(
        //   SnackBar(
        //     content: Text(l10n.taskCreatedSuccess),
        //     backgroundColor: Colors.green,
        //   ),
        // );
      }
    } else if (_viewModel.createTask.error) {
      // Error - show snackbar with retry option
      if (mounted) {
        final l10n = AppLocalizations.of(context)!;
        final error = (_viewModel.createTask.result as dynamic).error;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(l10n.taskCreatedError(error.toString())),
            backgroundColor: Colors.red,
            action: SnackBarAction(
              label: l10n.commonRetry,
              textColor: Colors.white,
              onPressed: () => _viewModel.createTask.execute(),
            ),
          ),
        );
      }
    }
  }

  void _handleCreate() {
    // Execute command - result will be handled by listener
    _viewModel.createTask.execute();
  }

  void _handleCancel() {
    Navigator.of(context).pop();
    widget.onDismiss(false); // false = no refresh needed
  }

  Future<void> _selectDueDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _viewModel.dueDate.value ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime(2100),
    );
    
    if (date != null) {
      _viewModel.dueDate.value = date;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;
    
    return DraggableScrollableSheet(
      initialChildSize: 0.75,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: const BorderRadius.vertical(
              top: Radius.circular(20),
            ),
          ),
          child: Column(
            children: [
              // Drag handle
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              
              // Header
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      l10n.taskCreateTitle,
                      style: theme.textTheme.headlineSmall,
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: _handleCancel,
                    ),
                  ],
                ),
              ),
              
              const Divider(height: 1),
              
              // Form content
              Expanded(
                child: SingleChildScrollView(
                  controller: scrollController,
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // Title field with validation
                      ValueListenableBuilder<String?>(
                        valueListenable: _viewModel.titleError,
                        builder: (context, error, _) {
                          return Semantics(
                            identifier: TestIds.txtTaskTitle,
                            child: TextField(
                              key: const Key(TestIds.txtTaskTitle),
                              controller: _titleController,
                              decoration: InputDecoration(
                                labelText: l10n.taskTitleRequired,
                                hintText: l10n.taskTitleHint,
                                border: const OutlineInputBorder(),
                                errorText: error,
                              ),
                              textInputAction: TextInputAction.next,
                            ),
                          );
                        },
                      ),
                      
                      const SizedBox(height: 16),
                      
                      // Description field with validation
                      ValueListenableBuilder<String?>(
                        valueListenable: _viewModel.descriptionError,
                        builder: (context, error, _) {
                          return Semantics(
                            identifier: TestIds.txtTaskDescription,
                            child: TextField(
                              key: const Key(TestIds.txtTaskDescription),
                              controller: _descriptionController,
                              decoration: InputDecoration(
                                labelText: l10n.taskDescriptionLabel,
                                hintText: l10n.taskDescriptionHint,
                                border: const OutlineInputBorder(),
                                errorText: error,
                              ),
                              maxLines: 4,
                              textInputAction: TextInputAction.next,
                            ),
                          );
                        },
                      ),
                      
                      const SizedBox(height: 16),
                      
                      // Priority dropdown
                      ValueListenableBuilder<Priority>(
                        valueListenable: _viewModel.priority,
                        builder: (context, selectedPriority, _) {
                          return DropdownButtonFormField<Priority>(
                            initialValue: selectedPriority,
                            decoration: InputDecoration(
                              labelText: l10n.taskPriorityLabel,
                              border: const OutlineInputBorder(),
                            ),
                            items: Priority.values.map((priority) {
                              return DropdownMenuItem(
                                value: priority,
                                child: Text(priority.name.toUpperCase()),
                              );
                            }).toList(),
                            onChanged: (value) {
                              if (value != null) {
                                _viewModel.priority.value = value;
                              }
                            },
                          );
                        },
                      ),
                      
                      const SizedBox(height: 16),
                      
                      // Due date picker
                      ValueListenableBuilder<DateTime?>(
                        valueListenable: _viewModel.dueDate,
                        builder: (context, selectedDate, _) {
                          return TextField(
                            decoration: InputDecoration(
                              labelText: l10n.taskDueDateLabel,
                              hintText: selectedDate == null
                                  ? l10n.taskSetDueDate
                                  : '${selectedDate.day}/${selectedDate.month}/${selectedDate.year}',
                              border: const OutlineInputBorder(),
                              suffixIcon: const Icon(Icons.calendar_today),
                            ),
                            readOnly: true,
                            onTap: _selectDueDate,
                          );
                        },
                      ),
                      
                      const SizedBox(height: 24),
                      
                      // Action buttons - side by side
                      Row(
                        children: [
                          // Cancel button
                          Expanded(
                            child: OutlinedButton(
                              onPressed: _handleCancel,
                              child: Padding(
                                padding: const EdgeInsets.symmetric(vertical: 16),
                                child: Text(l10n.commonCancel),
                              ),
                            ),
                          ),
                          
                          const SizedBox(width: 12),
                          
                          // Create button with validation and loading state
                          Expanded(
                            child: ValueListenableBuilder<bool>(
                              valueListenable: _viewModel.isFormValid,
                              builder: (context, isValid, _) {
                                return ListenableBuilder(
                                  listenable: _viewModel.createTask,
                                  builder: (context, _) {
                                    final isRunning = _viewModel.createTask.running;
                                    return Semantics(
                                      identifier: TestIds.btnCreateTask,
                                      button: true,
                                      child: FilledButton(
                                        onPressed: isValid && !isRunning ? _handleCreate : null,
                                        child: Padding(
                                          padding: const EdgeInsets.symmetric(vertical: 16),
                                          child: isRunning
                                              ? const SizedBox(
                                                  height: 20,
                                                  width: 20,
                                                  child: CircularProgressIndicator(
                                                    strokeWidth: 2,
                                                    valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                                                  ),
                                                )
                                              : Text(l10n.taskCreateTitle),
                                        ),
                                      ),
                                    );
                                  },
                                );
                              },
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

/// Convenience method to show the TaskCreateBottomSheet
/// 
/// Usage:
/// ```dart
/// showTaskCreateBottomSheet(
///   context: context,
///   onDismiss: (shouldRefresh) {
///     if (shouldRefresh) {
///       viewModel.refresh();
///     }
///   },
///   projectId: 'optional-project-id',
/// );
/// ```
Future<void> showTaskCreateBottomSheet({
  required BuildContext context,
  required void Function(bool shouldRefresh) onDismiss,
  String? projectId,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    builder: (context) => TaskCreateBottomSheet(
      onDismiss: onDismiss,
      projectId: projectId,
    ),
  );
}


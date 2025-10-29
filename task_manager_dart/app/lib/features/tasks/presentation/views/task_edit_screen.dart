import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/l10n/app_l10n.dart';
import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../viewmodels/task_edit_viewmodel.dart';

/// Task edit screen with Hero animations
/// Follows Flutter's best practices and matches KMM's EditTaskScreen
class TaskEditScreen extends StatefulWidget {
  final String taskId;

  const TaskEditScreen({
    super.key,
    required this.taskId,
  });

  @override
  State<TaskEditScreen> createState() => _TaskEditScreenState();
}

class _TaskEditScreenState extends State<TaskEditScreen> {
  late TaskEditViewModel _viewModel;
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<TaskEditViewModel>();
    _viewModel.updateTask.addListener(_onUpdateResult);
    _viewModel.deleteTask.addListener(_onDeleteResult);

    // Listen to state changes to update text controllers
    _viewModel.addListener(_updateControllers);
  }

  void _updateControllers() {
    final state = _viewModel.state;
    if (_titleController.text != state.title) {
      _titleController.text = state.title;
    }
    if (_descriptionController.text != state.description) {
      _descriptionController.text = state.description;
    }
  }

  @override
  void dispose() {
    _viewModel.updateTask.removeListener(_onUpdateResult);
    _viewModel.deleteTask.removeListener(_onDeleteResult);
    _viewModel.removeListener(_updateControllers);
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _showSnackBar(String message, {bool isError = false}) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
      ),
    );
  }

  void _onUpdateResult() {
    if (_viewModel.updateTask.completed) {
      _viewModel.updateTask.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskUpdatedSuccess);
    }

    if (_viewModel.updateTask.error) {
      _viewModel.updateTask.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskUpdatedError, isError: true);
    }
  }

  void _onDeleteResult() {
    if (_viewModel.deleteTask.completed) {
      _viewModel.deleteTask.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskDeletedSuccess);
    }

    if (_viewModel.deleteTask.error) {
      _viewModel.deleteTask.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskDeletedError, isError: true);
    }
  }

  Future<void> _showDeleteConfirmationDialog() async {
    final l10n = AppLocalizations.of(context)!;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(l10n.taskDeleteDialogTitle),
          content: Text(l10n.taskDeleteDialogMessage),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: Text(l10n.commonCancel),
            ),
            FilledButton(
              onPressed: () => Navigator.of(context).pop(true),
              style: FilledButton.styleFrom(
                backgroundColor: Colors.red,
              ),
              child: Text(l10n.commonDelete),
            ),
          ],
        );
      },
    );

    if (confirmed == true) {
      _viewModel.deleteTask.execute();
    }
  }

  Future<void> _pickDate() async {
    final now = DateTime.now();
    final initialDate = _viewModel.state.dueDate ?? now;
    
    final date = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: now.subtract(const Duration(days: 365)),
      lastDate: now.add(const Duration(days: 365 * 2)),
    );

    if (date != null) {
      _viewModel.setDueDate(date);
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<TaskEditViewModel>();
    final state = viewModel.state;

    return Hero(
      tag: 'task_card_${widget.taskId}',
      child: Scaffold(
        backgroundColor: const Color(0xFFF5F5F5),
        appBar: _TaskEditAppBar(
          isDeleting: state.isDeleting,
          onNavigateBack: viewModel.handleNavigateBack,
          onDelete: _showDeleteConfirmationDialog,
        ),
        body: state.isLoading
            ? const Center(child: CircularProgressIndicator())
            : SingleChildScrollView(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    if (state.errorMessage != null) ...[
                      _ErrorMessage(message: state.errorMessage!),
                      const SizedBox(height: 16),
                    ],
                    _TaskEditForm(
                      titleController: _titleController,
                      descriptionController: _descriptionController,
                      priority: state.priority,
                      status: state.status,
                      dueDate: state.dueDate,
                      onTitleChanged: viewModel.setTitle,
                      onDescriptionChanged: viewModel.setDescription,
                      onPriorityChanged: viewModel.setPriority,
                      onStatusChanged: viewModel.setStatus,
                      onDatePick: _pickDate,
                      onDateClear: () => viewModel.setDueDate(null),
                    ),
                    const SizedBox(height: 24),
                    _TaskEditButtons(
                      isLoading: state.isLoading,
                      isEnabled: state.isButtonEnabled,
                      onCancel: viewModel.handleNavigateBack,
                      onUpdate: () => viewModel.updateTask.execute(),
                    ),
                  ],
                ),
              ),
      ),
    );
  }
}

class _TaskEditAppBar extends StatelessWidget implements PreferredSizeWidget {
  final bool isDeleting;
  final VoidCallback onNavigateBack;
  final VoidCallback onDelete;

  const _TaskEditAppBar({
    required this.isDeleting,
    required this.onNavigateBack,
    required this.onDelete,
  });

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return TaskItTopAppBar(
      title: l10n.taskEditTitle,
      showNavigationIcon: true,
      onNavigateBack: onNavigateBack,
      actions: [
        IconButton(
          icon: isDeleting
              ? const SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                  ),
                )
              : const Icon(Icons.delete),
          onPressed: isDeleting ? null : onDelete,
        ),
      ],
    );
  }
}

class _ErrorMessage extends StatelessWidget {
  final String message;

  const _ErrorMessage({required this.message});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.red.shade50,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.red.shade200),
      ),
      child: Row(
        children: [
          Icon(Icons.error_outline, color: Colors.red.shade700),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              message,
              style: TextStyle(color: Colors.red.shade700),
            ),
          ),
        ],
      ),
    );
  }
}

class _TaskEditForm extends StatelessWidget {
  final TextEditingController titleController;
  final TextEditingController descriptionController;
  final Priority priority;
  final TaskStatus status;
  final DateTime? dueDate;
  final ValueChanged<String> onTitleChanged;
  final ValueChanged<String> onDescriptionChanged;
  final ValueChanged<Priority> onPriorityChanged;
  final ValueChanged<TaskStatus> onStatusChanged;
  final VoidCallback onDatePick;
  final VoidCallback onDateClear;

  const _TaskEditForm({
    required this.titleController,
    required this.descriptionController,
    required this.priority,
    required this.status,
    required this.dueDate,
    required this.onTitleChanged,
    required this.onDescriptionChanged,
    required this.onPriorityChanged,
    required this.onStatusChanged,
    required this.onDatePick,
    required this.onDateClear,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // Title Field
        TextField(
          controller: titleController,
          decoration: InputDecoration(
            labelText: l10n.taskTitleLabel,
            hintText: l10n.taskTitleHint,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            filled: true,
            fillColor: Colors.white,
          ),
          onChanged: onTitleChanged,
          textCapitalization: TextCapitalization.sentences,
        ),
        const SizedBox(height: 16),

        // Description Field
        TextField(
          controller: descriptionController,
          decoration: InputDecoration(
            labelText: l10n.taskDescriptionLabel,
            hintText: l10n.taskDescriptionHint,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            filled: true,
            fillColor: Colors.white,
            alignLabelWithHint: true,
          ),
          onChanged: onDescriptionChanged,
          maxLines: 4,
          textCapitalization: TextCapitalization.sentences,
        ),
        const SizedBox(height: 16),

        // Priority Dropdown
        DropdownButtonFormField<Priority>(
          initialValue: priority,
          decoration: InputDecoration(
            labelText: l10n.taskPriorityLabel,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            filled: true,
            fillColor: Colors.white,
          ),
          items: Priority.values.map((priority) {
            return DropdownMenuItem(
              value: priority,
              child: Row(
                children: [
                  Icon(
                    Icons.flag,
                    size: 20,
                    color: _getPriorityColor(priority),
                  ),
                  const SizedBox(width: 8),
                  Text(_priorityLabel(priority, l10n)),
                ],
              ),
            );
          }).toList(),
          onChanged: (value) {
            if (value != null) onPriorityChanged(value);
          },
        ),
        const SizedBox(height: 16),

        // Status Dropdown
        DropdownButtonFormField<TaskStatus>(
          value: status,
          decoration: InputDecoration(
            labelText: l10n.taskStatusLabel,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            filled: true,
            fillColor: Colors.white,
          ),
          items: TaskStatus.values.map((status) {
            return DropdownMenuItem(
              value: status,
              child: Row(
                children: [
                  Icon(
                    _getStatusIcon(status),
                    size: 20,
                    color: _getStatusColor(status),
                  ),
                  const SizedBox(width: 8),
                  Text(_statusLabel(status, l10n)),
                ],
              ),
            );
          }).toList(),
          onChanged: (value) {
            if (value != null) onStatusChanged(value);
          },
        ),
        const SizedBox(height: 16),

        // Due Date Picker
        InkWell(
          onTap: onDatePick,
          borderRadius: BorderRadius.circular(8),
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.grey.shade300),
            ),
            child: Row(
              children: [
                const Icon(Icons.calendar_today, size: 20),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    dueDate != null
                        ? DateFormat('MMM d, y').format(dueDate!)
                        : l10n.taskSetDueDate,
                    style: TextStyle(
                      fontSize: 16,
                      color: dueDate != null ? Colors.black : Colors.grey.shade600,
                    ),
                  ),
                ),
                if (dueDate != null)
                  IconButton(
                    icon: const Icon(Icons.clear, size: 20),
                    onPressed: onDateClear,
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(),
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Color _getPriorityColor(Priority priority) {
    return switch (priority) {
      Priority.none => const Color(0xFF9CA3AF),
      Priority.high => const Color(0xFFEF4444),
      Priority.medium => const Color(0xFFF59E0B),
      Priority.low => const Color(0xFF10B981),
    };
  }

  Color _getStatusColor(TaskStatus status) {
    return switch (status) {
      TaskStatus.todo => const Color(0xFF6B7280),
      TaskStatus.inProgress => const Color(0xFF3B82F6),
      TaskStatus.done => const Color(0xFF10B981),
    };
  }

  IconData _getStatusIcon(TaskStatus status) {
    return switch (status) {
      TaskStatus.todo => Icons.radio_button_unchecked,
      TaskStatus.inProgress => Icons.pending,
      TaskStatus.done => Icons.check_circle,
    };
  }
}

class _TaskEditButtons extends StatelessWidget {
  final bool isLoading;
  final bool isEnabled;
  final VoidCallback onCancel;
  final VoidCallback onUpdate;

  const _TaskEditButtons({
    required this.isLoading,
    required this.isEnabled,
    required this.onCancel,
    required this.onUpdate,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Row(
      children: [
        Expanded(
          child: OutlinedButton(
            onPressed: isLoading ? null : onCancel,
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            child: Text(l10n.commonCancel),
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: FilledButton(
            onPressed: isEnabled ? onUpdate : null,
            style: FilledButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            child: isLoading
                ? const SizedBox(
                    height: 20,
                    width: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                : Text(l10n.commonUpdate),
          ),
        ),
      ],
    );
  }
}

String _statusLabel(TaskStatus status, AppLocalizations l10n) {
  return switch (status) {
    TaskStatus.todo => l10n.taskStatusTodo,
    TaskStatus.inProgress => l10n.taskStatusInProgress,
    TaskStatus.done => l10n.taskStatusDone,
  };
}

String _priorityLabel(Priority priority, AppLocalizations l10n) {
  return switch (priority) {
    Priority.none => l10n.taskPriorityNone,
    Priority.high => l10n.taskPriorityHigh,
    Priority.medium => l10n.taskPriorityMedium,
    Priority.low => l10n.taskPriorityLow,
  };
}


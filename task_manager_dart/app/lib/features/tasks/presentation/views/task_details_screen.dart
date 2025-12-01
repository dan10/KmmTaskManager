import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/l10n/app_l10n.dart';
import '../../../../core/theme/theme.dart';
import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../state/task_details_state.dart';
import '../viewmodels/task_details_viewmodel.dart';

/// Task details screen with Hero animations
/// Follows Flutter's best practices and matches KMM's TaskDetailsScreen
class TaskDetailsScreen extends StatefulWidget {
  final String taskId;
  final TaskDto? initialTask;

  const TaskDetailsScreen({
    super.key,
    required this.taskId,
    this.initialTask,
  });

  @override
  State<TaskDetailsScreen> createState() => _TaskDetailsScreenState();
}

class _TaskDetailsScreenState extends State<TaskDetailsScreen> {
  late TaskDetailsViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<TaskDetailsViewModel>();
    _viewModel.deleteTask.addListener(_onDeleteResult);
  }

  @override
  void dispose() {
    _viewModel.deleteTask.removeListener(_onDeleteResult);
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

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<TaskDetailsViewModel>();
    final state = viewModel.state;

    return PopScope(
      canPop: true,
      onPopInvokedWithResult: (bool didPop, dynamic result) {
        if (didPop) {
          // Clean up when user navigates back via gesture or back button
          // The ViewModel disposal is handled by Provider automatically
        }
      },
      child: Builder(
        builder: (context) => Scaffold(
          backgroundColor: Theme.of(context).colorScheme.surface,
          appBar: _TaskDetailsAppBar(
          isDeleting: state.isDeleting,
          onNavigateBack: viewModel.handleNavigateBack,
          onEdit: viewModel.handleEditTask,
          onDelete: _showDeleteConfirmationDialog,
        ),
          body: _TaskDetailsBody(
            state: state,
            onRefresh: () => _viewModel.refresh(),
          ),
        ),
      ),
    );
  }
}

class _TaskDetailsAppBar extends StatelessWidget implements PreferredSizeWidget {
  final bool isDeleting;
  final VoidCallback onNavigateBack;
  final VoidCallback onEdit;
  final VoidCallback onDelete;

  const _TaskDetailsAppBar({
    required this.isDeleting,
    required this.onNavigateBack,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return TaskItTopAppBar(
      title: l10n.taskDetailsTitle,
      showNavigationIcon: true,
      onNavigateBack: onNavigateBack,
      actions: [
        IconButton(
          icon: const Icon(Icons.edit),
          onPressed: onEdit,
        ),
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

class _TaskDetailsBody extends StatelessWidget {
  final TaskDetailsState state;
  final Future<void> Function() onRefresh;

  const _TaskDetailsBody({
    required this.state,
    required this.onRefresh,
  });

  @override
  Widget build(BuildContext context) {
    if (state.isLoading) {
      return const _TaskDetailsLoading();
    }

    if (state.errorMessage != null) {
      return _TaskDetailsError(message: state.errorMessage!);
    }

    final task = state.task;
    if (task == null) {
      return const _TaskDetailsEmpty();
    }

    return RefreshIndicator(
      onRefresh: onRefresh,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: _TaskDetailsContent(task: task),
      ),
    );
  }
}

class _TaskDetailsLoading extends StatelessWidget {
  const _TaskDetailsLoading();

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: CircularProgressIndicator(),
    );
  }
}

class _TaskDetailsError extends StatelessWidget {
  final String message;

  const _TaskDetailsError({required this.message});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.error_outline,
            size: 64,
            color: Colors.red,
          ),
          const SizedBox(height: 16),
          Text(
            message,
            style: const TextStyle(fontSize: 16),
          ),
        ],
      ),
    );
  }
}

class _TaskDetailsEmpty extends StatelessWidget {
  const _TaskDetailsEmpty();

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Center(
      child: Text(l10n.taskNotFound),
    );
  }
}

class _TaskDetailsContent extends StatelessWidget {
  final TaskDto task;

  const _TaskDetailsContent({required this.task});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _TaskHeaderCard(task: task),
        const SizedBox(height: 16),
        if (task.description.isNotEmpty) ...[
          _TaskDescriptionCard(task: task),
          const SizedBox(height: 16),
        ],
        _TaskInformationCard(task: task),
        const SizedBox(height: 16),
        _TaskDatesCard(task: task),
      ],
    );
  }
}

class _TaskHeaderCard extends StatelessWidget {
  final TaskDto task;

  const _TaskHeaderCard({required this.task});

  @override
  Widget build(BuildContext context) {
    return Hero(
      tag: 'task_card_${task.id}',
      transitionOnUserGestures: true,
      child: Material(
        type: MaterialType.transparency,
        child: SizedBox(
          width: double.infinity,
          child: Builder(
            builder: (context) => Card(
              color: context.extColors.surfaceCard,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              elevation: 1,
              child: ClipRect(
                child: IntrinsicHeight(
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      _TaskIndicator(priority: task.priority),
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: SingleChildScrollView(
                            physics: const NeverScrollableScrollPhysics(),
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                _TaskTitle(title: task.title),
                                const SizedBox(height: 8),
                                _TaskHeaderMetadata(
                                  status: task.status,
                                  priority: task.priority,
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _TaskIndicator extends StatelessWidget {
  final Priority priority;

  const _TaskIndicator({required this.priority});

  @override
  Widget build(BuildContext context) {
    final ext = context.extColors;
    final priorityColor = switch (priority) {
      Priority.none => ext.priorityNoneText,
      Priority.high => ext.priorityHighText,
      Priority.medium => ext.priorityMediumText,
      Priority.low => ext.priorityLowText,
    };
    
    return Container(
      width: 4,
      decoration: BoxDecoration(
        color: priorityColor,
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(12),
          bottomLeft: Radius.circular(12),
        ),
      ),
    );
  }
}

class _TaskTitle extends StatelessWidget {
  final String title;

  const _TaskTitle({
    required this.title,
  });

  @override
  Widget build(BuildContext context) {
    return Text(
      title,
      style: TextStyle(
        fontSize: 22,
        fontWeight: FontWeight.bold,
        color: context.extColors.textPrimary,
      ),
    );
  }
}

class _TaskHeaderMetadata extends StatelessWidget {
  final TaskStatus status;
  final Priority priority;

  const _TaskHeaderMetadata({
    required this.status,
    required this.priority,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final ext = context.extColors;
    final priorityColor = switch (priority) {
      Priority.none => ext.priorityNoneText,
      Priority.high => ext.priorityHighText,
      Priority.medium => ext.priorityMediumText,
      Priority.low => ext.priorityLowText,
    };
    
    return Wrap(
      spacing: 8,
      runSpacing: 4,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        _TaskStatusBadge(status: status),
        Icon(
          Icons.edit,
          size: 16,
          color: context.extColors.textSecondary,
        ),
        Text(
          l10n.taskPriorityText(_priorityLabel(priority, l10n)),
          style: TextStyle(
            fontSize: 14,
            color: priorityColor,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }
}

class _TaskStatusBadge extends StatelessWidget {
  final TaskStatus status;

  const _TaskStatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final ext = context.extColors;
    final backgroundColor = switch (status) {
      TaskStatus.todo => ext.statusTodoContainer,
      TaskStatus.inProgress => ext.statusInProgressContainer,
      TaskStatus.done => ext.statusDoneContainer,
    };
    final textColor = switch (status) {
      TaskStatus.todo => ext.statusTodoText,
      TaskStatus.inProgress => ext.statusInProgressText,
      TaskStatus.done => ext.statusDoneText,
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        _statusLabel(status, l10n),
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w500,
          color: textColor,
        ),
      ),
    );
  }
}

class _TaskDescriptionCard extends StatelessWidget {
  final TaskDto task;

  const _TaskDescriptionCard({required this.task});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Builder(
      builder: (context) {
        final ext = context.extColors;
        return Card(
          color: ext.surfaceCard,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          elevation: 1,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.taskDescriptionLabel,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: ext.textPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  task.description,
                  style: TextStyle(
                    fontSize: 14,
                    color: ext.textSecondary,
                    height: 1.5,
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _TaskInformationCard extends StatelessWidget {
  final TaskDto task;

  const _TaskInformationCard({required this.task});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Builder(
      builder: (context) {
        final ext = context.extColors;
        return Card(
          color: ext.surfaceCard,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          elevation: 1,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.taskInformationLabel,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: ext.textPrimary,
                  ),
                ),
            const SizedBox(height: 16),
            _TaskInfoRow(
              icon: Icons.calendar_today_outlined,
              label: l10n.taskDueDateLabel,
              value: task.dueDate != null
                  ? DateFormat('MMM d, y').format(task.dueDate!)
                  : l10n.taskNoDueDate,
            ),
            const SizedBox(height: 12),
            _TaskInfoRow(
              icon: Icons.edit_outlined,
              label: l10n.taskStatusLabel,
              value: _statusLabel(task.status, l10n),
            ),
            const SizedBox(height: 12),
            _TaskInfoRow(
              icon: Icons.flag_outlined,
              label: l10n.taskPriorityLabel,
              value: _priorityLabel(task.priority, l10n),
              valueColor: _getPriorityColor(context, task.priority),
            ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _TaskDatesCard extends StatelessWidget {
  final TaskDto task;

  const _TaskDatesCard({required this.task});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Builder(
      builder: (context) {
        final ext = context.extColors;
        return Card(
          color: ext.surfaceCard,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          elevation: 1,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.taskDatesLabel,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: ext.textPrimary,
                  ),
                ),
            const SizedBox(height: 16),
            _TaskDateRow(
              icon: Icons.add,
              label: l10n.taskCreatedAtLabel,
              value: task.createdAt != null
                  ? DateFormat('MMM d, y').format(task.createdAt!)
                  : l10n.commonNA,
            ),
            const SizedBox(height: 12),
            _TaskDateRow(
              icon: Icons.date_range,
              label: l10n.taskUpdatedAtLabel,
              value: task.updatedAt != null
                  ? DateFormat('MMM d, y').format(task.updatedAt!)
                  : task.createdAt != null
                      ? DateFormat('MMM d, y').format(task.createdAt!)
                      : l10n.commonNA,
            ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _TaskInfoRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color? valueColor;

  const _TaskInfoRow({
    required this.icon,
    required this.label,
    required this.value,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(
          icon,
          size: 20,
          color: context.extColors.iconNeutral,
        ),
        const SizedBox(width: 12),
        SizedBox(
          width: 80,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 14,
              color: context.extColors.textSecondary,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: valueColor ?? context.extColors.textPrimary,
            ),
          ),
        ),
      ],
    );
  }
}

class _TaskDateRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _TaskDateRow({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(
          icon,
          size: 20,
          color: context.extColors.iconPurple,
        ),
        const SizedBox(width: 12),
        SizedBox(
          width: 100,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 14,
              color: context.extColors.textSecondary,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: context.extColors.textPrimary,
            ),
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

Color _getPriorityColor(BuildContext context, Priority priority) {
  final ext = context.extColors;
  return switch (priority) {
    Priority.none => ext.priorityNoneText,
    Priority.high => ext.priorityHighText,
    Priority.medium => ext.priorityMediumText,
    Priority.low => ext.priorityLowText,
  };
}


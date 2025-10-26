import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

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
      _showSnackBar('Task deleted successfully');
    }

    if (_viewModel.deleteTask.error) {
      _viewModel.deleteTask.clearResult();
      _showSnackBar('Failed to delete task', isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<TaskDetailsViewModel>();
    final state = viewModel.state;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: _TaskDetailsAppBar(
        isDeleting: state.isDeleting,
        onNavigateBack: viewModel.handleNavigateBack,
        onEdit: viewModel.handleEditTask,
        onDelete: () => viewModel.deleteTask.execute(),
      ),
      body: _TaskDetailsBody(
        state: state,
        onRefresh: () => _viewModel.refresh(),
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
    return TaskItTopAppBar(
      title: 'Task Details',
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
    return const Center(
      child: Text('Task not found'),
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
      child: Material(
        type: MaterialType.transparency,
        child: Card(
          color: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          elevation: 1,
          child: IntrinsicHeight(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _TaskIndicator(),
                Expanded(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
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
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _TaskIndicator extends StatelessWidget {
  const _TaskIndicator();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 4,
      decoration: const BoxDecoration(
        color: Color(0xFFFDB022),
        borderRadius: BorderRadius.only(
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
      style: const TextStyle(
        fontSize: 22,
        fontWeight: FontWeight.bold,
        color: Color(0xFF1A1A1A),
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
    return Wrap(
      spacing: 8,
      runSpacing: 4,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        _TaskStatusBadge(status: status),
        const Icon(
          Icons.edit,
          size: 16,
          color: Color(0xFF6B7280),
        ),
        Text(
          '${_priorityLabel(priority)} Priority',
          style: const TextStyle(
            fontSize: 14,
            color: Color(0xFF6B7280),
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
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFFE8F5E9),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        _statusLabel(status),
        style: const TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w500,
          color: Color(0xFF2E7D32),
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
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Description',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A1A),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              task.description,
              style: const TextStyle(
                fontSize: 14,
                color: Color(0xFF6B7280),
                height: 1.5,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _TaskInformationCard extends StatelessWidget {
  final TaskDto task;

  const _TaskInformationCard({required this.task});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Task Information',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A1A),
              ),
            ),
            const SizedBox(height: 16),
            _TaskInfoRow(
              icon: Icons.calendar_today_outlined,
              label: 'Due Date',
              value: task.dueDate != null
                  ? DateFormat('MMM d, y').format(task.dueDate!)
                  : 'No due date',
            ),
            const SizedBox(height: 12),
            _TaskInfoRow(
              icon: Icons.edit_outlined,
              label: 'Status',
              value: _statusLabel(task.status),
            ),
            const SizedBox(height: 12),
            _TaskInfoRow(
              icon: Icons.flag_outlined,
              label: 'Priority',
              value: _priorityLabel(task.priority),
            ),
          ],
        ),
      ),
    );
  }
}

class _TaskDatesCard extends StatelessWidget {
  final TaskDto task;

  const _TaskDatesCard({required this.task});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Dates',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A1A),
              ),
            ),
            const SizedBox(height: 16),
            _TaskDateRow(
              icon: Icons.add,
              label: 'Created',
              value: task.createdAt != null
                  ? DateFormat('MMM d, y').format(task.createdAt!)
                  : 'N/A',
            ),
            const SizedBox(height: 12),
            _TaskDateRow(
              icon: Icons.date_range,
              label: 'Last Updated',
              value: task.updatedAt != null
                  ? DateFormat('MMM d, y').format(task.updatedAt!)
                  : task.createdAt != null
                      ? DateFormat('MMM d, y').format(task.createdAt!)
                      : 'N/A',
            ),
          ],
        ),
      ),
    );
  }
}

class _TaskInfoRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _TaskInfoRow({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(
          icon,
          size: 20,
          color: const Color(0xFF9CA3AF),
        ),
        const SizedBox(width: 12),
        SizedBox(
          width: 80,
          child: Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              color: Color(0xFF6B7280),
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Color(0xFF1A1A1A),
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
          color: const Color(0xFF7C3AED),
        ),
        const SizedBox(width: 12),
        SizedBox(
          width: 100,
          child: Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              color: Color(0xFF6B7280),
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Color(0xFF1A1A1A),
            ),
          ),
        ),
      ],
    );
  }
}

String _statusLabel(TaskStatus status) {
  return switch (status) {
    TaskStatus.todo => 'To Do',
    TaskStatus.inProgress => 'In Progress',
    TaskStatus.done => 'Done',
  };
}

String _priorityLabel(Priority priority) {
  return switch (priority) {
    Priority.high => 'High',
    Priority.medium => 'Medium',
    Priority.low => 'Low',
  };
}


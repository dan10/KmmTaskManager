import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:task_manager_shared/models.dart';

import '../../viewmodels/task_detail_viewmodel.dart';

class TaskDetailView extends StatefulWidget {
  final String taskId;

  const TaskDetailView({
    super.key,
    required this.taskId,
  });

  @override
  State<TaskDetailView> createState() => _TaskDetailViewState();
}

class _TaskDetailViewState extends State<TaskDetailView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<TaskDetailViewModel>(context, listen: false).loadTask(widget.taskId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Consumer<TaskDetailViewModel>(
      builder: (context, taskDetailViewModel, child) {
        if (taskDetailViewModel.isLoading) {
          return Scaffold(
            appBar: AppBar(title: Text(l10n.taskDetailsTitle)),
            body: const Center(child: CircularProgressIndicator()),
          );
        }

        if (taskDetailViewModel.state == TaskDetailState.error) {
          return Scaffold(
            appBar: AppBar(title: Text(l10n.taskDetailsTitle)),
            body: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(
                    taskDetailViewModel.errorMessage ?? 'Error loading task',
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => taskDetailViewModel.loadTask(widget.taskId),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            ),
          );
        }

        final task = taskDetailViewModel.task;
        if (task == null) {
          return Scaffold(
            appBar: AppBar(title: Text(l10n.taskDetailsTitle)),
            body: const Center(child: Text('Task not found')),
          );
        }

        return Scaffold(
          backgroundColor: const Color(0xFFF1F5F9),
          appBar: AppBar(
            title: Text(l10n.taskDetailsTitle),
            backgroundColor: const Color(0xFFF1F5F9),
            elevation: 0,
            actions: [
              IconButton(
                icon: const Icon(Icons.edit),
                onPressed: () => context.go('/tasks/${task.id}/edit'),
              ),
              PopupMenuButton<String>(
                onSelected: (value) {
                  if (value == 'delete') {
                    _showDeleteConfirmation(context, taskDetailViewModel);
                  }
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(
                    value: 'delete',
                    child: Row(
                      children: [
                        Icon(Icons.delete, color: Colors.red),
                        SizedBox(width: 8),
                        Text('Delete Task'),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
          body: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Task Title Card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                task.title,
                                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                              decoration: BoxDecoration(
                                color: _getPriorityBackgroundColor(task.priority),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                task.priority.name.toUpperCase(),
                                style: TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.bold,
                                  color: _getPriorityColor(task.priority),
                                ),
                              ),
                            ),
                          ],
                        ),
                        if (task.description.isNotEmpty) ...[
                          const SizedBox(height: 12),
                          Text(
                            task.description,
                            style: Theme.of(context).textTheme.bodyLarge,
                          ),
                        ],
                      ],
                    ),
                  ),
                ),

                const SizedBox(height: 16),

                // Status Section
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Status',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 12),
                        Row(
                          children: [
                            Expanded(
                              child: _buildStatusButton(
                                context,
                                'To Do',
                                TaskStatus.todo,
                                task.status,
                                () => taskDetailViewModel.changeTaskStatus(task.id, TaskStatus.todo),
                              ),
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: _buildStatusButton(
                                context,
                                'In Progress',
                                TaskStatus.inProgress,
                                task.status,
                                () => taskDetailViewModel.changeTaskStatus(task.id, TaskStatus.inProgress),
                              ),
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: _buildStatusButton(
                                context,
                                'Done',
                                TaskStatus.done,
                                task.status,
                                () => taskDetailViewModel.changeTaskStatus(task.id, TaskStatus.done),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),

                const SizedBox(height: 16),

                // Task Details Card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Details',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 16),
                        
                        if (task.dueDate != null) ...[
                          _buildDetailRow(
                            context,
                            'Due Date',
                            _formatDate(task.dueDate!),
                            Icons.calendar_today,
                          ),
                          const SizedBox(height: 12),
                        ],
                        
                        _buildDetailRow(
                          context,
                          'Priority',
                          task.priority.name.toUpperCase(),
                          Icons.flag,
                          color: _getPriorityColor(task.priority),
                        ),
                        
                        if (task.projectId != null) ...[
                          const SizedBox(height: 12),
                          _buildDetailRow(
                            context,
                            'Project',
                            task.projectId!,
                            Icons.folder,
                          ),
                        ],
                        
                        if (task.assigneeId != null) ...[
                          const SizedBox(height: 12),
                          _buildDetailRow(
                            context,
                            'Assigned to',
                            task.assigneeId!,
                            Icons.person,
                          ),
                        ],
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildStatusButton(
    BuildContext context,
    String label,
    TaskStatus status,
    TaskStatus currentStatus,
    VoidCallback onPressed,
  ) {
    final isSelected = status == currentStatus;
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: isSelected
            ? Theme.of(context).colorScheme.primary
            : Theme.of(context).colorScheme.surface,
        foregroundColor: isSelected
            ? Theme.of(context).colorScheme.onPrimary
            : Theme.of(context).colorScheme.onSurface,
        elevation: isSelected ? 2 : 0,
        side: BorderSide(
          color: Theme.of(context).colorScheme.outline,
          width: 1,
        ),
      ),
      child: Text(label),
    );
  }

  Widget _buildDetailRow(
    BuildContext context,
    String label,
    String value,
    IconData icon, {
    Color? color,
  }) {
    return Row(
      children: [
        Icon(
          icon,
          size: 20,
          color: color ?? Theme.of(context).colorScheme.primary,
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Theme.of(context).colorScheme.onSurface.withOpacity(0.7),
                ),
              ),
              Text(
                value,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w500,
                  color: color,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Color _getPriorityColor(Priority priority) {
    switch (priority) {
      case Priority.high:
        return const Color(0xFFDC2626);
      case Priority.medium:
        return const Color(0xFFEAB308);
      case Priority.low:
        return const Color(0xFF22C55E);
    }
  }

  Color _getPriorityBackgroundColor(Priority priority) {
    switch (priority) {
      case Priority.high:
        return const Color(0xFFFFE4E4);
      case Priority.medium:
        return const Color(0xFFFEF9C3);
      case Priority.low:
        return const Color(0xFFDCFCE7);
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }

  void _showDeleteConfirmation(BuildContext context, TaskDetailViewModel viewModel) {
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
            onPressed: () {
              viewModel.deleteTask(widget.taskId);
              Navigator.of(context).pop();
              context.pop(); // Go back to previous screen
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
} 
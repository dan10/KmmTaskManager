import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';

import '../../viewmodels/task_viewmodel.dart';
import '../../viewmodels/project_viewmodel.dart';
import '../../../domain/entities/task.dart';

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
  Widget build(BuildContext context) {
    return Consumer<TaskViewModel>(
      builder: (context, taskViewModel, child) {
        final task = taskViewModel.getTask(widget.taskId);

        if (task == null) {
          return Scaffold(
            appBar: AppBar(title: const Text('Task Not Found')),
            body: const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.error_outline, size: 64, color: Colors.red),
                  SizedBox(height: 16),
                  Text('Task not found'),
                ],
              ),
            ),
          );
        }

        return Scaffold(
          appBar: AppBar(
            title: Text(task.title),
            actions: [
              PopupMenuButton<String>(
                onSelected: (value) {
                  if (value == 'edit') {
                    _showEditTaskDialog(context, task);
                  } else if (value == 'delete') {
                    _showDeleteConfirmation(context);
                  }
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(
                    value: 'edit',
                    child: Row(
                      children: [
                        Icon(Icons.edit),
                        SizedBox(width: 8),
                        Text('Edit'),
                      ],
                    ),
                  ),
                  const PopupMenuItem(
                    value: 'delete',
                    child: Row(
                      children: [
                        Icon(Icons.delete, color: Colors.red),
                        SizedBox(width: 8),
                        Text('Delete'),
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
                // Task Header Card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            CircleAvatar(
                              backgroundColor: _getStatusColor(task.status),
                              child: Icon(
                                _getStatusIcon(task.status),
                                color: Colors.white,
                              ),
                            ),
                            const SizedBox(width: 16),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    task.title,
                                    style: Theme.of(context).textTheme.titleLarge,
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    _getStatusText(task.status),
                                    style: TextStyle(
                                      color: _getStatusColor(task.status),
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        Wrap(
                          spacing: 8,
                          children: [
                            _buildPriorityChip(task.priority),
                            Consumer<ProjectViewModel>(
                              builder: (context, projectViewModel, child) {
                                final project = projectViewModel.getProject(task.projectId);
                                return Chip(
                                  avatar: const Icon(Icons.folder, size: 16),
                                  label: Text(project?.name ?? 'Unknown Project'),
                                );
                              },
                            ),
                            if (task.dueDate != null)
                              Chip(
                                avatar: Icon(
                                  Icons.schedule,
                                  size: 16,
                                  color: task.isOverdue ? Colors.red : null,
                                ),
                                label: Text(
                                  'Due ${_formatDate(task.dueDate!)}',
                                  style: TextStyle(
                                    color: task.isOverdue ? Colors.red : null,
                                  ),
                                ),
                                backgroundColor: task.isOverdue ? Colors.red.withOpacity(0.1) : null,
                              ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // Description Card
                if (task.description != null) ...[
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Description',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 8),
                          Text(task.description!),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                ],

                // Status Actions Card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Actions',
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 16),
                        Row(
                          children: [
                            if (task.status != TaskStatus.todo)
                              Expanded(
                                child: ElevatedButton.icon(
                                  onPressed: () => taskViewModel.updateTaskStatus(task.id, TaskStatus.todo),
                                  icon: const Icon(Icons.radio_button_unchecked),
                                  label: const Text('Mark as Todo'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.grey,
                                    foregroundColor: Colors.white,
                                  ),
                                ),
                              ),
                            if (task.status != TaskStatus.todo && task.status != TaskStatus.inProgress)
                              const SizedBox(width: 8),
                            if (task.status != TaskStatus.inProgress)
                              Expanded(
                                child: ElevatedButton.icon(
                                  onPressed: () => taskViewModel.updateTaskStatus(task.id, TaskStatus.inProgress),
                                  icon: const Icon(Icons.play_circle),
                                  label: const Text('Start'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.blue,
                                    foregroundColor: Colors.white,
                                  ),
                                ),
                              ),
                            if (task.status != TaskStatus.inProgress && task.status != TaskStatus.completed)
                              const SizedBox(width: 8),
                            if (task.status != TaskStatus.completed)
                              Expanded(
                                child: ElevatedButton.icon(
                                  onPressed: () => taskViewModel.updateTaskStatus(task.id, TaskStatus.completed),
                                  icon: const Icon(Icons.check_circle),
                                  label: const Text('Complete'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.green,
                                    foregroundColor: Colors.white,
                                  ),
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
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 16),
                        _buildDetailRow('Created', task.createdAt != null ? _formatDateTime(task.createdAt!) : 'Unknown'),
                        if (task.updatedAt != null)
                          _buildDetailRow('Updated', _formatDateTime(task.updatedAt!)),
                        if (task.assigneeId != null)
                          _buildDetailRow('Assignee', 'User ${task.assigneeId}'),
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

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }

  Widget _buildPriorityChip(TaskPriority priority) {
    Color color;
    String text;
    
    switch (priority) {
      case TaskPriority.high:
        color = Colors.red;
        text = 'High Priority';
        break;
      case TaskPriority.medium:
        color = Colors.orange;
        text = 'Medium Priority';
        break;
      case TaskPriority.low:
        color = Colors.green;
        text = 'Low Priority';
        break;
    }

    return Chip(
      avatar: Icon(Icons.flag, size: 16, color: color),
      label: Text(text),
      backgroundColor: color.withOpacity(0.1),
      side: BorderSide(color: color),
    );
  }

  Color _getStatusColor(TaskStatus status) {
    switch (status) {
      case TaskStatus.todo:
        return Colors.grey;
      case TaskStatus.inProgress:
        return Colors.blue;
      case TaskStatus.completed:
        return Colors.green;
    }
  }

  IconData _getStatusIcon(TaskStatus status) {
    switch (status) {
      case TaskStatus.todo:
        return Icons.radio_button_unchecked;
      case TaskStatus.inProgress:
        return Icons.play_circle;
      case TaskStatus.completed:
        return Icons.check_circle;
    }
  }

  String _getStatusText(TaskStatus status) {
    switch (status) {
      case TaskStatus.todo:
        return 'To Do';
      case TaskStatus.inProgress:
        return 'In Progress';
      case TaskStatus.completed:
        return 'Completed';
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }

  String _formatDateTime(DateTime date) {
    return '${date.day}/${date.month}/${date.year} at ${date.hour}:${date.minute.toString().padLeft(2, '0')}';
  }

  void _showEditTaskDialog(BuildContext context, Task task) {
    final titleController = TextEditingController(text: task.title);
    final descriptionController = TextEditingController(text: task.description);
    TaskPriority selectedPriority = task.priority;

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Edit Task'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: titleController,
                decoration: const InputDecoration(
                  labelText: 'Task Title',
                  hintText: 'Enter task title',
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: descriptionController,
                decoration: const InputDecoration(
                  labelText: 'Description (Optional)',
                  hintText: 'Enter task description',
                ),
                maxLines: 3,
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<TaskPriority>(
                value: selectedPriority,
                decoration: const InputDecoration(labelText: 'Priority'),
                items: TaskPriority.values.map((priority) {
                  return DropdownMenuItem(
                    value: priority,
                    child: Text(priority.name.toUpperCase()),
                  );
                }).toList(),
                onChanged: (value) {
                  if (value != null) {
                    setState(() {
                      selectedPriority = value;
                    });
                  }
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                if (titleController.text.isNotEmpty) {
                  Provider.of<TaskViewModel>(context, listen: false).updateTask(
                    task.id,
                    title: titleController.text,
                    description: descriptionController.text.isEmpty ? null : descriptionController.text,
                    priority: selectedPriority,
                  );
                  Navigator.of(context).pop();
                }
              },
              child: const Text('Update'),
            ),
          ],
        ),
      ),
    );
  }

  void _showDeleteConfirmation(BuildContext context) {
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
              Provider.of<TaskViewModel>(context, listen: false).deleteTask(widget.taskId);
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
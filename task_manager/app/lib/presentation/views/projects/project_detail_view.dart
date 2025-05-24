import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';

import '../../viewmodels/project_viewmodel.dart';
import '../../viewmodels/task_viewmodel.dart';
import '../../../domain/entities/task.dart';

class ProjectDetailView extends StatefulWidget {
  final String projectId;

  const ProjectDetailView({
    super.key,
    required this.projectId,
  });

  @override
  State<ProjectDetailView> createState() => _ProjectDetailViewState();
}

class _ProjectDetailViewState extends State<ProjectDetailView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final taskViewModel = Provider.of<TaskViewModel>(context, listen: false);
      taskViewModel.loadTasks(projectId: widget.projectId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<ProjectViewModel>(
      builder: (context, projectViewModel, child) {
        final project = projectViewModel.getProject(widget.projectId);

        if (project == null) {
          return Scaffold(
            appBar: AppBar(title: const Text('Project Not Found')),
            body: const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.error_outline, size: 64, color: Colors.red),
                  SizedBox(height: 16),
                  Text('Project not found'),
                ],
              ),
            ),
          );
        }

        return Scaffold(
          appBar: AppBar(
            title: Text(project.name),
            actions: [
              IconButton(
                icon: const Icon(Icons.add_task),
                onPressed: () => _showCreateTaskDialog(context),
              ),
            ],
          ),
          body: Column(
            children: [
              // Project Info Card
              Card(
                margin: const EdgeInsets.all(16),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          CircleAvatar(
                            backgroundColor: Theme.of(context).primaryColor,
                            child: Text(
                              project.name.substring(0, 1).toUpperCase(),
                              style: const TextStyle(color: Colors.white),
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  project.name,
                                  style: Theme.of(context).textTheme.titleLarge,
                                ),
                                if (project.description != null)
                                  Text(
                                    project.description!,
                                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                      color: Colors.grey[600],
                                    ),
                                  ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Consumer<TaskViewModel>(
                        builder: (context, taskViewModel, child) {
                          final projectTasks = taskViewModel.getTasksForProject(widget.projectId);
                          final completedTasks = projectTasks.where((task) => task.status == TaskStatus.completed).length;
                          
                          return Row(
                            children: [
                              _buildStatChip(
                                icon: Icons.task_alt,
                                label: 'Total Tasks',
                                value: projectTasks.length.toString(),
                                color: Colors.blue,
                              ),
                              const SizedBox(width: 8),
                              _buildStatChip(
                                icon: Icons.check_circle,
                                label: 'Completed',
                                value: completedTasks.toString(),
                                color: Colors.green,
                              ),
                            ],
                          );
                        },
                      ),
                    ],
                  ),
                ),
              ),

              // Tasks Section
              Expanded(
                child: Consumer<TaskViewModel>(
                  builder: (context, taskViewModel, child) {
                    if (taskViewModel.isLoading) {
                      return const Center(child: CircularProgressIndicator());
                    }

                    final projectTasks = taskViewModel.getTasksForProject(widget.projectId);

                    if (projectTasks.isEmpty) {
                      return Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(Icons.assignment, size: 64, color: Colors.grey),
                            const SizedBox(height: 16),
                            Text(
                              'No Tasks Yet',
                              style: Theme.of(context).textTheme.titleLarge,
                            ),
                            const SizedBox(height: 8),
                            const Text('Create your first task for this project'),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              onPressed: () => _showCreateTaskDialog(context),
                              child: const Text('Create Task'),
                            ),
                          ],
                        ),
                      );
                    }

                    return ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      itemCount: projectTasks.length,
                      itemBuilder: (context, index) {
                        final task = projectTasks[index];
                        return Card(
                          margin: const EdgeInsets.only(bottom: 8),
                          child: ListTile(
                            leading: CircleAvatar(
                              backgroundColor: _getStatusColor(task.status),
                              child: Icon(
                                _getStatusIcon(task.status),
                                color: Colors.white,
                                size: 20,
                              ),
                            ),
                            title: Text(task.title),
                            subtitle: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                if (task.description != null)
                                  Text(task.description!),
                                const SizedBox(height: 4),
                                Row(
                                  children: [
                                    _buildPriorityChip(task.priority),
                                    const SizedBox(width: 8),
                                    if (task.dueDate != null)
                                      Text(
                                        'Due: ${_formatDate(task.dueDate!)}',
                                        style: TextStyle(
                                          color: task.isOverdue ? Colors.red : Colors.grey[600],
                                          fontSize: 12,
                                        ),
                                      ),
                                  ],
                                ),
                              ],
                            ),
                            trailing: PopupMenuButton<String>(
                              onSelected: (value) {
                                if (value == 'complete' && task.status != TaskStatus.completed) {
                                  taskViewModel.updateTaskStatus(task.id, TaskStatus.completed);
                                } else if (value == 'progress' && task.status != TaskStatus.inProgress) {
                                  taskViewModel.updateTaskStatus(task.id, TaskStatus.inProgress);
                                } else if (value == 'todo' && task.status != TaskStatus.todo) {
                                  taskViewModel.updateTaskStatus(task.id, TaskStatus.todo);
                                }
                              },
                              itemBuilder: (context) => [
                                if (task.status != TaskStatus.todo)
                                  const PopupMenuItem(
                                    value: 'todo',
                                    child: Row(
                                      children: [
                                        Icon(Icons.radio_button_unchecked),
                                        SizedBox(width: 8),
                                        Text('Mark as Todo'),
                                      ],
                                    ),
                                  ),
                                if (task.status != TaskStatus.inProgress)
                                  const PopupMenuItem(
                                    value: 'progress',
                                    child: Row(
                                      children: [
                                        Icon(Icons.play_circle),
                                        SizedBox(width: 8),
                                        Text('Mark as In Progress'),
                                      ],
                                    ),
                                  ),
                                if (task.status != TaskStatus.completed)
                                  const PopupMenuItem(
                                    value: 'complete',
                                    child: Row(
                                      children: [
                                        Icon(Icons.check_circle, color: Colors.green),
                                        SizedBox(width: 8),
                                        Text('Mark as Completed'),
                                      ],
                                    ),
                                  ),
                              ],
                            ),
                            onTap: () => context.go('/tasks/${task.id}'),
                          ),
                        );
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildStatChip({
    required IconData icon,
    required String label,
    required String value,
    required Color color,
  }) {
    return Chip(
      avatar: Icon(icon, size: 16, color: color),
      label: Text('$value $label'),
      backgroundColor: color.withOpacity(0.1),
    );
  }

  Widget _buildPriorityChip(TaskPriority priority) {
    Color color;
    String text;
    
    switch (priority) {
      case TaskPriority.high:
        color = Colors.red;
        text = 'High';
        break;
      case TaskPriority.medium:
        color = Colors.orange;
        text = 'Medium';
        break;
      case TaskPriority.low:
        color = Colors.green;
        text = 'Low';
        break;
    }

    return Chip(
      label: Text(text, style: const TextStyle(fontSize: 10)),
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

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }

  void _showCreateTaskDialog(BuildContext context) {
    final titleController = TextEditingController();
    final descriptionController = TextEditingController();
    TaskPriority selectedPriority = TaskPriority.medium;

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Create Task'),
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
                  Provider.of<TaskViewModel>(context, listen: false).createTask(
                    title: titleController.text,
                    description: descriptionController.text.isEmpty ? null : descriptionController.text,
                    priority: selectedPriority,
                    projectId: widget.projectId,
                  );
                  Navigator.of(context).pop();
                }
              },
              child: const Text('Create'),
            ),
          ],
        ),
      ),
    );
  }
} 
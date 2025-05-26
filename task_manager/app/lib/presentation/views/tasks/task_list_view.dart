import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:task_manager_shared/models.dart';

import '../../viewmodels/task_list_viewmodel.dart';
import '../../viewmodels/project_viewmodel.dart';

class TaskListView extends StatefulWidget {
  final String? projectId;

  const TaskListView({
    super.key,
    this.projectId,
  });

  @override
  State<TaskListView> createState() => _TaskListViewState();
}

class _TaskListViewState extends State<TaskListView> {
  final _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<TaskListViewModel>(context, listen: false).loadTasks(projectId: widget.projectId);
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Scaffold(
      backgroundColor: const Color(0xFFF1F5F9), // Match Compose background
      body: SafeArea(
        child: Consumer<TaskListViewModel>(
          builder: (context, taskListViewModel, child) {
            if (taskListViewModel.isLoading) {
              return const Center(child: CircularProgressIndicator());
            }

            if (taskListViewModel.state == TaskListState.error) {
              return Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.error_outline, size: 64, color: Colors.red),
                    const SizedBox(height: 16),
                    Text(
                      taskListViewModel.errorMessage ?? 'Error loading tasks',
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () => taskListViewModel.loadTasks(projectId: widget.projectId),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              );
            }

            return Column(
              children: [
                // Top Bar Section (similar to Compose)
                _buildTopBar(context, taskListViewModel, l10n),
                
                // Task List (no tabs, just all tasks)
                Expanded(
                  child: _buildTaskList(taskListViewModel.tasks, l10n),
                ),
              ],
            );
          },
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          if (widget.projectId != null) {
            context.go('/task/create?projectId=${widget.projectId}');
          } else {
            context.go('/task/create');
          }
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildTopBar(BuildContext context, TaskListViewModel taskListViewModel,
      AppLocalizations l10n) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Title
          Text(
            widget.projectId != null ? 'Project Tasks' : l10n.tasksTitle,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          
          // Progress Section (similar to YourProgressSection in Compose)
          _buildProgressSection(context, taskListViewModel, l10n),
          const SizedBox(height: 16),
          
          // Search Field
          TextField(
            controller: _searchController,
            decoration: InputDecoration(
              labelText: l10n.tasksSearchPlaceholder,
              prefixIcon: const Icon(Icons.search),
              filled: true,
              fillColor: Theme.of(context).colorScheme.surface,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide.none,
              ),
            ),
            onChanged: (value) {
              // TODO: Implement search functionality
            },
          ),
        ],
      ),
    );
  }

  Widget _buildProgressSection(BuildContext context,
      TaskListViewModel taskListViewModel, AppLocalizations l10n) {
    final totalTasks = taskListViewModel.totalTasksCount;
    final completedTasks = taskListViewModel.doneTasksCount;
    final progress = totalTasks > 0 ? (completedTasks / totalTasks) : 0.0;

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  l10n.tasksProgressTitle,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  l10n.tasksProgressPercentage((progress * 100).round()),
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            LinearProgressIndicator(
              value: progress,
              backgroundColor: Theme
                  .of(context)
                  .colorScheme
                  .surfaceContainerHighest,
              valueColor: AlwaysStoppedAnimation<Color>(
                Theme.of(context).colorScheme.primary,
              ),
              borderRadius: BorderRadius.circular(4),
              minHeight: 8,
            ),
            const SizedBox(height: 8),
            Text(
              l10n.tasksProgressCompleted(completedTasks, totalTasks),
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme
                    .of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.7),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTaskList(List<TaskDto> tasks, AppLocalizations l10n) {
    if (tasks.isEmpty) {
      return _buildEmptyState(l10n);
    }

    return RefreshIndicator(
      onRefresh: () => Provider.of<TaskListViewModel>(context, listen: false).loadTasks(projectId: widget.projectId),
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: tasks.length,
        itemBuilder: (context, index) {
          final task = tasks[index];
          return _buildTaskCard(task, l10n);
        },
      ),
    );
  }

  Widget _buildTaskCard(TaskDto task, AppLocalizations l10n) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      elevation: 1,
      child: InkWell(
        onTap: () => context.go('/tasks/${task.id}'),
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Main content column
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Title and Priority Row
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Expanded(
                          child: Text(
                            task.title,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.w600,
                              color: task.status == TaskStatus.done
                                  ? Theme.of(context).colorScheme.onSurface.withOpacity(0.6)
                                  : Theme.of(context).colorScheme.onSurface,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        // Priority Badge
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: _getPriorityBackgroundColor(task.priority),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            task.priority.name.toUpperCase(),
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                              color: _getPriorityColor(task.priority),
                            ),
                          ),
                        ),
                      ],
                    ),
                    
                    if (task.description.isNotEmpty) ...[
                      const SizedBox(height: 8),
                      Text(
                        task.description,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurface.withOpacity(0.7),
                        ),
                      ),
                    ],
                    
                    if (task.dueDate != null) ...[
                      const SizedBox(height: 8),
                      Text(
                        '${l10n.taskDueDate} ${_formatDate(task.dueDate!)}',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                        ),
                      ),
                    ],
                    
                    // Project info
                    if (task.projectId != null) ...[
                      const SizedBox(height: 8),
                      Consumer<ProjectViewModel>(
                        builder: (context, projectViewModel, child) {
                          final project = projectViewModel.getProject(task.projectId!);
                          return Text(
                            '${l10n.taskProject} ${project?.name ?? 'Unknown Project'}',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: Theme.of(context).colorScheme.primary,
                            ),
                          );
                        },
                      ),
                    ],
                  ],
                ),
              ),
              
              const SizedBox(width: 12),
              
              // Checkbox (matching Compose)
              Checkbox(
                value: task.status == TaskStatus.done,
                onChanged: (bool? value) {
                  final taskListViewModel = Provider.of<TaskListViewModel>(context, listen: false);
                  if (value == true) {
                    taskListViewModel.changeTaskStatus(task.id, TaskStatus.done);
                  } else {
                    taskListViewModel.changeTaskStatus(task.id, TaskStatus.todo);
                  }
                },
                activeColor: _getPriorityColor(task.priority),
                checkColor: Colors.white,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState(AppLocalizations l10n) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.assignment_outlined,
              size: 80,
              color: Theme
                  .of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              l10n.tasksEmptyTitle,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                color: Theme
                    .of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.7),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              l10n.tasksEmptySubtitle,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Theme
                    .of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.5),
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: () {
                if (widget.projectId != null) {
                  context.go('/task/create?projectId=${widget.projectId}');
                } else {
                  context.go('/task/create');
                }
              },
              icon: const Icon(Icons.add),
              label: Text(l10n.createTask),
            ),
          ],
        ),
      ),
    );
  }

  Color _getPriorityColor(Priority priority) {
    switch (priority) {
      case Priority.high:
        return const Color(0xFFDC2626); // Bright Red
      case Priority.medium:
        return const Color(0xFFEAB308); // Bright Yellow
      case Priority.low:
        return const Color(0xFF22C55E); // Bright Green
    }
  }

  Color _getPriorityBackgroundColor(Priority priority) {
    switch (priority) {
      case Priority.high:
        return const Color(0xFFFFE4E4); // Light Red
      case Priority.medium:
        return const Color(0xFFFEF9C3); // Light Yellow
      case Priority.low:
        return const Color(0xFFDCFCE7); // Light Green
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }

  void _showCreateTaskDialog(BuildContext context) {
    final titleController = TextEditingController();
    final descriptionController = TextEditingController();
    Priority selectedPriority = Priority.medium;
    String? selectedProjectId = widget.projectId;

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
              DropdownButtonFormField<Priority>(
                value: selectedPriority,
                decoration: const InputDecoration(labelText: 'Priority'),
                items: Priority.values.map((priority) {
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
              if (widget.projectId == null) ...[
                const SizedBox(height: 16),
                Consumer<ProjectViewModel>(
                  builder: (context, projectViewModel, child) {
                    return DropdownButtonFormField<String>(
                      value: selectedProjectId,
                      decoration: const InputDecoration(labelText: 'Project'),
                      items: projectViewModel.projects.map((project) {
                        return DropdownMenuItem(
                          value: project.id,
                          child: Text(project.name),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          selectedProjectId = value;
                        });
                      },
                    );
                  },
                ),
              ],
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                if (titleController.text.isNotEmpty && selectedProjectId != null) {
                  final request = TaskCreateRequestDto(
                    title: titleController.text,
                    description: descriptionController.text.isEmpty ? '' : descriptionController.text,
                    priority: selectedPriority,
                    projectId: selectedProjectId,
                  );
                  Provider.of<TaskListViewModel>(context, listen: false).createTask(request);
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

  void _showDeleteConfirmation(BuildContext context, String taskId) {
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
              Provider.of<TaskListViewModel>(context, listen: false).deleteTask(taskId);
              Navigator.of(context).pop();
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
} 
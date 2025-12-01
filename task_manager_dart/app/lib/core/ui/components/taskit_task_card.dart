import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:task_manager_shared/models.dart';

/// Card that displays a task with status, due date and quick actions.
class TaskItTaskCard extends StatelessWidget {
  const TaskItTaskCard({
    super.key,
    required this.task,
    required this.onTap,
    required this.onStatusChanged,
    required this.onDelete,
  });

  final TaskDto task;
  final VoidCallback onTap;
  final ValueChanged<bool> onStatusChanged;
  final VoidCallback onDelete;

  bool get _isCompleted => task.status == TaskStatus.done;

  Color _priorityColor(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    switch (task.priority) {
      case Priority.none:
        return colors.outline;
      case Priority.low:
        return colors.tertiary;
      case Priority.medium:
        return colors.primary;
      case Priority.high:
        return colors.error;
    }
  }

  IconData _priorityIcon() {
    switch (task.priority) {
      case Priority.none:
        return Icons.remove_rounded;
      case Priority.low:
        return Icons.arrow_downward_rounded;
      case Priority.medium:
        return Icons.drag_handle_rounded;
      case Priority.high:
        return Icons.arrow_upward_rounded;
    }
  }

  String? _formattedDueDate() {
    if (task.dueDate == null) {
      return null;
    }
    return DateFormat.yMMMMd().format(task.dueDate!.toLocal());
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dueDateText = _formattedDueDate();

    return InkWell(
      borderRadius: BorderRadius.circular(20),
      onTap: onTap,
      child: Ink(
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: _isCompleted
                ? theme.colorScheme.primary.withOpacity(0.6)
                : theme.colorScheme.outlineVariant,
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Checkbox(
                value: _isCompleted,
                onChanged: (value) => onStatusChanged(value ?? false),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        _PriorityPill(
                          priority: task.priority,
                          color: _priorityColor(context),
                          icon: _priorityIcon(),
                        ),
                        const Spacer(),
                        if (task.assignee != null)
                          Tooltip(
                            message: 'Assigned to ${task.assignee!.displayName}',
                            child: CircleAvatar(
                              radius: 16,
                              backgroundColor: theme.colorScheme.primary,
                              child: Text(
                                task.assignee!.displayName
                                    .split(' ')
                                    .map((part) => part.isNotEmpty
                                        ? part.characters.first
                                        : '')
                                    .take(2)
                                    .join()
                                    .toUpperCase(),
                                style: theme.textTheme.labelMedium?.copyWith(
                                  color: theme.colorScheme.onPrimary,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      task.title,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                        decoration: _isCompleted
                            ? TextDecoration.lineThrough
                            : null,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      task.description,
                      maxLines: 3,
                      overflow: TextOverflow.ellipsis,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                    if (dueDateText != null) ...[
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          Icon(
                            Icons.calendar_month,
                            size: 16,
                            color: theme.colorScheme.primary,
                          ),
                          const SizedBox(width: 6),
                          Text(
                            dueDateText,
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.primary,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(width: 12),
              Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  IconButton(
                    icon: const Icon(Icons.more_vert),
                    tooltip: 'More actions',
                    onPressed: () => _showTaskMenu(context),
                  ),
                  IconButton(
                    icon: const Icon(Icons.delete_outline),
                    tooltip: 'Delete task',
                    onPressed: onDelete,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showTaskMenu(BuildContext context) {
    final RenderBox button = context.findRenderObject()! as RenderBox;
    final box = button.localToGlobal(Offset.zero);

    showMenu<String>(
      context: context,
      position: RelativeRect.fromLTRB(
        box.dx,
        box.dy,
        box.dx + button.size.width,
        box.dy + button.size.height,
      ),
      items: [
        PopupMenuItem<String>(
          value: 'details',
          child: const Text('View details'),
          onTap: onTap,
        ),
        PopupMenuItem<String>(
          value: _isCompleted ? 'mark_incomplete' : 'mark_complete',
          child: Text(_isCompleted ? 'Mark as incomplete' : 'Mark as complete'),
          onTap: () => onStatusChanged(!_isCompleted),
        ),
      ],
    );
  }
}

class _PriorityPill extends StatelessWidget {
  const _PriorityPill({
    required this.priority,
    required this.color,
    required this.icon,
  });

  final Priority priority;
  final Color color;
  final IconData icon;

  String get _label {
    switch (priority) {
      case Priority.none:
        return 'None';
      case Priority.low:
        return 'Low';
      case Priority.medium:
        return 'Medium';
      case Priority.high:
        return 'High';
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(50),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: color),
          const SizedBox(width: 4),
          Text(
            _label,
            style: theme.textTheme.labelMedium?.copyWith(
              color: color,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}


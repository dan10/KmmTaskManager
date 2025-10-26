import 'package:flutter/material.dart';
import 'package:task_manager_shared/models.dart';
import 'package:intl/intl.dart';

/// Task item widget
/// Simplified version of KMM's TaskItem (without swipe and shared transitions)
class TaskItemWidget extends StatelessWidget {
  final TaskDto task;
  final VoidCallback onTap;
  final Function(TaskStatus)? onStatusChanged;
  final VoidCallback? onDelete;

  const TaskItemWidget({
    super.key,
    required this.task,
    required this.onTap,
    this.onStatusChanged,
    this.onDelete,
  });

  Color _getPriorityColor() {
    return switch (task.priority) {
      Priority.high => const Color(0xFFEF4444),
      Priority.medium => const Color(0xFFF59E0B),
      Priority.low => const Color(0xFF10B981),
    };
  }

  Color _getStatusColor() {
    return switch (task.status) {
      TaskStatus.todo => const Color(0xFF6B7280),
      TaskStatus.inProgress => const Color(0xFF3B82F6),
      TaskStatus.done => const Color(0xFF10B981),
    };
  }

  String _formatStatus() {
    return switch (task.status) {
      TaskStatus.todo => 'To Do',
      TaskStatus.inProgress => 'In Progress',
      TaskStatus.done => 'Done',
    };
  }

  String _formatPriority() {
    return switch (task.priority) {
      Priority.high => 'High',
      Priority.medium => 'Medium',
      Priority.low => 'Low',
    };
  }

  bool _isOverdue() {
    if (task.dueDate == null || task.status == TaskStatus.done) {
      return false;
    }
    return task.dueDate!.isBefore(DateTime.now());
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isOverdue = _isOverdue();
    final priorityColor = _getPriorityColor();
    final statusColor = _getStatusColor();
    final indicatorColor = isOverdue ? const Color(0xFFEF4444) : priorityColor;

    final containerColor = switch (task.status) {
      TaskStatus.done => theme.colorScheme.surfaceVariant.withOpacity(0.45),
      _ when isOverdue => const Color(0xFFFFF1F2),
      _ => theme.colorScheme.surface,
    };

    final titleColor = task.status == TaskStatus.done
        ? theme.colorScheme.onSurface.withOpacity(0.65)
        : theme.colorScheme.onSurface;

    final descriptionColor = task.status == TaskStatus.done
        ? theme.colorScheme.onSurfaceVariant.withOpacity(0.6)
        : theme.colorScheme.onSurfaceVariant;

    return Card(
      color: containerColor,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: IntrinsicHeight(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Priority indicator with Hero animation
              Hero(
                tag: 'task_indicator_${task.id}',
                child: Material(
                  color: Colors.transparent,
                  child: Container(
                    width: 4,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          indicatorColor.withOpacity(0.95),
                          indicatorColor.withOpacity(0.65),
                        ],
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                      ),
                      borderRadius: const BorderRadius.only(
                        topLeft: Radius.circular(8),
                        bottomLeft: Radius.circular(8),
                      ),
                    ),
                  ),
                ),
              ),
              // Content
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Title and status
                      Row(
                        children: [
                          Expanded(
                            child: Hero(
                              tag: 'task_title_${task.id}',
                              child: Material(
                                color: Colors.transparent,
                                child: Text(
                                  task.title,
                                  style: theme.textTheme.titleMedium?.copyWith(
                                    fontWeight: FontWeight.w600,
                                    color: titleColor,
                                    decoration: task.status == TaskStatus.done
                                        ? TextDecoration.lineThrough
                                        : null,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Hero(
                            tag: 'task_status_${task.id}',
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 10,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: statusColor.withOpacity(0.12),
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                _formatStatus(),
                                style: theme.textTheme.labelSmall?.copyWith(
                                  color: statusColor,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                      // Description
                      if (task.description.isNotEmpty) ...[
                        const SizedBox(height: 6),
                        Hero(
                          tag: 'task_description_${task.id}',
                          child: Material(
                            color: Colors.transparent,
                            child: Text(
                              task.description,
                              style: theme.textTheme.bodyMedium?.copyWith(
                                color: descriptionColor,
                              ),
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ),
                      ],
                      const SizedBox(height: 12),
                      // Project, priority, and due date
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          // Project
                          if (task.projectName != null)
                            _InfoChip(
                              icon: Icons.folder_outlined,
                              label: task.projectName!,
                              theme: theme,
                            ),
                          // Priority
                          _InfoChip(
                            icon: Icons.flag_outlined,
                            label: _formatPriority(),
                            theme: theme,
                            color: priorityColor,
                          ),
                          // Due date
                          if (task.dueDate != null)
                            _InfoChip(
                              icon: isOverdue ? Icons.warning_outlined : Icons.calendar_today_outlined,
                              label: DateFormat('MMM d, y').format(task.dueDate!),
                              theme: theme,
                              color: isOverdue ? const Color(0xFFEF4444) : null,
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final ThemeData theme;
  final Color? color;

  const _InfoChip({
    required this.icon,
    required this.label,
    required this.theme,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final chipColor = color ?? theme.colorScheme.onSurfaceVariant;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: chipColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            icon,
            size: 14,
            color: chipColor,
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: theme.textTheme.labelSmall?.copyWith(
              color: chipColor,
            ),
          ),
        ],
      ),
    );
  }
}


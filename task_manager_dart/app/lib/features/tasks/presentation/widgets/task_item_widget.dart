import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/l10n/app_l10n.dart';
import '../../../../core/theme/theme.dart';

/// Task item widget
/// Simplified version of KMM's TaskItem (without swipe and shared transitions)
class TaskItemWidget extends StatelessWidget {
  final TaskDto task;
  final VoidCallback onTap;
  final Function(TaskStatus)? onStatusChanged;
  final VoidCallback? onDelete;
  final bool showProjectName;

  const TaskItemWidget({
    super.key,
    required this.task,
    required this.onTap,
    this.onStatusChanged,
    this.onDelete,
    this.showProjectName = true,
  });

  Color _getPriorityColor(BuildContext context) {
    final ext = context.extColors;
    return switch (task.priority) {
      Priority.none => ext.priorityNoneText,
      Priority.high => ext.priorityHighText,
      Priority.medium => ext.priorityMediumText,
      Priority.low => ext.priorityLowText,
    };
  }

  Color _getStatusColor(BuildContext context) {
    final ext = context.extColors;
    return switch (task.status) {
      TaskStatus.todo => ext.statusTodoText,
      TaskStatus.inProgress => ext.statusInProgressText,
      TaskStatus.done => ext.statusDoneText,
    };
  }

  Color _getStatusBackgroundColor(BuildContext context) {
    final ext = context.extColors;
    return switch (task.status) {
      TaskStatus.todo => ext.statusTodoContainer,
      TaskStatus.inProgress => ext.statusInProgressContainer,
      TaskStatus.done => ext.statusDoneContainer,
    };
  }

  Color _getPriorityBackgroundColor(BuildContext context) {
    final ext = context.extColors;
    return switch (task.priority) {
      Priority.none => ext.priorityNoneContainer,
      Priority.high => ext.priorityHighContainer,
      Priority.medium => ext.priorityMediumContainer,
      Priority.low => ext.priorityLowContainer,
    };
  }

  String _formatStatus(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return switch (task.status) {
      TaskStatus.todo => l10n.taskStatusTodo,
      TaskStatus.inProgress => l10n.taskStatusInProgress,
      TaskStatus.done => l10n.taskStatusDone,
    };
  }

  String _formatPriority(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return switch (task.priority) {
      Priority.none => l10n.taskPriorityNone,
      Priority.high => l10n.taskPriorityHigh,
      Priority.medium => l10n.taskPriorityMedium,
      Priority.low => l10n.taskPriorityLow,
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
    final textTheme = theme.textTheme;
    final ext = context.extColors;
    final isOverdue = _isOverdue();
    final priorityColor = _getPriorityColor(context);
    final statusColor = _getStatusColor(context);
    final indicatorColor = isOverdue ? ext.taskIndicatorOverdue : priorityColor;

    final containerColor = switch (task.status) {
      TaskStatus.done => ext.taskContainerDone,
      _ when isOverdue => ext.taskContainerOverdue,
      _ => ext.taskContainerDefault,
    };

    final titleColor = task.status == TaskStatus.done
        ? ext.taskTitleDone
        : ext.taskTitleDefault;

    final descriptionColor = task.status == TaskStatus.done
        ? ext.taskDescriptionDone
        : ext.taskDescriptionDefault;

    final statusLabel = _formatStatus(context);
    final priorityLabel = _formatPriority(context);

    return Hero(
      tag: 'task_card_${task.id}',
      transitionOnUserGestures: true,
      child: Material(
        type: MaterialType.transparency,
        child: SizedBox(
          width: double.infinity,
          child: Card(
            color: containerColor,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            child: InkWell(
              onTap: onTap,
              borderRadius: BorderRadius.circular(8),
              child: ClipRect(
                child: IntrinsicHeight(
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                    _TaskIndicator(
                      indicatorColor: indicatorColor,
                    ),
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: SingleChildScrollView(
                          physics: const NeverScrollableScrollPhysics(),
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              _TaskHeaderRow(
                                task: task,
                                titleColor: titleColor,
                                statusColor: statusColor,
                                statusBackgroundColor: _getStatusBackgroundColor(context),
                                statusLabel: statusLabel,
                                titleStyle: textTheme.titleMedium,
                                statusTextStyle: textTheme.labelSmall,
                              ),
                              if (task.description.isNotEmpty) ...[
                                const SizedBox(height: 6),
                                _TaskDescription(
                                  description: task.description,
                                  style: textTheme.bodyMedium?.copyWith(
                                    color: descriptionColor,
                                  ),
                                ),
                              ],
                              const SizedBox(height: 12),
                              _TaskMetadataChips(
                                task: task,
                                theme: theme,
                                priorityColor: priorityColor,
                                priorityBackgroundColor: _getPriorityBackgroundColor(context),
                                priorityLabel: priorityLabel,
                                isOverdue: isOverdue,
                                showProjectName: showProjectName,
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
  final Color indicatorColor;

  const _TaskIndicator({
    required this.indicatorColor,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 4,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            indicatorColor.withValues(alpha: 0.95),
            indicatorColor.withValues(alpha: 0.65),
          ],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(8),
          bottomLeft: Radius.circular(8),
        ),
      ),
    );
  }
}

class _TaskHeaderRow extends StatelessWidget {
  final TaskDto task;
  final Color titleColor;
  final Color statusColor;
  final Color statusBackgroundColor;
  final String statusLabel;
  final TextStyle? titleStyle;
  final TextStyle? statusTextStyle;

  const _TaskHeaderRow({
    required this.task,
    required this.titleColor,
    required this.statusColor,
    required this.statusBackgroundColor,
    required this.statusLabel,
    required this.titleStyle,
    required this.statusTextStyle,
  });

  @override
  Widget build(BuildContext context) {
    final resolvedTitleStyle = (titleStyle ?? const TextStyle()).copyWith(
      fontWeight: FontWeight.w600,
      color: titleColor,
      decoration:
          task.status == TaskStatus.done ? TextDecoration.lineThrough : null,
    );

    return Row(
      children: [
        Expanded(
          child: Text(
            task.title,
            style: resolvedTitleStyle,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ),
        const SizedBox(width: 8),
        _TaskStatusBadge(
          label: statusLabel,
          statusColor: statusColor,
          statusBackgroundColor: statusBackgroundColor,
          textStyle: statusTextStyle,
        ),
      ],
    );
  }
}

class _TaskStatusBadge extends StatelessWidget {
  final String label;
  final Color statusColor;
  final Color statusBackgroundColor;
  final TextStyle? textStyle;

  const _TaskStatusBadge({
    required this.label,
    required this.statusColor,
    required this.statusBackgroundColor,
    required this.textStyle,
  });

  @override
  Widget build(BuildContext context) {
    final resolvedStyle = (textStyle ?? const TextStyle(fontSize: 12)).copyWith(
      color: statusColor,
    );

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: statusBackgroundColor,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(label, style: resolvedStyle),
    );
  }
}

class _TaskDescription extends StatelessWidget {
  final String description;
  final TextStyle? style;

  const _TaskDescription({
    required this.description,
    required this.style,
  });

  @override
  Widget build(BuildContext context) {
    final resolvedStyle = style ?? const TextStyle(fontSize: 14);

    return Text(
      description,
      style: resolvedStyle,
      maxLines: 2,
      overflow: TextOverflow.ellipsis,
    );
  }
}

class _TaskMetadataChips extends StatelessWidget {
  final TaskDto task;
  final ThemeData theme;
  final Color priorityColor;
  final Color priorityBackgroundColor;
  final String priorityLabel;
  final bool isOverdue;
  final bool showProjectName;

  const _TaskMetadataChips({
    required this.task,
    required this.theme,
    required this.priorityColor,
    required this.priorityBackgroundColor,
    required this.priorityLabel,
    required this.isOverdue,
    required this.showProjectName,
  });

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        if (showProjectName && task.projectName != null)
          _InfoChip(
            icon: Icons.folder_outlined,
            label: task.projectName!,
            theme: theme,
            color: context.extColors.chipProjectText,
            backgroundColor: context.extColors.chipProjectContainer,
          ),
        _InfoChip(
          icon: Icons.flag_outlined,
          label: priorityLabel,
          theme: theme,
          color: priorityColor,
          backgroundColor: priorityBackgroundColor,
        ),
        if (task.dueDate != null)
          _InfoChip(
            icon:
                isOverdue ? Icons.warning_outlined : Icons.calendar_today_outlined,
            label: DateFormat('MMM d, y').format(task.dueDate!),
            theme: theme,
            color: isOverdue ? context.extColors.chipDueDateOverdueText : null,
            backgroundColor: isOverdue 
                ? context.extColors.chipDueDateOverdueContainer 
                : context.extColors.chipDueDateContainer,
          ),
      ],
    );
  }
}

class _InfoChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final ThemeData theme;
  final Color? color;
  final Color? backgroundColor;

  const _InfoChip({
    required this.icon,
    required this.label,
    required this.theme,
    this.color,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    final ext = context.extColors;
    final chipColor = color ?? ext.chipDueDateText;
    final chipBackground = backgroundColor ?? ext.chipDueDateContainer;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: chipBackground,
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


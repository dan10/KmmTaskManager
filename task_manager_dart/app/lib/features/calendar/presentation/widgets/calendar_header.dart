import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

/// Header showing the selected date and task count
class CalendarHeader extends StatelessWidget {
  final DateTime selectedDate;
  final int taskCount;

  const CalendarHeader({
    super.key,
    required this.selectedDate,
    required this.taskCount,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final formattedDate = DateFormat('MMMM d, y').format(selectedDate);

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            'Tasks for $formattedDate',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          Text(
            '$taskCount ${taskCount == 1 ? 'task' : 'tasks'}',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}


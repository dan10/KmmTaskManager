import 'dart:ui';

import 'package:flutter/material.dart';

/// Displays a summary card with the number of completed vs total tasks.
class TaskItProgressSummary extends StatelessWidget {
  const TaskItProgressSummary({
    super.key,
    required this.completedTasks,
    required this.totalTasks,
  });

  final int completedTasks;
  final int totalTasks;

  double get _progress {
    if (totalTasks == 0) {
      return 0;
    }
    return (completedTasks / totalTasks).clamp(0.0, 1.0);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final progress = _progress;

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 0,
      color: theme.colorScheme.primaryContainer.withOpacity(0.35),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Progress',
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: theme.colorScheme.onPrimaryContainer,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  '$completedTasks/$totalTasks',
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: theme.colorScheme.onPrimaryContainer,
                    fontFeatures: const [FontFeature.tabularFigures()],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: LinearProgressIndicator(
                value: progress,
                minHeight: 10,
                backgroundColor:
                    theme.colorScheme.primaryContainer.withOpacity(0.3),
                valueColor: AlwaysStoppedAnimation<Color>(
                  theme.colorScheme.primary,
                ),
              ),
            ),
            const SizedBox(height: 12),
            Text(
              _progressLabel(progress),
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onPrimaryContainer,
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _progressLabel(double progress) {
    final percentage = (progress * 100).round();

    if (progress == 0) {
      return 'Let’s plan your first task!';
    }
    if (progress >= 1) {
      return 'All tasks completed. Great job!';
    }
    if (percentage >= 75) {
      return 'You’re almost done!';
    }
    if (percentage >= 50) {
      return 'Keep up the momentum.';
    }
    return 'Progressing steadily, keep going!';
  }
}


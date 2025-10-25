import 'package:flutter/material.dart';

import 'progress_summary_item.dart';
import 'task_placeholders.dart';
import '../../../../core/ui/components/shimmer.dart';

/// Progress summary sliver - loaded independently
class ProgressSummarySliver extends StatelessWidget {
  final int completedTasks;
  final int totalTasks;
  final bool isLoading;

  const ProgressSummarySliver({
    super.key,
    required this.completedTasks,
    required this.totalTasks,
    required this.isLoading,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return SliverToBoxAdapter(
        child: ShimmerLoading(
          isLoading: true,
          child: const ProgressSummaryPlaceholder(),
        ),
      );
    }

    return SliverToBoxAdapter(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: ProgressSummaryItem(
          completedTasks: completedTasks,
          totalTasks: totalTasks,
        ),
      ),
    );
  }
}


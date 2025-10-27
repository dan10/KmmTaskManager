import 'package:flutter/material.dart';

import '../../../../core/l10n/app_l10n.dart';

/// Empty state widget for tasks list
/// Matches KMM's EmptyTasksList
class EmptyTasksList extends StatelessWidget {
  const EmptyTasksList({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Icon
          Icon(
            Icons.task_alt_outlined,
            size: 120,
            color: theme.colorScheme.primary.withValues(alpha: 0.4),
          ),
          const SizedBox(height: 24),
          // Title
          Text(
            l10n.taskEmptyTitle,
            style: theme.textTheme.titleLarge?.copyWith(
              color: theme.colorScheme.onSurface,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 8),
          // Message
          Text(
            l10n.taskEmptySubtitle,
            style: theme.textTheme.bodyLarge?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          // Suggestions
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _SuggestionItem(
                text: l10n.taskEmptyTip1,
                theme: theme,
              ),
              const SizedBox(height: 8),
              _SuggestionItem(
                text: l10n.taskEmptyTip2,
                theme: theme,
              ),
              const SizedBox(height: 8),
              _SuggestionItem(
                text: l10n.taskEmptyTip3,
                theme: theme,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _SuggestionItem extends StatelessWidget {
  final String text;
  final ThemeData theme;

  const _SuggestionItem({
    required this.text,
    required this.theme,
  });

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: theme.textTheme.bodyLarge?.copyWith(
        color: theme.colorScheme.onSurface,
      ),
    );
  }
}


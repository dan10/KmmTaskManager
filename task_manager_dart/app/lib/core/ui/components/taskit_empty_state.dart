import 'package:flutter/material.dart';

/// Generic TaskIt empty state with icon, title, subtitle and optional action.
class TaskItEmptyState extends StatelessWidget {
  const TaskItEmptyState({
    super.key,
    required this.icon,
    required this.title,
    required this.subtitle,
    this.suggestions = const <String>[],
    this.actionLabel,
    this.onAction,
  });

  /// Main icon displayed at the top of the empty state.
  final IconData icon;

  /// Headline title of the empty state.
  final String title;

  /// Supporting subtitle message.
  final String subtitle;

  /// Optional bullet-point suggestions.
  final List<String> suggestions;

  /// Optional call-to-action button label.
  final String? actionLabel;

  /// Optional callback for the call-to-action button.
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 360),
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Icon(
                icon,
                size: 96,
                color: theme.colorScheme.primary.withOpacity(0.3),
              ),
              const SizedBox(height: 24),
              Text(
                title,
                style: theme.textTheme.headlineSmall?.copyWith(
                  color: theme.colorScheme.onSurface,
                  fontWeight: FontWeight.w600,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Text(
                subtitle,
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
              if (suggestions.isNotEmpty) ...[
                const SizedBox(height: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: suggestions
                      .map(
                        (suggestion) => Padding(
                          padding: const EdgeInsets.only(bottom: 4),
                          child: Text(
                            suggestion,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ),
                      )
                      .toList(),
                ),
              ],
              if (actionLabel != null && onAction != null) ...[
                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: onAction,
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24,
                      vertical: 12,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                  ),
                  child: Text(actionLabel!),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}


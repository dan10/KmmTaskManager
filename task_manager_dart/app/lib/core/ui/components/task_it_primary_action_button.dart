import 'package:flutter/material.dart';

/// Primary action button matching the KMM design
class TaskItPrimaryActionButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final bool enabled;
  final bool isLoading;

  const TaskItPrimaryActionButton({
    super.key,
    required this.text,
    this.onPressed,
    this.enabled = true,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return FilledButton(
      onPressed: (enabled && !isLoading) ? onPressed : null,
      style: FilledButton.styleFrom(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(100), // Fully rounded like Material3
        ),
        backgroundColor: theme.colorScheme.primary,
        foregroundColor: theme.colorScheme.onPrimary,
        disabledBackgroundColor: theme.colorScheme.surfaceContainerHighest,
        disabledForegroundColor: theme.colorScheme.onSurfaceVariant,
      ),
      child: isLoading
          ? SizedBox(
              height: 20,
              width: 20,
              child: CircularProgressIndicator(
                strokeWidth: 2,
                valueColor: AlwaysStoppedAnimation<Color>(
                  theme.colorScheme.onPrimary,
                ),
              ),
            )
          : Text(
              text,
              style: theme.textTheme.labelLarge?.copyWith(
                fontWeight: FontWeight.w600,
                color: theme.colorScheme.onPrimary, // Ensure white text on primary color
              ),
            ),
    );
  }
}

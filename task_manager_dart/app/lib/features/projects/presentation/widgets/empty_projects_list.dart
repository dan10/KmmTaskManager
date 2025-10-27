import 'package:flutter/material.dart';
import '../../../../core/l10n/app_l10n.dart';

/// Empty state widget for projects list
class EmptyProjectsList extends StatelessWidget {
  const EmptyProjectsList({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Folder icon
            Icon(
              Icons.folder_outlined,
              size: 120,
              color: theme.colorScheme.primary.withOpacity(0.5),
            ),
            
            const SizedBox(height: 24),
            
            // Title
            Text(
              l10n.projectsEmptyTitle,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
            ),
            
            const SizedBox(height: 12),
            
            // Subtitle
            Text(
              l10n.projectsEmptySubtitle,
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurface.withOpacity(0.6),
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}


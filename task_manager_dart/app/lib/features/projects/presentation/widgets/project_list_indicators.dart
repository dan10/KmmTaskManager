import 'package:flutter/material.dart';
import '../../../../core/l10n/app_l10n.dart';
import 'empty_projects_list.dart';

/// First page progress indicator
class FirstPageProgressIndicator extends StatelessWidget {
  const FirstPageProgressIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Padding(
        padding: EdgeInsets.all(32.0),
        child: CircularProgressIndicator(),
      ),
    );
  }
}

/// New page progress indicator
class NewPageProgressIndicator extends StatelessWidget {
  const NewPageProgressIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16.0),
      child: Center(
        child: CircularProgressIndicator(),
      ),
    );
  }
}

/// First page error indicator
class FirstPageErrorIndicator extends StatelessWidget {
  final String? errorMessage;
  final VoidCallback onRetry;

  const FirstPageErrorIndicator({
    super.key,
    this.errorMessage,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 48,
              color: Theme.of(context).colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text(
              errorMessage ?? l10n.projectsLoadError,
              style: Theme.of(context).textTheme.bodyLarge,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: Text(l10n.commonRetry),
            ),
          ],
        ),
      ),
    );
  }
}

/// New page error indicator
class NewPageErrorIndicator extends StatelessWidget {
  final VoidCallback onRetry;

  const NewPageErrorIndicator({
    super.key,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Center(
        child: Column(
          children: [
            Text(
              l10n.projectsLoadMoreError,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: onRetry,
              child: Text(l10n.commonRetry),
            ),
          ],
        ),
      ),
    );
  }
}

/// No items found indicator
class NoItemsFoundIndicator extends StatelessWidget {
  const NoItemsFoundIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const EmptyProjectsList();
  }
}

/// No more items indicator
class NoMoreItemsIndicator extends StatelessWidget {
  const NoMoreItemsIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16.0),
      child: Center(
        child: Text(''),
      ),
    );
  }
}


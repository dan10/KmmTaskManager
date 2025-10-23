import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../viewmodels/tasks_viewmodel.dart';
import '../effects/tasks_effect.dart';

/// Effect handler for Tasks screen matching KMM's TasksEffectHandler
/// Handles side effects like navigation and snackbars
class TasksEffectHandler extends StatefulWidget {
  final TasksViewModel viewModel;
  final Widget child;

  const TasksEffectHandler({
    super.key,
    required this.viewModel,
    required this.child,
  });

  @override
  State<TasksEffectHandler> createState() => _TasksEffectHandlerState();
}

class _TasksEffectHandlerState extends State<TasksEffectHandler> {
  @override
  void initState() {
    super.initState();
    _listenToEffects();
  }

  void _listenToEffects() {
    widget.viewModel.effects.listen((effect) {
      if (!mounted) return;

      switch (effect) {
        case ShowSuccessSnackbar():
          _showSnackbar(
            effect.message,
            SnackBarAction(
              label: 'OK',
              onPressed: () {
                ScaffoldMessenger.of(context).hideCurrentSnackBar();
              },
            ),
            duration: const Duration(seconds: 2),
          );
          break;

        case ShowErrorSnackbar():
          _showSnackbar(
            effect.message,
            effect.actionLabel != null && effect.onAction != null
                ? SnackBarAction(
                    label: effect.actionLabel!,
                    onPressed: effect.onAction!,
                  )
                : null,
            backgroundColor: Theme.of(context).colorScheme.error,
            duration: const Duration(seconds: 4),
          );
          break;

        case ShowConfirmationSnackbar():
          _showSnackbar(
            effect.message,
            SnackBarAction(
              label: effect.actionLabel,
              onPressed: effect.onAction,
            ),
            duration: const Duration(seconds: 4),
          );
          break;

        case NavigateToTaskDetail():
          context.go('/tasks/${effect.taskId}');
          break;

        case ShowCreateTaskBottomSheet():
          context.go('/task/create');
          break;
      }
    });
  }

  void _showSnackbar(
    String message,
    SnackBarAction? action, {
    Color? backgroundColor,
    Duration duration = const Duration(seconds: 3),
  }) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        action: action,
        backgroundColor: backgroundColor,
        duration: duration,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}




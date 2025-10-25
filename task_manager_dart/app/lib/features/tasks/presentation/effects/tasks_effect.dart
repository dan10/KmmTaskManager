/// Effects for the Tasks screen
/// Matches KMM's TasksEffect sealed interface
sealed class TasksEffect {
  const TasksEffect();

  // Factory constructors for different effects
  factory TasksEffect.navigateToTaskDetail(String taskId) = NavigateToTaskDetail;
  factory TasksEffect.showCreateTaskBottomSheet() = ShowCreateTaskBottomSheet;
  factory TasksEffect.showSuccessSnackbar(String message) = ShowSuccessSnackbar;
  factory TasksEffect.showErrorSnackbar({
    required String message,
    String? actionLabel,
    VoidCallback? onAction,
  }) = ShowErrorSnackbar;
  factory TasksEffect.showConfirmationSnackbar({
    required String message,
    required String actionLabel,
    required VoidCallback onAction,
  }) = ShowConfirmationSnackbar;
}

class NavigateToTaskDetail extends TasksEffect {
  final String taskId;
  const NavigateToTaskDetail(this.taskId);
}

class ShowCreateTaskBottomSheet extends TasksEffect {
  const ShowCreateTaskBottomSheet();
}

class ShowSuccessSnackbar extends TasksEffect {
  final String message;
  const ShowSuccessSnackbar(this.message);
}

class ShowErrorSnackbar extends TasksEffect {
  final String message;
  final String? actionLabel;
  final VoidCallback? onAction;

  const ShowErrorSnackbar({
    required this.message,
    this.actionLabel,
    this.onAction,
  });
}

class ShowConfirmationSnackbar extends TasksEffect {
  final String message;
  final String actionLabel;
  final VoidCallback onAction;

  const ShowConfirmationSnackbar({
    required this.message,
    required this.actionLabel,
    required this.onAction,
  });
}

// Type alias for callback
typedef VoidCallback = void Function();

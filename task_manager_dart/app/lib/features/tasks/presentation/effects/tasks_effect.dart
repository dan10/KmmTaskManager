/// Side effects for the Tasks screen
/// Matches KMM's TasksEffect sealed class
sealed class TasksEffect {
  const TasksEffect();
}

class ShowConfirmationSnackbar extends TasksEffect {
  final String message;
  final String actionLabel;
  final Function() onAction;

  const ShowConfirmationSnackbar({
    required this.message,
    required this.actionLabel,
    required this.onAction,
  });
}

class ShowErrorSnackbar extends TasksEffect {
  final String message;
  final String? actionLabel;
  final Function()? onAction;

  const ShowErrorSnackbar({
    required this.message,
    this.actionLabel,
    this.onAction,
  });
}

class ShowSuccessSnackbar extends TasksEffect {
  final String message;

  const ShowSuccessSnackbar(this.message);
}

class NavigateToTaskDetail extends TasksEffect {
  final String taskId;

  const NavigateToTaskDetail(this.taskId);
}

class ShowCreateTaskBottomSheet extends TasksEffect {
  const ShowCreateTaskBottomSheet();
}




import 'package:task_manager_shared/models.dart';

/// Actions that can be performed on the Tasks screen
/// Matches KMM's TasksAction sealed interface
sealed class TasksAction {
  const TasksAction();
}

class LoadTasks extends TasksAction {
  const LoadTasks();
}

class RefreshTasks extends TasksAction {
  const RefreshTasks();
}

class LoadMoreTasks extends TasksAction {
  const LoadMoreTasks();
}

class SearchTasks extends TasksAction {
  final String query;

  const SearchTasks(this.query);
}

class OpenCreateTask extends TasksAction {
  const OpenCreateTask();
}

class OpenTaskDetails extends TasksAction {
  final String taskId;

  const OpenTaskDetails(this.taskId);
}

class UpdateTaskStatus extends TasksAction {
  final String taskId;
  final TaskStatus status;

  const UpdateTaskStatus(this.taskId, this.status);
}

class DeleteTask extends TasksAction {
  final String taskId;

  const DeleteTask(this.taskId);
}

class ConfirmTaskCompletion extends TasksAction {
  final String taskId;
  final TaskStatus status;

  const ConfirmTaskCompletion(this.taskId, this.status);
}

class ConfirmTaskDeletion extends TasksAction {
  final String taskId;

  const ConfirmTaskDeletion(this.taskId);
}




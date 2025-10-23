import 'package:flutter/foundation.dart';

/// Represents the result of a command execution
class CommandResult<T> {
  final T? data;
  final Object? error;
  final bool isSuccess;

  const CommandResult.success(this.data)
      : error = null,
        isSuccess = true;

  const CommandResult.error(this.error)
      : data = null,
        isSuccess = false;
}

/// Base class for commands following Flutter's Command pattern
/// Encapsulates an asynchronous operation with loading/error states
abstract class Command<T> extends ChangeNotifier {
  Command() {
    _running = false;
    _completed = false;
    _error = null;
  }

  bool _running = false;
  bool _completed = false;
  Object? _error;
  T? _result;

  /// Whether the command is currently running
  bool get running => _running;

  /// Whether the command has completed successfully
  bool get completed => _completed;

  /// Whether the command resulted in an error
  bool get hasError => _error != null;

  /// The error if command failed
  Object? get error => _error;

  /// The result if command succeeded
  T? get result => _result;

  /// Execute the command
  Future<void> execute();

  /// Clear the command state
  void clear() {
    _running = false;
    _completed = false;
    _error = null;
    _result = null;
    notifyListeners();
  }

  /// Internal method to update running state
  @protected
  void setRunning() {
    _running = true;
    _completed = false;
    _error = null;
    notifyListeners();
  }

  /// Internal method to set success state
  @protected
  void setSuccess(T result) {
    _result = result;
    _running = false;
    _completed = true;
    _error = null;
    notifyListeners();
  }

  /// Internal method to set error state
  @protected
  void setError(Object error) {
    _error = error;
    _running = false;
    _completed = false;
    _result = null;
    notifyListeners();
  }
}

/// Command that takes parameters when executed
abstract class CommandWithParam<T, P> extends ChangeNotifier {
  CommandWithParam() {
    _running = false;
    _completed = false;
    _error = null;
  }

  bool _running = false;
  bool _completed = false;
  Object? _error;
  T? _result;

  /// Whether the command is currently running
  bool get running => _running;

  /// Whether the command has completed successfully
  bool get completed => _completed;

  /// Whether the command resulted in an error
  bool get hasError => _error != null;

  /// The error if command failed
  Object? get error => _error;

  /// The result if command succeeded
  T? get result => _result;

  /// Execute the command with the given parameter
  Future<void> execute(P param);

  /// Clear the command state
  void clear() {
    _running = false;
    _completed = false;
    _error = null;
    _result = null;
    notifyListeners();
  }

  /// Internal method to update running state
  @protected
  void setRunning() {
    _running = true;
    _completed = false;
    _error = null;
    notifyListeners();
  }

  /// Internal method to set success state
  @protected
  void setSuccess(T result) {
    _result = result;
    _running = false;
    _completed = true;
    _error = null;
    notifyListeners();
  }

  /// Internal method to set error state
  @protected
  void setError(Object error) {
    _error = error;
    _running = false;
    _completed = false;
    _result = null;
    notifyListeners();
  }
}

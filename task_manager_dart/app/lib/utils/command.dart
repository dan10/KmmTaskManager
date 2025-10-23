import 'package:flutter/foundation.dart';
import 'result.dart';

/// A simple command with no parameters that returns a Result.
class Command0<R> extends ChangeNotifier {
  Command0(this._executor);

  final Future<Result<R>> Function() _executor;

  bool _running = false;
  bool _completed = false;
  bool _error = false;
  Result<R>? _result;

  bool get running => _running;
  bool get completed => _completed;
  bool get error => _error;
  Result<R>? get result => _result;

  Future<void> execute() async {
    _setRunning();
    try {
      final r = await _executor();
      _result = r;
      if (r is Ok<R>) {
        _setCompleted();
      } else {
        _setError();
      }
    } catch (e, st) {
      _result = Error<R>(e, st);
      _setError();
    }
  }

  void clearResult() {
    _result = null;
    _completed = false;
    _error = false;
    notifyListeners();
  }

  void _setRunning() {
    _running = true;
    _completed = false;
    _error = false;
    notifyListeners();
  }

  void _setCompleted() {
    _running = false;
    _completed = true;
    _error = false;
    notifyListeners();
  }

  void _setError() {
    _running = false;
    _completed = false;
    _error = true;
    notifyListeners();
  }
}

/// A simple command that accepts one parameter and returns a Result.
///
/// Pattern inspired by Flutter samples' Command utilities.
class Command1<R, P> extends ChangeNotifier {
  Command1(this._executor);

  final Future<Result<R>> Function(P) _executor;

  bool _running = false;
  bool _completed = false;
  bool _error = false;
  Result<R>? _result;

  bool get running => _running;
  bool get completed => _completed;
  bool get error => _error;
  Result<R>? get result => _result;

  Future<void> execute(P param) async {
    _setRunning();
    try {
      final r = await _executor(param);
      _result = r;
      if (r is Ok<R>) {
        _setCompleted();
      } else {
        _setError();
      }
    } catch (e, st) {
      _result = Error<R>(e, st);
      _setError();
    }
  }

  void clearResult() {
    _result = null;
    _completed = false;
    _error = false;
    notifyListeners();
  }

  void _setRunning() {
    _running = true;
    _completed = false;
    _error = false;
    notifyListeners();
  }

  void _setCompleted() {
    _running = false;
    _completed = true;
    _error = false;
    notifyListeners();
  }

  void _setError() {
    _running = false;
    _completed = false;
    _error = true;
    notifyListeners();
  }
}



/// Minimal Result type inspired by Flutter samples
sealed class Result<T> {}

class Ok<T> implements Result<T> {
  Ok(this.value);
  final T value;
}

class Error<T> implements Result<T> {
  Error(this.error, [this.stackTrace]);
  final Object error;
  final StackTrace? stackTrace;
}



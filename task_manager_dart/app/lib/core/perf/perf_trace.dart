import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// Cross-platform performance tracing API.
/// Emits markers visible in Perfetto (Android) and Instruments (iOS).
class PerfTrace {
  static const _channel = MethodChannel('perf.trace');
  
  /// Begin an async trace section.
  /// Returns a cookie that must be passed to [end].
  /// 
  /// On release builds, this is a no-op and returns 0.
  static Future<int> begin(String name) async {
    if (kReleaseMode) return 0;
    
    try {
      final cookie = await _channel.invokeMethod<int>('begin', {'name': name});
      return cookie ?? 0;
    } catch (e) {
      debugPrint('PerfTrace.begin failed: $e');
      return 0;
    }
  }
  
  /// End an async trace section.
  /// 
  /// [name] must match the name used in [begin].
  /// [cookie] is the value returned from [begin].
  static Future<void> end(String name, int cookie) async {
    if (kReleaseMode || cookie == 0) return;
    
    try {
      await _channel.invokeMethod('end', {'name': name, 'cookie': cookie});
    } catch (e) {
      debugPrint('PerfTrace.end failed: $e');
    }
  }
}

/// Widget that traces its lifecycle.
/// Emits begin marker when created and end marker when disposed.
/// 
/// Usage:
/// ```dart
/// TracedWidget(
///   name: 'LoginScreen',
///   child: LoginScreenContent(),
/// )
/// ```
/// 
/// The trace will appear as "act:LoginScreen" in Perfetto/Instruments.
class TracedWidget extends StatefulWidget {
  final String name;
  final Widget child;
  
  const TracedWidget({
    super.key,
    required this.name,
    required this.child,
  });
  
  @override
  State<TracedWidget> createState() => _TracedWidgetState();
}

class _TracedWidgetState extends State<TracedWidget> {
  int? _cookie;
  
  @override
  void initState() {
    super.initState();
    PerfTrace.begin(widget.name).then((cookie) {
      if (mounted) {
        setState(() => _cookie = cookie);
      }
    });
  }
  
  @override
  void dispose() {
    if (_cookie != null && _cookie != 0) {
      PerfTrace.end(widget.name, _cookie!);
    }
    super.dispose();
  }
  
  @override
  Widget build(BuildContext context) => widget.child;
}


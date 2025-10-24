import 'package:flutter/material.dart';
import 'package:flutter/widget_previews.dart';

/// Visibility toggle button as a separate widget to isolate rebuilds
class _VisibilityToggleButton extends StatelessWidget {
  final bool obscureText;
  final VoidCallback onToggle;

  const _VisibilityToggleButton({
    required this.obscureText,
    required this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return IconButton(
      icon: Icon(
        obscureText ? Icons.visibility : Icons.visibility_off,
        color: theme.colorScheme.onSurfaceVariant,
      ),
      onPressed: onToggle,
    );
  }
}

/// Custom password input field matching the KMM design
class TaskItPasswordField extends StatefulWidget {
  final TextEditingController controller;
  final String label;
  final String? errorMessage;
  final bool isError;
  final bool enabled;
  final TextInputAction? textInputAction;
  final ValueChanged<String>? onSubmitted;

  const TaskItPasswordField({
    super.key,
    required this.controller,
    required this.label,
    this.errorMessage,
    this.isError = false,
    this.enabled = true,
    this.textInputAction,
    this.onSubmitted,
  });

  @override
  State<TaskItPasswordField> createState() => _TaskItPasswordFieldState();
}

class _TaskItPasswordFieldState extends State<TaskItPasswordField> {
  final ValueNotifier<bool> _obscureTextNotifier = ValueNotifier<bool>(true);

  @override
  void dispose() {
    _obscureTextNotifier.dispose();
    super.dispose();
  }

  void _toggleVisibility() {
    _obscureTextNotifier.value = !_obscureTextNotifier.value;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // Use white background for enabled, grey for disabled
    final fillColor = widget.enabled
        ? Colors.white
        : theme.colorScheme.surfaceContainerHighest;

    return RepaintBoundary(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ValueListenableBuilder<bool>(
            valueListenable: _obscureTextNotifier,
            builder: (context, obscureText, child) {
              return TextField(
                controller: widget.controller,
                enabled: widget.enabled,
                obscureText: obscureText,
                textInputAction: widget.textInputAction,
                onSubmitted: widget.onSubmitted,
                style: TextStyle(color: theme.colorScheme.onSurface),
                decoration: InputDecoration(
                  labelText: widget.label,
                  errorText: widget.isError ? widget.errorMessage : null,
                  filled: true,
                  fillColor: fillColor,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                    borderSide: BorderSide(
                      color: theme.colorScheme.primary,
                      width: 2,
                    ),
                  ),
                  errorBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                    borderSide: BorderSide(
                      color: theme.colorScheme.error,
                      width: 2,
                    ),
                  ),
                  focusedErrorBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                    borderSide: BorderSide(
                      color: theme.colorScheme.error,
                      width: 2,
                    ),
                  ),
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 16,
                  ),
                  suffixIcon: RepaintBoundary(
                    child: _VisibilityToggleButton(
                      obscureText: obscureText,
                      onToggle: _toggleVisibility,
                    ),
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}

// Widget Previews
@Preview(name: 'TaskIt Password Field - Normal')
Widget previewTaskItPasswordFieldNormal() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItPasswordField(
            controller: TextEditingController(text: 'password123'),
            label: 'Password',
          ),
        ),
      ),
    ),
  );
}

@Preview(name: 'TaskIt Password Field - Empty')
Widget previewTaskItPasswordFieldEmpty() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItPasswordField(
            controller: TextEditingController(),
            label: 'Password',
          ),
        ),
      ),
    ),
  );
}

@Preview(name: 'TaskIt Password Field - Error')
Widget previewTaskItPasswordFieldError() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItPasswordField(
            controller: TextEditingController(text: 'short'),
            label: 'Password',
            isError: true,
            errorMessage: 'Password must be at least 8 characters',
          ),
        ),
      ),
    ),
  );
}

@Preview(name: 'TaskIt Password Field - Disabled')
Widget previewTaskItPasswordFieldDisabled() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItPasswordField(
            controller: TextEditingController(text: 'password123'),
            label: 'Password',
            enabled: false,
          ),
        ),
      ),
    ),
  );
}

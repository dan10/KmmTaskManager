import 'package:flutter/material.dart';
import 'package:flutter/widget_previews.dart';

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
  bool _obscureText = true;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // Use white background for enabled, grey for disabled
    final fillColor = widget.enabled
        ? Colors.white
        : theme.colorScheme.surfaceContainerHighest;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextField(
          controller: widget.controller,
          enabled: widget.enabled,
          obscureText: _obscureText,
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
            suffixIcon: IconButton(
              icon: Icon(
                _obscureText ? Icons.visibility : Icons.visibility_off,
                color: theme.colorScheme.onSurfaceVariant,
              ),
              onPressed: () {
                setState(() {
                  _obscureText = !_obscureText;
                });
              },
            ),
          ),
        ),
      ],
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

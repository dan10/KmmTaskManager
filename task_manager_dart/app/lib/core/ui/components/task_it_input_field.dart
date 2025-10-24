import 'package:flutter/material.dart';
import 'package:flutter/widget_previews.dart';

/// Custom text input field matching the KMM design
class TaskItInputField extends StatelessWidget {
  final TextEditingController controller;
  final String label;
  final String? errorMessage;
  final bool isError;
  final bool enabled;
  final TextInputType? keyboardType;
  final TextInputAction? textInputAction;
  final ValueChanged<String>? onSubmitted;

  const TaskItInputField({
    super.key,
    required this.controller,
    required this.label,
    this.errorMessage,
    this.isError = false,
    this.enabled = true,
    this.keyboardType,
    this.textInputAction,
    this.onSubmitted,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // Use white background for enabled, grey for disabled
    final fillColor = enabled
        ? Colors.white
        : theme.colorScheme.surfaceContainerHighest;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextField(
          controller: controller,
          enabled: enabled,
          keyboardType: keyboardType,
          textInputAction: textInputAction,
          onSubmitted: onSubmitted,
          style: TextStyle(color: theme.colorScheme.onSurface),
          decoration: InputDecoration(
            labelText: label,
            errorText: isError ? errorMessage : null,
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
          ),
        ),
      ],
    );
  }
}

// Widget Previews
@Preview(name: 'TaskIt Input Field - Normal')
Widget previewTaskItInputFieldNormal() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItInputField(
            controller: TextEditingController(text: 'Sample text'),
            label: 'Email',
          ),
        ),
      ),
    ),
  );
}

@Preview(name: 'TaskIt Input Field - Empty')
Widget previewTaskItInputFieldEmpty() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItInputField(
            controller: TextEditingController(),
            label: 'Email',
          ),
        ),
      ),
    ),
  );
}

@Preview(name: 'TaskIt Input Field - Error')
Widget previewTaskItInputFieldError() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItInputField(
            controller: TextEditingController(text: 'invalid'),
            label: 'Email',
            isError: true,
            errorMessage: 'Please enter a valid email address',
          ),
        ),
      ),
    ),
  );
}

@Preview(name: 'TaskIt Input Field - Disabled')
Widget previewTaskItInputFieldDisabled() {
  return MaterialApp(
    home: Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: TaskItInputField(
            controller: TextEditingController(text: 'disabled@example.com'),
            label: 'Email',
            enabled: false,
          ),
        ),
      ),
    ),
  );
}

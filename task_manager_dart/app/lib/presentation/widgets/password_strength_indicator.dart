import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../core/utils/validation_utils.dart';

/// Widget that displays password strength with visual indicators
class PasswordStrengthIndicator extends StatelessWidget {
  final String password;
  final bool showText;

  const PasswordStrengthIndicator({
    super.key,
    required this.password,
    this.showText = true,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final strength = ValidationUtils.getPasswordStrength(password);
    final strengthText = ValidationUtils.getPasswordStrengthText(
        strength, l10n);
    final strengthColor = Color(
        ValidationUtils.getPasswordStrengthColor(strength));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Strength bars
        Row(
          children: List.generate(5, (index) {
            return Expanded(
              child: Container(
                height: 4,
                margin: EdgeInsets.only(right: index < 4 ? 4 : 0),
                decoration: BoxDecoration(
                  color: index < strength
                      ? strengthColor
                      : Theme
                      .of(context)
                      .colorScheme
                      .outline
                      .withOpacity(0.3),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            );
          }),
        ),

        if (showText && password.isNotEmpty) ...[
          const SizedBox(height: 4),
          Text(
            strengthText,
            style: Theme
                .of(context)
                .textTheme
                .bodySmall
                ?.copyWith(
              color: strengthColor,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ],
    );
  }
}

/// Widget that shows password requirements checklist
class PasswordRequirementsWidget extends StatelessWidget {
  final String password;

  const PasswordRequirementsWidget({
    super.key,
    required this.password,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    final requirements = [
      {
        'text': 'At least 8 characters',
        'met': password.length >= 8,
      },
      {
        'text': 'Contains lowercase letter',
        'met': RegExp(r'[a-z]').hasMatch(password),
      },
      {
        'text': 'Contains uppercase letter',
        'met': RegExp(r'[A-Z]').hasMatch(password),
      },
      {
        'text': 'Contains number',
        'met': RegExp(r'[0-9]').hasMatch(password),
      },
      {
        'text': 'Contains special character',
        'met': RegExp(r'[!@#$%^&*(),.?":{}|<>]').hasMatch(password),
      },
    ];

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceVariant.withOpacity(0.3),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: theme.colorScheme.outline.withOpacity(0.2),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Password Requirements:',
            style: theme.textTheme.bodyMedium?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          ...requirements.map((req) =>
              Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Row(
                  children: [
                    Icon(
                      req['met'] as bool ? Icons.check_circle : Icons
                          .radio_button_unchecked,
                      size: 16,
                      color: req['met'] as bool
                          ? theme.colorScheme.primary
                          : theme.colorScheme.outline,
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        req['text'] as String,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: req['met'] as bool
                              ? theme.colorScheme.onSurface
                              : theme.colorScheme.outline,
                        ),
                      ),
                    ),
                  ],
                ),
              )),
        ],
      ),
    );
  }
} 
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/localization/applocalization.dart';
import '../../../core/routing/app_router.dart';
import '../../../core/ui/components/task_it_input_field.dart';
import '../../../core/ui/components/task_it_password_field.dart';
import '../../../core/ui/components/task_it_primary_action_button.dart';
import '../view_models/register_viewmodel.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key, required this.viewModel});

  final RegisterViewModel viewModel;

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final TextEditingController _name = TextEditingController();
  final TextEditingController _email = TextEditingController();
  final TextEditingController _password = TextEditingController();
  final TextEditingController _confirmPassword = TextEditingController();

  @override
  void initState() {
    super.initState();
    widget.viewModel.register.addListener(_onResult);
    
    // Listen to text changes for real-time validation
    _name.addListener(_onTextChanged);
    _email.addListener(_onTextChanged);
    _password.addListener(_onTextChanged);
    _confirmPassword.addListener(_onTextChanged);
  }

  @override
  void didUpdateWidget(covariant RegisterScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    oldWidget.viewModel.register.removeListener(_onResult);
    widget.viewModel.register.addListener(_onResult);
  }

  @override
  void dispose() {
    widget.viewModel.register.removeListener(_onResult);
    _name.removeListener(_onTextChanged);
    _email.removeListener(_onTextChanged);
    _password.removeListener(_onTextChanged);
    _confirmPassword.removeListener(_onTextChanged);
    _name.dispose();
    _email.dispose();
    _password.dispose();
    _confirmPassword.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    widget.viewModel.validateForm(
      _name.text,
      _email.text,
      _password.text,
      _confirmPassword.text,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalization.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.primaryContainer,
      body: Center(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: FractionallySizedBox(
              widthFactor: 0.9,
              child: Card(
                elevation: 8,
                color: theme.colorScheme.surface,
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16.0,
                    vertical: 24.0,
                  ),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      // App Logo Icon
                      Icon(
                        Icons.task_alt_rounded,
                        size: 64,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(height: 8),

                      // Create Account Title
                      Text(
                        'Create Account',
                        style: theme.textTheme.headlineMedium?.copyWith(
                          color: theme.colorScheme.onSurface,
                        ),
                      ),
                      const SizedBox(height: 8),

                      // Name Field
                      ValueListenableBuilder<bool>(
                        valueListenable: widget.viewModel.nameHasError,
                        builder: (context, hasError, _) {
                          return TaskItInputField(
                            controller: _name,
                            label: l10n.name,
                            keyboardType: TextInputType.name,
                            textInputAction: TextInputAction.next,
                            enabled: !widget.viewModel.register.running,
                            isError: hasError,
                            errorMessage: l10n.nameError,
                          );
                        },
                      ),
                      const SizedBox(height: 8),

                      // Email Field
                      ValueListenableBuilder<bool>(
                        valueListenable: widget.viewModel.emailHasError,
                        builder: (context, hasError, _) {
                          return TaskItInputField(
                            controller: _email,
                            label: l10n.email,
                            keyboardType: TextInputType.emailAddress,
                            textInputAction: TextInputAction.next,
                            enabled: !widget.viewModel.register.running,
                            isError: hasError,
                            errorMessage: l10n.emailError,
                          );
                        },
                      ),
                      const SizedBox(height: 8),

                      // Password Field
                      ValueListenableBuilder<bool>(
                        valueListenable: widget.viewModel.passwordHasError,
                        builder: (context, hasError, _) {
                          return TaskItPasswordField(
                            controller: _password,
                            label: l10n.password,
                            textInputAction: TextInputAction.next,
                            enabled: !widget.viewModel.register.running,
                            isError: hasError,
                            errorMessage: l10n.passwordError,
                          );
                        },
                      ),
                      const SizedBox(height: 8),

                      // Confirm Password Field
                      ValueListenableBuilder<bool>(
                        valueListenable: widget.viewModel.confirmPasswordHasError,
                        builder: (context, hasError, _) {
                          return TaskItPasswordField(
                            controller: _confirmPassword,
                            label: l10n.confirmPassword,
                            textInputAction: TextInputAction.done,
                            enabled: !widget.viewModel.register.running,
                            onSubmitted: (_) => _handleRegister(),
                            isError: hasError,
                            errorMessage: l10n.confirmPasswordError,
                          );
                        },
                      ),
                      const SizedBox(height: 8),

                      // Register Button
                      ValueListenableBuilder<bool>(
                        valueListenable: widget.viewModel.isFormValid,
                        builder: (context, isValid, _) {
                          return ListenableBuilder(
                            listenable: widget.viewModel.register,
                            builder: (context, _) {
                              return SizedBox(
                                width: double.infinity,
                                child: TaskItPrimaryActionButton(
                                  text: l10n.register,
                                  onPressed: _handleRegister,
                                  enabled: isValid && !widget.viewModel.register.running,
                                  isLoading: widget.viewModel.register.running,
                                ),
                              );
                            },
                          );
                        },
                      ),
                      const SizedBox(height: 16),

                      // Sign In Link
                      _RegisterAccountLink(
                        onLinkClick: () {
                          context.go(AppRoutes.login);
                        },
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  void _handleRegister() {
    if (!widget.viewModel.register.running) {
      widget.viewModel.register.execute((
        _name.text,
        _email.text,
        _password.text,
        _confirmPassword.text,
      ));
    }
  }

  void _onResult() {
    if (widget.viewModel.register.completed) {
      widget.viewModel.register.clearResult();
      context.go(AppRoutes.home);
    }

    if (widget.viewModel.register.error) {
      widget.viewModel.register.clearResult();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(AppLocalization.of(context).errorWhileLogin),
          action: SnackBarAction(
            label: AppLocalization.of(context).tryAgain,
            onPressed: _handleRegister,
          ),
        ),
      );
    }
  }
}

class _RegisterAccountLink extends StatelessWidget {
  final VoidCallback onLinkClick;

  const _RegisterAccountLink({required this.onLinkClick});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalization.of(context);

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          l10n.alreadyHaveAccount,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        TextButton(
          onPressed: onLinkClick,
          child: Text(
            l10n.signIn,
            style: theme.textTheme.labelLarge?.copyWith(
              color: theme.colorScheme.primary,
            ),
          ),
        ),
      ],
    );
  }
}

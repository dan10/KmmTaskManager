import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:go_router/go_router.dart';

import '../../../core/localization/applocalization.dart';
import '../../../core/routing/app_router.dart';
import '../../../core/ui/components/task_it_input_field.dart';
import '../../../core/ui/components/task_it_password_field.dart';
import '../../../core/ui/components/task_it_primary_action_button.dart';
import '../view_models/login_viewmodel.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key, required this.viewModel});

  final LoginViewModel viewModel;

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController _email = TextEditingController();
  final TextEditingController _password = TextEditingController();
  Timer? _debounceTimer;

  @override
  void initState() {
    super.initState();
    widget.viewModel.login.addListener(_onResult);
    
    // Listen to text changes for debounced validation
    _email.addListener(_onTextChanged);
    _password.addListener(_onTextChanged);
  }

  @override
  void didUpdateWidget(covariant LoginScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    oldWidget.viewModel.login.removeListener(_onResult);
    widget.viewModel.login.addListener(_onResult);
  }

  @override
  void dispose() {
    _debounceTimer?.cancel();
    widget.viewModel.login.removeListener(_onResult);
    _email.removeListener(_onTextChanged);
    _password.removeListener(_onTextChanged);
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    // Cancel previous timer
    _debounceTimer?.cancel();
    
    // Start new timer - validate after 300ms of no typing
    _debounceTimer = Timer(const Duration(milliseconds: 300), () {
      widget.viewModel.validateForm(_email.text, _password.text);
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalization.of(context);

    return Scaffold(
      resizeToAvoidBottomInset: false, // Keep screen fixed when keyboard appears
      backgroundColor: theme.colorScheme.primaryContainer,
      body: Center(
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
                    spacing: 8.0,
                    children: [
                      // App Logo Icon
                      SvgPicture.asset(
                        'assets/icons/app_icon.svg',
                        width: 108,
                        height: 108,
                        clipBehavior: Clip.none,
                        allowDrawingOutsideViewBox: true,
                        fit: BoxFit.contain,
                      ),

                    // App Name
                    Text(
                      l10n.appName,
                      style: theme.textTheme.headlineLarge?.copyWith(
                        color: theme.colorScheme.onSurface,
                      ),
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
                          enabled: !widget.viewModel.login.running,
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
                          textInputAction: TextInputAction.done,
                          enabled: !widget.viewModel.login.running,
                          onSubmitted: (_) => _handleLogin(),
                          isError: hasError,
                          errorMessage: l10n.passwordError,
                        );
                      },
                    ),
                    const SizedBox(height: 16),

                    // Login Button
                    ValueListenableBuilder<bool>(
                      valueListenable: widget.viewModel.isFormValid,
                      builder: (context, isValid, _) {
                        return ListenableBuilder(
                          listenable: widget.viewModel.login,
                          builder: (context, _) {
                            return SizedBox(
                              width: double.infinity,
                              child: TaskItPrimaryActionButton(
                                text: l10n.login,
                                onPressed: _handleLogin,
                                enabled: isValid && !widget.viewModel.login.running,
                                isLoading: widget.viewModel.login.running,
                              ),
                            );
                          },
                        );
                      },
                    ),
                    const SizedBox(height: 8),

                    // Sign Up Link
                    _LoginAccountLink(
                      onLinkClick: () {
                        context.go(AppRoutes.register);
                      },
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  void _handleLogin() {
    if (!widget.viewModel.login.running) {
      widget.viewModel.login.execute((
        _email.value.text,
        _password.value.text,
      ));
    }
  }

  void _onResult() {
    if (widget.viewModel.login.completed) {
      widget.viewModel.login.clearResult();
      context.go(AppRoutes.home);
    }

    if (widget.viewModel.login.error) {
      widget.viewModel.login.clearResult();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(AppLocalization.of(context).errorWhileLogin),
          action: SnackBarAction(
            label: AppLocalization.of(context).tryAgain,
            onPressed: _handleLogin,
          ),
        ),
      );
    }
  }
}

class _LoginAccountLink extends StatelessWidget {
  final VoidCallback onLinkClick;

  const _LoginAccountLink({required this.onLinkClick});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalization.of(context);

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          l10n.withoutAccount,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        TextButton(
          onPressed: onLinkClick,
          child: Text(
            l10n.signUp,
            style: theme.textTheme.labelLarge?.copyWith(
              color: theme.colorScheme.primary,
            ),
          ),
        ),
      ],
    );
  }
}



import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'dart:math' as math;

import '../../viewmodels/login_viewmodel.dart';
import '../../viewmodels/auth_viewmodel.dart';
import '../../../core/utils/validation_utils.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key});

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  // Validation state
  String? _emailError;
  String? _passwordError;
  bool _hasAttemptedSubmit = false;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _validateForm() {
    final l10n = AppLocalizations.of(context)!;
    final validationResults = ValidationUtils.validateLoginForm(
      email: _emailController.text,
      password: _passwordController.text,
      l10n: l10n,
    );

    setState(() {
      _emailError = validationResults['email'];
      _passwordError = validationResults['password'];
    });
  }

  void _onEmailChanged(String value) {
    if (_hasAttemptedSubmit) {
      final l10n = AppLocalizations.of(context)!;
      setState(() {
        _emailError = ValidationUtils.validateEmail(value, l10n);
      });
    }
  }

  void _onPasswordChanged(String value) {
    if (_hasAttemptedSubmit) {
      final l10n = AppLocalizations.of(context)!;
      setState(() {
        _passwordError = value.isEmpty ? l10n.validationPasswordRequired : null;
      });
    }
  }

  Future<void> _handleLogin() async {
    setState(() {
      _hasAttemptedSubmit = true;
    });

    _validateForm();

    if (_emailError == null && _passwordError == null) {
      final loginViewModel = Provider.of<LoginViewModel>(
          context, listen: false);
      await loginViewModel.login(
          _emailController.text.trim(), _passwordController.text);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Scaffold(
      backgroundColor: Theme
          .of(context)
          .colorScheme
          .secondaryContainer,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: ConstrainedBox(
              constraints: BoxConstraints(
                minHeight: math.max(0, MediaQuery
                    .of(context)
                    .size
                    .height -
                    MediaQuery
                        .of(context)
                        .padding
                        .top -
                    MediaQuery
                        .of(context)
                        .padding
                        .bottom - 32),
              ),
              child: IntrinsicHeight(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Consumer2<LoginViewModel, AuthViewModel>(
                      builder: (context, loginViewModel, authViewModel, child) {
                        // Handle successful login
                        WidgetsBinding.instance.addPostFrameCallback((_) {
                          if (loginViewModel.isSuccess &&
                              loginViewModel.user != null) {
                            // Set authenticated state in main auth view model
                            authViewModel.setAuthenticated(
                                loginViewModel.user!);
                            // Reset login view model for next time
                            loginViewModel.reset();
                            // Navigate to home
                            context.go('/');
                          }
                        });

                        return Form(
                          key: _formKey,
                          child: Card(
                            elevation: 8,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(16),
                            ),
                            child: Container(
                              width: MediaQuery
                                  .of(context)
                                  .size
                                  .width * 0.9,
                              padding: const EdgeInsets.fromLTRB(
                                  16, 24, 16, 24),
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                crossAxisAlignment: CrossAxisAlignment.center,
                                children: [
                                  // App Logo
                                  Container(
                                    width: 80,
                                    height: 80,
                                    decoration: BoxDecoration(
                                      color: Theme
                                          .of(context)
                                          .colorScheme
                                          .primary,
                                      borderRadius: BorderRadius.circular(16),
                                    ),
                                    child: Icon(
                                      Icons.task_alt,
                                      size: 40,
                                      color: Theme
                                          .of(context)
                                          .colorScheme
                                          .onPrimary,
                                    ),
                                  ),
                                  const SizedBox(height: 16),

                                  // App Name
                                  Text(
                                    l10n.authAppName,
                                    style: Theme
                                        .of(context)
                                        .textTheme
                                        .headlineMedium
                                        ?.copyWith(
                                      fontWeight: FontWeight.bold,
                                      color: Theme
                                          .of(context)
                                          .colorScheme
                                          .onSurface,
                                    ),
                                  ),
                                  const SizedBox(height: 24),

                                  // Email Field
                                  TextFormField(
                                    controller: _emailController,
                                    decoration: InputDecoration(
                                      labelText: l10n.authEmail,
                                      errorText: _emailError,
                                      prefixIcon: const Icon(
                                          Icons.email_outlined),
                                    ),
                                    keyboardType: TextInputType.emailAddress,
                                    enabled: !loginViewModel.isLoading,
                                    onChanged: _onEmailChanged,
                                    textInputAction: TextInputAction.next,
                                  ),
                                  const SizedBox(height: 16),

                                  // Password Field
                                  TextFormField(
                                    controller: _passwordController,
                                    decoration: InputDecoration(
                                      labelText: l10n.authPassword,
                                      errorText: _passwordError,
                                      prefixIcon: const Icon(
                                          Icons.lock_outlined),
                                    ),
                                    obscureText: true,
                                    enabled: !loginViewModel.isLoading,
                                    onChanged: _onPasswordChanged,
                                    textInputAction: TextInputAction.done,
                                    onFieldSubmitted: (_) => _handleLogin(),
                                  ),
                                  const SizedBox(height: 24),

                                  // Error Message from ViewModel
                                  if (loginViewModel.state == LoginState.error)
                                    Container(
                                      width: double.infinity,
                                      padding: const EdgeInsets.all(12),
                                      margin: const EdgeInsets.only(bottom: 16),
                                      decoration: BoxDecoration(
                                        color: Theme
                                            .of(context)
                                            .colorScheme
                                            .errorContainer,
                                        borderRadius: BorderRadius.circular(8),
                                      ),
                                      child: Row(
                                        children: [
                                          Icon(
                                            Icons.error_outline,
                                            color: Theme
                                                .of(context)
                                                .colorScheme
                                                .onErrorContainer,
                                          ),
                                          const SizedBox(width: 8),
                                          Expanded(
                                            child: Text(
                                              loginViewModel.errorMessage ??
                                                  'An error occurred',
                                              style: TextStyle(
                                                color: Theme
                                                    .of(context)
                                                    .colorScheme
                                                    .onErrorContainer,
                                              ),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),

                                  // Login Button
                                  SizedBox(
                                    width: double.infinity,
                                    height: 56,
                                    child: ElevatedButton(
                                      onPressed: loginViewModel.isLoading
                                          ? null
                                          : _handleLogin,
                                      child: loginViewModel.isLoading
                                          ? SizedBox(
                                        width: 24,
                                        height: 24,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                          valueColor: AlwaysStoppedAnimation<
                                              Color>(
                                            Theme
                                                .of(context)
                                                .colorScheme
                                                .onPrimary,
                                          ),
                                        ),
                                      )
                                          : Text(l10n.authLoginButton),
                                    ),
                                  ),
                                  const SizedBox(height: 16),

                                  // Register Link
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Text(
                                        l10n.authWithoutAccount,
                                        style: TextStyle(
                                          color: Theme
                                              .of(context)
                                              .colorScheme
                                              .onSurface
                                              .withOpacity(0.7),
                                        ),
                                      ),
                                      TextButton(
                                        onPressed: () =>
                                            context.go('/register'),
                                        child: Text(
                                          l10n.authSignUp,
                                          style: TextStyle(
                                            color: Theme
                                                .of(context)
                                                .colorScheme
                                                .primary,
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),

                                  // Demo Credentials Info
                                  const SizedBox(height: 16),
                                  Container(
                                    padding: const EdgeInsets.all(12),
                                    decoration: BoxDecoration(
                                      color: Theme
                                          .of(context)
                                          .colorScheme
                                          .primaryContainer,
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                    child: Column(
                                      children: [
                                        Text(
                                          l10n.authDemoCredentials,
                                          style: TextStyle(
                                            fontWeight: FontWeight.bold,
                                            color: Theme
                                                .of(context)
                                                .colorScheme
                                                .onPrimaryContainer,
                                          ),
                                        ),
                                        const SizedBox(height: 4),
                                        Text(
                                          l10n.authDemoCredentialsText,
                                          textAlign: TextAlign.center,
                                          style: TextStyle(
                                            fontSize: 12,
                                            color: Theme
                                                .of(context)
                                                .colorScheme
                                                .onPrimaryContainer,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        );
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
} 
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'dart:math' as math;

import '../../viewmodels/register_viewmodel.dart';
import '../../viewmodels/auth_viewmodel.dart';
import '../../widgets/password_strength_indicator.dart';
import '../../../core/utils/validation_utils.dart';

class RegisterView extends StatefulWidget {
  const RegisterView({super.key});

  @override
  State<RegisterView> createState() => _RegisterViewState();
}

class _RegisterViewState extends State<RegisterView> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();

  // Validation state
  String? _nameError;
  String? _emailError;
  String? _passwordError;
  String? _confirmPasswordError;
  bool _hasAttemptedSubmit = false;
  bool _showPasswordRequirements = false;

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  void _validateForm() {
    final l10n = AppLocalizations.of(context)!;
    final validationResults = ValidationUtils.validateRegisterForm(
      name: _nameController.text,
      email: _emailController.text,
      password: _passwordController.text,
      confirmPassword: _confirmPasswordController.text,
      l10n: l10n,
    );

    setState(() {
      _nameError = validationResults['name'];
      _emailError = validationResults['email'];
      _passwordError = validationResults['password'];
      _confirmPasswordError = validationResults['confirmPassword'];
    });
  }

  void _onNameChanged(String value) {
    if (_hasAttemptedSubmit) {
      final l10n = AppLocalizations.of(context)!;
      setState(() {
        _nameError = ValidationUtils.validateName(value, l10n);
      });
    }
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
        _passwordError = ValidationUtils.validatePassword(value, l10n);
        _confirmPasswordError = ValidationUtils.validatePasswordConfirmation(
          value,
          _confirmPasswordController.text,
          l10n,
        );
      });
    }
  }

  void _onConfirmPasswordChanged(String value) {
    if (_hasAttemptedSubmit) {
      final l10n = AppLocalizations.of(context)!;
      setState(() {
        _confirmPasswordError = ValidationUtils.validatePasswordConfirmation(
          _passwordController.text,
          value,
          l10n,
        );
      });
    }
  }

  Future<void> _handleRegister() async {
    setState(() {
      _hasAttemptedSubmit = true;
    });

    _validateForm();

    if (_nameError == null &&
        _emailError == null &&
        _passwordError == null &&
        _confirmPasswordError == null) {
      final registerViewModel = Provider.of<RegisterViewModel>(
          context, listen: false);
      await registerViewModel.register(
        _nameController.text.trim(),
        _emailController.text.trim(),
        _passwordController.text,
      );
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
                    Consumer2<RegisterViewModel, AuthViewModel>(
                      builder: (context, registerViewModel, authViewModel,
                          child) {
                        // Handle successful registration
                        WidgetsBinding.instance.addPostFrameCallback((_) {
                          if (registerViewModel.isSuccess &&
                              registerViewModel.user != null) {
                            // Set authenticated state in main auth view model
                            authViewModel.setAuthenticated(
                                registerViewModel.user!);
                            // Reset register view model for next time
                            registerViewModel.reset();
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

                                  // Name Field
                                  TextFormField(
                                    controller: _nameController,
                                    decoration: InputDecoration(
                                      labelText: l10n.authName,
                                      errorText: _nameError,
                                      prefixIcon: const Icon(
                                          Icons.person_outlined),
                                    ),
                                    textCapitalization: TextCapitalization
                                        .words,
                                    enabled: !registerViewModel.isLoading,
                                    onChanged: _onNameChanged,
                                    textInputAction: TextInputAction.next,
                                  ),
                                  const SizedBox(height: 16),

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
                                    enabled: !registerViewModel.isLoading,
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
                                      suffixIcon: IconButton(
                                        icon: Icon(
                                          _showPasswordRequirements
                                              ? Icons.info
                                              : Icons.info_outline,
                                        ),
                                        onPressed: () {
                                          setState(() {
                                            _showPasswordRequirements =
                                            !_showPasswordRequirements;
                                          });
                                        },
                                        tooltip: 'Show password requirements',
                                      ),
                                    ),
                                    obscureText: true,
                                    enabled: !registerViewModel.isLoading,
                                    onChanged: (value) {
                                      _onPasswordChanged(value);
                                      setState(() {}); // Trigger rebuild for password strength
                                    },
                                    textInputAction: TextInputAction.next,
                                  ),

                                  // Password Strength Indicator
                                  if (_passwordController.text.isNotEmpty) ...[
                                    const SizedBox(height: 8),
                                    PasswordStrengthIndicator(
                                      password: _passwordController.text,
                                    ),
                                  ],

                                  // Password Requirements (expandable)
                                  if (_showPasswordRequirements) ...[
                                    const SizedBox(height: 12),
                                    PasswordRequirementsWidget(
                                      password: _passwordController.text,
                                    ),
                                  ],

                                  const SizedBox(height: 16),

                                  // Confirm Password Field
                                  TextFormField(
                                    controller: _confirmPasswordController,
                                    decoration: InputDecoration(
                                      labelText: l10n.authConfirmPassword,
                                      errorText: _confirmPasswordError,
                                      prefixIcon: const Icon(
                                          Icons.lock_outlined),
                                    ),
                                    obscureText: true,
                                    enabled: !registerViewModel.isLoading,
                                    onChanged: _onConfirmPasswordChanged,
                                    textInputAction: TextInputAction.done,
                                    onFieldSubmitted: (_) => _handleRegister(),
                                  ),
                                  const SizedBox(height: 24),

                                  // Error Message from ViewModel
                                  if (registerViewModel.state ==
                                      RegisterState.error)
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
                                              registerViewModel.errorMessage ??
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

                                  // Register Button
                                  SizedBox(
                                    width: double.infinity,
                                    height: 56,
                                    child: ElevatedButton(
                                      onPressed: registerViewModel.isLoading
                                          ? null
                                          : _handleRegister,
                                      child: registerViewModel.isLoading
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
                                          : Text(l10n.authRegisterButton),
                                    ),
                                  ),
                                  const SizedBox(height: 16),

                                  // Login Link
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Text(
                                        l10n.authAlreadyHaveAccount,
                                        style: TextStyle(
                                          color: Theme
                                              .of(context)
                                              .colorScheme
                                              .onSurface
                                              .withOpacity(0.7),
                                        ),
                                      ),
                                      TextButton(
                                        onPressed: () => context.go('/login'),
                                        child: Text(
                                          l10n.authSignIn,
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
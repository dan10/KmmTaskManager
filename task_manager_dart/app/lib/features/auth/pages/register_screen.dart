import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/localization/applocalization.dart';
import '../../../core/themes/dimens.dart';
import '../view_models/register_viewmodel.dart';
import 'tilted_cards.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key, required this.viewModel});

  final RegisterViewModel viewModel;

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final TextEditingController _name = TextEditingController(text: 'John Doe');
  final TextEditingController _email = TextEditingController(text: 'email@example.com');
  final TextEditingController _password = TextEditingController(text: 'password');

  @override
  void initState() {
    super.initState();
    widget.viewModel.register.addListener(_onResult);
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
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            const TiltedCards(),
            Padding(
              padding: Dimens.of(context).edgeInsetsScreenSymmetric,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  TextField(controller: _name),
                  const SizedBox(height: Dimens.paddingVertical),
                  TextField(controller: _email),
                  const SizedBox(height: Dimens.paddingVertical),
                  TextField(controller: _password, obscureText: true),
                  const SizedBox(height: Dimens.paddingVertical),
                  ListenableBuilder(
                    listenable: widget.viewModel.register,
                    builder: (context, _) {
                      return FilledButton(
                        onPressed: () {
                          widget.viewModel.register.execute((
                            _name.text,
                            _email.text,
                            _password.text,
                          ));
                        },
                        child: Text(AppLocalization.of(context).login),
                      );
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _onResult() {
    if (widget.viewModel.register.completed) {
      widget.viewModel.register.clearResult();
      context.go('/');
    }

    if (widget.viewModel.register.error) {
      widget.viewModel.register.clearResult();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(AppLocalization.of(context).errorWhileLogin),
          action: SnackBarAction(
            label: AppLocalization.of(context).tryAgain,
            onPressed: () => widget.viewModel.register.execute((
              _name.text,
              _email.text,
              _password.text,
            )),
          ),
        ),
      );
    }
  }
}



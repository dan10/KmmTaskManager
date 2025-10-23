import 'package:flutter/material.dart';

import '../../../core/localization/applocalization.dart';

class TaskItPasswordField extends StatelessWidget {
  const TaskItPasswordField({super.key, required this.controller});
  final TextEditingController controller;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalization.of(context);
    return TextField(
      controller: controller,
      obscureText: true,
      decoration: InputDecoration(
        labelText: l10n.login,
      ),
    );
  }
}

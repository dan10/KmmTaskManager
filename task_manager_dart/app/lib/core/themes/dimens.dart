import 'package:flutter/widgets.dart';

class Dimens {
  const Dimens._(this.edgeInsetsScreenSymmetric);

  final EdgeInsets edgeInsetsScreenSymmetric;

  static const double paddingVertical = 16.0;

  static Dimens of(BuildContext context) {
    return const Dimens._(EdgeInsets.symmetric(horizontal: 24.0, vertical: 16.0));
  }
}



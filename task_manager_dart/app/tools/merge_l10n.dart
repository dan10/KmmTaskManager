#!/usr/bin/env dart
// Script to merge feature-specific ARB files into the main app ARB files
// Usage: dart tools/merge_l10n.dart

import 'dart:convert';
import 'dart:io';

void main() {
  final locales = ['en', 'es', 'pt_BR'];

  for (final locale in locales) {
    print('Merging locale: $locale');

    // Read all feature ARB files for this locale
    final mergedData = <String, dynamic>{};
    mergedData['@@locale'] = locale;

    // Read main app strings
    final appFile = File('lib/l10n/app_$locale.arb');
    if (appFile.existsSync()) {
      final appData = jsonDecode(appFile.readAsStringSync()) as Map<String, dynamic>;
      mergedData.addAll(appData);
    }

    // Find and merge all feature ARB files
    final featureFiles = [
      'lib/l10n/auth_$locale.arb',
      'lib/l10n/tasks_$locale.arb',
      'lib/l10n/projects_$locale.arb',
      'lib/l10n/profile_$locale.arb',
    ];

    for (final featureFile in featureFiles) {
      final file = File(featureFile);
      if (file.existsSync()) {
        print('  - Merging $featureFile');
        final featureData = jsonDecode(file.readAsStringSync()) as Map<String, dynamic>;

        // Merge, but skip @@locale and @@context keys
        featureData.forEach((key, value) {
          if (!key.startsWith('@@') || key == '@@locale') {
            mergedData[key] = value;
          }
        });
      }
    }

    // Write merged file
    final outputFile = File('lib/l10n/merged_$locale.arb');
    final encoder = JsonEncoder.withIndent('  ');
    outputFile.writeAsStringSync(encoder.convert(mergedData));
    print('  ✓ Created merged_$locale.arb');
  }

  print('\n✨ Done! Generated merged ARB files.');
  print('Now run: flutter gen-l10n');
}

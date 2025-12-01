#!/bin/bash
# Script to generate localization files for all features

set -e

echo "🌍 Generating localization files..."

# Generate core/global localizations
echo "📦 Core localizations..."
flutter gen-l10n --arb-dir=lib/core/l10n \
  --template-arb-file=app_en.arb \
  --output-localization-file=app_localizations.dart \
  --output-class=AppLocalizations \
  --output-dir=lib/core/l10n/generated \
  --nullable-getter

# Generate auth feature localizations
echo "🔐 Auth feature localizations..."
flutter gen-l10n --arb-dir=lib/features/auth/l10n \
  --template-arb-file=auth_en.arb \
  --output-localization-file=auth_localizations.dart \
  --output-class=AuthLocalizations \
  --output-dir=lib/features/auth/l10n/generated \
  --nullable-getter

# Add more features here as they are created
# echo "✅ Tasks feature localizations..."
# flutter gen-l10n --arb-dir=lib/features/tasks/l10n \
#   --template-arb-file=tasks_en.arb \
#   --output-localization-file=tasks_localizations.dart \
#   --output-class=TasksLocalizations \
#   --output-dir=lib/features/tasks/l10n/generated \
#   --nullable-getter

echo "✅ All localizations generated!"

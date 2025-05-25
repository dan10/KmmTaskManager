import 'package:flutter_test/flutter_test.dart';

import '../../../lib/core/utils/validation_utils.dart';

void main() {
  group('ValidationUtils Tests', () {
    group('Password Strength', () {
      test('should return 0 for empty password', () {
        final strength = ValidationUtils.getPasswordStrength('');
        expect(strength, 0);
      });

      test('should return 1 for password with only length', () {
        final strength = ValidationUtils.getPasswordStrength('12345678');
        expect(strength, 2); // length + digits
      });

      test('should return 2 for password with length and lowercase', () {
        final strength = ValidationUtils.getPasswordStrength('password');
        expect(strength, 2); // length + lowercase
      });

      test(
          'should return 3 for password with length, lowercase, and uppercase', () {
        final strength = ValidationUtils.getPasswordStrength('Password');
        expect(strength, 3); // length + lowercase + uppercase
      });

      test(
          'should return 4 for password with length, lowercase, uppercase, and digits', () {
        final strength = ValidationUtils.getPasswordStrength('Password123');
        expect(strength, 4); // length + lowercase + uppercase + digits
      });

      test('should return 5 for password with all criteria', () {
        final strength = ValidationUtils.getPasswordStrength('Password123!');
        expect(
            strength, 5); // length + lowercase + uppercase + digits + special
      });

      test('should handle short passwords', () {
        final strength = ValidationUtils.getPasswordStrength('Pass1!');
        expect(strength,
            4); // lowercase + uppercase + digits + special (no length)
      });
    });

    group('Password Strength Color', () {
      test('should return red for strength 0-1', () {
        expect(ValidationUtils.getPasswordStrengthColor(0), 0xFFE53E3E);
        expect(ValidationUtils.getPasswordStrengthColor(1), 0xFFE53E3E);
      });

      test('should return orange for strength 2', () {
        expect(ValidationUtils.getPasswordStrengthColor(2), 0xFFDD6B20);
      });

      test('should return yellow for strength 3', () {
        expect(ValidationUtils.getPasswordStrengthColor(3), 0xFFD69E2E);
      });

      test('should return green for strength 4', () {
        expect(ValidationUtils.getPasswordStrengthColor(4), 0xFF38A169);
      });

      test('should return dark green for strength 5', () {
        expect(ValidationUtils.getPasswordStrengthColor(5), 0xFF2F855A);
      });
    });

    group('Form Validation Helpers', () {
      test('should detect errors in validation results', () {
        final resultsWithErrors = {
          'email': 'Invalid email',
          'password': null,
        };
        expect(ValidationUtils.hasErrors(resultsWithErrors), true);
      });

      test('should detect no errors in validation results', () {
        final resultsWithoutErrors = {
          'email': null,
          'password': null,
        };
        expect(ValidationUtils.hasErrors(resultsWithoutErrors), false);
      });

      test('should handle empty validation results', () {
        final emptyResults = <String, String?>{};
        expect(ValidationUtils.hasErrors(emptyResults), false);
      });
    });

    group('Edge Cases', () {
      test('should handle very long passwords', () {
        final longPassword = 'A' * 200 + 'a1!';
        final strength = ValidationUtils.getPasswordStrength(longPassword);
        expect(strength, 5); // All criteria met
      });

      test('should handle passwords with only special characters', () {
        final specialPassword = '!@#\$%^&*()';
        final strength = ValidationUtils.getPasswordStrength(specialPassword);
        expect(strength, 2); // length + special characters
      });

      test('should handle mixed case passwords', () {
        final mixedPassword = 'AbCdEfGh';
        final strength = ValidationUtils.getPasswordStrength(mixedPassword);
        expect(strength, 3); // length + lowercase + uppercase
      });
    });
  });
} 
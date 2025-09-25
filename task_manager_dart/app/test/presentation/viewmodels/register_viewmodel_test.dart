import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/register_viewmodel.dart';
import '../../mocks/mock_auth_repository.dart';

void main() {
  group('RegisterViewModel Tests', () {
    late RegisterViewModel registerViewModel;
    late MockAuthRepository mockAuthRepository;
    late UserPublicResponseDto mockUser;
    late LoginResponseDto mockRegisterResponse;

    setUp(() {
      mockAuthRepository = MockAuthRepository();
      registerViewModel = RegisterViewModel(mockAuthRepository);

      mockUser = UserPublicResponseDto(
        id: 'test-id',
        email: 'test@example.com',
        displayName: 'Test User',
      );

      mockRegisterResponse = LoginResponseDto(
        token: 'test-token',
        user: mockUser,
      );
    });

    tearDown(() {
      mockAuthRepository.reset();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(registerViewModel.state, RegisterState.initial);
        expect(registerViewModel.user, isNull);
        expect(registerViewModel.errorMessage, isNull);
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.isSuccess, false);
      });
    });

    group('Register', () {
      test('should register successfully', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockRegisterResponse);

        // Act
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.success);
        expect(registerViewModel.user, mockUser);
        expect(registerViewModel.errorMessage, isNull);
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.isSuccess, true);
      });

      test('should handle register failure', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Email already exists'));

        // Act
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(registerViewModel.user, isNull);
        expect(registerViewModel.errorMessage,
            'Registration failed: Exception: Email already exists');
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.isSuccess, false);
      });

      test('should set loading state during registration', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockRegisterResponse);
        bool wasLoading = false;

        // Listen to state changes
        registerViewModel.addListener(() {
          if (registerViewModel.isLoading) {
            wasLoading = true;
          }
        });

        // Act
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        // Assert
        expect(wasLoading, true);
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.state, RegisterState.success);
      });

      test('should not register when repository is null', () async {
        // Arrange
        final viewModelWithNullRepo = RegisterViewModel(null);

        // Act
        await viewModelWithNullRepo.register(
            'Test User', 'test@example.com', 'password123');

        // Assert
        expect(viewModelWithNullRepo.state, RegisterState.initial);
        expect(viewModelWithNullRepo.user, isNull);
        expect(viewModelWithNullRepo.isLoading, false);
      });
    });

    group('Google Register', () {
      test('should google register successfully', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockRegisterResponse);

        // Act
        await registerViewModel.googleRegister('google-id-token');

        // Assert
        expect(registerViewModel.state, RegisterState.success);
        expect(registerViewModel.user, mockUser);
        expect(registerViewModel.errorMessage, isNull);
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.isSuccess, true);
      });

      test('should handle google register failure', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Invalid Google token'));

        // Act
        await registerViewModel.googleRegister('invalid-token');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(registerViewModel.user, isNull);
        expect(registerViewModel.errorMessage,
            'Google registration failed: Exception: Invalid Google token');
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.isSuccess, false);
      });
    });

    group('State Management', () {
      test('should clear error', () async {
        // Arrange - set error state first
        mockAuthRepository.setMockException(Exception('Test error'));
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        expect(registerViewModel.errorMessage, isNotNull);

        // Act
        registerViewModel.clearError();

        // Assert
        expect(registerViewModel.errorMessage, isNull);
      });

      test('should reset state', () async {
        // Arrange - set success state first
        mockAuthRepository.setMockResponse(mockRegisterResponse);
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        expect(registerViewModel.state, RegisterState.success);
        expect(registerViewModel.user, isNotNull);

        // Act
        registerViewModel.reset();

        // Assert
        expect(registerViewModel.state, RegisterState.initial);
        expect(registerViewModel.user, isNull);
        expect(registerViewModel.errorMessage, isNull);
        expect(registerViewModel.isLoading, false);
        expect(registerViewModel.isSuccess, false);
      });
    });

    group('Repository Update', () {
      test('should update repository', () async {
        // Arrange
        final newMockRepository = MockAuthRepository();
        newMockRepository.setMockResponse(mockRegisterResponse);

        // Act
        registerViewModel.updateRepository(newMockRepository);
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.success);
        expect(registerViewModel.user, mockUser);
      });
    });

    group('Edge Cases', () {
      test('should handle empty fields', () async {
        // Arrange
        mockAuthRepository.setMockException(
            Exception('All fields are required'));

        // Act
        await registerViewModel.register('', '', '');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(registerViewModel.errorMessage,
            contains('All fields are required'));
      });

      test('should handle weak password', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Password too weak'));

        // Act
        await registerViewModel.register(
            'Test User', 'test@example.com', '123');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(registerViewModel.errorMessage, contains('Password too weak'));
      });

      test('should handle invalid email format', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Invalid email format'));

        // Act
        await registerViewModel.register(
            'Test User', 'invalid-email', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(
            registerViewModel.errorMessage, contains('Invalid email format'));
      });

      test('should handle network errors', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Network error'));

        // Act
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(registerViewModel.errorMessage, contains('Network error'));
      });

      test(
          'should handle multiple consecutive registration attempts', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockRegisterResponse);

        // Act
        await registerViewModel.register(
            'Test User', 'test@example.com', 'password123');
        registerViewModel.reset();
        await registerViewModel.register(
            'Test User 2', 'test2@example.com', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.success);
        expect(registerViewModel.user, mockUser);
      });

      test('should handle server validation errors', () async {
        // Arrange
        mockAuthRepository.setMockException(
            Exception('Name must be at least 2 characters'));

        // Act
        await registerViewModel.register(
            'A', 'test@example.com', 'password123');

        // Assert
        expect(registerViewModel.state, RegisterState.error);
        expect(registerViewModel.errorMessage,
            contains('Name must be at least 2 characters'));
      });
    });
  });
} 
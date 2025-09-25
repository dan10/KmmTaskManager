import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/login_viewmodel.dart';
import '../../mocks/mock_auth_repository.dart';

void main() {
  group('LoginViewModel Tests', () {
    late LoginViewModel loginViewModel;
    late MockAuthRepository mockAuthRepository;
    late UserPublicResponseDto mockUser;
    late LoginResponseDto mockLoginResponse;

    setUp(() {
      mockAuthRepository = MockAuthRepository();
      loginViewModel = LoginViewModel(mockAuthRepository);

      mockUser = UserPublicResponseDto(
        id: 'test-id',
        email: 'test@example.com',
        displayName: 'Test User',
      );

      mockLoginResponse = LoginResponseDto(
        token: 'test-token',
        user: mockUser,
      );
    });

    tearDown(() {
      mockAuthRepository.reset();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(loginViewModel.state, LoginState.initial);
        expect(loginViewModel.user, isNull);
        expect(loginViewModel.errorMessage, isNull);
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.isSuccess, false);
      });
    });

    group('Login', () {
      test('should login successfully', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockLoginResponse);

        // Act
        await loginViewModel.login('test@example.com', 'password123');

        // Assert
        expect(loginViewModel.state, LoginState.success);
        expect(loginViewModel.user, mockUser);
        expect(loginViewModel.errorMessage, isNull);
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.isSuccess, true);
      });

      test('should handle login failure', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Invalid credentials'));

        // Act
        await loginViewModel.login('test@example.com', 'wrongpassword');

        // Assert
        expect(loginViewModel.state, LoginState.error);
        expect(loginViewModel.user, isNull);
        expect(loginViewModel.errorMessage,
            'Login failed: Exception: Invalid credentials');
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.isSuccess, false);
      });

      test('should set loading state during login', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockLoginResponse);
        bool wasLoading = false;

        // Listen to state changes
        loginViewModel.addListener(() {
          if (loginViewModel.isLoading) {
            wasLoading = true;
          }
        });

        // Act
        await loginViewModel.login('test@example.com', 'password123');

        // Assert
        expect(wasLoading, true);
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.state, LoginState.success);
      });

      test('should not login when repository is null', () async {
        // Arrange
        final viewModelWithNullRepo = LoginViewModel(null);

        // Act
        await viewModelWithNullRepo.login('test@example.com', 'password123');

        // Assert
        expect(viewModelWithNullRepo.state, LoginState.initial);
        expect(viewModelWithNullRepo.user, isNull);
        expect(viewModelWithNullRepo.isLoading, false);
      });
    });

    group('Google Login', () {
      test('should google login successfully', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockLoginResponse);

        // Act
        await loginViewModel.googleLogin('google-id-token');

        // Assert
        expect(loginViewModel.state, LoginState.success);
        expect(loginViewModel.user, mockUser);
        expect(loginViewModel.errorMessage, isNull);
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.isSuccess, true);
      });

      test('should handle google login failure', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Invalid Google token'));

        // Act
        await loginViewModel.googleLogin('invalid-token');

        // Assert
        expect(loginViewModel.state, LoginState.error);
        expect(loginViewModel.user, isNull);
        expect(loginViewModel.errorMessage,
            'Google login failed: Exception: Invalid Google token');
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.isSuccess, false);
      });
    });

    group('State Management', () {
      test('should clear error', () async {
        // Arrange - set error state first
        mockAuthRepository.setMockException(Exception('Test error'));
        await loginViewModel.login('test@example.com', 'wrongpassword');

        expect(loginViewModel.errorMessage, isNotNull);

        // Act
        loginViewModel.clearError();

        // Assert
        expect(loginViewModel.errorMessage, isNull);
      });

      test('should reset state', () async {
        // Arrange - set success state first
        mockAuthRepository.setMockResponse(mockLoginResponse);
        await loginViewModel.login('test@example.com', 'password123');

        expect(loginViewModel.state, LoginState.success);
        expect(loginViewModel.user, isNotNull);

        // Act
        loginViewModel.reset();

        // Assert
        expect(loginViewModel.state, LoginState.initial);
        expect(loginViewModel.user, isNull);
        expect(loginViewModel.errorMessage, isNull);
        expect(loginViewModel.isLoading, false);
        expect(loginViewModel.isSuccess, false);
      });
    });

    group('Repository Update', () {
      test('should update repository', () async {
        // Arrange
        final newMockRepository = MockAuthRepository();
        newMockRepository.setMockResponse(mockLoginResponse);

        // Act
        loginViewModel.updateRepository(newMockRepository);
        await loginViewModel.login('test@example.com', 'password123');

        // Assert
        expect(loginViewModel.state, LoginState.success);
        expect(loginViewModel.user, mockUser);
      });
    });

    group('Edge Cases', () {
      test('should handle empty email and password', () async {
        // Arrange
        mockAuthRepository.setMockException(
            Exception('Email and password required'));

        // Act
        await loginViewModel.login('', '');

        // Assert
        expect(loginViewModel.state, LoginState.error);
        expect(loginViewModel.errorMessage,
            contains('Email and password required'));
      });

      test('should handle network errors', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Network error'));

        // Act
        await loginViewModel.login('test@example.com', 'password123');

        // Assert
        expect(loginViewModel.state, LoginState.error);
        expect(loginViewModel.errorMessage, contains('Network error'));
      });

      test('should handle multiple consecutive login attempts', () async {
        // Arrange
        mockAuthRepository.setMockResponse(mockLoginResponse);

        // Act
        await loginViewModel.login('test@example.com', 'password123');
        loginViewModel.reset();
        await loginViewModel.login('test@example.com', 'password123');

        // Assert
        expect(loginViewModel.state, LoginState.success);
        expect(loginViewModel.user, mockUser);
      });
    });
  });
} 
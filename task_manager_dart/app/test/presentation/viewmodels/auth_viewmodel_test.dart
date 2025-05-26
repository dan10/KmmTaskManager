import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/auth_viewmodel.dart';
import '../../mocks/mock_auth_repository.dart';

void main() {
  group('AuthViewModel Tests', () {
    late AuthViewModel authViewModel;
    late MockAuthRepository mockAuthRepository;
    late UserPublicResponseDto mockUser;

    setUp(() {
      mockAuthRepository = MockAuthRepository();
      authViewModel = AuthViewModel(mockAuthRepository);

      mockUser = UserPublicResponseDto(
        id: 'test-id',
        email: 'test@example.com',
        displayName: 'Test User',
      );
    });

    tearDown(() {
      mockAuthRepository.reset();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(authViewModel.state, AuthState.initial);
        expect(authViewModel.currentUser, isNull);
        expect(authViewModel.errorMessage, isNull);
        expect(authViewModel.isLoading, false);
        expect(authViewModel.isAuthenticated, false);
      });
    });

    group('Initialize', () {
      test(
          'should initialize as authenticated when user is logged in', () async {
        // Arrange
        mockAuthRepository.setLoggedIn(true);
        mockAuthRepository.setCurrentUser(mockUser);

        // Act
        await authViewModel.initialize();

        // Assert
        expect(authViewModel.state, AuthState.authenticated);
        expect(authViewModel.currentUser, mockUser);
        expect(authViewModel.isAuthenticated, true);
        expect(authViewModel.isLoading, false);
      });

      test(
          'should initialize as unauthenticated when user is not logged in', () async {
        // Arrange
        mockAuthRepository.setLoggedIn(false);

        // Act
        await authViewModel.initialize();

        // Assert
        expect(authViewModel.state, AuthState.unauthenticated);
        expect(authViewModel.currentUser, isNull);
        expect(authViewModel.isAuthenticated, false);
        expect(authViewModel.isLoading, false);
      });

      test('should handle initialization error', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Network error'));

        // Act
        await authViewModel.initialize();

        // Assert
        expect(authViewModel.state, AuthState.error);
        expect(authViewModel.errorMessage,
            'Failed to initialize authentication: Exception: Network error');
        expect(authViewModel.isLoading, false);
      });

      test('should not initialize when repository is null', () async {
        // Arrange
        final viewModelWithNullRepo = AuthViewModel(null);

        // Act
        await viewModelWithNullRepo.initialize();

        // Assert
        expect(viewModelWithNullRepo.state, AuthState.initial);
        expect(viewModelWithNullRepo.isLoading, false);
      });

      test('should set loading state during initialization', () async {
        // Arrange
        mockAuthRepository.setLoggedIn(true);
        mockAuthRepository.setCurrentUser(mockUser);
        bool wasLoading = false;

        // Listen to state changes
        authViewModel.addListener(() {
          if (authViewModel.isLoading) {
            wasLoading = true;
          }
        });

        // Act
        await authViewModel.initialize();

        // Assert
        expect(wasLoading, true);
        expect(authViewModel.isLoading, false);
      });
    });

    group('Set Authenticated', () {
      test('should set authenticated state with user', () {
        // Act
        authViewModel.setAuthenticated(mockUser);

        // Assert
        expect(authViewModel.state, AuthState.authenticated);
        expect(authViewModel.currentUser, mockUser);
        expect(authViewModel.isAuthenticated, true);
      });

      test('should notify listeners when setting authenticated', () {
        // Arrange
        bool wasNotified = false;
        authViewModel.addListener(() {
          wasNotified = true;
        });

        // Act
        authViewModel.setAuthenticated(mockUser);

        // Assert
        expect(wasNotified, true);
      });
    });

    group('Logout', () {
      test('should logout successfully', () async {
        // Arrange
        authViewModel.setAuthenticated(
            mockUser); // Set initial authenticated state

        // Act
        await authViewModel.logout();

        // Assert
        expect(authViewModel.state, AuthState.unauthenticated);
        expect(authViewModel.currentUser, isNull);
        expect(authViewModel.isAuthenticated, false);
        expect(authViewModel.isLoading, false);
      });

      test('should handle logout error', () async {
        // Arrange
        authViewModel.setAuthenticated(mockUser);
        mockAuthRepository.setMockException(Exception('Logout failed'));

        // Act
        await authViewModel.logout();

        // Assert
        expect(authViewModel.state, AuthState.error);
        expect(authViewModel.errorMessage,
            'Logout failed: Exception: Logout failed');
        expect(authViewModel.isLoading, false);
      });

      test('should not logout when repository is null', () async {
        // Arrange
        final viewModelWithNullRepo = AuthViewModel(null);

        // Act
        await viewModelWithNullRepo.logout();

        // Assert
        expect(viewModelWithNullRepo.state, AuthState.initial);
        expect(viewModelWithNullRepo.isLoading, false);
      });

      test('should set loading state during logout', () async {
        // Arrange
        authViewModel.setAuthenticated(mockUser);
        bool wasLoading = false;

        authViewModel.addListener(() {
          if (authViewModel.isLoading) {
            wasLoading = true;
          }
        });

        // Act
        await authViewModel.logout();

        // Assert
        expect(wasLoading, true);
        expect(authViewModel.isLoading, false);
      });
    });

    group('Get Token', () {
      test('should return token from repository', () async {
        // Arrange
        mockAuthRepository.setStoredToken('test-token');

        // Act
        final token = await authViewModel.getToken();

        // Assert
        expect(token, 'test-token');
      });

      test('should return null when no token stored', () async {
        // Arrange
        mockAuthRepository.setStoredToken(null);

        // Act
        final token = await authViewModel.getToken();

        // Assert
        expect(token, isNull);
      });

      test('should return null when repository is null', () async {
        // Arrange
        final viewModelWithNullRepo = AuthViewModel(null);

        // Act
        final token = await viewModelWithNullRepo.getToken();

        // Assert
        expect(token, isNull);
      });

      test('should handle token retrieval error', () async {
        // Arrange
        mockAuthRepository.setMockException(
            Exception('Token retrieval failed'));

        // Act
        final token = await authViewModel.getToken();

        // Assert
        expect(token, isNull);
      });
    });

    group('Refresh User', () {
      test('should refresh user data successfully', () async {
        // Arrange
        final updatedUser = UserPublicResponseDto(
          id: 'test-id',
          email: 'updated@example.com',
          displayName: 'Updated User',
        );
        mockAuthRepository.setCurrentUser(updatedUser);

        // Act
        await authViewModel.refreshUser();

        // Assert
        expect(authViewModel.currentUser, updatedUser);
      });

      test('should handle refresh user error', () async {
        // Arrange
        mockAuthRepository.setMockException(Exception('Refresh failed'));

        // Act
        await authViewModel.refreshUser();

        // Assert
        expect(authViewModel.state, AuthState.error);
        expect(authViewModel.errorMessage,
            'Failed to refresh user data: Exception: Refresh failed');
      });

      test('should not refresh when repository is null', () async {
        // Arrange
        final viewModelWithNullRepo = AuthViewModel(null);

        // Act
        await viewModelWithNullRepo.refreshUser();

        // Assert - should not throw or change state
        expect(viewModelWithNullRepo.state, AuthState.initial);
      });

      test('should notify listeners when user is refreshed', () async {
        // Arrange
        final updatedUser = UserPublicResponseDto(
          id: 'test-id',
          email: 'updated@example.com',
          displayName: 'Updated User',
        );
        mockAuthRepository.setCurrentUser(updatedUser);
        bool wasNotified = false;

        authViewModel.addListener(() {
          wasNotified = true;
        });

        // Act
        await authViewModel.refreshUser();

        // Assert
        expect(wasNotified, true);
      });
    });

    group('State Management', () {
      test('should clear error', () async {
        // Arrange - set error state first
        mockAuthRepository.setMockException(Exception('Test error'));
        await authViewModel.initialize();

        expect(authViewModel.errorMessage, isNotNull);

        // Act
        authViewModel.clearError();

        // Assert
        expect(authViewModel.errorMessage, isNull);
      });

      test('should notify listeners when clearing error', () {
        // Arrange
        authViewModel.setAuthenticated(mockUser);
        bool wasNotified = false;

        authViewModel.addListener(() {
          wasNotified = true;
        });

        // Act
        authViewModel.clearError();

        // Assert
        expect(wasNotified, true);
      });
    });

    group('Repository Update', () {
      test('should update repository', () async {
        // Arrange
        final newMockRepository = MockAuthRepository();
        newMockRepository.setStoredToken('new-token');

        // Act
        authViewModel.updateRepository(newMockRepository);
        final token = await authViewModel.getToken();

        // Assert
        expect(token, 'new-token');
      });
    });

    group('Edge Cases', () {
      test('should handle multiple initialization calls', () async {
        // Arrange
        mockAuthRepository.setLoggedIn(true);
        mockAuthRepository.setCurrentUser(mockUser);

        // Act
        await authViewModel.initialize();
        await authViewModel.initialize();

        // Assert
        expect(authViewModel.state, AuthState.authenticated);
        expect(authViewModel.currentUser, mockUser);
      });

      test('should handle logout when not authenticated', () async {
        // Act
        await authViewModel.logout();

        // Assert
        expect(authViewModel.state, AuthState.unauthenticated);
        expect(authViewModel.currentUser, isNull);
      });

      test('should handle refresh user when not authenticated', () async {
        // Arrange
        mockAuthRepository.setCurrentUser(null);

        // Act
        await authViewModel.refreshUser();

        // Assert
        expect(authViewModel.currentUser, isNull);
      });

      test('should handle concurrent operations', () async {
        // Arrange
        mockAuthRepository.setLoggedIn(true);
        mockAuthRepository.setCurrentUser(mockUser);

        // Act
        final futures = [
          authViewModel.initialize(),
          authViewModel.refreshUser(),
          authViewModel.getToken(),
        ];
        await Future.wait(futures);

        // Assert - should not crash and maintain consistent state
        expect(authViewModel.state, AuthState.authenticated);
      });
    });
  });
} 
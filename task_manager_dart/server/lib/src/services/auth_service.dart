import 'dart:convert'; // For utf8 and jsonDecode
import 'package:crypto/crypto.dart'; // For sha256
import 'package:http/http.dart' as http; // For tokeninfo endpoint call
// import 'package:jwt_decoder/jwt_decoder.dart'; // No longer needed for Google token
import 'package:task_manager_shared/models.dart';
import '../repositories/auth_repository.dart';
import '../services/jwt_service.dart';
import '../exceptions/custom_exceptions.dart';
import '../config/app_config.dart'; // To access potential YOUR_SERVER_CLIENT_ID

abstract class AuthService {
  Future<User> register(String displayName, String email, String password,
      {bool isSocialLogin = false});
  Future<User> login(String email, String password);
  Future<User?> getCurrentUser(String id);
  Future<User> googleLogin(GoogleLoginRequestDto request); // New method
}

class AuthServiceImpl implements AuthService {
  final AuthRepository _authRepository;
  final JwtService _jwtService;
  final AppConfig _appConfig; // Add AppConfig

  AuthServiceImpl(this._authRepository, this._jwtService,
      this._appConfig); // Update constructor

  @override
  Future<User> register(String displayName, String email, String password,
      {bool isSocialLogin = false}) async {
    // Check if user already exists
    final existingUser = await _authRepository.findUserByEmail(email);
    if (existingUser != null) {
      // If it's a social login and user exists, we might link accounts or just return the user.
      // For now, if email exists, we assume it's the same user.
      // This part needs careful consideration for account linking vs. conflict.
      if (isSocialLogin) {
        print(
            'User $email already exists. Returning existing user for social login.');
        // Potentially update user with social login info if needed, e.g. linking a Google ID.
        return existingUser;
      }
      throw ConflictException(message: 'User with this email already exists.');
    }

    String passwordHashValue;
    if (isSocialLogin) {
      // For social logins, store a non-usable hash or a specific marker.
      // Using a unique, non-reversible string.
      // The User model expects a non-null passwordHash.
      passwordHashValue =
          "social_login_user_placeholder_hash_${email}_${DateTime.now().microsecondsSinceEpoch}";
      print("Social login registration for $email: using a placeholder hash.");
    } else {
      // Hash the password for non-social logins
      final bytes = utf8.encode(password); // Encode password to bytes
      final digest = sha256.convert(bytes); // Hash it
      passwordHashValue = digest.toString(); // Store hex string of the hash
    }

    // Create new user
    final user = User(
      id: DateTime.now()
          .millisecondsSinceEpoch
          .toString(), // Placeholder ID generation
      displayName: displayName,
      email: email,
      passwordHash: passwordHashValue,
      createdAt: DateTime.now().toIso8601String(),
    );

    return _authRepository.createUser(user); // Existing repository method
  }

  @override
  Future<User> login(String email, String password) async {
    // Find user
    final user = await _authRepository.findUserByEmail(email);
    if (user == null) {
      throw AuthenticationException(
          message: 'Invalid email or password.'); // Generic message
    }

    // Verify password
    final providedPasswordBytes = utf8.encode(password);
    final providedPasswordHash =
        sha256.convert(providedPasswordBytes).toString();
    if (user.passwordHash != providedPasswordHash) {
      throw AuthenticationException(
          message: 'Invalid email or password.'); // Generic message
    }

    return user;
  }

  @override
  Future<User?> getCurrentUser(String id) async {
    return _authRepository.findUserById(id);
  }

  @override
  Future<User> googleLogin(GoogleLoginRequestDto request) async {
    final idToken = request.idToken;
    final uri = Uri.parse(
        'https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=$idToken');

    Map<String, dynamic> tokenInfo;

    try {
      final response = await http.get(uri);
      if (response.statusCode == 200) {
        tokenInfo = jsonDecode(response.body) as Map<String, dynamic>;

        // 1. Verify Audience (aud)
        // TODO: Store YOUR_SERVER_CLIENT_ID in AppConfig and use it here.
        // final expectedAudience = _appConfig.googleClientId;
        final expectedAudience = "YOUR_SERVER_CLIENT_ID"; // Placeholder
        if (tokenInfo['aud'] != expectedAudience) {
          throw AuthenticationException(
              message: 'Google ID token audience mismatch.');
        }

        // 2. Verify Issuer (iss)
        if (tokenInfo['iss'] != 'accounts.google.com' &&
            tokenInfo['iss'] != 'https://accounts.google.com') {
          throw AuthenticationException(
              message: 'Invalid Google ID token issuer.');
        }

        // 3. Verify Expiry (exp)
        final expiry = tokenInfo['exp'] as int?;
        if (expiry == null ||
            DateTime.fromMillisecondsSinceEpoch(expiry * 1000)
                .isBefore(DateTime.now())) {
          throw AuthenticationException(
              message: 'Google ID token has expired.');
        }

        // If all checks pass, extract user information
        final email = tokenInfo['email'] as String?;
        if (email == null || email.isEmpty) {
          throw AuthenticationException(
              message: 'Email not found in Google ID token.');
        }

        final isEmailVerified = tokenInfo['email_verified'] == 'true' ||
            tokenInfo['email_verified'] == true;
        if (!isEmailVerified) {
          throw AuthenticationException(message: 'Google email not verified.');
        }

        User? user = await _authRepository.findUserByEmail(email);

        if (user == null) {
          final name = tokenInfo['name'] as String? ?? email.split('@').first;
          user = await register(name, email, '', isSocialLogin: true);
        } else {
          // Optional: Update user's name or other details if they logged in with Google before
          // and their Google profile info has changed. For simplicity, we don't do this here.
          print('User with email $email found for Google Sign-In.');
        }
        return user;
      } else {
        throw AuthenticationException(
            message:
                'Failed to verify Google ID token. Status: ${response.statusCode}, Body: ${response.body}');
      }
    } catch (e) {
      if (e is AuthenticationException)
        rethrow; // Re-throw our specific exceptions
      print('Error during Google ID token verification: $e');
      throw AuthenticationException(
          message:
              'Error during Google ID token verification: ${e.toString()}');
    }
  }
}

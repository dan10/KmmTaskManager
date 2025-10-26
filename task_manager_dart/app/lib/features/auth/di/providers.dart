import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../data/services/auth_service.dart';
import '../../../data/sources/local/secure_storage.dart';
import '../data/repositories/auth_repository.dart';
import '../../../core/constants/api_constants.dart';

List<SingleChildWidget> get providers => [
  // Auth API Service (no auth token needed for login/register)
  Provider<AuthApiService>(
    create: (_) {
      // Parse the base URL to extract host, port, and scheme
      final uri = Uri.parse(ApiConstants.baseUrl);
      return AuthApiServiceImpl(
        host: uri.host,
        port: uri.port,
        scheme: uri.scheme,
      );
    },
  ),

  // Auth Repository
  ProxyProvider2<AuthApiService, SecureStorage, AuthRepository>(
    update: (_, api, storage, __) => AuthRepositoryImpl(
      apiService: api,
      secureStorage: storage,
    ),
  ),
];



import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../data/services/auth_service.dart';
import '../../../data/sources/local/secure_storage.dart';
import '../data/repositories/auth_repository.dart';

List<SingleChildWidget> get providers => [
  ProxyProvider2<AuthApiService, SecureStorage, AuthRepository>(
    update: (_, api, storage, __) => AuthRepositoryImpl(apiService: api, secureStorage: storage),
  ),
];



library shared;

// Export all models
export 'src/models/user.dart';
export 'src/models/project.dart';
export 'src/models/task.dart';
export 'src/models/paginated_response.dart';

// Export DTOs
export 'src/dto/user_public_response_dto.dart';
export 'src/dto/error_response_dto.dart';
export 'src/dto/login_response_dto.dart';

// Export Auth DTOs
export 'src/dto/auth/register_request_dto.dart';
export 'src/dto/auth/login_request_dto.dart';
export 'src/dto/auth/google_login_request_dto.dart';

// Export Project DTOs
export 'src/dto/project/create_project_request_dto.dart';

// Export Task DTOs
export 'src/dto/task/task_assign_request_dto.dart';
export 'src/dto/task/task_status_change_request_dto.dart';

// Export converters if needed
export 'src/converters/task_status_converter.dart'; 
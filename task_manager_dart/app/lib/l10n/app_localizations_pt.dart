// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Portuguese (`pt`).
class AppLocalizationsPt extends AppLocalizations {
  AppLocalizationsPt([String locale = 'pt']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get contentDescriptionShowPassword => 'Mostrar senha';

  @override
  String get contentDescriptionHidePassword => 'Ocultar senha';

  @override
  String get contentDescriptionBack => 'Voltar';

  @override
  String get contentDescriptionDelete => 'Excluir';

  @override
  String get contentDescriptionAddTask => 'Adicionar Tarefa';

  @override
  String get contentDescriptionSearch => 'Pesquisar';

  @override
  String get titleWithoutAccount => 'Não tem uma conta?';

  @override
  String get buttonSignUp => 'Cadastrar';

  @override
  String get titleEmail => 'Email';

  @override
  String get titlePassword => 'Senha';

  @override
  String get titleLoginButton => 'Entrar';

  @override
  String get titleEmailError => 'Por favor, insira um endereço de email válido';

  @override
  String get titlePasswordError => 'A senha deve ter pelo menos 8 caracteres';

  @override
  String get titleName => 'Nome';

  @override
  String get titleNameError => 'Por favor, insira um nome';

  @override
  String get titleRegisterButton => 'Registrar';

  @override
  String get titleConfirmPassword => 'Confirmar Senha';

  @override
  String get titleConfirmPasswordError => 'As senhas não coincidem';

  @override
  String get titleAlreadyHaveAccount => 'Já tem uma conta?';

  @override
  String get buttonSignIn => 'Entrar';

  @override
  String get navTasks => 'Tarefas';

  @override
  String get navProjects => 'Projetos';

  @override
  String get navProfile => 'Perfil';

  @override
  String get navCalendar => 'Calendário';

  @override
  String get tasksTitle => 'Minhas Tarefas';

  @override
  String get tasksSearchPlaceholder => 'Pesquisar tarefas...';

  @override
  String get tasksProgressTitle => 'Seu Progresso';

  @override
  String tasksProgressPercentage(int percentage) {
    return '$percentage%';
  }

  @override
  String tasksProgressCompleted(int completed, int total) {
    return '$completed de $total tarefas concluídas';
  }

  @override
  String get tasksEmptyTitle => 'Pronto para Começar?';

  @override
  String get tasksEmptySubtitle =>
      'Aqui estão algumas ideias para ajudá-lo a começar:';

  @override
  String get taskDetailsTitle => 'Detalhes da Tarefa';

  @override
  String get taskDueDate => 'Vencimento:';

  @override
  String get taskNoDueDate => 'Sem data de vencimento';

  @override
  String get taskProject => 'Projeto:';

  @override
  String get taskAssignedTo => 'Atribuído a:';

  @override
  String get projectsTitle => 'Projetos';

  @override
  String get projectsSearchPlaceholder => 'Pesquisar projetos...';

  @override
  String get projectsAll => 'Todos os Projetos';

  @override
  String get projectsAdd => 'Adicionar Projeto';

  @override
  String get projectsEmptyTitle => 'Nenhum Projeto Encontrado';

  @override
  String get projectsEmptySubtitle => 'Crie seu primeiro projeto para começar';

  @override
  String projectCompleted(int count) {
    return '$count concluídas';
  }

  @override
  String projectInProgress(int count) {
    return '$count em andamento';
  }

  @override
  String projectTotal(int count) {
    return '$count total';
  }

  @override
  String get profileComingSoon => 'Tela de Perfil - Em Breve';

  @override
  String get createTask => 'Criar Tarefa';

  @override
  String get editTask => 'Editar Tarefa';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleError => 'Título é obrigatório';

  @override
  String get taskDescriptionLabel => 'Descrição';

  @override
  String get taskPriorityLabel => 'Prioridade';

  @override
  String get taskDueDateLabel => 'Data de Vencimento';

  @override
  String get taskDueDatePlaceholder => 'DD/MM/AAAA';

  @override
  String get taskTitlePlaceholder => 'Digite o título da tarefa';

  @override
  String get taskDescriptionPlaceholder =>
      'Digite a descrição da tarefa (opcional)';

  @override
  String get taskProjectLabel => 'Projeto';

  @override
  String get taskProjectPlaceholder => 'Selecione um projeto';

  @override
  String get taskProjectRequired => 'Por favor, selecione um projeto';

  @override
  String taskLoadError(String error) {
    return 'Falha ao carregar tarefa: $error';
  }

  @override
  String taskCreateError(String error) {
    return 'Falha ao criar tarefa: $error';
  }

  @override
  String taskUpdateError(String error) {
    return 'Falha ao atualizar tarefa: $error';
  }

  @override
  String get taskDeleteTitle => 'Excluir Tarefa';

  @override
  String get taskDeleteMessage =>
      'Tem certeza de que deseja excluir esta tarefa? Esta ação não pode ser desfeita.';

  @override
  String get taskDeleteButton => 'Excluir';

  @override
  String taskDeleteError(String error) {
    return 'Falha ao excluir tarefa: $error';
  }

  @override
  String get createProject => 'Criar Projeto';

  @override
  String get editProject => 'Editar Projeto';

  @override
  String get projectNameLabel => 'Nome do Projeto';

  @override
  String get projectNameError => 'O nome do projeto não pode estar vazio';

  @override
  String get projectDescriptionLabel => 'Descrição (opcional)';

  @override
  String get projectCancelButton => 'Cancelar';

  @override
  String get projectCreateButton => 'Criar';

  @override
  String get projectUpdateButton => 'Atualizar';

  @override
  String projectLoadError(String error) {
    return 'Falha ao carregar projeto: $error';
  }

  @override
  String projectCreateError(String error) {
    return 'Falha ao criar projeto: $error';
  }

  @override
  String projectUpdateError(String error) {
    return 'Falha ao atualizar projeto: $error';
  }

  @override
  String get projectNamePlaceholder => 'Digite o nome do projeto';

  @override
  String get projectDescriptionPlaceholder =>
      'Digite a descrição do projeto (opcional)';

  @override
  String get authAppName => 'Task Manager';

  @override
  String get authWithoutAccount => 'Não tem uma conta?';

  @override
  String get authSignUp => 'Cadastrar';

  @override
  String get authEmail => 'Email';

  @override
  String get authPassword => 'Senha';

  @override
  String get authLoginButton => 'Entrar';

  @override
  String get authEmailError => 'Por favor, insira um endereço de email válido';

  @override
  String get authPasswordError => 'A senha deve ter pelo menos 8 caracteres';

  @override
  String get authName => 'Nome';

  @override
  String get authNameError => 'Por favor, insira um nome';

  @override
  String get authRegisterButton => 'Registrar';

  @override
  String get authConfirmPassword => 'Confirmar Senha';

  @override
  String get authConfirmPasswordError => 'As senhas não coincidem';

  @override
  String get authAlreadyHaveAccount => 'Já tem uma conta?';

  @override
  String get authSignIn => 'Entrar';

  @override
  String get authDemoCredentials => 'Credenciais Demo';

  @override
  String get authDemoCredentialsText =>
      'Email: test@example.com\nSenha: password';

  @override
  String get accessibilityShowPassword => 'Mostrar senha';

  @override
  String get accessibilityHidePassword => 'Ocultar senha';

  @override
  String get taskCreateButton => 'Criar';

  @override
  String get taskUpdateButton => 'Salvar Alterações';

  @override
  String get taskCancelButton => 'Cancelar';

  @override
  String get validationEmailRequired => 'O email é obrigatório';

  @override
  String get validationEmailInvalid =>
      'Por favor insira um endereço de email válido';

  @override
  String get validationPasswordRequired => 'A senha é obrigatória';

  @override
  String get validationPasswordTooShort =>
      'A senha deve ter pelo menos 8 caracteres';

  @override
  String get validationPasswordTooLong =>
      'A senha deve ter menos de 128 caracteres';

  @override
  String get validationPasswordNeedsLowercase =>
      'A senha deve conter pelo menos uma letra minúscula';

  @override
  String get validationPasswordNeedsUppercase =>
      'A senha deve conter pelo menos uma letra maiúscula';

  @override
  String get validationPasswordNeedsNumber =>
      'A senha deve conter pelo menos um número';

  @override
  String get validationPasswordNeedsSpecialChar =>
      'A senha deve conter pelo menos um caractere especial';

  @override
  String get validationConfirmPasswordRequired =>
      'Por favor confirme sua senha';

  @override
  String get validationPasswordsDoNotMatch => 'As senhas não coincidem';

  @override
  String get validationNameRequired => 'O nome é obrigatório';

  @override
  String get validationNameTooShort =>
      'O nome deve ter pelo menos 2 caracteres';

  @override
  String get validationNameTooLong => 'O nome deve ter menos de 50 caracteres';

  @override
  String get validationNameInvalidCharacters =>
      'O nome pode conter apenas letras, espaços, hífens e apostrofes';

  @override
  String validationFieldRequired(String fieldName) {
    return '$fieldName é obrigatório';
  }

  @override
  String get passwordStrengthVeryWeak => 'Muito Fraca';

  @override
  String get passwordStrengthWeak => 'Fraca';

  @override
  String get passwordStrengthMedium => 'Média';

  @override
  String get passwordStrengthStrong => 'Forte';

  @override
  String get passwordStrengthVeryStrong => 'Muito Forte';

  @override
  String get taskProjectHint => 'Selecione um projeto';

  @override
  String get saveChanges => 'Salvar Alterações';

  @override
  String get taskCreated => 'Tarefa criada com sucesso';

  @override
  String get taskUpdated => 'Tarefa atualizada com sucesso';

  @override
  String get cancel => 'Cancelar';

  @override
  String get delete => 'Excluir';

  @override
  String get deleteTask => 'Excluir Tarefa';
}

/// The translations for Portuguese, as used in Brazil (`pt_BR`).
class AppLocalizationsPtBr extends AppLocalizationsPt {
  AppLocalizationsPtBr() : super('pt_BR');

  @override
  String get appName => 'TaskIt';

  @override
  String get contentDescriptionShowPassword => 'Mostrar senha';

  @override
  String get contentDescriptionHidePassword => 'Ocultar senha';

  @override
  String get contentDescriptionBack => 'Voltar';

  @override
  String get contentDescriptionDelete => 'Excluir';

  @override
  String get contentDescriptionAddTask => 'Adicionar Tarefa';

  @override
  String get contentDescriptionSearch => 'Pesquisar';

  @override
  String get titleWithoutAccount => 'Não tem uma conta?';

  @override
  String get buttonSignUp => 'Cadastrar';

  @override
  String get titleEmail => 'Email';

  @override
  String get titlePassword => 'Senha';

  @override
  String get titleLoginButton => 'Entrar';

  @override
  String get titleEmailError => 'Por favor, insira um endereço de email válido';

  @override
  String get titlePasswordError => 'A senha deve ter pelo menos 8 caracteres';

  @override
  String get titleName => 'Nome';

  @override
  String get titleNameError => 'Por favor, insira um nome';

  @override
  String get titleRegisterButton => 'Registrar';

  @override
  String get titleConfirmPassword => 'Confirmar Senha';

  @override
  String get titleConfirmPasswordError => 'As senhas não coincidem';

  @override
  String get titleAlreadyHaveAccount => 'Já tem uma conta?';

  @override
  String get buttonSignIn => 'Entrar';

  @override
  String get navTasks => 'Tarefas';

  @override
  String get navProjects => 'Projetos';

  @override
  String get navProfile => 'Perfil';

  @override
  String get navCalendar => 'Calendário';

  @override
  String get tasksTitle => 'Minhas Tarefas';

  @override
  String get tasksSearchPlaceholder => 'Pesquisar tarefas...';

  @override
  String get tasksProgressTitle => 'Seu Progresso';

  @override
  String tasksProgressPercentage(int percentage) {
    return '$percentage%';
  }

  @override
  String tasksProgressCompleted(int completed, int total) {
    return '$completed de $total tarefas concluídas';
  }

  @override
  String get tasksEmptyTitle => 'Pronto para Começar?';

  @override
  String get tasksEmptySubtitle =>
      'Aqui estão algumas ideias para ajudá-lo a começar:';

  @override
  String get taskDetailsTitle => 'Detalhes da Tarefa';

  @override
  String get taskDueDate => 'Vencimento:';

  @override
  String get taskNoDueDate => 'Sem data de vencimento';

  @override
  String get taskProject => 'Projeto:';

  @override
  String get taskAssignedTo => 'Atribuído a:';

  @override
  String get projectsTitle => 'Projetos';

  @override
  String get projectsSearchPlaceholder => 'Pesquisar projetos...';

  @override
  String get projectsAll => 'Todos os Projetos';

  @override
  String get projectsAdd => 'Adicionar Projeto';

  @override
  String get projectsEmptyTitle => 'Nenhum Projeto Encontrado';

  @override
  String get projectsEmptySubtitle => 'Crie seu primeiro projeto para começar';

  @override
  String projectCompleted(int count) {
    return '$count concluídas';
  }

  @override
  String projectInProgress(int count) {
    return '$count em andamento';
  }

  @override
  String projectTotal(int count) {
    return '$count total';
  }

  @override
  String get profileComingSoon => 'Tela de Perfil - Em Breve';

  @override
  String get createTask => 'Criar Tarefa';

  @override
  String get editTask => 'Editar Tarefa';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleError => 'Título é obrigatório';

  @override
  String get taskDescriptionLabel => 'Descrição';

  @override
  String get taskPriorityLabel => 'Prioridade';

  @override
  String get taskDueDateLabel => 'Data de Vencimento';

  @override
  String get taskDueDatePlaceholder => 'DD/MM/AAAA';

  @override
  String get taskTitlePlaceholder => 'Digite o título da tarefa';

  @override
  String get taskDescriptionPlaceholder =>
      'Digite a descrição da tarefa (opcional)';

  @override
  String get taskProjectLabel => 'Projeto';

  @override
  String get taskProjectPlaceholder => 'Selecione um projeto';

  @override
  String get taskProjectRequired => 'Por favor, selecione um projeto';

  @override
  String taskLoadError(String error) {
    return 'Falha ao carregar tarefa: $error';
  }

  @override
  String taskCreateError(String error) {
    return 'Falha ao criar tarefa: $error';
  }

  @override
  String taskUpdateError(String error) {
    return 'Falha ao atualizar tarefa: $error';
  }

  @override
  String get taskDeleteTitle => 'Excluir Tarefa';

  @override
  String get taskDeleteMessage =>
      'Tem certeza de que deseja excluir esta tarefa? Esta ação não pode ser desfeita.';

  @override
  String get taskDeleteButton => 'Excluir';

  @override
  String taskDeleteError(String error) {
    return 'Falha ao excluir tarefa: $error';
  }

  @override
  String get createProject => 'Criar Projeto';

  @override
  String get editProject => 'Editar Projeto';

  @override
  String get projectNameLabel => 'Nome do Projeto';

  @override
  String get projectNameError => 'O nome do projeto não pode estar vazio';

  @override
  String get projectDescriptionLabel => 'Descrição (opcional)';

  @override
  String get projectCancelButton => 'Cancelar';

  @override
  String get projectCreateButton => 'Criar';

  @override
  String get projectUpdateButton => 'Atualizar';

  @override
  String projectLoadError(String error) {
    return 'Falha ao carregar projeto: $error';
  }

  @override
  String projectCreateError(String error) {
    return 'Falha ao criar projeto: $error';
  }

  @override
  String projectUpdateError(String error) {
    return 'Falha ao atualizar projeto: $error';
  }

  @override
  String get projectNamePlaceholder => 'Digite o nome do projeto';

  @override
  String get projectDescriptionPlaceholder =>
      'Digite a descrição do projeto (opcional)';

  @override
  String get authAppName => 'Task Manager';

  @override
  String get authWithoutAccount => 'Não tem uma conta?';

  @override
  String get authSignUp => 'Cadastrar';

  @override
  String get authEmail => 'Email';

  @override
  String get authPassword => 'Senha';

  @override
  String get authLoginButton => 'Entrar';

  @override
  String get authEmailError => 'Por favor, insira um endereço de email válido';

  @override
  String get authPasswordError => 'A senha deve ter pelo menos 8 caracteres';

  @override
  String get authName => 'Nome';

  @override
  String get authNameError => 'Por favor, insira um nome';

  @override
  String get authRegisterButton => 'Registrar';

  @override
  String get authConfirmPassword => 'Confirmar Senha';

  @override
  String get authConfirmPasswordError => 'As senhas não coincidem';

  @override
  String get authAlreadyHaveAccount => 'Já tem uma conta?';

  @override
  String get authSignIn => 'Entrar';

  @override
  String get authDemoCredentials => 'Credenciais Demo';

  @override
  String get authDemoCredentialsText =>
      'Email: test@example.com\nSenha: password';

  @override
  String get accessibilityShowPassword => 'Mostrar senha';

  @override
  String get accessibilityHidePassword => 'Ocultar senha';

  @override
  String get taskCreateButton => 'Criar';

  @override
  String get taskUpdateButton => 'Salvar Alterações';

  @override
  String get taskCancelButton => 'Cancelar';

  @override
  String get validationEmailRequired => 'O e-mail é obrigatório';

  @override
  String get validationEmailInvalid =>
      'Por favor insira um endereço de e-mail válido';

  @override
  String get validationPasswordRequired => 'A senha é obrigatória';

  @override
  String get validationPasswordTooShort =>
      'A senha deve ter pelo menos 8 caracteres';

  @override
  String get validationPasswordTooLong =>
      'A senha deve ter menos de 128 caracteres';

  @override
  String get validationPasswordNeedsLowercase =>
      'A senha deve conter pelo menos uma letra minúscula';

  @override
  String get validationPasswordNeedsUppercase =>
      'A senha deve conter pelo menos uma letra maiúscula';

  @override
  String get validationPasswordNeedsNumber =>
      'A senha deve conter pelo menos um número';

  @override
  String get validationPasswordNeedsSpecialChar =>
      'A senha deve conter pelo menos um caractere especial';

  @override
  String get validationConfirmPasswordRequired =>
      'Por favor confirme sua senha';

  @override
  String get validationPasswordsDoNotMatch => 'As senhas não coincidem';

  @override
  String get validationNameRequired => 'O nome é obrigatório';

  @override
  String get validationNameTooShort =>
      'O nome deve ter pelo menos 2 caracteres';

  @override
  String get validationNameTooLong => 'O nome deve ter menos de 50 caracteres';

  @override
  String get validationNameInvalidCharacters =>
      'O nome pode conter apenas letras, espaços, hífens e apostrofes';

  @override
  String validationFieldRequired(String fieldName) {
    return '$fieldName é obrigatório';
  }

  @override
  String get passwordStrengthVeryWeak => 'Muito Fraca';

  @override
  String get passwordStrengthWeak => 'Fraca';

  @override
  String get passwordStrengthMedium => 'Média';

  @override
  String get passwordStrengthStrong => 'Forte';

  @override
  String get passwordStrengthVeryStrong => 'Muito Forte';

  @override
  String get taskProjectHint => 'Selecione um projeto';

  @override
  String get saveChanges => 'Salvar Alterações';

  @override
  String get taskCreated => 'Tarefa criada com sucesso';

  @override
  String get taskUpdated => 'Tarefa atualizada com sucesso';

  @override
  String get cancel => 'Cancelar';

  @override
  String get delete => 'Excluir';

  @override
  String get deleteTask => 'Excluir Tarefa';
}

// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_l10n.dart';

// ignore_for_file: type=lint

/// The translations for Portuguese (`pt`).
class AppLocalizationsPt extends AppLocalizations {
  AppLocalizationsPt([String locale = 'pt']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get authAppName => 'Gestor de Tarefas';

  @override
  String get authLoginTitle => 'Bem-vindo';

  @override
  String get authRegisterTitle => 'Criar Conta';

  @override
  String get authEmail => 'E-mail';

  @override
  String get authEmailHint => 'Digite o seu e-mail';

  @override
  String get authPassword => 'Palavra-passe';

  @override
  String get authPasswordHint => 'Digite a sua palavra-passe';

  @override
  String get authConfirmPassword => 'Confirmar Palavra-passe';

  @override
  String get authName => 'Nome';

  @override
  String get authNameHint => 'Digite o seu nome completo';

  @override
  String get authLoginButton => 'Entrar';

  @override
  String get authRegisterButton => 'Criar Conta';

  @override
  String get authSignUp => 'Registar';

  @override
  String get authSignIn => 'Entrar';

  @override
  String get authWithoutAccount => 'Não tem uma conta?';

  @override
  String get authAlreadyHaveAccount => 'Já tem uma conta?';

  @override
  String get authEmailError => 'Por favor, insira um endereço de e-mail válido';

  @override
  String get authPasswordError =>
      'A palavra-passe deve ter pelo menos 8 caracteres';

  @override
  String authPasswordTooShort(int minLength) {
    return 'A palavra-passe deve ter pelo menos $minLength caracteres';
  }

  @override
  String get authNameError => 'O nome não deve estar vazio';

  @override
  String get authNameTooShort => 'O nome deve ter pelo menos 2 caracteres';

  @override
  String get authConfirmPasswordError => 'As palavras-passe não coincidem';

  @override
  String authLoginError(String error) {
    return 'Falha no início de sessão: $error';
  }

  @override
  String authRegisterError(String error) {
    return 'Falha no registo: $error';
  }

  @override
  String get authForgotPassword => 'Esqueceu-se da palavra-passe?';

  @override
  String get authResetPassword => 'Redefinir Palavra-passe';

  @override
  String get authLogout => 'Sair';

  @override
  String get authLoggingIn => 'A entrar...';

  @override
  String get authCreatingAccount => 'A criar conta...';

  @override
  String get taskDetailsTitle => 'Detalhes da Tarefa';

  @override
  String get taskEditTitle => 'Editar Tarefa';

  @override
  String get taskListTitle => 'Tarefas';

  @override
  String get taskSearchPlaceholder => 'Pesquisar tarefas...';

  @override
  String get taskNotFound => 'Tarefa não encontrada';

  @override
  String get taskLoadError => 'Falha ao carregar detalhes da tarefa';

  @override
  String get taskDeletedSuccess => 'Tarefa eliminada com sucesso';

  @override
  String get taskDeletedError => 'Falha ao eliminar tarefa';

  @override
  String get taskUpdatedSuccess => 'Tarefa atualizada com sucesso';

  @override
  String get taskUpdatedError => 'Falha ao atualizar tarefa';

  @override
  String get taskCreatedSuccess => 'Tarefa criada com sucesso';

  @override
  String taskCreatedError(String error) {
    return 'Falha ao criar tarefa: $error';
  }

  @override
  String get taskDeleteDialogTitle => 'Eliminar Tarefa';

  @override
  String get taskDeleteDialogMessage =>
      'Tem a certeza de que deseja eliminar esta tarefa? Esta ação não pode ser revertida.';

  @override
  String get taskDescriptionLabel => 'Descrição';

  @override
  String get taskInformationLabel => 'Informações da Tarefa';

  @override
  String get taskDatesLabel => 'Datas';

  @override
  String get taskDueDateLabel => 'Data de Vencimento';

  @override
  String get taskNoDueDate => 'Sem data de vencimento';

  @override
  String get taskSetDueDate => 'Definir data de vencimento';

  @override
  String get taskStatusLabel => 'Estado';

  @override
  String get taskPriorityLabel => 'Prioridade';

  @override
  String taskPriorityText(String priority) {
    return 'Prioridade $priority';
  }

  @override
  String get taskCreatedAtLabel => 'Criada em';

  @override
  String get taskUpdatedAtLabel => 'Última Actualização';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleHint => 'Digite o título da tarefa';

  @override
  String get taskDescriptionHint => 'Digite a descrição da tarefa';

  @override
  String get taskDescriptionOptionalHint =>
      'Digite a descrição da tarefa (opcional)';

  @override
  String get taskDueDateOptionalHint =>
      'Seleccione a data de vencimento (opcional)';

  @override
  String get taskTitleRequired => 'Título *';

  @override
  String get taskStatusTodo => 'Por Fazer';

  @override
  String get taskStatusInProgress => 'Em Progresso';

  @override
  String get taskStatusDone => 'Concluída';

  @override
  String get taskPriorityHigh => 'Alta';

  @override
  String get taskPriorityMedium => 'Média';

  @override
  String get taskPriorityLow => 'Baixa';

  @override
  String get taskPriorityNone => 'Nenhuma';

  @override
  String get taskCreateTitle => 'Criar Tarefa';

  @override
  String get taskEmptyTitle => 'Ainda Sem Tarefas';

  @override
  String get taskEmptySubtitle => 'Crie a sua primeira tarefa para começar';

  @override
  String get taskEmptyTip1 => '• Toque no botão + para criar uma tarefa';

  @override
  String get taskEmptyTip2 => '• Adicione um título e descrição';

  @override
  String get taskEmptyTip3 => '• Defina a prioridade e data de vencimento';

  @override
  String get taskProgressTitle => 'O Seu Progresso';

  @override
  String get taskProgressNoTasks => 'Ainda sem tarefas';

  @override
  String taskProgressCompleted(int completed, int total) {
    return '$completed de $total concluídas';
  }

  @override
  String taskProgressCount(int completed, int total) {
    return '$completed/$total';
  }

  @override
  String get taskProgressWelcome =>
      'Bem-vindo! Vamos adicionar a sua primeira tarefa.';

  @override
  String get taskProgressEncouragement => 'Está a fazer um progresso constante';

  @override
  String get commonCancel => 'Cancelar';

  @override
  String get commonDelete => 'Eliminar';

  @override
  String get commonUpdate => 'Actualizar';

  @override
  String get commonRetry => 'Tentar Novamente';

  @override
  String get commonNA => 'N/D';

  @override
  String get projectsTitle => 'Projetos';

  @override
  String get projectsSearchPlaceholder => 'Pesquisar projetos...';

  @override
  String get projectsEmptyTitle => 'Ainda Sem Projetos';

  @override
  String get projectsEmptySubtitle =>
      'Crie o seu primeiro projeto para organizar as suas tarefas';

  @override
  String get projectsLoadError => 'Falha ao carregar projetos';

  @override
  String get projectsLoadMoreError => 'Falha ao carregar mais projetos';

  @override
  String get projectDeletedSuccess => 'Projeto eliminado com sucesso';

  @override
  String get projectDeletedError => 'Falha ao eliminar projeto';

  @override
  String get projectCreatedSuccess => 'Projeto criado com sucesso';

  @override
  String get projectCreatedError => 'Falha ao criar projeto';

  @override
  String get projectUpdatedSuccess => 'Projeto actualizado com sucesso';

  @override
  String get projectUpdatedError => 'Falha ao actualizar projeto';

  @override
  String get projectDetailsTitle => 'Detalhes do Projeto';

  @override
  String get projectCreateTitle => 'Criar Projeto';

  @override
  String get projectEditTitle => 'Editar Projeto';

  @override
  String get projectNameLabel => 'Nome do Projeto';

  @override
  String get projectNameHint => 'Introduza o nome do projeto';

  @override
  String get projectDescriptionLabel => 'Descrição';

  @override
  String get projectDescriptionHint =>
      'Introduza a descrição do projeto (opcional)';

  @override
  String get projectNameRequired => 'Nome do projeto é obrigatório';

  @override
  String get calendarTitle => 'Calendar';

  @override
  String calendarTasksFor(String date) {
    return 'Tasks for $date';
  }

  @override
  String calendarTaskCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count tasks',
      one: '1 task',
      zero: 'No tasks',
    );
    return '$_temp0';
  }

  @override
  String get calendarNoTasksForDate => 'No tasks scheduled for this date';

  @override
  String get profileTitle => 'Profile';

  @override
  String get profileDisplayName => 'Display Name';

  @override
  String get profileEmail => 'Email';

  @override
  String get profileLogout => 'Logout';

  @override
  String get profileLogoutConfirmTitle => 'Logout';

  @override
  String get profileLogoutConfirmMessage => 'Are you sure you want to logout?';

  @override
  String get profileErrorLoading => 'Unable to load user information';

  @override
  String get cancel => 'Cancel';
}

/// The translations for Portuguese, as used in Brazil (`pt_BR`).
class AppLocalizationsPtBr extends AppLocalizationsPt {
  AppLocalizationsPtBr() : super('pt_BR');

  @override
  String get appName => 'TaskIt';

  @override
  String get authAppName => 'Gerenciador de Tarefas';

  @override
  String get authLoginTitle => 'Bem-vindo de Volta';

  @override
  String get authRegisterTitle => 'Criar Conta';

  @override
  String get authEmail => 'E-mail';

  @override
  String get authEmailHint => 'Digite seu e-mail';

  @override
  String get authPassword => 'Senha';

  @override
  String get authPasswordHint => 'Digite sua senha';

  @override
  String get authConfirmPassword => 'Confirmar Senha';

  @override
  String get authName => 'Nome';

  @override
  String get authNameHint => 'Digite seu nome completo';

  @override
  String get authLoginButton => 'Entrar';

  @override
  String get authRegisterButton => 'Criar Conta';

  @override
  String get authSignUp => 'Cadastrar';

  @override
  String get authSignIn => 'Entrar';

  @override
  String get authWithoutAccount => 'Não tem uma conta?';

  @override
  String get authAlreadyHaveAccount => 'Já tem uma conta?';

  @override
  String get authEmailError => 'Por favor, insira um endereço de e-mail válido';

  @override
  String get authPasswordError => 'A senha deve ter pelo menos 8 caracteres';

  @override
  String authPasswordTooShort(int minLength) {
    return 'A senha deve ter pelo menos $minLength caracteres';
  }

  @override
  String get authNameError => 'O nome não deve estar vazio';

  @override
  String get authNameTooShort => 'O nome deve ter pelo menos 2 caracteres';

  @override
  String get authConfirmPasswordError => 'As senhas não coincidem';

  @override
  String authLoginError(String error) {
    return 'Falha no login: $error';
  }

  @override
  String authRegisterError(String error) {
    return 'Falha no cadastro: $error';
  }

  @override
  String get authForgotPassword => 'Esqueceu a senha?';

  @override
  String get authResetPassword => 'Redefinir Senha';

  @override
  String get authLogout => 'Sair';

  @override
  String get authLoggingIn => 'Entrando...';

  @override
  String get authCreatingAccount => 'Criando conta...';

  @override
  String get taskDetailsTitle => 'Detalhes da Tarefa';

  @override
  String get taskEditTitle => 'Editar Tarefa';

  @override
  String get taskListTitle => 'Tarefas';

  @override
  String get taskSearchPlaceholder => 'Pesquisar tarefas...';

  @override
  String get taskNotFound => 'Tarefa não encontrada';

  @override
  String get taskLoadError => 'Falha ao carregar detalhes da tarefa';

  @override
  String get taskDeletedSuccess => 'Tarefa excluída com sucesso';

  @override
  String get taskDeletedError => 'Falha ao excluir tarefa';

  @override
  String get taskUpdatedSuccess => 'Tarefa atualizada com sucesso';

  @override
  String get taskUpdatedError => 'Falha ao atualizar tarefa';

  @override
  String get taskCreatedSuccess => 'Tarefa criada com sucesso';

  @override
  String taskCreatedError(String error) {
    return 'Falha ao criar tarefa: $error';
  }

  @override
  String get taskDeleteDialogTitle => 'Excluir Tarefa';

  @override
  String get taskDeleteDialogMessage =>
      'Tem certeza de que deseja excluir esta tarefa? Esta ação não pode ser desfeita.';

  @override
  String get taskDescriptionLabel => 'Descrição';

  @override
  String get taskInformationLabel => 'Informações da Tarefa';

  @override
  String get taskDatesLabel => 'Datas';

  @override
  String get taskDueDateLabel => 'Data de Vencimento';

  @override
  String get taskNoDueDate => 'Sem data de vencimento';

  @override
  String get taskSetDueDate => 'Definir data de vencimento';

  @override
  String get taskStatusLabel => 'Status';

  @override
  String get taskPriorityLabel => 'Prioridade';

  @override
  String taskPriorityText(String priority) {
    return 'Prioridade $priority';
  }

  @override
  String get taskCreatedAtLabel => 'Criada em';

  @override
  String get taskUpdatedAtLabel => 'Última Atualização';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleHint => 'Digite o título da tarefa';

  @override
  String get taskDescriptionHint => 'Digite a descrição da tarefa';

  @override
  String get taskDescriptionOptionalHint =>
      'Digite a descrição da tarefa (opcional)';

  @override
  String get taskDueDateOptionalHint =>
      'Selecione a data de vencimento (opcional)';

  @override
  String get taskTitleRequired => 'Título *';

  @override
  String get taskStatusTodo => 'A Fazer';

  @override
  String get taskStatusInProgress => 'Em Andamento';

  @override
  String get taskStatusDone => 'Concluída';

  @override
  String get taskPriorityHigh => 'Alta';

  @override
  String get taskPriorityMedium => 'Média';

  @override
  String get taskPriorityLow => 'Baixa';

  @override
  String get taskPriorityNone => 'Nenhuma';

  @override
  String get taskCreateTitle => 'Criar Tarefa';

  @override
  String get taskEmptyTitle => 'Nenhuma Tarefa Ainda';

  @override
  String get taskEmptySubtitle => 'Crie sua primeira tarefa para começar';

  @override
  String get taskEmptyTip1 => '• Toque no botão + para criar uma tarefa';

  @override
  String get taskEmptyTip2 => '• Adicione um título e descrição';

  @override
  String get taskEmptyTip3 => '• Defina a prioridade e data de vencimento';

  @override
  String get taskProgressTitle => 'Seu Progresso';

  @override
  String get taskProgressNoTasks => 'Nenhuma tarefa ainda';

  @override
  String taskProgressCompleted(int completed, int total) {
    return '$completed de $total concluídas';
  }

  @override
  String taskProgressCount(int completed, int total) {
    return '$completed/$total';
  }

  @override
  String get taskProgressWelcome =>
      'Bem-vindo! Vamos adicionar sua primeira tarefa.';

  @override
  String get taskProgressEncouragement =>
      'Você está fazendo um progresso constante';

  @override
  String get commonCancel => 'Cancelar';

  @override
  String get commonDelete => 'Excluir';

  @override
  String get commonUpdate => 'Atualizar';

  @override
  String get commonRetry => 'Tentar Novamente';

  @override
  String get commonNA => 'N/D';

  @override
  String get projectsTitle => 'Projetos';

  @override
  String get projectsSearchPlaceholder => 'Buscar projetos...';

  @override
  String get projectsEmptyTitle => 'Nenhum Projeto Ainda';

  @override
  String get projectsEmptySubtitle =>
      'Crie seu primeiro projeto para organizar suas tarefas';

  @override
  String get projectsLoadError => 'Falha ao carregar projetos';

  @override
  String get projectsLoadMoreError => 'Falha ao carregar mais projetos';

  @override
  String get projectDeletedSuccess => 'Projeto excluído com sucesso';

  @override
  String get projectDeletedError => 'Falha ao excluir projeto';

  @override
  String get projectCreatedSuccess => 'Projeto criado com sucesso';

  @override
  String get projectCreatedError => 'Falha ao criar projeto';

  @override
  String get projectUpdatedSuccess => 'Projeto atualizado com sucesso';

  @override
  String get projectUpdatedError => 'Falha ao atualizar projeto';

  @override
  String get projectDetailsTitle => 'Detalhes do Projeto';

  @override
  String get projectCreateTitle => 'Criar Projeto';

  @override
  String get projectEditTitle => 'Editar Projeto';

  @override
  String get projectNameLabel => 'Nome do Projeto';

  @override
  String get projectNameHint => 'Digite o nome do projeto';

  @override
  String get projectDescriptionLabel => 'Descrição';

  @override
  String get projectDescriptionHint =>
      'Digite a descrição do projeto (opcional)';

  @override
  String get projectNameRequired => 'Nome do projeto é obrigatório';
}

package com.datasphere.fusex.domain.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTENTICACAO_OBRIGATORIA("AUTENTICACAO_OBRIGATORIA", "Autentique-se para realizar esta operação.", HttpStatus.UNAUTHORIZED),
    BENEFICIARIO_SEM_PERMISSAO("BENEFICIARIO_SEM_PERMISSAO", "Usuário sem permissão para consultar os dados desta guia.", HttpStatus.FORBIDDEN),
    QR_NAO_GERADO("QR_NAO_GERADO", "O QR Code ainda não foi gerado na emissão da guia.", HttpStatus.CONFLICT),
    QR_NAO_ENCONTRADO("QR_NAO_ENCONTRADO", "QR Code não encontrado.", HttpStatus.NOT_FOUND),
    OCS_DIFERENTE_DO_CONTRATO("OCS_DIFERENTE_DO_CONTRATO", "OCS não corresponde ao contrato.", HttpStatus.FORBIDDEN),
    GUIA_EXPIRADA("GUIA_EXPIRADA", "A guia está vencida.", HttpStatus.UNPROCESSABLE_ENTITY),
    PROCEDIMENTO_JA_REALIZADO("PROCEDIMENTO_JA_REALIZADO", "Procedimento já realizado.", HttpStatus.UNPROCESSABLE_ENTITY),
    SESSOES_INCOMPLETAS("SESSOES_INCOMPLETAS", "Sessões incompletas.", HttpStatus.UNPROCESSABLE_ENTITY),
    SESSOES_ESGOTADAS("SESSOES_ESGOTADAS", "Sessões esgotadas.", HttpStatus.UNPROCESSABLE_ENTITY),
    ATENDIMENTO_NAO_ENCONTRADO("ATENDIMENTO_NAO_ENCONTRADO", "Atendimento não encontrado.", HttpStatus.NOT_FOUND),
    GUIA_NAO_EMITIDA("GUIA_NAO_EMITIDA", "Guia não está EMITIDA.", HttpStatus.UNPROCESSABLE_ENTITY),

    BENEFICIARIO_NAO_ENCONTRADO("BENEFICIARIO_NAO_ENCONTRADO", "Beneficiário não encontrado.", HttpStatus.NOT_FOUND),
    PRE_GUIA_NAO_ENCONTRADA("PRE_GUIA_NAO_ENCONTRADA", "Pré-guia não encontrada.", HttpStatus.NOT_FOUND),
    PROCEDIMENTO_SEM_CONTRATO("PROCEDIMENTO_SEM_CONTRATO", "O procedimento não possui contrato com a OCS informada.", HttpStatus.UNPROCESSABLE_ENTITY),
    PRE_GUIA_STATUS_INVALIDO("PRE_GUIA_STATUS_INVALIDO", "O status atual da pré-guia não permite esta operação.", HttpStatus.CONFLICT),
    PRE_GUIA_NAO_VALIDADA("PRE_GUIA_NAO_VALIDADA", "Só é possível aprovar ou reprovar uma pré-guia validada.", HttpStatus.CONFLICT),
    PRE_GUIA_NAO_APROVADA("PRE_GUIA_NAO_APROVADA", "A pré-guia ainda não foi aprovada pelo médico responsável ou pelo chefe do FUSEx.", HttpStatus.CONFLICT),
    PRE_GUIA_JA_EMITIDA("PRE_GUIA_JA_EMITIDA", "A guia desta pré-guia já foi emitida.", HttpStatus.CONFLICT),
    STATUS_INVALIDO("STATUS_INVALIDO", "Status inválido.", HttpStatus.BAD_REQUEST),
    MOTIVO_OBRIGATORIO("MOTIVO_OBRIGATORIO", "O motivo da reprovação é obrigatório.", HttpStatus.BAD_REQUEST),
    APROVADOR_NAO_IDENTIFICADO("APROVADOR_NAO_IDENTIFICADO", "Informe o Prec-CP de quem está aprovando.", HttpStatus.UNAUTHORIZED),
    PERFIL_SEM_PERMISSAO("PERFIL_SEM_PERMISSAO", "Apenas o médico responsável ou o chefe do FUSEx podem fazer esta operação.", HttpStatus.FORBIDDEN),
    MEDICO_NAO_RESPONSAVEL("MEDICO_NAO_RESPONSAVEL", "Este médico não é o responsável pela pré-guia.", HttpStatus.FORBIDDEN),
    APROVACAO_MEDICO_PENDENTE("APROVACAO_MEDICO_PENDENTE", "O médico precisa aprovar antes do chefe do FUSEx.", HttpStatus.CONFLICT),
    MEDICO_NAO_ENCONTRADO("MEDICO_NAO_ENCONTRADO", "Médico não encontrado.", HttpStatus.UNPROCESSABLE_ENTITY),
    PROCEDIMENTO_REPETIDO("PROCEDIMENTO_REPETIDO", "O mesmo procedimento na mesma OCS foi pedido mais de uma vez.", HttpStatus.UNPROCESSABLE_ENTITY),
    APROVACAO_JA_REGISTRADA("APROVACAO_JA_REGISTRADA", "Esta aprovação já foi registrada.", HttpStatus.CONFLICT),

    FATURA_NAO_ENCONTRADA("FATURA_NAO_ENCONTRADA", "Fatura não encontrada.", HttpStatus.NOT_FOUND),
    FATURA_NAO_ENCAMINHAVEL("FATURA_NAO_ENCAMINHAVEL", "A fatura não está em um status que permita o encaminhamento.", HttpStatus.BAD_REQUEST),
    AUDITORIA_PENDENTE("AUDITORIA_PENDENTE", "Existem itens não autorizados na lisura. A fatura depende da auditoria.", HttpStatus.UNPROCESSABLE_ENTITY),
    MES_NAO_ENCERRADO("MES_NAO_ENCERRADO", "A fatura só pode ser encaminhada a partir do dia 6 do mês seguinte ao da competência.", HttpStatus.UNPROCESSABLE_ENTITY),
    ESPELHO_PENDENTE("ESPELHO_PENDENTE", "Ainda há espelhos desta OCS e deste mês esperando auditoria ou correção.", HttpStatus.UNPROCESSABLE_ENTITY),

    CREDENCIAIS_INVALIDAS("CREDENCIAIS_INVALIDAS", "CPF ou senha incorretos.", HttpStatus.UNAUTHORIZED),
    NAO_AUTENTICADO("NAO_AUTENTICADO", "Faça login para continuar.", HttpStatus.UNAUTHORIZED),
    ACESSO_NEGADO("ACESSO_NEGADO", "Seu perfil não tem acesso a esta operação.", HttpStatus.FORBIDDEN),
    PREC_CP_INVALIDO("PREC_CP_INVALIDO", "O Prec-CP precisa ter o formato 123456789-00.", HttpStatus.BAD_REQUEST),
    PREC_CP_TITULAR_INVALIDO("PREC_CP_TITULAR_INVALIDO", "O Prec-CP do beneficiário principal termina em -00.", HttpStatus.UNPROCESSABLE_ENTITY),
    PREC_CP_DEPENDENTE_INVALIDO("PREC_CP_DEPENDENTE_INVALIDO", "O Prec-CP do dependente não pode terminar em -00.", HttpStatus.UNPROCESSABLE_ENTITY),
    RESPONSAVEL_NAO_ENCONTRADO("RESPONSAVEL_NAO_ENCONTRADO", "Não encontramos um beneficiário principal com esse CPF.", HttpStatus.UNPROCESSABLE_ENTITY),
    PREC_CP_NAO_CONFERE("PREC_CP_NAO_CONFERE", "O Prec-CP não bate com o do responsável: o início precisa ser o mesmo.", HttpStatus.UNPROCESSABLE_ENTITY),
    BENEFICIARIO_NAO_CONFERE("BENEFICIARIO_NAO_CONFERE", "Este Prec-CP já está cadastrado com outra data de nascimento.", HttpStatus.UNPROCESSABLE_ENTITY),
    BENEFICIARIO_JA_CADASTRADO("BENEFICIARIO_JA_CADASTRADO", "Este Prec-CP já tem acesso. Entre com seu CPF e senha.", HttpStatus.CONFLICT),

    VALIDACAO("VALIDACAO", "Dados inválidos", HttpStatus.BAD_REQUEST),
    RECURSO_NAO_ENCONTRADO("RECURSO_NAO_ENCONTRADO", "Recurso não encontrado.", HttpStatus.NOT_FOUND),
    METODO_NAO_PERMITIDO("METODO_NAO_PERMITIDO", "Método HTTP não permitido para esta rota.", HttpStatus.METHOD_NOT_ALLOWED),
    ERRO_INTERNO("ERRO_INTERNO", "Erro inesperado no servidor. Tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR),
    ESPELHO_NAO_ENCONTRADO("ESPELHO_NAO_ENCONTRADO", "Espelho não encontrado.", HttpStatus.NOT_FOUND),
    ITEM_NAO_ENCONTRADO("ITEM_NAO_ENCONTRADO", "Item do espelho não encontrado.", HttpStatus.NOT_FOUND),
    ESPELHO_CANCELADO("ESPELHO_CANCELADO", "Operação não permitida em espelho cancelado.", HttpStatus.CONFLICT),
    ESPELHO_NAO_EDITAVEL("ESPELHO_NAO_EDITAVEL", "Só é possível alterar itens de espelhos em aberto ou reprovados.", HttpStatus.CONFLICT),
    ESPELHO_SEM_ITENS("ESPELHO_SEM_ITENS", "O espelho deve conter pelo menos um item.", HttpStatus.UNPROCESSABLE_ENTITY),
    OCS_DIVERGENTE("OCS_DIVERGENTE", "A OCS informada não corresponde à OCS do contrato do atendimento.", HttpStatus.UNPROCESSABLE_ENTITY),
    BENEFICIARIO_FORA_DA_GUIA("BENEFICIARIO_FORA_DA_GUIA", "O beneficiário informado não pertence à guia do atendimento.", HttpStatus.UNPROCESSABLE_ENTITY),
    ATENDIMENTO_NAO_REALIZADO("ATENDIMENTO_NAO_REALIZADO", "Atendimento não encontrado ou ainda não realizado.", HttpStatus.BAD_REQUEST),
    ATENDIMENTO_JA_EM_ESPELHO("ATENDIMENTO_JA_EM_ESPELHO", "Já existe um espelho para este atendimento.", HttpStatus.CONFLICT),
    ITEM_ESPELHO_NAO_CORRIGIDO("ITEM_ESPELHO_NAO_CORRIGIDO", "Não foi possível corrigir o item do espelho.", HttpStatus.UNPROCESSABLE_ENTITY),
    ITEM_ESPELHO_NAO_REMOVIDO("ITEM_ESPELHO_NAO_REMOVIDO", "Não foi possível remover o item do espelho.", HttpStatus.UNPROCESSABLE_ENTITY),
    ESPELHO_NAO_FATURAVEL("ESPELHO_NAO_FATURAVEL", "O espelho já foi faturado ou está cancelado.", HttpStatus.CONFLICT),
    ESPELHO_JA_AUDITADO("ESPELHO_JA_AUDITADO", "O espelho já foi auditado.", HttpStatus.CONFLICT),
    ESPELHO_NAO_REPROVADO("ESPELHO_NAO_REPROVADO", "Apenas espelhos reprovados podem ser reenviados.", HttpStatus.CONFLICT),
    FATURA_JA_ENCAMINHADA("FATURA_JA_ENCAMINHADA", "Fatura já encaminhada não aceita mais alterações.", HttpStatus.CONFLICT),
    AUDITOR_INVALIDO("AUDITOR_INVALIDO", "Auditor não encontrado ou sem permissão.", HttpStatus.FORBIDDEN),
    FATURA_NAO_REGERAVEL("FATURA_NAO_REGERAVEL", "A fatura já foi regerada ou o espelho não mudou depois da geração.", HttpStatus.CONFLICT),

    OCS_JA_CADASTRADA("OCS_JA_CADASTRADA", "Já existe uma OCS com este CNPJ.", HttpStatus.CONFLICT),
    PROCEDIMENTO_JA_CADASTRADO("PROCEDIMENTO_JA_CADASTRADO", "Já existe um procedimento com esse nome.", HttpStatus.CONFLICT),
    PROCEDIMENTO_NAO_ENCONTRADO("PROCEDIMENTO_NAO_ENCONTRADO", "Procedimento não encontrado.", HttpStatus.NOT_FOUND),
    VALOR_INVALIDO("VALOR_INVALIDO", "O valor do contrato precisa ser maior que zero.", HttpStatus.UNPROCESSABLE_ENTITY),
    CONTRATO_DUPLICADO("CONTRATO_DUPLICADO", "Já existe contrato desse procedimento com essa OCS.", HttpStatus.CONFLICT),
    PRIMEIRO_ACESSO_PENDENTE("PRIMEIRO_ACESSO_PENDENTE", "O militar precisa realizar o autocadastro primeiro.", HttpStatus.UNPROCESSABLE_ENTITY),
    PERFIL_OBRIGATORIO("PERFIL_OBRIGATORIO", "Informe pelo menos um perfil militar válido.", HttpStatus.UNPROCESSABLE_ENTITY),
    MEDICO_SEM_CRM("MEDICO_SEM_CRM", "Médico precisa de CRM.", HttpStatus.UNPROCESSABLE_ENTITY),
    CPF_JA_CADASTRADO("CPF_JA_CADASTRADO", "Já existe um usuário com este CPF.", HttpStatus.CONFLICT),
    CRM_JA_CADASTRADO("CRM_JA_CADASTRADO", "Já existe médico com este CRM.", HttpStatus.CONFLICT),
    OCS_NAO_ENCONTRADA("OCS_NAO_ENCONTRADA", "OCS não encontrada.", HttpStatus.UNPROCESSABLE_ENTITY),
    CADASTRO_RESTRITO_AO_CHEFE("CADASTRO_RESTRITO_AO_CHEFE", "Apenas o chefe do FUSEx pode cadastrar.", HttpStatus.FORBIDDEN),
    FATURA_TRANSICAO_INVALIDA("FATURA_TRANSICAO_INVALIDA", "O status atual da fatura não permite esta operação.", HttpStatus.CONFLICT),
    GUIA_NAO_ENCONTRADA("GUIA_NAO_ENCONTRADA", "Guia não encontrada.", HttpStatus.NOT_FOUND),
    DOCUMENTO_GUIA_INDISPONIVEL("DOCUMENTO_GUIA_INDISPONIVEL", "Esta guia foi emitida antes do registro do documento.", HttpStatus.NOT_FOUND),
    DOCUMENTO_GUIA_JA_GRAVADO("DOCUMENTO_GUIA_JA_GRAVADO", "O documento desta guia já foi gravado e não pode mudar.", HttpStatus.CONFLICT),

    DADOS_INVALIDOS("DADOS_INVALIDOS", "Dados da requisição inválidos.", HttpStatus.BAD_REQUEST);

    private final String codigo;
    private final String mensagem;
    private final HttpStatus status;

    ErrorCode(String codigo, String mensagem, HttpStatus status) {
        this.codigo = codigo;
        this.mensagem = mensagem;
        this.status = status;
    }

    public String getCodigo() { return codigo; }
    public String getMensagem() { return mensagem; }
    public HttpStatus getStatus() { return status; }
}

package com.datasphere.fusex.domain.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    QR_NAO_ENCONTRADO("QR_NAO_ENCONTRADO", "QR Code não encontrado.", HttpStatus.NOT_FOUND),
    OCS_DIFERENTE_DO_CONTRATO("OCS_DIFERENTE_DO_CONTRATO", "OCS não corresponde ao contrato.", HttpStatus.UNPROCESSABLE_ENTITY),
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
    PRE_GUIA_NAO_APROVADA("PRE_GUIA_NAO_APROVADA", "A pré-guia ainda não foi aprovada pelo médico e pelo chefe do FUSEx.", HttpStatus.CONFLICT),
    PRE_GUIA_JA_EMITIDA("PRE_GUIA_JA_EMITIDA", "A guia desta pré-guia já foi emitida.", HttpStatus.CONFLICT),
    STATUS_INVALIDO("STATUS_INVALIDO", "Status inválido.", HttpStatus.BAD_REQUEST),
    MOTIVO_OBRIGATORIO("MOTIVO_OBRIGATORIO", "O motivo da reprovação é obrigatório.", HttpStatus.BAD_REQUEST),
    APROVADOR_NAO_IDENTIFICADO("APROVADOR_NAO_IDENTIFICADO", "Informe o Prec-CP de quem está aprovando.", HttpStatus.UNAUTHORIZED),
    PERFIL_SEM_PERMISSAO("PERFIL_SEM_PERMISSAO", "Apenas o médico responsável ou o chefe do FUSEx podem fazer esta operação.", HttpStatus.FORBIDDEN),
    MEDICO_NAO_RESPONSAVEL("MEDICO_NAO_RESPONSAVEL", "Este médico não é o responsável pela pré-guia.", HttpStatus.FORBIDDEN),
    APROVACAO_MEDICO_PENDENTE("APROVACAO_MEDICO_PENDENTE", "O médico precisa aprovar antes do chefe do FUSEx.", HttpStatus.CONFLICT),
    APROVACAO_JA_REGISTRADA("APROVACAO_JA_REGISTRADA", "Esta aprovação já foi registrada.", HttpStatus.CONFLICT),

    FATURA_NAO_ENCONTRADA("FATURA_NAO_ENCONTRADA", "Fatura não encontrada.", HttpStatus.NOT_FOUND),
    FATURA_NAO_ENCAMINHAVEL("FATURA_NAO_ENCAMINHAVEL", "A fatura não está em um status que permita o encaminhamento.", HttpStatus.BAD_REQUEST),
    AUDITORIA_PENDENTE("AUDITORIA_PENDENTE", "Existem itens não autorizados na lisura. A fatura depende da auditoria.", HttpStatus.UNPROCESSABLE_ENTITY),

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
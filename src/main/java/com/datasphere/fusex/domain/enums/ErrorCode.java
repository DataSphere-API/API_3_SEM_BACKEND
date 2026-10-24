package com.datasphere.fusex.domain.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ---- Fluxo de QR / atendimento (comportamento original: 422; QR_NAO_ENCONTRADO = 404) ----
    QR_NAO_ENCONTRADO("QR_NAO_ENCONTRADO", "QR Code não encontrado.", HttpStatus.NOT_FOUND),
    OCS_DIFERENTE_DO_CONTRATO("OCS_DIFERENTE_DO_CONTRATO", "OCS não corresponde ao contrato."),
    GUIA_EXPIRADA("GUIA_EXPIRADA", "Guia fora do prazo de validade."),
    PROCEDIMENTO_JA_REALIZADO("PROCEDIMENTO_JA_REALIZADO", "Procedimento já realizado."),
    SESSOES_INCOMPLETAS("SESSOES_INCOMPLETAS", "Sessões incompletas."),
    SESSOES_ESGOTADAS("SESSOES_ESGOTADAS", "Sessões esgotadas."),
    ATENDIMENTO_NAO_ENCONTRADO("ATENDIMENTO_NAO_ENCONTRADO", "Atendimento não encontrado."),
    GUIA_NAO_EMITIDA("GUIA_NAO_EMITIDA", "Guia não está EMITIDA."),

    // ---- Login e permissão ----
    NAO_AUTENTICADO("NAO_AUTENTICADO", "Informe o token no cabeçalho Authorization (Bearer).", HttpStatus.UNAUTHORIZED),
    CREDENCIAIS_INVALIDAS("CREDENCIAIS_INVALIDAS", "Login ou senha inválidos.", HttpStatus.UNAUTHORIZED),
    SEM_PERMISSAO("SEM_PERMISSAO", "Seu perfil não pode executar esta ação.", HttpStatus.FORBIDDEN),

    // ---- Pré-guia: dados de entrada (400) ----
    DADOS_OBRIGATORIOS("DADOS_OBRIGATORIOS",
            "Informe o beneficiário, o médico responsável e pelo menos um procedimento.", HttpStatus.BAD_REQUEST),
    SESSOES_INVALIDAS("SESSOES_INVALIDAS", "A quantidade de sessões precisa ser maior que zero.", HttpStatus.BAD_REQUEST),
    MOTIVO_OBRIGATORIO("MOTIVO_OBRIGATORIO", "O motivo da reprovação é obrigatório.", HttpStatus.BAD_REQUEST),
    STATUS_INVALIDO("STATUS_INVALIDO", "Status inválido.", HttpStatus.BAD_REQUEST),

    // ---- Pré-guia: não encontrado (404) ----
    PRE_GUIA_NAO_ENCONTRADA("PRE_GUIA_NAO_ENCONTRADA", "Pré-guia não encontrada.", HttpStatus.NOT_FOUND),
    BENEFICIARIO_NAO_ENCONTRADO("BENEFICIARIO_NAO_ENCONTRADO", "Beneficiário não encontrado.", HttpStatus.NOT_FOUND),
    MEDICO_NAO_ENCONTRADO("MEDICO_NAO_ENCONTRADO", "Médico não encontrado.", HttpStatus.NOT_FOUND),
    PROCEDIMENTO_NAO_ENCONTRADO("PROCEDIMENTO_NAO_ENCONTRADO", "Procedimento não encontrado.", HttpStatus.NOT_FOUND),
    OCS_NAO_ENCONTRADA("OCS_NAO_ENCONTRADA", "OCS não encontrada.", HttpStatus.NOT_FOUND),

    // ---- Pré-guia: regra de fluxo (409) ----
    PRE_GUIA_STATUS_INVALIDO("PRE_GUIA_STATUS_INVALIDO",
            "A pré-guia não está em um status que permita esta ação.", HttpStatus.CONFLICT),
    APROVACAO_FORA_DE_ORDEM("APROVACAO_FORA_DE_ORDEM",
            "O médico responsável precisa decidir antes do chefe do FUSEx.", HttpStatus.CONFLICT),

    // ---- Pré-guia: contrato (422) ----
    PROCEDIMENTO_SEM_CONTRATO("PROCEDIMENTO_SEM_CONTRATO",
            "Procedimento sem contrato com a OCS informada.", HttpStatus.UNPROCESSABLE_ENTITY);

    private final String codigo;
    private final String mensagem;
    private final HttpStatus status;

    ErrorCode(String codigo, String mensagem) {
        this(codigo, mensagem, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    ErrorCode(String codigo, String mensagem, HttpStatus status) {
        this.codigo = codigo;
        this.mensagem = mensagem;
        this.status = status;
    }

    public String getCodigo() { return codigo; }
    public String getMensagem() { return mensagem; }
    public HttpStatus getStatus() { return status; }
}
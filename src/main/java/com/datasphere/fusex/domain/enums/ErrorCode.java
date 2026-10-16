package com.datasphere.fusex.domain.enums;

public enum ErrorCode {
    QR_NAO_ENCONTRADO("QR_NAO_ENCONTRADO", "QR Code não encontrado."),
    OCS_DIFERENTE_DO_CONTRATO("OCS_DIFERENTE_DO_CONTRATO", "OCS não corresponde ao contrato."),
    GUIA_EXPIRADA("GUIA_EXPIRADA", "Guia fora do prazo de validade."),

    ATENDIMENTO_NAO_ENCONTRADO("ATENDIMENTO_NAO_ENCONTRADO", "Atendimento não encontrado."),
    GUIA_NAO_EMITIDA("GUIA_NAO_EMITIDA", "Guia não está EMITIDA ou PARCIALMENTE_REALIZADA."),
    SESSOES_ESGOTADAS("SESSOES_ESGOTADAS", "Não há saldo de sessões disponível."),

    PROCEDIMENTO_JA_REALIZADO("PROCEDIMENTO_JA_REALIZADO", "Procedimento já realizado."),
    SESSOES_INCOMPLETAS("SESSOES_INCOMPLETAS", "Sessões incompletas.");

    private final String codigo;
    private final String mensagem;

    ErrorCode(String codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    public String getCodigo() { return codigo; }
    public String getMensagem() { return mensagem; }
}
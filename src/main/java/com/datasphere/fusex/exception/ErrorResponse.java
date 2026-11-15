package com.datasphere.fusex.exception;

// detalhes: lista na validação, mapa nas regras de negócio.
public record ErrorResponse(String codigo, String mensagem, Object detalhes) {}

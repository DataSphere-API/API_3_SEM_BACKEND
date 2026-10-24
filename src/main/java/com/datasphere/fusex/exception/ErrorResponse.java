package com.datasphere.fusex.exception;

import java.util.Map;

public record ErrorResponse(String codigo, String mensagem, Map<String, Object> detalhes) {}
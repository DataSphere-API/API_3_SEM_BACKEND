package com.datasphere.fusex.contrato.dto;

import java.math.BigDecimal;

public record ContratoRequest(String ocsCnpj, Integer procedimentoId, BigDecimal valor) {}

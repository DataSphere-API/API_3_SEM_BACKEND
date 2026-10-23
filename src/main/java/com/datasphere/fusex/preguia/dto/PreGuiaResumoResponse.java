package com.datasphere.fusex.preguia.dto;

import com.datasphere.fusex.guia.StatusGuia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PreGuiaResumoResponse(
        Integer id,
        String beneficiarioNome,
        StatusGuia status,
        int qtdProcedimentos,
        BigDecimal valorTotal,
        LocalDate dataCriacao
) {}
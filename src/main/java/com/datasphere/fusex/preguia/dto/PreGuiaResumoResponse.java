package com.datasphere.fusex.preguia.dto;

import com.datasphere.fusex.preguia.StatusPreGuia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PreGuiaResumoResponse(
        Long id,
        String beneficiarioNome,
        StatusPreGuia status,
        int qtdProcedimentos,
        BigDecimal valorTotal,
        LocalDate dataCriacao,
        boolean medicoAprovou,
        boolean chefeAprovou,
        LocalDate dataEmissao) {}
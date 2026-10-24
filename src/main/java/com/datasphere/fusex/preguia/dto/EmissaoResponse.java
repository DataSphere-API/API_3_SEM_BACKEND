package com.datasphere.fusex.preguia.dto;

import java.time.LocalDate;
import java.util.List;

public record EmissaoResponse(
        Long guiaId,
        String status,
        LocalDate dataEmissao,
        LocalDate validoAte,
        List<EmissaoProcedimentoResponse> procedimentos) {}
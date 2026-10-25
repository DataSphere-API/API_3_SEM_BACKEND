package com.datasphere.fusex.guia.dto.request;

import java.time.LocalDate;
import java.util.List;

public record GuiaBeneficiarioResponse(
        Long guiaId,
        String status,
        LocalDate dataEmissao,
        long diasRestantes,
        List<ProcedimentoGuiaResponse> procedimentos) {}
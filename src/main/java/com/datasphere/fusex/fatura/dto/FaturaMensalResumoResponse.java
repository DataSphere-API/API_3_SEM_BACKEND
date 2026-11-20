package com.datasphere.fusex.fatura.dto;

import com.datasphere.fusex.fatura.StatusFatura;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FaturaMensalResumoResponse(
        Long id,
        String ocsId,
        String ocsNome,
        String competencia,
        StatusFatura status,
        int quantidadeEspelhos,
        int quantidadeItens,
        BigDecimal valorApresentado,
        BigDecimal valorContratado,
        int espelhosPendentes,
        LocalDateTime dataGeracao,
        LocalDateTime dataEncaminhamento,
        boolean podeEncaminhar) {
}

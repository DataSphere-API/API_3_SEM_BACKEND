package com.datasphere.fusex.dto.request;

import java.time.LocalDate;

public record ValidacaoQrResultado(
        Long atendimentoId,
        Integer qtdAutorizada,
        Integer qtdRealizada,
        boolean prontoParaBaixa,
        LocalDate dataValidade
) {
}
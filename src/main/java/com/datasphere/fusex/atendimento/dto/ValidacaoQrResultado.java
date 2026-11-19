package com.datasphere.fusex.atendimento.dto;

import java.time.LocalDate;

public record ValidacaoQrResultado(
        Long atendimentoId,
        Integer qtdAutorizada,
        Integer qtdRealizada,
        boolean prontoParaBaixa,
        LocalDate dataValidade
) {
}
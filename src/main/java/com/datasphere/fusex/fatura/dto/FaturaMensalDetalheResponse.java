package com.datasphere.fusex.fatura.dto;

import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.fatura.StatusFatura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FaturaMensalDetalheResponse(
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
        boolean podeEncaminhar,
        List<Espelho> espelhos,
        List<Pendente> pendentes) {

    public record Espelho(Integer espelhoId, LocalDate dataInicio, LocalDate dataFim, List<Item> itens) {}

    public record Item(Integer id, Integer espelhoItemId, Long atendimentoId, String descricao,
                       String beneficiarioId, BigDecimal valorApresentado, BigDecimal valorContratado,
                       BigDecimal divergenciaValor) {}

    public record Pendente(Integer espelhoId, StatusEspelho status) {}
}

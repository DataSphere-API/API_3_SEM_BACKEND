package com.datasphere.fusex.preguia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PacoteSireResponse(
        String versao,
        Long preGuiaId,
        String status,
        LocalDateTime geradoEm,
        AprovadoPor aprovadoPor,
        Beneficiario beneficiario,
        Medico medico,
        List<Procedimento> procedimentos,
        BigDecimal valorTotal,
        long validadeDias,
        String hash) {

    public record AprovadoPor(Integer chefeId, LocalDateTime dataAprovacaoChefe, LocalDateTime dataAprovacaoMedico) {}

    public record Beneficiario(String precCp, String nome, LocalDate dataNascimento,
                               boolean dependente, String grupo, String ug) {}

    public record Medico(String crm, String nome) {}

    public record Procedimento(Long atendimentoId, String nome, String ocsCnpj, String ocsNome,
                               int qtdSessoes, BigDecimal valorUnitario, BigDecimal valorTotal) {}
}
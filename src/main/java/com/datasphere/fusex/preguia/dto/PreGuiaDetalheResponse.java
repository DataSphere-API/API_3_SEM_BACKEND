package com.datasphere.fusex.preguia.dto;

import com.datasphere.fusex.preguia.StatusPreGuia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PreGuiaDetalheResponse(
        Long id,
        StatusPreGuia status,
        Beneficiario beneficiario,
        Medico medico,
        List<PreGuiaProcedimentoResponse> procedimentos,
        BigDecimal valorTotal,
        ValidacaoPreGuiaResponse validacao,
        LocalDate dataCriacao,
        LocalDateTime dataAprovacaoMedico,
        LocalDateTime dataAprovacaoChefe,
        String motivoReprovacao,
        LocalDate dataEmissao,
        LocalDate validoAte) {

    public record Beneficiario(String precCp, String nome) {}

    public record Medico(Integer id, String nome, String crm) {}
}
package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.guia.dto.request.PreGuiaDetalheResponse;
import com.datasphere.fusex.guia.dto.request.PreGuiaDetalheResponse.Beneficiario;
import com.datasphere.fusex.guia.dto.request.PreGuiaDetalheResponse.Medico;
import com.datasphere.fusex.guia.dto.request.PreGuiaDetalheResponse.Validacao;
import com.datasphere.fusex.guia.dto.request.PreGuiaProcedimentoResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PreGuiaService {

    public static class PreGuiaNaoEncontradaException extends RuntimeException {
        public PreGuiaNaoEncontradaException(Long id) {
            super("Pré-guia " + id + " não encontrada");
        }
    }

    // Dados mínimos que a tela precisa. Não dependem das entidades.
    record ProcedimentoDados(Long atendimentoId, String nome, String ocsCnpj, String ocsNome,
                             Integer qtdSessoes, BigDecimal valorContratado) {}
    record PreGuiaDados(Long id, String status, String precCp, String beneficiarioNome,
                        String crm, List<ProcedimentoDados> procedimentos) {}

    public PreGuiaDetalheResponse buscarPreGuiaPorId(Long id) {
        PreGuiaDados dados = carregarPreGuia(id)
                .orElseThrow(() -> new PreGuiaNaoEncontradaException(id));

        List<PreGuiaProcedimentoResponse> procedimentos = listarProcedimentosDaPreGuia(dados);

        return new PreGuiaDetalheResponse(
                dados.id(),
                dados.status(),
                new Beneficiario(dados.precCp(), dados.beneficiarioNome()),
                new Medico(dados.crm()),
                procedimentos,
                calcularValorTotalPreGuia(procedimentos),
                // MOCK: trocar pelo resultado do consolidarPendencias (US006-1)
                new Validacao(true, List.of()));
    }

    List<PreGuiaProcedimentoResponse> listarProcedimentosDaPreGuia(PreGuiaDados d) {
        return d.procedimentos().stream()
                .map(p -> new PreGuiaProcedimentoResponse(
                        p.atendimentoId(),
                        p.nome(),
                        new PreGuiaProcedimentoResponse.Ocs(p.ocsCnpj(), p.ocsNome()),
                        p.qtdSessoes(),
                        p.valorContratado(),
                        p.valorContratado().multiply(BigDecimal.valueOf(p.qtdSessoes()))))
                .toList();
    }

    BigDecimal calcularValorTotalPreGuia(List<PreGuiaProcedimentoResponse> procedimentos) {
        return procedimentos.stream()
                .map(PreGuiaProcedimentoResponse::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // TODO(US000-11): MOCK TEMPORÁRIO. Trocar por leitura real de Guia/GuiaProc/contrato.
    // No mock, só o id 7 existe; qualquer outro devolve 404.
    private Optional<PreGuiaDados> carregarPreGuia(Long id) {
        if (!Long.valueOf(7).equals(id)) {
            return Optional.empty();
        }
        return Optional.of(new PreGuiaDados(7L, "VALIDADA", "123456", "Fulano", "SP123456",
                List.of(
                        new ProcedimentoDados(10L, "TC Abdome Superior", "12345678000199", "Clínica X",
                                1, new BigDecimal("720.49")),
                        new ProcedimentoDados(11L, "Ultrassom Abdominal", "12345678000199", "Clínica X",
                                1, new BigDecimal("615.93")))));
    }
}
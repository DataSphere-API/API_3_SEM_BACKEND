package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.guia.dto.request.GuiaBeneficiarioResponse;
import com.datasphere.fusex.guia.dto.request.ProcedimentoGuiaResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GuiaBeneficiarioService {

    public static class BeneficiarioNaoEncontradoException extends RuntimeException {
        public BeneficiarioNaoEncontradoException(String precCp) {
            super("Beneficiário " + precCp + " não encontrado");
        }
    }

    // Dados mínimos que a listagem precisa. Não dependem das entidades.
    record BeneficiarioDados(String precCp, String nome) {}
    record ProcedimentoDados(Long atendimentoId, String nome, String ocsCnpj, String ocsNome,
                             String status, Integer qtdAutorizada, long qtdRealizada, boolean temQr) {}
    record GuiaDados(Long id, String status, LocalDate dataEmissao, List<ProcedimentoDados> procedimentos) {}

    // Padrão 60 caso a propriedade ainda não exista no application.properties
    @Value("${fusex.guia.validade-dias:60}")
    private long validadeDias;

    public List<GuiaBeneficiarioResponse> listarGuiasPorBeneficiario(String precCp) {
        BeneficiarioDados beneficiario = buscarBeneficiarioPorPrecCp(precCp);

        return carregarGuias(beneficiario.precCp()).stream()
                .filter(g -> g.dataEmissao() != null) // pré-guia ainda não foi emitida: não lista
                .map(g -> new GuiaBeneficiarioResponse(
                        g.id(),
                        g.status(),
                        g.dataEmissao(),
                        calcularDiasRestantes(g.dataEmissao()),
                        listarProcedimentosDaGuia(g)))
                .toList();
    }

    BeneficiarioDados buscarBeneficiarioPorPrecCp(String precCp) {
        return carregarBeneficiario(precCp)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(precCp));
    }

    List<ProcedimentoGuiaResponse> listarProcedimentosDaGuia(GuiaDados g) {
        return g.procedimentos().stream()
                .map(p -> new ProcedimentoGuiaResponse(
                        p.atendimentoId(),
                        p.nome(),
                        new ProcedimentoGuiaResponse.Ocs(p.ocsCnpj(), p.ocsNome()),
                        p.status(),
                        p.qtdAutorizada(),
                        p.qtdRealizada(),
                        p.temQr()))
                .toList();
    }

    // Validade = dataEmissao + validadeDias. Nunca devolve negativo.
    long calcularDiasRestantes(LocalDate dataEmissao) {
        long diasDesdeEmissao = ChronoUnit.DAYS.between(dataEmissao, LocalDate.now());
        return Math.max(0, validadeDias - diasDesdeEmissao);
    }

    // TODO(US000-11): MOCKS TEMPORÁRIOS. Trocar por leitura real de Beneficiario/Guia/GuiaProc.
    // Na versão real, qtdRealizada = contagem em Sessao_Procedimento.
    // No mock, só o precCp "123456" existe; qualquer outro devolve 404.
    private Optional<BeneficiarioDados> carregarBeneficiario(String precCp) {
        if (!"123456".equals(precCp)) {
            return Optional.empty();
        }
        return Optional.of(new BeneficiarioDados("123456", "Fulano"));
    }

    private List<GuiaDados> carregarGuias(String precCp) {
        return List.of(
                new GuiaDados(4L, "EMITIDA", LocalDate.now().minusDays(8), List.of(
                        new ProcedimentoDados(10L, "Sessão de psicoterapia", "12345678000199", "Clínica X",
                                "EM_ANDAMENTO", 10, 3, true))),
                // pré-guia (sem data de emissão): não deve aparecer na lista
                new GuiaDados(7L, "PRE_GUIA", null, List.of()));
    }
}
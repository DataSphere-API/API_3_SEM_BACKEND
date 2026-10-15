package com.datasphere.fusex.guia.service;

import static com.datasphere.fusex.guia.dto.request.PendenciaResponse.CodigoPendencia.*;

import com.datasphere.fusex.guia.dto.request.PendenciaResponse;
import com.datasphere.fusex.guia.dto.request.ValidacaoPreGuiaResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PreGuiaValidacaoService {

    // Dados mínimos que a validação precisa. Não dependem das entidades.
    record BeneficiarioDados(LocalDate dataNascimento) {}
    record MedicoDados(String crm) {}
    // valorContratado == null significa "sem contrato com a OCS"
    record ProcedimentoDados(Long atendimentoId, String nome, Integer qtdAutorizada, BigDecimal valorContratado) {}
    record PreGuiaDados(BeneficiarioDados beneficiario, MedicoDados medico, String ocsNome,
                        BigDecimal valorTotalInformado, List<ProcedimentoDados> procedimentos) {}

    public ValidacaoPreGuiaResponse consolidarPendencias(Long preGuiaId) {
        PreGuiaDados dados = carregarPreGuia(preGuiaId);

        List<PendenciaResponse> pendencias = new ArrayList<>();
        pendencias.addAll(validarBeneficiarioDaPreGuia(dados));
        pendencias.addAll(validarMedicoDaPreGuia(dados));
        pendencias.addAll(validarContratoDosProcedimentos(dados));
        pendencias.addAll(validarQuantidadeDeSessoes(dados));
        pendencias.addAll(validarValorTotal(dados));

        return new ValidacaoPreGuiaResponse(pendencias.isEmpty(), pendencias);
    }

    List<PendenciaResponse> validarBeneficiarioDaPreGuia(PreGuiaDados d) {
        if (d.beneficiario() == null || d.beneficiario().dataNascimento() == null) {
            return List.of(new PendenciaResponse(BENEFICIARIO_INVALIDO, null,
                    "Beneficiário inexistente ou sem data de nascimento"));
        }
        return List.of();
    }

    List<PendenciaResponse> validarMedicoDaPreGuia(PreGuiaDados d) {
        if (d.medico() == null || d.medico().crm() == null || d.medico().crm().isBlank()) {
            return List.of(new PendenciaResponse(MEDICO_SEM_CRM, null, "Médico sem CRM informado"));
        }
        return List.of();
    }

    List<PendenciaResponse> validarContratoDosProcedimentos(PreGuiaDados d) {
        if (d.procedimentos().isEmpty()) {
            return List.of(new PendenciaResponse(PRE_GUIA_SEM_PROCEDIMENTOS, null,
                    "A pré-guia não possui procedimentos"));
        }
        return d.procedimentos().stream()
                .filter(p -> p.valorContratado() == null)
                .map(p -> new PendenciaResponse(PROCEDIMENTO_SEM_CONTRATO, p.atendimentoId(),
                        "Procedimento " + p.nome() + " não possui contrato com a OCS " + d.ocsNome()))
                .toList();
    }

    List<PendenciaResponse> validarQuantidadeDeSessoes(PreGuiaDados d) {
        return d.procedimentos().stream()
                .filter(p -> p.qtdAutorizada() == null || p.qtdAutorizada() <= 0)
                .map(p -> new PendenciaResponse(SESSOES_INVALIDAS, p.atendimentoId(),
                        "Quantidade autorizada de " + p.nome() + " deve ser maior que zero"))
                .toList();
    }

    List<PendenciaResponse> validarValorTotal(PreGuiaDados d) {
        BigDecimal esperado = d.procedimentos().stream()
                .filter(p -> p.valorContratado() != null && p.qtdAutorizada() != null && p.qtdAutorizada() > 0)
                .map(p -> p.valorContratado().multiply(BigDecimal.valueOf(p.qtdAutorizada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (d.valorTotalInformado() != null && esperado.compareTo(d.valorTotalInformado()) != 0) {
            return List.of(new PendenciaResponse(VALOR_TOTAL_DIVERGENTE, null,
                    "Valor total informado " + d.valorTotalInformado() + " difere do calculado " + esperado));
        }
        return List.of();
    }

    // TODO(US000-11): MOCK TEMPORÁRIO. Trocar por leitura real de Guia/GuiaProc/contrato
    // quando a US000-11 for mergeada. As regras acima não mudam.
    private PreGuiaDados carregarPreGuia(Long id) {
        return new PreGuiaDados(
                new BeneficiarioDados(null),
                new MedicoDados("SP123456"),
                "Clínica X",
                new BigDecimal("100.00"),
                List.of(
                        new ProcedimentoDados(12L, "TC Abdome Superior", 0, new BigDecimal("720.49")),
                        new ProcedimentoDados(13L, "Sessão de psicoterapia", 3, null)));
    }
}
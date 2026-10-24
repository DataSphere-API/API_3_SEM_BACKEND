package com.datasphere.fusex.guia.service;

import static com.datasphere.fusex.guia.dto.request.PendenciaResponse.CodigoPendencia.*;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.request.PendenciaResponse;
import com.datasphere.fusex.guia.dto.request.ValidacaoPreGuiaResponse;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreGuiaValidacaoService {

    private final GuiaRepository guiaRepository;
    private final GuiaProcRepository guiaProcRepository;

    public PreGuiaValidacaoService(GuiaRepository guiaRepository,
                                   GuiaProcRepository guiaProcRepository) {
        this.guiaRepository = guiaRepository;
        this.guiaProcRepository = guiaProcRepository;
    }

    @Transactional(readOnly = true)
    public ValidacaoPreGuiaResponse consolidarPendencias(Long preGuiaId) {
        GuiaModel guia = guiaRepository.findById(preGuiaId).orElse(null);
        if (guia == null) {
            throw new ValidarPreGuiaService.PreGuiaNaoEncontradaException(preGuiaId);
        }

        return consolidarPendencias(guia);
    }

    @Transactional(readOnly = true)
    public ValidacaoPreGuiaResponse consolidarPendencias(GuiaModel guia) {
        BeneficiarioModel beneficiario = guia.getBeneficiarioModel();
        MedicoModel medico = guia.getMedicoModel();
        List<ProcedimentoDados> procedimentos = guiaProcRepository.findByGuiaModel_Id(guia.getId())
                .stream()
                .map(this::mapearProcedimento)
                .toList();

        PreGuiaDados dados = new PreGuiaDados(
                beneficiario == null ? null : new BeneficiarioDados(beneficiario.getDataNasc()),
                medico == null ? null : new MedicoDados(medico.getCrm()),
                guia.getValorTotal(),
                procedimentos);

        List<PendenciaResponse> pendencias = new ArrayList<>();
        pendencias.addAll(validarBeneficiarioDaPreGuia(dados));
        pendencias.addAll(validarMedicoDaPreGuia(dados));
        pendencias.addAll(validarContratoDosProcedimentos(dados));
        pendencias.addAll(validarQuantidadeDeSessoes(dados));
        pendencias.addAll(validarValorTotal(dados));

        return new ValidacaoPreGuiaResponse(pendencias.isEmpty(), pendencias);
    }

    private ProcedimentoDados mapearProcedimento(GuiaProcModel guiaProc) {
        ContratoModel contrato = guiaProc.getContratoModel();
        ProcedimentoModel procedimento = contrato == null ? null : contrato.getProcedimentoModel();
        String ocsNome = contrato == null || contrato.getOcsModel() == null
                ? null : contrato.getOcsModel().getNome();

        return new ProcedimentoDados(
                guiaProc.getGuiaId(),
                procedimento == null ? "desconhecido" : procedimento.getNome(),
                guiaProc.getQtdAutorizada(),
                contrato == null ? null : contrato.getValor(),
                ocsNome);
    }

    List<PendenciaResponse> validarBeneficiarioDaPreGuia(PreGuiaDados dados) {
        if (dados.beneficiario() == null || dados.beneficiario().dataNascimento() == null) {
            return List.of(new PendenciaResponse(BENEFICIARIO_INVALIDO, null,
                    "Beneficiário inexistente ou sem data de nascimento"));
        }
        return List.of();
    }

    List<PendenciaResponse> validarMedicoDaPreGuia(PreGuiaDados dados) {
        if (dados.medico() == null || dados.medico().crm() == null || dados.medico().crm().isBlank()) {
            return List.of(new PendenciaResponse(MEDICO_SEM_CRM, null, "Médico sem CRM informado"));
        }
        return List.of();
    }

    List<PendenciaResponse> validarContratoDosProcedimentos(PreGuiaDados dados) {
        if (dados.procedimentos().isEmpty()) {
            return List.of(new PendenciaResponse(PRE_GUIA_SEM_PROCEDIMENTOS, null,
                    "A pré-guia não possui procedimentos"));
        }

        return dados.procedimentos().stream()
                .filter(procedimento -> procedimento.valorContratado() == null)
                .map(procedimento -> new PendenciaResponse(PROCEDIMENTO_SEM_CONTRATO,
                        procedimento.atendimentoId(),
                        "Procedimento " + procedimento.nome() + " não possui contrato com a OCS "
                                + (procedimento.ocsNome() == null ? "informada" : procedimento.ocsNome())))
                .toList();
    }

    List<PendenciaResponse> validarQuantidadeDeSessoes(PreGuiaDados dados) {
        return dados.procedimentos().stream()
                .filter(procedimento -> procedimento.qtdAutorizada() == null
                        || procedimento.qtdAutorizada() <= 0)
                .map(procedimento -> new PendenciaResponse(SESSOES_INVALIDAS,
                        procedimento.atendimentoId(),
                        "Quantidade autorizada de " + procedimento.nome() + " deve ser maior que zero"))
                .toList();
    }

    List<PendenciaResponse> validarValorTotal(PreGuiaDados dados) {
        BigDecimal esperado = dados.procedimentos().stream()
                .filter(procedimento -> procedimento.valorContratado() != null
                        && procedimento.qtdAutorizada() != null)
                .map(procedimento -> procedimento.valorContratado()
                        .multiply(BigDecimal.valueOf(procedimento.qtdAutorizada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (dados.valorTotalInformado() == null
                || esperado.compareTo(dados.valorTotalInformado()) != 0) {
            return List.of(new PendenciaResponse(VALOR_TOTAL_DIVERGENTE, null,
                    "Valor total informado " + dados.valorTotalInformado()
                            + " difere do calculado " + esperado));
        }
        return List.of();
    }

    record BeneficiarioDados(LocalDate dataNascimento) {}
    record MedicoDados(String crm) {}
    record ProcedimentoDados(Long atendimentoId, String nome, Integer qtdAutorizada,
                             BigDecimal valorContratado, String ocsNome) {}
    record PreGuiaDados(BeneficiarioDados beneficiario, MedicoDados medico,
                        BigDecimal valorTotalInformado, List<ProcedimentoDados> procedimentos) {}
}

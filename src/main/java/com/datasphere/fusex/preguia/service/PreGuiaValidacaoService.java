package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.dto.PendenciaResponse;
import com.datasphere.fusex.preguia.dto.ValidacaoPreGuiaResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.datasphere.fusex.preguia.dto.PendenciaResponse.CodigoPendencia.BENEFICIARIO_INVALIDO;
import static com.datasphere.fusex.preguia.dto.PendenciaResponse.CodigoPendencia.MEDICO_SEM_CRM;
import static com.datasphere.fusex.preguia.dto.PendenciaResponse.CodigoPendencia.PRE_GUIA_SEM_PROCEDIMENTOS;
import static com.datasphere.fusex.preguia.dto.PendenciaResponse.CodigoPendencia.PROCEDIMENTO_SEM_CONTRATO;
import static com.datasphere.fusex.preguia.dto.PendenciaResponse.CodigoPendencia.SESSOES_INVALIDAS;

@Service
public class PreGuiaValidacaoService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final MedicoRepository medicoRepository;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService;

    public PreGuiaValidacaoService(BeneficiarioRepository beneficiarioRepository,
                                   MedicoRepository medicoRepository,
                                   PreGuiaProcRepository preGuiaProcRepository,
                                   BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.medicoRepository = medicoRepository;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.buscarContratoDaPreGuiaProcService = buscarContratoDaPreGuiaProcService;
    }

    public ValidacaoPreGuiaResponse consolidarPendencias(PreGuiaModel preGuia) {
        List<PreGuiaProcModel> procedimentos = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuia.getId());

        List<PendenciaResponse> pendencias = new ArrayList<>();
        pendencias.addAll(validarBeneficiarioDaPreGuia(preGuia));
        pendencias.addAll(validarMedicoDaPreGuia(preGuia));
        pendencias.addAll(validarProcedimentosDaPreGuia(procedimentos));
        pendencias.addAll(validarContratoDosProcedimentos(procedimentos));
        pendencias.addAll(validarQuantidadeDeSessoes(procedimentos));

        return new ValidacaoPreGuiaResponse(pendencias.isEmpty(), pendencias);
    }

    List<PendenciaResponse> validarBeneficiarioDaPreGuia(PreGuiaModel preGuia) {
        List<PendenciaResponse> pendencias = new ArrayList<>();
        BeneficiarioModel beneficiario = beneficiarioRepository.findByPrecCP(preGuia.getBenfId()).orElse(null);
        if (beneficiario == null || beneficiario.getDataNasc() == null) {
            pendencias.add(new PendenciaResponse(BENEFICIARIO_INVALIDO, null,
                    "Beneficiário inexistente ou sem data de nascimento."));
        }
        return pendencias;
    }

    List<PendenciaResponse> validarMedicoDaPreGuia(PreGuiaModel preGuia) {
        List<PendenciaResponse> pendencias = new ArrayList<>();
        MedicoModel medico = medicoRepository.findById(preGuia.getMedId()).orElse(null);
        if (medico == null || medico.getCrm() == null || medico.getCrm().isBlank()) {
            pendencias.add(new PendenciaResponse(MEDICO_SEM_CRM, null, "O médico responsável não tem CRM cadastrado."));
        }
        return pendencias;
    }

    List<PendenciaResponse> validarProcedimentosDaPreGuia(List<PreGuiaProcModel> procedimentos) {
        List<PendenciaResponse> pendencias = new ArrayList<>();
        if (procedimentos.isEmpty()) {
            pendencias.add(new PendenciaResponse(PRE_GUIA_SEM_PROCEDIMENTOS, null, "A pré-guia não tem procedimentos."));
        }
        return pendencias;
    }

    List<PendenciaResponse> validarContratoDosProcedimentos(List<PreGuiaProcModel> procedimentos) {
        List<PendenciaResponse> pendencias = new ArrayList<>();
        for (PreGuiaProcModel procedimento : procedimentos) {
            if (buscarContratoDaPreGuiaProcService.buscarContrato(procedimento) == null) {
                pendencias.add(new PendenciaResponse(PROCEDIMENTO_SEM_CONTRATO, procedimento.getId(),
                        "Procedimento " + procedimento.getProcedimentoModel().getNome()
                                + " não possui contrato com a OCS " + procedimento.getOcsModel().getNome() + "."));
            }
        }
        return pendencias;
    }

    List<PendenciaResponse> validarQuantidadeDeSessoes(List<PreGuiaProcModel> procedimentos) {
        List<PendenciaResponse> pendencias = new ArrayList<>();
        for (PreGuiaProcModel procedimento : procedimentos) {
            Integer quantidade = procedimento.getQtdAutorizada();
            if (quantidade == null || quantidade <= 0) {
                pendencias.add(new PendenciaResponse(SESSOES_INVALIDAS, procedimento.getId(),
                        "A quantidade de sessões de " + procedimento.getProcedimentoModel().getNome()
                                + " precisa ser maior que zero."));
            }
        }
        return pendencias;
    }
}
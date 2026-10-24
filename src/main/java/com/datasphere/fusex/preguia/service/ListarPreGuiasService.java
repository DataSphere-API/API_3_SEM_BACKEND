package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListarPreGuiasService {

    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final MedicoRepository medicoRepository;
    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    private final BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService;

    public ListarPreGuiasService(PreGuiaRepository preGuiaRepository,
                                 PreGuiaProcRepository preGuiaProcRepository,
                                 MedicoRepository medicoRepository,
                                 CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService,
                                 BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService) {
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.medicoRepository = medicoRepository;
        this.calcularValorTotalPreGuiaService = calcularValorTotalPreGuiaService;
        this.buscarDataEmissaoDaPreGuiaService = buscarDataEmissaoDaPreGuiaService;
    }

    @Transactional(readOnly = true)
    public List<PreGuiaResumoResponse> listarPreGuiasPorStatus(String statusTexto, String precCpSolicitante) {
        StatusPreGuia status = converterStatus(statusTexto);
        Integer medicoId = buscarIdDoMedico(precCpSolicitante);

        List<PreGuiaModel> preGuias = buscarPreGuias(status, medicoId);

        List<PreGuiaResumoResponse> respostas = new ArrayList<>();
        for (PreGuiaModel preGuia : preGuias) {
            respostas.add(montarResumo(preGuia));
        }
        return respostas;
    }

    private StatusPreGuia converterStatus(String statusTexto) {
        if (statusTexto == null || statusTexto.isBlank()) {
            return null;
        }
        try {
            return StatusPreGuia.valueOf(statusTexto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.STATUS_INVALIDO);
        }
    }

    private Integer buscarIdDoMedico(String precCp) {
        if (precCp == null || precCp.isBlank()) {
            return null;
        }
        MedicoModel medico = medicoRepository.findByBenefId(precCp.trim()).orElse(null);
        if (medico == null) {
            return null;
        }
        return medico.getId();
    }

    private List<PreGuiaModel> buscarPreGuias(StatusPreGuia status, Integer medicoId) {
        if (medicoId != null && status != null) {
            return preGuiaRepository.findByMedIdAndStatusOrderByIdAsc(medicoId, status);
        }
        if (medicoId != null) {
            return preGuiaRepository.findByMedIdOrderByIdAsc(medicoId);
        }
        if (status != null) {
            return preGuiaRepository.findByStatusOrderByIdAsc(status);
        }
        return preGuiaRepository.findAllByOrderByIdAsc();
    }

    private PreGuiaResumoResponse montarResumo(PreGuiaModel preGuia) {
        List<PreGuiaProcModel> procedimentos = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuia.getId());

        return new PreGuiaResumoResponse(
                preGuia.getId(),
                preGuia.getBeneficiarioModel().getNome(),
                preGuia.getStatus(),
                procedimentos.size(),
                calcularValorTotalPreGuiaService.calcularValorTotal(procedimentos),
                preGuia.getDataCriacao(),
                preGuia.getDataAprovacaoMedico() != null,
                preGuia.getDataAprovacaoChefe() != null,
                buscarDataEmissaoDaPreGuiaService.buscarDataEmissao(preGuia));
    }
}
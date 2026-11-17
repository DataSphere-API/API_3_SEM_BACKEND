package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.dto.PreGuiaRequest;
import com.datasphere.fusex.dto.PreGuiaResponse;
import com.datasphere.fusex.dto.ProcedimentoPreGuiaRequest;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.exception.BusinessRuleException;
import com.datasphere.fusex.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PreGuiaService {

    private final PreGuiaRepository preGuiaRepository;

    public PreGuiaService(PreGuiaRepository preGuiaRepository) {
        this.preGuiaRepository = preGuiaRepository;
    }

    @Transactional
    public PreGuiaResponse criarPreGuia(PreGuiaRequest request, Long emissorId) {
        validarProcedimentos(request.getProcedimentos());

        PreGuiaModel preGuia = new PreGuiaModel();

        // CORRIGIDO: O campo no Model é benfId
        preGuia.setBenfId(request.getBeneficiarioPrecCp());

        // CORRIGIDO: O campo no Model é medId (Integer), então convertemos o Long do request para Integer
        preGuia.setMedId(request.getMedicoId().intValue());

        // ADICIONADO: Salvando o emissor que veio do Controller
        preGuia.setEmissorId(emissorId.intValue());

        // CORRIGIDO: O status usa o Enum StatusPreGuia
        preGuia.setStatus(StatusPreGuia.PRE_GUIA);

        // CORRIGIDO: Data de criação é LocalDate
        preGuia.setDataCriacao(LocalDate.now());

        PreGuiaModel salva = preGuiaRepository.save(preGuia);
        return toResponse(salva);
    }

    @Transactional
    public PreGuiaResponse editarPreGuia(Long id, PreGuiaRequest request, Long emissorId) {
        PreGuiaModel preGuia = preGuiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pré-guia não encontrada com ID: " + id));

        List<StatusPreGuia> statusPermitidos = List.of(StatusPreGuia.PRE_GUIA, StatusPreGuia.COM_PENDENCIAS, StatusPreGuia.REPROVADA);

        if (!statusPermitidos.contains(preGuia.getStatus())) {
            throw new BusinessRuleException("Edição não permitida para o status atual: " + preGuia.getStatus());
        }

        validarProcedimentos(request.getProcedimentos());

        // CORRIGIDO: Os mesmos setters ajustados aqui também
        preGuia.setBenfId(request.getBeneficiarioPrecCp());
        preGuia.setMedId(request.getMedicoId().intValue());

        PreGuiaModel atualizada = preGuiaRepository.save(preGuia);
        return toResponse(atualizada);
    }

    private void validarProcedimentos(List<ProcedimentoPreGuiaRequest> procedimentos) {
        if (procedimentos != null) {
            long distinctCount = procedimentos.stream()
                    .map(p -> p.getProcedimentoId() + "-" + p.getOcsCnpj())
                    .distinct()
                    .count();
            if (distinctCount < procedimentos.size()) {
                throw new BusinessRuleException("Procedimento duplicado na mesma OCS.");
            }
        }
    }

    private PreGuiaResponse toResponse(PreGuiaModel preGuia) {
        PreGuiaResponse response = new PreGuiaResponse();
        response.setId(preGuia.getId());
        // O .name() converte o Enum para String para o DTO
        response.setStatus(preGuia.getStatus().name());
        return response;
    }
}
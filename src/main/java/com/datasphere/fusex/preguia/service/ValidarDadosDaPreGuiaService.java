package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.contrato.ContratoRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.dto.PreGuiaRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ValidarDadosDaPreGuiaService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final MedicoRepository medicoRepository;
    private final ContratoRepository contratoRepository;

    public ValidarDadosDaPreGuiaService(BeneficiarioRepository beneficiarioRepository,
                                       MedicoRepository medicoRepository,
                                       ContratoRepository contratoRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.medicoRepository = medicoRepository;
        this.contratoRepository = contratoRepository;
    }

    public void validar(PreGuiaRequest request) {
        if (!beneficiarioRepository.existsById(request.beneficiarioPrecCp())) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_NAO_ENCONTRADO);
        }
        var medico = medicoRepository.findById(request.medicoId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO));
        if (medico.getCrm() == null || medico.getCrm().isBlank()) {
            throw new BusinessException(ErrorCode.MEDICO_SEM_CRM);
        }
        Set<ProcedimentoOcs> pedidos = new HashSet<>();
        for (var item : request.procedimentos()) {
            if (!pedidos.add(new ProcedimentoOcs(item.procedimentoId(), item.ocsCnpj()))) {
                throw new BusinessException(ErrorCode.PROCEDIMENTO_REPETIDO);
            }
            if (!contratoRepository.existsByProcedimentoModel_IdAndOcsModel_Cnpj(
                    item.procedimentoId(), item.ocsCnpj())) {
                throw new BusinessException(ErrorCode.PROCEDIMENTO_SEM_CONTRATO);
            }
        }
    }

    private record ProcedimentoOcs(Integer procedimentoId, String ocsCnpj) {}
}

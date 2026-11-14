package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class ValidarDonoDaGuiaService {

    private final BeneficiarioRepository beneficiarioRepository;

    public ValidarDonoDaGuiaService(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    public String identificarBeneficiario(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new BusinessException(ErrorCode.AUTENTICACAO_OBRIGATORIA);
        }
        return beneficiarioRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO)).getPrecCP();
    }

    public void validarDonoDaGuia(GuiaModel guia, String precCpAutenticado) {
        if (guia == null || precCpAutenticado == null || !precCpAutenticado.equals(guia.getBenfId())) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        }
    }
}

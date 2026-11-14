package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.analista.AnalistaRepository;
import com.datasphere.fusex.atendimento.service.ValidarDonoDaGuiaService;
import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class ValidarConsultaGuiasService {

    private final ValidarDonoDaGuiaService validarDonoDaGuiaService;
    private final ChefeFusexRepository chefeFusexRepository;
    private final AnalistaRepository analistaRepository;

    public ValidarConsultaGuiasService(ValidarDonoDaGuiaService validarDonoDaGuiaService,
                                       ChefeFusexRepository chefeFusexRepository,
                                       AnalistaRepository analistaRepository) {
        this.validarDonoDaGuiaService = validarDonoDaGuiaService;
        this.chefeFusexRepository = chefeFusexRepository;
        this.analistaRepository = analistaRepository;
    }

    public void validarConsultaGuias(String precCpConsultado, Principal principal) {
        String precCpAutenticado = validarDonoDaGuiaService.identificarBeneficiario(principal);
        if (precCpAutenticado.equals(precCpConsultado)) {
            return;
        }
        if (chefeFusexRepository.findByBenefId(precCpAutenticado).isPresent()
                || analistaRepository.findByBenefId(precCpAutenticado).isPresent()) {
            return;
        }
        throw new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
    }
}

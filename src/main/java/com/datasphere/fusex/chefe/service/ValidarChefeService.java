package com.datasphere.fusex.chefe.service;

import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.security.Principal;

@Service
public class ValidarChefeService {

    private final ChefeFusexRepository chefeFusexRepository;

    public ValidarChefeService(ChefeFusexRepository chefeFusexRepository) {
        this.chefeFusexRepository = chefeFusexRepository;
    }

    public void validar(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new BusinessException(ErrorCode.AUTENTICACAO_OBRIGATORIA);
        }
        if (!chefeFusexRepository.existsByLogin(principal.getName())) {
            throw new BusinessException(ErrorCode.CADASTRO_RESTRITO_AO_CHEFE);
        }
    }
}

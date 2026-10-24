package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.chefe.ChefeFusexModel;
import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import org.springframework.stereotype.Service;

@Service
public class IdentificarAprovadorService {

    public enum Papel {
        MEDICO,
        CHEFE
    }

    public record Aprovador(Papel papel, Integer id) {}

    private final MedicoRepository medicoRepository;
    private final ChefeFusexRepository chefeFusexRepository;

    public IdentificarAprovadorService(MedicoRepository medicoRepository,
                                       ChefeFusexRepository chefeFusexRepository) {
        this.medicoRepository = medicoRepository;
        this.chefeFusexRepository = chefeFusexRepository;
    }

    public Aprovador identificar(String precCp, PreGuiaModel preGuia) {
        if (precCp == null || precCp.isBlank()) {
            throw new BusinessException(ErrorCode.APROVADOR_NAO_IDENTIFICADO);
        }

        MedicoModel medico = medicoRepository.findByBenefId(precCp.trim()).orElse(null);
        if (medico != null) {
            if (!medico.getId().equals(preGuia.getMedId())) {
                throw new BusinessException(ErrorCode.MEDICO_NAO_RESPONSAVEL);
            }
            return new Aprovador(Papel.MEDICO, medico.getId());
        }

        ChefeFusexModel chefe = chefeFusexRepository.findByBenefId(precCp.trim()).orElse(null);
        if (chefe != null) {
            return new Aprovador(Papel.CHEFE, chefe.getId().intValue());
        }

        throw new BusinessException(ErrorCode.PERFIL_SEM_PERMISSAO);
    }
}
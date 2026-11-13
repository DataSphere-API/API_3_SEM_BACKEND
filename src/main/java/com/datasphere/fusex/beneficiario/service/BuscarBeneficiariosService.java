package com.datasphere.fusex.beneficiario.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.beneficiario.dto.BeneficiarioResponse;
import com.datasphere.fusex.beneficiario.dto.BeneficiariosResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuscarBeneficiariosService {

    private final BeneficiarioRepository beneficiarioRepository;

    public BuscarBeneficiariosService(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    @Transactional(readOnly = true)
    public BeneficiariosResponse buscar(String busca) {
        String termo = busca == null ? "" : busca.trim();
        return new BeneficiariosResponse(beneficiarioRepository
                .findTop20ByNomeContainingIgnoreCaseOrPrecCPContainingIgnoreCaseOrderByNomeAscPrecCPAsc(termo, termo)
                .stream().map(BeneficiarioResponse::de).toList());
    }
}

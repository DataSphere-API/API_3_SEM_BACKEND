package com.datasphere.fusex.beneficiario.services;

import com.datasphere.fusex.model.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BuscarBenificarioPorPrecCPService {
    private final BeneficiarioRepository beneficiarioRepository;

    public BuscarBenificarioPorPrecCPService(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    public BeneficiarioModel buscarBeneficioarioPorPrecCP(Integer precCP){
        Optional<BeneficiarioModel> beneficiarioModelOptional = beneficiarioRepository.findByPrecCP(precCP);
        return beneficiarioModelOptional.orElse(null);
    }
}

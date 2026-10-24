package com.datasphere.fusex.beneficiario.services;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import org.springframework.stereotype.Service;

@Service
public class BuscarBenificarioPorPrecCPService {
    private final BeneficiarioRepository beneficiarioRepository;

    public BuscarBenificarioPorPrecCPService(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    public BeneficiarioModel buscarBeneficioarioPorPrecCP(String precCP) {
        return beneficiarioRepository.findByPrecCP(precCP).orElse(null);
    }
}
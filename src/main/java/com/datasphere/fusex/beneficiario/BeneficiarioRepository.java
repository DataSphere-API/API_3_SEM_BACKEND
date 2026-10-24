package com.datasphere.fusex.beneficiario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioModel, String> {
    Optional<BeneficiarioModel> findByPrecCP(String precCP);
}
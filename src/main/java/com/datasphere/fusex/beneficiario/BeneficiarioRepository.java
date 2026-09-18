package com.datasphere.fusex.repository;

import com.datasphere.fusex.model.BeneficiarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioModel, Integer> {
    public Optional<BeneficiarioModel> findByPrecCP(Integer precCP);
}

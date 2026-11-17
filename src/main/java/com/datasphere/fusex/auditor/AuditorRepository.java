package com.datasphere.fusex.auditor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditorRepository extends JpaRepository<AuditorModel, Integer> {
    Optional<AuditorModel> findByBenefId(String benefId);
}

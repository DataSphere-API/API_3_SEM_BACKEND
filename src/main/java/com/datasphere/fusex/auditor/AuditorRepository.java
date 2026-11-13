package com.datasphere.fusex.auditor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuditorRepository extends JpaRepository<AuditorModel, Integer> {
    Optional<AuditorModel> findByBenefId(String benefId);
}

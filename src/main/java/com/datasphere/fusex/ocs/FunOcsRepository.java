package com.datasphere.fusex.ocs;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FunOcsRepository extends JpaRepository<FunOcsModel, Integer> {
    Optional<FunOcsModel> findByUsuarioId(Integer usuarioId);
}

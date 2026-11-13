package com.datasphere.fusex.beneficiario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioModel, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BeneficiarioModel b WHERE b.precCP = :precCp")
    Optional<BeneficiarioModel> buscarParaAtualizar(@Param("precCp") String precCp);
    Optional<BeneficiarioModel> findByUsuarioId(Integer usuarioId);
    List<BeneficiarioModel> findTop20ByNomeContainingIgnoreCaseOrPrecCPContainingIgnoreCaseOrderByNomeAscPrecCPAsc(
            String nome, String precCP);
    public Optional<BeneficiarioModel> findByPrecCP(String precCP);
}

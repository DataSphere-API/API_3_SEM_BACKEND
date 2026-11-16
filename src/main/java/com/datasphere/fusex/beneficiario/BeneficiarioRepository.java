package com.datasphere.fusex.beneficiario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioModel, String> {
    java.util.Optional<BeneficiarioModel> findByUsuarioId(Integer usuarioId);

    java.util.List<BeneficiarioModel> findTop20ByNomeContainingIgnoreCaseOrPrecCPContainingIgnoreCaseOrderByNomeAscPrecCPAsc(
            String nome, String precCp);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select b from BeneficiarioModel b where b.precCP = :precCp")
    java.util.Optional<BeneficiarioModel> buscarParaAtualizar(
            @org.springframework.data.repository.query.Param("precCp") String precCp);
    @Query(value = """
            SELECT b.* FROM beneficiario b
            JOIN usuario u ON u.id = b.usuario_id
            WHERE u.login = :login
            """, nativeQuery = true)
    Optional<BeneficiarioModel> findByLogin(@Param("login") String login);
    public Optional<BeneficiarioModel> findByPrecCP(String precCP);
}

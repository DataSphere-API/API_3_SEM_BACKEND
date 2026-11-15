package com.datasphere.fusex.beneficiario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioModel, String> {
    @Query(value = """
            SELECT b.* FROM beneficiario b
            JOIN usuario u ON u.id = b.usuario_id
            WHERE u.login = :login
            """, nativeQuery = true)
    Optional<BeneficiarioModel> findByLogin(@Param("login") String login);
    public Optional<BeneficiarioModel> findByPrecCP(String precCP);
}

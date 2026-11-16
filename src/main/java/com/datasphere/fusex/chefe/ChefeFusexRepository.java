package com.datasphere.fusex.chefe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface ChefeFusexRepository extends JpaRepository<ChefeFusexModel, Long> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM chefe_fusex c
                JOIN beneficiario b ON b.preccp = c.benef_id
                JOIN usuario u ON u.id = b.usuario_id
                WHERE u.login = :login
            )
            """, nativeQuery = true)
    boolean existsByLogin(@Param("login") String login);

    Optional<ChefeFusexModel> findByBenefId(String benefId);
}

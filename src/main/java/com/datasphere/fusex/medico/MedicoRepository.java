package com.datasphere.fusex.medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import com.datasphere.fusex.medico.dto.MedicoResponse;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoModel, Integer> {

    @Query("""
            SELECT new com.datasphere.fusex.medico.dto.MedicoResponse(m.id, b.nome, m.crm)
            FROM MedicoModel m, BeneficiarioModel b WHERE b.precCP = m.benefId
            ORDER BY b.nome, m.id
            """)
    List<MedicoResponse> listarComNome();

    boolean existsByCrmAndBenefIdNot(String crm, String benefId);

    Optional<MedicoModel> findByBenefId(String benefId);
}
